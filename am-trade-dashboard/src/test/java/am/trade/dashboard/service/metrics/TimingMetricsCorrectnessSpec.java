package am.trade.dashboard.service.metrics;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.TradeMetrics;
import am.trade.common.util.HoldingStyleClassifier;
import am.trade.dashboard.service.metrics.calculator.MetricsRegistry;
import am.trade.dashboard.service.metrics.calculator.impl.AverageHoldingTimeCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.AverageTradeCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.ProfitLossCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.WinRateCalculator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_0915_1100;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_1100_1300;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Golden invariants: Timing KPI universe ↔ session/day/month maps ↔ hold-style intervals.
 */
class TimingMetricsCorrectnessSpec {

    private final TradeDistributionMetricsService distService = new TradeDistributionMetricsService();
    private final MetricsRegistry registry = new MetricsRegistry(List.of(
            new ProfitLossCalculator(),
            new WinRateCalculator(),
            new AverageHoldingTimeCalculator(),
            new AverageTradeCalculator()
    ));

    @ParameterizedTest
    @CsvSource({
            "14, SCALPER",
            "15, INTRADAY",
            "1439, INTRADAY",
            "1440, SWING"
    })
    void holdingStyle_cutovers(long holdMinutes, String expected) {
        LocalDateTime entry = LocalDateTime.of(2024, 1, 8, 10, 0);
        TradeDetails trade = trade("t", entry, entry.plusMinutes(holdMinutes), "1");
        assertEquals(expected, HoldingStyleClassifier.classifyHold(trade));
        assertTrue(HoldingStyleClassifier.matchesStyle(trade, expected));
    }

    @Test
    void sessionDayMonthPnlIdentity_andTimingKpiUniverse() {
        List<TradeDetails> trades = List.of(
                trade("a", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "100"),
                trade("b", LocalDateTime.of(2024, 1, 8, 11, 0),
                        LocalDateTime.of(2024, 1, 8, 11, 30), "-40"),
                trade("c", LocalDateTime.of(2024, 1, 8, 14, 0),
                        LocalDateTime.of(2024, 1, 8, 14, 5), "0"),
                trade("d", LocalDateTime.of(2024, 2, 5, 10, 0),
                        LocalDateTime.of(2024, 2, 5, 10, 20), "20"),
                // missing entry — skipped from Timing universe
                TradeDetails.builder()
                        .tradeId("no-entry")
                        .metrics(TradeMetrics.builder().profitLoss(new BigDecimal("999")).build())
                        .build(),
                // null PnL — in Timing universe for trade count, not eligible
                tradeNullPnl("open", LocalDateTime.of(2024, 1, 8, 15, 10),
                        LocalDateTime.of(2024, 1, 8, 15, 20)),
                // bad hold
                trade("bad", LocalDateTime.of(2024, 1, 8, 12, 30),
                        LocalDateTime.of(2024, 1, 8, 12, 0), "5")
        );

        TradeDistributionMetrics dist = distService.calculateMetrics(trades);
        assertEquals(1, dist.getSkippedMissingEntryCount());
        assertEquals(1, dist.getOpenOrMissingPnlCount());
        assertEquals(1, dist.getBadTimestampCount());

        BigDecimal sessionSum = sum(dist.getProfitBySession());
        BigDecimal daySum = sum(dist.getProfitByDay());
        BigDecimal monthSum = sum(dist.getProfitByMonth());
        assertEquals(0, sessionSum.compareTo(daySum));
        assertEquals(0, sessionSum.compareTo(monthSum));
        // 100 - 40 + 0 + 20 + 5 = 85 (null PnL omitted; no-entry omitted)
        assertEquals(0, new BigDecimal("85").compareTo(sessionSum));

        int eligibleSum = dist.getEligibleTradesBySession().values().stream().mapToInt(i -> i).sum();
        assertEquals(5, eligibleSum); // a,b,c,d,bad — open has null pnl

        // Weighted win rate from session maps
        double wins = 0;
        double eligible = 0;
        for (String key : dist.getEligibleTradesBySession().keySet()) {
            int e = dist.getEligibleTradesBySession().getOrDefault(key, 0);
            BigDecimal wr = dist.getWinRateBySession().get(key);
            if (e > 0 && wr != null) {
                wins += wr.doubleValue() / 100.0 * e;
                eligible += e;
            }
        }
        assertTrue(eligible > 0);
        double timingWinRate = wins * 100.0 / eligible;
        // wins: a(+), d(+), bad(+) = 3; eligible 5 → 60%
        assertEquals(60.0, timingWinRate, 0.05);

        assertEquals(SESSION_0915_1100,
                TradeDistributionMetricsService.resolveSessionKey(LocalDateTime.of(2024, 1, 8, 10, 0)));
        assertEquals(SESSION_1100_1300,
                TradeDistributionMetricsService.resolveSessionKey(LocalDateTime.of(2024, 1, 8, 11, 0)));

        PerformanceMetrics perf = new PerformanceMetrics();
        registry.calculateAndApplyMetrics(trades, perf);
        // PERFORMANCE includes no-entry PnL 999 → 85+999=1084
        assertEquals(0, new BigDecimal("1084").compareTo(perf.getTotalProfitLoss()));
        assertNotNull(perf.getAverageHoldingTimeMinutes());
    }

    @Test
    void avgPnlMatchesProfitOverEligible() {
        List<TradeDetails> trades = List.of(
                trade("a", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "100"),
                trade("b", LocalDateTime.of(2024, 1, 8, 10, 30),
                        LocalDateTime.of(2024, 1, 8, 10, 40), "-40")
        );
        TradeDistributionMetrics dist = distService.calculateMetrics(trades);
        BigDecimal profit = dist.getProfitBySession().get(SESSION_0915_1100);
        int eligible = dist.getEligibleTradesBySession().get(SESSION_0915_1100);
        BigDecimal avg = dist.getAvgPnlBySession().get(SESSION_0915_1100);
        assertNotNull(avg);
        assertEquals(0, profit.divide(BigDecimal.valueOf(eligible), 4, java.math.RoundingMode.HALF_UP)
                .compareTo(avg));
    }

    private static BigDecimal sum(Map<String, BigDecimal> map) {
        return map.values().stream()
                .filter(v -> v != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    private static TradeDetails tradeNullPnl(String id, LocalDateTime entry, LocalDateTime exit) {
        return TradeDetails.builder()
                .tradeId(id)
                .entryInfo(EntryExitInfo.builder().timestamp(entry).build())
                .exitInfo(EntryExitInfo.builder().timestamp(exit).build())
                .metrics(TradeMetrics.builder().build())
                .build();
    }
}
