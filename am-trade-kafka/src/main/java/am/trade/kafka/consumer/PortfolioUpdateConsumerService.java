package am.trade.kafka.consumer;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.models.enums.TradePositionType;
import am.trade.models.enums.TradeStatus;
import am.trade.models.kafka.inbound.InboundEquityModel;
import am.trade.models.kafka.inbound.PortfolioUpdateInboundEvent;
import am.trade.services.service.PortfolioService;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;

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
     */
    private void upsertPortfolio(String portfolioId, String userId, String name, String brokerType) {
        Optional<PortfolioModel> existing = portfolioService.findByPortfolioId(portfolioId);
        if (existing.isPresent()) {
            log.debug("Portfolio {} already exists in trade-management DB. Skipping upsert.", portfolioId);
            return;
        }
        String portfolioName = (name != null && !name.isBlank()) ? name
                : (brokerType != null ? brokerType : "Imported Portfolio");
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
            log.info("Deleting portfolio and trades for portfolioId={}", portfolioId);
            try {
                portfolioService.deleteByPortfolioId(portfolioId);
                tradeDetailsService.deleteByPortfolioId(portfolioId);
                
                // Fix: Also delete associated Trade Summaries (Dashboard data)
                List<TradeSummaryBasic> summaries = tradeSummaryService.findBasicByPortfolioId(portfolioId);
                for (TradeSummaryBasic summary : summaries) {
                    log.info("Deleting associated TradeSummary ID={}", summary.getId());
                    tradeSummaryService.deleteTradeSummary(summary.getId());
                }
                
                log.info("Successfully deleted portfolio and its trades for portfolioId={}", portfolioId);
            } catch (Exception e) {
                log.error("Failed to delete portfolio/trades for portfolioId={}: {}", portfolioId, e.getMessage(), e);
            }
            return;
        }

        // Upsert the portfolio record in the trade-management database so it appears
        // in the UI dropdown. Without this, trades get created but the portfolio is invisible.
        upsertPortfolio(portfolioId, userId, event.getName(), event.getBrokerType());

        if (event.getEquities() == null || event.getEquities().isEmpty()) {
            log.info("PortfolioUpdateInboundEvent has no equities. Upserted portfolio only. EventId: {}", event.getId());
            return;
        }

        // Fetch ALL existing trades for this portfolio once, rather than querying per symbol.
        // This is far more efficient when a portfolio has 50+ holdings.
        List<TradeDetails> existingTrades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        List<TradeDetails> newTrades = new ArrayList<>();

        for (InboundEquityModel equity : event.getEquities()) {
            if (equity.getSymbol() == null || equity.getQuantity() == null || equity.getQuantity() <= 0) {
                log.debug("Skipping equity with null/zero symbol or quantity: {}", equity);
                continue;
            }

            String symbol = equity.getSymbol().toUpperCase();

            // Dedup check: skip if any trade already exists for this symbol in this portfolio
            boolean alreadyExists = existingTrades.stream()
                    .anyMatch(t -> symbol.equalsIgnoreCase(t.getSymbol()));

            if (alreadyExists) {
                log.debug("Trade already exists for symbol {} in portfolio {}. Skipping.", symbol, portfolioId);
                continue;
            }

            log.info("Creating baseline 'Imported Holding' trade for portfolioId: {}, symbol: {}, userId: {}",
                    portfolioId, symbol, userId);

            TradeDetails trade = new TradeDetails();
            trade.setTradeId(UUID.randomUUID().toString());
            trade.setPortfolioId(portfolioId);
            trade.setUserId(userId);
            trade.setSymbol(symbol);
            trade.setStatus(TradeStatus.OPEN);
            trade.setTradePositionType(TradePositionType.LONG);
            trade.setStrategy("Imported Holding");

            // Instrument Info
            am.trade.common.models.InstrumentInfo instrumentInfo = new am.trade.common.models.InstrumentInfo();
            instrumentInfo.setSymbol(symbol);
            instrumentInfo.setIsin(equity.getIsin());
            trade.setInstrumentInfo(instrumentInfo);

            // Entry Info — timestamp required for Analysis metrics date queries
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

            newTrades.add(trade);
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
