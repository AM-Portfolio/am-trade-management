package am.trade.dashboard.service.metrics.calculator;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import am.trade.dashboard.service.metrics.calculator.impl.AverageHoldingTimeCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.AverageTradeCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.ProfitLossCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.WinRateCalculator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Ensures BigDecimal calculators are on MetricsCalculator and applied by the registry.
 * Fails if AbstractBigDecimalMetricCalculator is not injectable as MetricsCalculator.
 */
class MetricsRegistryPerformanceWiringSpec {

    @Test
    void registryAppliesProfitLossHoldWinRateAndEligible() {
        MetricsRegistry registry = new MetricsRegistry(List.of(
                new ProfitLossCalculator(),
                new WinRateCalculator(),
                new AverageHoldingTimeCalculator(),
                new AverageTradeCalculator()
        ));

        List<TradeDetails> trades = List.of(
                trade("w", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "100"),
                trade("l", LocalDateTime.of(2024, 1, 8, 11, 0),
                        LocalDateTime.of(2024, 1, 8, 11, 20), "-40"),
                trade("be", LocalDateTime.of(2024, 1, 8, 12, 0),
                        LocalDateTime.of(2024, 1, 8, 12, 5), "0"),
                TradeDetails.builder()
                        .tradeId("open")
                        .entryInfo(EntryExitInfo.builder()
                                .timestamp(LocalDateTime.of(2024, 1, 8, 13, 0)).build())
                        .exitInfo(EntryExitInfo.builder()
                                .timestamp(LocalDateTime.of(2024, 1, 8, 13, 30)).build())
                        .metrics(TradeMetrics.builder().profitLoss(null).build())
                        .build()
        );

        PerformanceMetrics metrics = new PerformanceMetrics();
        registry.calculateAndApplyMetrics(trades, metrics);

        assertEquals(0, new BigDecimal("60").compareTo(metrics.getTotalProfitLoss()));
        // eligible = 3 (null PnL excluded); wins = 1 → 33.33%
        assertEquals(0, new BigDecimal("33.33").compareTo(metrics.getWinRate()));
        assertEquals(1, metrics.getWinningTradesCount());
        assertEquals(1, metrics.getLosingTradesCount());
        assertEquals(3, metrics.getEligibleTradesCount());
        // holds: 10, 20, 5, 30 → avg 16.25 minutes
        assertNotNull(metrics.getAverageHoldingTimeMinutes());
        assertEquals(0, new BigDecimal("16.2500").compareTo(metrics.getAverageHoldingTimeMinutes()));
    }

    @Test
    void winRateNullWhenNoEligiblePnl() {
        MetricsRegistry registry = new MetricsRegistry(List.of(new WinRateCalculator()));
        PerformanceMetrics metrics = new PerformanceMetrics();
        registry.calculateAndApplyMetrics(List.of(
                TradeDetails.builder()
                        .tradeId("x")
                        .metrics(TradeMetrics.builder().build())
                        .build()
        ), metrics);
        assertNull(metrics.getWinRate());
    }

    private static TradeDetails trade(
            String id, LocalDateTime entry, LocalDateTime exit, String pnl) {
        return TradeDetails.builder()
                .tradeId(id)
                .entryInfo(EntryExitInfo.builder().timestamp(entry).build())
                .exitInfo(EntryExitInfo.builder().timestamp(exit).build())
                .metrics(TradeMetrics.builder().profitLoss(new BigDecimal(pnl)).build())
                .build();
    }
}
