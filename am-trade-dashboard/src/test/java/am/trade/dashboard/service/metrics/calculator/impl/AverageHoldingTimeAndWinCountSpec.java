package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AverageHoldingTimeAndWinCountSpec {

    @Test
    void averageHoldingTime_usesFractionalMinutes() {
        AverageHoldingTimeCalculator calc = new AverageHoldingTimeCalculator();
        PerformanceMetrics metrics = new PerformanceMetrics();
        // 10 minutes and 20 minutes → avg 15 minutes = 0.25 hours
        List<TradeDetails> trades = List.of(
                trade("a", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "10"),
                trade("b", LocalDateTime.of(2024, 1, 8, 11, 0),
                        LocalDateTime.of(2024, 1, 8, 11, 20), "-5")
        );

        calc.applyHoldingTimes(trades, metrics);

        assertEquals(0, new BigDecimal("15.0000").compareTo(metrics.getAverageHoldingTimeMinutes()));
        assertEquals(0, new BigDecimal("0.2500").compareTo(metrics.getAverageHoldingTimeOverall()));
    }

    @Test
    void averageTradeCalculator_setsWinLossCounts() {
        AverageTradeCalculator calc = new AverageTradeCalculator();
        PerformanceMetrics metrics = new PerformanceMetrics();
        List<TradeDetails> trades = List.of(
                trade("w1", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 5), "10"),
                trade("w2", LocalDateTime.of(2024, 1, 8, 10, 10),
                        LocalDateTime.of(2024, 1, 8, 10, 15), "20"),
                trade("l1", LocalDateTime.of(2024, 1, 8, 10, 20),
                        LocalDateTime.of(2024, 1, 8, 10, 25), "-5"),
                trade("be", LocalDateTime.of(2024, 1, 8, 10, 30),
                        LocalDateTime.of(2024, 1, 8, 10, 35), "0")
        );

        calc.calculateAverageTrades(trades, metrics);

        assertEquals(2, metrics.getWinningTradesCount());
        assertEquals(1, metrics.getLosingTradesCount());
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
