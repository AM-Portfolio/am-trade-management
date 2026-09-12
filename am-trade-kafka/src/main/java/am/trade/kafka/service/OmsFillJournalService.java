package am.trade.kafka.service;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.kafka.model.OmsFillEvent;
import am.trade.models.enums.TradePositionType;
import am.trade.models.enums.TradeStatus;
import am.trade.models.kafka.EquityPosition;
import am.trade.models.kafka.PortfolioSyncEvent;
import am.trade.services.publisher.TradeHoldingEventPublisher;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OmsFillJournalService {

    private final PortfolioPersistenceService portfolioPersistenceService;
    private final TradeDetailsService tradeDetailsService;
    private final TradeProcessingService tradeProcessingService;
    private final TradeHoldingEventPublisher tradeHoldingEventPublisher;

    @CacheEvict(cacheNames = {"analyticsCache", "portfolioSummary", "tradeSummaryCache"}, allEntries = true)
    public void apply(OmsFillEvent fill) {
        if (fill == null || fill.getOrderId() == null || fill.getId() == null) {
            return;
        }
        if (tradeDetailsService.findBySourceOrderId(fill.getOrderId()).isPresent()) {
            log.info("Skipping replayed fill sourceOrderId={}", fill.getOrderId());
            return;
        }
        PortfolioModel portfolio = portfolioPersistenceService.findByPortfolioId(fill.getId()).orElse(null);
        if (portfolio == null || fill.getOwnerId() == null || !fill.getOwnerId().equals(portfolio.getOwnerId())) {
            log.warn("fill_owner_mismatch orderId={} ownerId={}", fill.getOrderId(), fill.getOwnerId());
            return;
        }
        String action = fill.getAction() == null ? "BUY" : fill.getAction().toUpperCase();
        if ("SELL".equals(action)) {
            applySell(fill, portfolio);
            return;
        }
        applyBuy(fill, portfolio);
    }

    private void applyBuy(OmsFillEvent fill, PortfolioModel portfolio) {
        int qty = qty(fill);
        BigDecimal price = price(fill);
        TradeDetails trade = TradeDetails.builder()
                .tradeId(UUID.randomUUID().toString())
                .sourceOrderId(fill.getOrderId())
                .portfolioId(fill.getId())
                .symbol(fill.getSymbol())
                .userId(fill.getOwnerId())
                .status(TradeStatus.OPEN)
                .tradePositionType(TradePositionType.LONG)
                .tags(List.of("PAPER"))
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(ts(fill.getTimestamp()))
                        .price(price)
                        .quantity(qty)
                        .totalValue(price.multiply(BigDecimal.valueOf(qty)))
                        .build())
                .build();
        persistAndSync(trade, "BUY");
    }

    private void applySell(OmsFillEvent fill, PortfolioModel portfolio) {
        int remaining = qty(fill);
        BigDecimal fillPrice = price(fill);
        List<TradeDetails> opens = tradeDetailsService.findModelsByPortfolioIdAndSymbol(fill.getId(), fill.getSymbol())
                .stream()
                .filter(t -> TradeStatus.OPEN.equals(t.getStatus()))
                .sorted(Comparator.comparing(t -> t.getEntryInfo() != null && t.getEntryInfo().getTimestamp() != null
                        ? t.getEntryInfo().getTimestamp() : LocalDateTime.MIN))
                .toList();
        for (TradeDetails open : opens) {
            if (remaining <= 0) {
                break;
            }
            int entryQty = open.getEntryInfo() != null && open.getEntryInfo().getQuantity() != null
                    ? open.getEntryInfo().getQuantity() : 0;
            int exited = open.getExitInfo() != null && open.getExitInfo().getQuantity() != null
                    ? open.getExitInfo().getQuantity() : 0;
            int openQty = entryQty - exited;
            if (openQty <= 0) {
                continue;
            }
            int take = Math.min(remaining, openQty);
            int newExited = exited + take;
            BigDecimal prevPx = open.getExitInfo() != null && open.getExitInfo().getPrice() != null
                    ? open.getExitInfo().getPrice() : BigDecimal.ZERO;
            BigDecimal newPx = exited == 0 ? fillPrice
                    : prevPx.multiply(BigDecimal.valueOf(exited)).add(fillPrice.multiply(BigDecimal.valueOf(take)))
                            .divide(BigDecimal.valueOf(newExited), 2, java.math.RoundingMode.HALF_UP);
            open.setExitInfo(EntryExitInfo.builder()
                    .timestamp(ts(fill.getTimestamp()))
                    .price(newPx)
                    .quantity(newExited)
                    .totalValue(newPx.multiply(BigDecimal.valueOf(newExited)))
                    .build());
            if (newExited >= entryQty) {
                BigDecimal entryPx = open.getEntryInfo().getPrice();
                int cmp = newPx.compareTo(entryPx);
                open.setStatus(cmp > 0 ? TradeStatus.WIN : cmp < 0 ? TradeStatus.LOSS : TradeStatus.BREAK_EVEN);
            }
            remaining -= take;
            persistAndSync(open, "SELL");
        }
        if (remaining > 0) {
            log.warn("FIFO SELL leftover qty={} orderId={}", remaining, fill.getOrderId());
        }
    }

    private void persistAndSync(TradeDetails trade, String action) {
        TradeDetails saved = tradeDetailsService.saveTradeDetails(trade);
        tradeProcessingService.applyTradesDelta(List.of(saved), saved.getPortfolioId(), saved.getUserId());
        BigDecimal qty = saved.getEntryInfo() != null && saved.getEntryInfo().getQuantity() != null
                ? BigDecimal.valueOf(saved.getEntryInfo().getQuantity()) : BigDecimal.ZERO;
        BigDecimal px = saved.getEntryInfo() != null && saved.getEntryInfo().getPrice() != null
                ? saved.getEntryInfo().getPrice() : BigDecimal.ZERO;
        BigDecimal sellQty = saved.getExitInfo() != null && saved.getExitInfo().getQuantity() != null
                ? BigDecimal.valueOf(saved.getExitInfo().getQuantity()) : null;
        BigDecimal sellPx = saved.getExitInfo() != null ? saved.getExitInfo().getPrice() : null;
        EquityPosition equity = EquityPosition.builder()
                .symbol(saved.getSymbol())
                .assetType("EQUITY")
                .quantity(qty)
                .avgBuyingPrice(px)
                .investmentValue(px.multiply(qty))
                .sellQuantity(sellQty)
                .sellPrice(sellPx)
                .saleValue(sellQty != null && sellPx != null ? sellQty.multiply(sellPx) : null)
                .tradeStatus(saved.getStatus() != null ? saved.getStatus().name() : "OPEN")
                .action(action)
                .build();
        tradeHoldingEventPublisher.publishHoldingUpdate(PortfolioSyncEvent.builder()
                .id(saved.getPortfolioId())
                .portfolioId(fillName(saved))
                .action(action)
                .userId(saved.getUserId())
                .portfolioKind("PAPER")
                .equities(List.of(equity))
                .timestamp(LocalDateTime.now())
                .build());
    }

    private String fillName(TradeDetails saved) {
        return portfolioPersistenceService.findByPortfolioId(saved.getPortfolioId())
                .map(p -> p.getName() != null ? p.getName() : saved.getPortfolioId())
                .orElse(saved.getPortfolioId());
    }

    private static int qty(OmsFillEvent fill) {
        return new BigDecimal(fill.getQuantity() == null ? "0" : fill.getQuantity()).intValue();
    }

    private static BigDecimal price(OmsFillEvent fill) {
        return new BigDecimal(fill.getPrice() == null ? "0" : fill.getPrice());
    }

    private static LocalDateTime ts(String value) {
        if (value == null || value.isBlank()) {
            return LocalDateTime.now();
        }
        try {
            return OffsetDateTime.parse(value).toLocalDateTime();
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }
}
