package am.trade.dashboard.service.metrics;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.TradeMetrics;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeDistributionHourGroupingSpec {

    private final TradeDistributionMetricsService service =
            new TradeDistributionMetricsService();

    @Test
    void calculateMetrics_fillsHourDayMonthAndWinRates() {
        List<TradeDetails> trades = List.of(
                trade("t1", LocalDateTime.of(2024, 1, 8, 10, 15), "100"), // Monday 10
                trade("t2", LocalDateTime.of(2024, 1, 8, 10, 45), "-40"), // Monday 10
                trade("t3", LocalDateTime.of(2024, 2, 14, 14, 0), "50") // Wednesday 14
        );

        TradeDistributionMetrics metrics = service.calculateMetrics(trades);

        assertNotNull(metrics.getTradesByHour());
        assertEquals(2, metrics.getTradesByHour().get("10"));
        assertEquals(1, metrics.getTradesByHour().get("14"));
        assertEquals(new BigDecimal("60"), metrics.getProfitByHour().get("10"));
        assertTrue(metrics.getWinRateByHour().get("10").compareTo(new BigDecimal("50")) == 0);

        assertEquals(2, metrics.getTradesByDay().get("MONDAY"));
        assertEquals(1, metrics.getTradesByDay().get("WEDNESDAY"));
        assertNotNull(metrics.getWinRateByDay().get("MONDAY"));

        assertEquals(2, metrics.getTradesByMonth().get("JANUARY"));
        assertEquals(1, metrics.getTradesByMonth().get("FEBRUARY"));
        assertNotNull(metrics.getWinRateByMonth().get("JANUARY"));
    }

    private static TradeDetails trade(String id, LocalDateTime entry, String pnl) {
        return TradeDetails.builder()
                .tradeId(id)
                .entryInfo(EntryExitInfo.builder().timestamp(entry).build())
                .metrics(TradeMetrics.builder().profitLoss(new BigDecimal(pnl)).build())
                .build();
    }
}
