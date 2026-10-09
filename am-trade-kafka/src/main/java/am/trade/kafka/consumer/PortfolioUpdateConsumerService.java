package am.trade.kafka.consumer;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.models.enums.TradePositionType;
import am.trade.models.enums.TradeStatus;
import am.trade.models.kafka.inbound.InboundEquityModel;
import am.trade.models.kafka.inbound.PortfolioUpdateInboundEvent;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.PortfolioSyncInstrumentResolver;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import am.trade.services.service.TradeSummaryService;
import am.trade.common.models.TradeSummaryBasic;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.CacheEvict;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Kafka consumer that listens to portfolio update events published by am-portfolio.
 *
 * <p>When am-portfolio updates holdings (e.g., via the Document Parser or manual entry),
 * it publishes a {@code PortfolioUpdateEvent} to the {@code am-portfolio-update} topic.
 * This consumer picks up those events and creates baseline "Imported Holding" trades
 * in the Trade database for any symbols that don't already have a trade entry.</p>
 *
 * <h3>Why we use {@link TradeDetailsService} instead of {@code TradeApiService}:</h3>
 * <ol>
 *   <li>{@code TradeApiService.addTrade()} calls {@code UserContext.getUserIdOrThrow()},
 *       which requires an authenticated HTTP user. Kafka consumers have NO HTTP context,
 *       so this would throw at runtime.</li>
 *   <li>{@code TradeApiService.addTrade()} publishes a {@code PortfolioSyncEvent} back
 *       to am-portfolio after saving. This would create an infinite message loop:
 *       Portfolio → Trade → Portfolio → Trade → ...</li>
 * </ol>
 *
 * <p>By using the lower-level {@link TradeDetailsService#saveTradeDetails}, we bypass
 * both the security context and the sync-back event, which is correct because this
 * is an internal system process — not a user action.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "am.trade.kafka.portfolio-update.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class PortfolioUpdateConsumerService {

    /**
     * Sources that must not create/overwrite trade Mongo rows.
     * <ul>
     *   <li>{@code TRADE} — our own sync echo (infinite-loop guard)</li>
     *   <li>{@code DEMO} / {@code PORTFOLIO_CALC} — portfolio calc / demo fallback;
     *       demo uses a shared portfolio UUID attributed to many users, which breaks
     *       upsert-by-portfolioId (first owner wins → others stuck on Demo inject)</li>
     * </ul>
     */
    private static final Set<String> IGNORED_SOURCES = Set.of("TRADE", "DEMO", "PORTFOLIO_CALC");

    private final ObjectMapper objectMapper;
    private final TradeDetailsService tradeDetailsService;
    private final PortfolioService portfolioService;
    private final TradeProcessingService tradeProcessingService;
    private final TradeSummaryService tradeSummaryService;
    private final PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;

    @Value("${app.demo.portfolio-id:}")
    private String demoPortfolioId;

    @KafkaListener(
            topics = "${am.trade.kafka.portfolio-update.topic:am-portfolio-update}",
            groupId = "${am.trade.kafka.portfolio-update.consumer-group-id:am-trade-portfolio-update-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    @CacheEvict(cacheNames = {"analyticsCache", "portfolioSummary", "tradeSummaryCache"}, allEntries = true)
    public void consume(String message, Acknowledgment acknowledgment) throws Exception {
        log.info("Received portfolio update message: {}", message);

        PortfolioUpdateInboundEvent event = objectMapper.readValue(message, PortfolioUpdateInboundEvent.class);

        String source = event.getSource();
        if (source != null && IGNORED_SOURCES.contains(source.toUpperCase())) {
            log.info("Ignoring portfolio update from source='{}'. EventId: {}", source, event.getId());
            acknowledgment.acknowledge();
            return;
        }

        // Shared demo document UUID must never be upserted under arbitrary userIds.
        if (demoPortfolioId != null && !demoPortfolioId.isBlank()
                && demoPortfolioId.equals(event.getPortfolioId())) {
            log.info("Ignoring update for shared demo portfolioId={} userId={}. EventId: {}",
                    event.getPortfolioId(), event.getUserId(), event.getId());
            acknowledgment.acknowledge();
            return;
        }

        processInboundPortfolioEvent(event);

        acknowledgment.acknowledge();
        log.info("Portfolio update message processed and acknowledged successfully");
    }

    /**
     * Upserts a portfolio record in the trade-management database.
     * This ensures the portfolio is visible in the UI dropdown even when it was
     * first created via the document processor (which publishes to am-portfolio-update
     * without going through the trade-management REST API).
     *
     * @return portfolioId that subsequent trades must use (may remap to an existing same-name row),
     *         or {@code null} when the event user does not own an already-existing portfolio
     */
    private String upsertPortfolio(String portfolioId, String userId, String name, String brokerType) {
        Optional<PortfolioModel> existing = portfolioService.findByPortfolioId(portfolioId);
        if (existing.isPresent()) {
            String ownerId = existing.get().getOwnerId();
            // Mirror deleteOwnedPortfolio: reject cross-owner UPDATE before any trade mutation.
            if (ownerId != null && !ownerId.equals(userId)) {
                log.warn("Ignoring UPDATE for portfolioId={} — event userId={} != ownerId={}",
                        portfolioId, userId, ownerId);
                return null;
            }
            log.debug("Portfolio {} already exists in trade-management DB. Skipping upsert.", portfolioId);
            return portfolioId;
        }
        String portfolioName = (name != null && !name.isBlank()) ? name
                : (brokerType != null ? brokerType : "Imported Portfolio");

        // Prevent duplicate cards: second Kafka event with a new UUID but same display name
        // for the same owner (e.g. "Upstox" ×2) must not create another row — remap trades.
        try {
            List<PortfolioModel> owned = portfolioService.findByOwnerId(userId);
            if (owned != null) {
                Optional<PortfolioModel> sameName = owned.stream()
                        .filter(Objects::nonNull)
                        .filter(p -> p.getName() != null
                                && p.getName().trim().equalsIgnoreCase(portfolioName.trim()))
                        .findFirst();
                if (sameName.isPresent()) {
                    String keepId = sameName.get().getPortfolioId();
                    log.info("Remapping portfolioId={} → existing id={} for owner {} (same name '{}')",
                            portfolioId, keepId, userId, portfolioName);
                    return keepId;
                }
            }
        } catch (Exception e) {
            log.warn("Owner duplicate-name check failed for {}: {}", userId, e.getMessage());
        }

        PortfolioModel portfolio = PortfolioModel.builder()
                .portfolioId(portfolioId)
                .ownerId(userId)
                .name(portfolioName)
                .active(true)
                .build();
        try {
            portfolioService.savePortfolio(portfolio);
            log.info("Created portfolio record in trade-management DB: portfolioId={}, name={}", portfolioId, portfolioName);
        } catch (Exception e) {
            log.error("Failed to upsert portfolio {} in trade-management DB: {}", portfolioId, e.getMessage(), e);
        }
        return portfolioId;
    }

    /**
     * Deletes only when the event user owns the portfolio (or owns orphan trades for that id).
     */
    private void deleteOwnedPortfolio(String portfolioId, String userId) {
        log.info("DELETE requested for portfolioId={} by userId={}", portfolioId, userId);
        try {
            Optional<PortfolioModel> existing = portfolioService.findByPortfolioId(portfolioId);
            if (existing.isPresent()) {
                String ownerId = existing.get().getOwnerId();
                if (ownerId != null && !ownerId.equals(userId)) {
                    log.warn("Ignoring DELETE for portfolioId={} — event userId={} != ownerId={}",
                            portfolioId, userId, ownerId);
                    return;
                }
                portfolioService.deleteByPortfolioId(portfolioId);
                tradeDetailsService.deleteByPortfolioId(portfolioId);

                List<TradeSummaryBasic> summaries = tradeSummaryService.findBasicByPortfolioId(portfolioId);
                for (TradeSummaryBasic summary : summaries) {
                    log.info("Deleting associated TradeSummary ID={}", summary.getId());
                    tradeSummaryService.deleteTradeSummary(summary.getId());
                }
            } else {
                log.warn("Ignoring DELETE for portfolioId={} — portfolio not found", portfolioId);
                return;
            }

            log.info("Successfully deleted owned portfolio/trades for portfolioId={} userId={}",
                    portfolioId, userId);
        } catch (Exception e) {
            log.error("Failed to delete portfolio/trades for portfolioId={}: {}", portfolioId, e.getMessage(), e);
        }
    }

    private void processInboundPortfolioEvent(PortfolioUpdateInboundEvent event) {
        if (event.getPortfolioId() == null) {
            log.warn("PortfolioUpdateInboundEvent has no portfolioId. Skipping. EventId: {}", event.getId());
            return;
        }

        String portfolioId = event.getPortfolioId();
        String userId = event.getUserId();

        if (userId == null || userId.isBlank()) {
            log.error("PortfolioUpdateInboundEvent has no userId. Cannot create trades without an owner. Skipping.");
            return;
        }

        if ("DELETE".equalsIgnoreCase(event.getAction())) {
            deleteOwnedPortfolio(portfolioId, userId);
            return;
        }

        // Upsert the portfolio record in the trade-management database so it appears
        // in the UI dropdown. Without this, trades get created but the portfolio is invisible.
        portfolioId = upsertPortfolio(portfolioId, userId, event.getName(), event.getBrokerType());
        if (portfolioId == null) {
            return;
        }

        if (event.getEquities() == null || event.getEquities().isEmpty()) {
            log.info("PortfolioUpdateInboundEvent has no equities. Upserted portfolio only. EventId: {}", event.getId());
            return;
        }

        List<TradeDetails> existingTrades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        List<TradeDetails> candidates = new ArrayList<>();

        for (InboundEquityModel equity : event.getEquities()) {
            if (equity.getSymbol() == null || equity.getQuantity() == null || equity.getQuantity() <= 0) {
                log.debug("Skipping equity with null/zero symbol or quantity: {}", equity);
                continue;
            }

            String rawSymbol = equity.getSymbol().toUpperCase();
            String isin = equity.getIsin() != null ? equity.getIsin().trim().toUpperCase() : null;
            // When Kafka sends ISIN as symbol, park it in isin for resolvers.
            if (isin == null && rawSymbol.length() == 12 && rawSymbol.startsWith("IN")) {
                isin = rawSymbol;
            }

            TradeDetails trade = new TradeDetails();
            trade.setTradeId(UUID.randomUUID().toString());
            trade.setPortfolioId(portfolioId);
            trade.setUserId(userId);
            trade.setSymbol(rawSymbol);
            trade.setStatus(TradeStatus.OPEN);
            trade.setTradePositionType(TradePositionType.LONG);
            trade.setStrategy("Imported Holding");

            am.trade.common.models.InstrumentInfo instrumentInfo = new am.trade.common.models.InstrumentInfo();
            instrumentInfo.setSymbol(rawSymbol);
            instrumentInfo.setIsin(isin);
            if (equity.getName() != null && !equity.getName().isBlank()) {
                instrumentInfo.setDescription(equity.getName().trim());
            }
            trade.setInstrumentInfo(instrumentInfo);

            EntryExitInfo entryInfo = new EntryExitInfo();
            entryInfo.setQuantity(equity.getQuantity().intValue());
            entryInfo.setPrice(equity.getAvgBuyingPrice() != null
                    ? BigDecimal.valueOf(equity.getAvgBuyingPrice())
                    : BigDecimal.ZERO);
            entryInfo.setTotalValue(equity.getInvestmentValue() != null
                    ? BigDecimal.valueOf(equity.getInvestmentValue())
                    : entryInfo.getPrice().multiply(BigDecimal.valueOf(entryInfo.getQuantity())));
            entryInfo.setTimestamp(java.time.LocalDateTime.now());
            trade.setEntryInfo(entryInfo);

            if (equity.getCurrentPrice() != null) {
                trade.setCurrentPrice(BigDecimal.valueOf(equity.getCurrentPrice()));
            }

            candidates.add(trade);
        }

        Map<String, String> rawSymbolByTradeId = new HashMap<>();
        for (TradeDetails trade : candidates) {
            rawSymbolByTradeId.put(trade.getTradeId(), trade.getSymbol());
        }

        // Resolve ISIN/SYMBOL/NAME → NSE ticker before dedupe/persist (every stock).
        if (!candidates.isEmpty() && portfolioSyncInstrumentResolver != null) {
            portfolioSyncInstrumentResolver.resolveForSync(candidates);
        }

        List<TradeDetails> newTrades = new ArrayList<>();
        List<TradeDetails> symbolCorrections = new ArrayList<>();

        for (TradeDetails trade : candidates) {
            String symbol = trade.getSymbol() != null ? trade.getSymbol().toUpperCase() : null;
            String rawSymbol = rawSymbolByTradeId.get(trade.getTradeId());
            String isin = trade.getInstrumentInfo() != null ? trade.getInstrumentInfo().getIsin() : null;
            String name = trade.getInstrumentInfo() != null ? trade.getInstrumentInfo().getDescription() : null;

            Optional<TradeDetails> existingOpt = Optional.empty();
            if (isin != null && !isin.isBlank()) {
                existingOpt = existingTrades.stream()
                        .filter(t -> t.getInstrumentInfo() != null
                                && isin.equalsIgnoreCase(t.getInstrumentInfo().getIsin()))
                        .findFirst();
            }
            if (existingOpt.isEmpty() && symbol != null) {
                existingOpt = existingTrades.stream()
                        .filter(t -> symbol.equalsIgnoreCase(t.getSymbol()))
                        .findFirst();
            }
            if (existingOpt.isEmpty() && rawSymbol != null) {
                existingOpt = existingTrades.stream()
                        .filter(t -> rawSymbol.equalsIgnoreCase(t.getSymbol()))
                        .findFirst();
            }
            if (existingOpt.isEmpty() && name != null && !name.isBlank()) {
                existingOpt = existingTrades.stream()
                        .filter(t -> t.getInstrumentInfo() != null
                                && name.equalsIgnoreCase(t.getInstrumentInfo().getDescription()))
                        .findFirst();
            }

            if (existingOpt.isPresent()) {
                TradeDetails existing = existingOpt.get();
                boolean changed = false;
                if (symbol != null && !symbol.equalsIgnoreCase(existing.getSymbol())) {
                    log.info("Correcting trade symbol {} → {} portfolioId={}",
                            existing.getSymbol(), symbol, portfolioId);
                    existing.setSymbol(symbol);
                    changed = true;
                }
                if (existing.getInstrumentInfo() == null) {
                    existing.setInstrumentInfo(trade.getInstrumentInfo());
                    changed = true;
                } else {
                    if (symbol != null) {
                        existing.getInstrumentInfo().setSymbol(symbol);
                    }
                    if (isin != null && (existing.getInstrumentInfo().getIsin() == null
                            || existing.getInstrumentInfo().getIsin().isBlank())) {
                        existing.getInstrumentInfo().setIsin(isin);
                        changed = true;
                    }
                    if (name != null) {
                        existing.getInstrumentInfo().setDescription(name);
                    }
                }
                if (changed) {
                    symbolCorrections.add(existing);
                }
                continue;
            }

            log.info("Creating baseline 'Imported Holding' trade for portfolioId: {}, symbol: {}, userId: {}",
                    portfolioId, symbol, userId);
            newTrades.add(trade);
        }

        if (!symbolCorrections.isEmpty()) {
            try {
                tradeDetailsService.saveAllTradeDetails(symbolCorrections);
                log.info("Persisted {} symbol corrections for portfolio {}", symbolCorrections.size(), portfolioId);
            } catch (Exception e) {
                log.error("Failed to persist symbol corrections for portfolio {}: {}", portfolioId, e.getMessage(), e);
            }
        }

        if (!newTrades.isEmpty()) {
            try {
                List<TradeDetails> savedList = tradeDetailsService.saveAllTradeDetails(newTrades);
                existingTrades.addAll(savedList);
                log.info("Successfully created {} baseline trades in batch", savedList.size());
            } catch (Exception e) {
                log.error("Failed to batch save baseline trades for portfolio {}: {}", portfolioId, e.getMessage(), e);
            }
        }

        // Link all existing and new trades to the portfolio and calculate metrics
        try {
            tradeProcessingService.processTradeDetailsWithObjects(existingTrades, portfolioId, userId);
            log.info("Successfully updated portfolio {} metrics and linked {} trades", portfolioId, existingTrades.size());
        } catch (Exception e) {
            log.error("Failed to link trades and calculate metrics for portfolio {}: {}", portfolioId, e.getMessage(), e);
        }
    }
}
