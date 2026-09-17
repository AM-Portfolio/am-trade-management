package am.trade.dashboard.service.metrics;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.TradingStyleHint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_0915_1100;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_1100_1300;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_1300_1500;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_1500_1530;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_KEYS;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_OTHER;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.resolveSessionKey;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TradeDistributionSessionGroupingSpec {

    private final TradeDistributionMetricsService service =
            new TradeDistributionMetricsService();

    @ParameterizedTest
    @CsvSource({
            "2024-01-08T09:14:00, OTHER",
            "2024-01-08T09:15:00, SESSION_0915_1100",
            "2024-01-08T10:59:00, SESSION_0915_1100",
            "2024-01-08T11:00:00, SESSION_1100_1300",
            "2024-01-08T12:59:00, SESSION_1100_1300",
            "2024-01-08T13:00:00, SESSION_1300_1500",
            "2024-01-08T14:59:00, SESSION_1300_1500",
            "2024-01-08T15:00:00, SESSION_1500_1530",
            "2024-01-08T15:29:00, SESSION_1500_1530",
            "2024-01-08T15:30:00, OTHER",
            "2024-01-08T08:00:00, OTHER",
            "2024-01-08T16:00:00, OTHER"
    })
    void resolveSessionKey_boundaries(String iso, String expected) {
        assertEquals(expected, resolveSessionKey(LocalDateTime.parse(iso)));
    }

    @Test
    void calculateMetrics_alwaysEmitsAllSessionKeys() {
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(
                trade("t1", LocalDateTime.of(2024, 1, 8, 10, 0), LocalDateTime.of(2024, 1, 8, 10, 30), "100")
        ));

        assertNotNull(metrics.getTradesBySession());
        for (String key : SESSION_KEYS) {
            assertTrue(metrics.getTradesBySession().containsKey(key), "missing " + key);
            assertTrue(metrics.getProfitBySession().containsKey(key));
            assertTrue(metrics.getEligibleTradesBySession().containsKey(key));
        }
        assertEquals(1, metrics.getTradesBySession().get(SESSION_0915_1100));
        assertEquals(0, metrics.getTradesBySession().get(SESSION_OTHER));
        assertEquals("entry_local_as_stored", metrics.getTimezoneNote());
    }

    @Test
    void calculateMetrics_sessionPnlWinRateAvgPnlGolden() {
        List<TradeDetails> trades = List.of(
                trade("a", LocalDateTime.of(2024, 1, 8, 10, 0), LocalDateTime.of(2024, 1, 8, 10, 20), "100"),
                trade("b", LocalDateTime.of(2024, 1, 8, 10, 30), LocalDateTime.of(2024, 1, 8, 10, 40), "-40"),
                trade("c", LocalDateTime.of(2024, 1, 8, 12, 0), LocalDateTime.of(2024, 1, 8, 12, 30), "50"),
                trade("d", LocalDateTime.of(2024, 1, 8, 14, 0), LocalDateTime.of(2024, 1, 8, 14, 10), "20"),
                trade("e", LocalDateTime.of(2024, 1, 8, 15, 10), LocalDateTime.of(2024, 1, 8, 15, 20), "-10"),
                trade("f", LocalDateTime.of(2024, 1, 8, 16, 0), LocalDateTime.of(2024, 1, 8, 16, 5), "5")
        );

        TradeDistributionMetrics metrics = service.calculateMetrics(trades);

        assertEquals(2, metrics.getTradesBySession().get(SESSION_0915_1100));
        assertEquals(new BigDecimal("60"), metrics.getProfitBySession().get(SESSION_0915_1100));
        assertEquals(0, new BigDecimal("50.00").compareTo(metrics.getWinRateBySession().get(SESSION_0915_1100)));
        assertEquals(0, new BigDecimal("30.0000").compareTo(metrics.getAvgPnlBySession().get(SESSION_0915_1100)));

        assertEquals(1, metrics.getTradesBySession().get(SESSION_1100_1300));
        assertEquals(1, metrics.getTradesBySession().get(SESSION_1300_1500));
        assertEquals(1, metrics.getTradesBySession().get(SESSION_1500_1530));
        assertEquals(1, metrics.getTradesBySession().get(SESSION_OTHER));
    }

    @Test
    void winRate_excludesNullPnlFromDenominator() {
        TradeDetails withPnl = trade("w", LocalDateTime.of(2024, 1, 8, 10, 0),
                LocalDateTime.of(2024, 1, 8, 10, 5), "100");
        TradeDetails open = TradeDetails.builder()
                .tradeId("o")
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(LocalDateTime.of(2024, 1, 8, 10, 30))
                        .build())
                .metrics(TradeMetrics.builder().build())
                .build();

        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(withPnl, open));

        assertEquals(2, metrics.getTradesBySession().get(SESSION_0915_1100));
        assertEquals(1, metrics.getEligibleTradesBySession().get(SESSION_0915_1100));
        assertEquals(0, new BigDecimal("100.00").compareTo(metrics.getWinRateBySession().get(SESSION_0915_1100)));
        assertEquals(1, metrics.getOpenOrMissingPnlCount());
    }

    @Test
    void skippedMissingEntry_countedAndExcludedFromSession() {
        TradeDetails missing = TradeDetails.builder()
                .tradeId("m")
                .metrics(TradeMetrics.builder().profitLoss(BigDecimal.TEN).build())
                .build();
        TradeDetails ok = trade("ok", LocalDateTime.of(2024, 1, 8, 10, 0),
                LocalDateTime.of(2024, 1, 8, 10, 5), "10");

        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(missing, ok));

        assertEquals(1, metrics.getSkippedMissingEntryCount());
        assertEquals(1, metrics.getTradesBySession().get(SESSION_0915_1100));
    }

    @Test
    void styleHint_unknownWhenFewerThan10() {
        List<TradeDetails> trades = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            trades.add(trade("s" + i,
                    LocalDateTime.of(2024, 1, 8, 10, i),
                    LocalDateTime.of(2024, 1, 8, 10, i).plusMinutes(5),
                    "1"));
        }
        TradingStyleHint hint = service.calculateMetrics(trades).getTradingStyleHint();
        assertEquals("UNKNOWN", hint.getStyle());
        assertEquals(5, hint.getSampleSize());
        assertEquals("holding_duration", hint.getBasis());
    }

    @Test
    void styleHint_scalperWhenMajorityUnder15Minutes() {
        List<TradeDetails> trades = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            trades.add(trade("s" + i,
                    LocalDateTime.of(2024, 1, 8, 10, 0),
                    LocalDateTime.of(2024, 1, 8, 10, 0).plusMinutes(5),
                    "1"));
        }
        assertEquals("SCALPER", service.calculateMetrics(trades).getTradingStyleHint().getStyle());
    }

    @Test
    void styleHint_swingWhenMajorityOver24Hours() {
        List<TradeDetails> trades = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime entry = LocalDateTime.of(2024, 1, 8, 10, 0);
            trades.add(trade("s" + i, entry, entry.plusDays(2), "1"));
        }
        assertEquals("SWING", service.calculateMetrics(trades).getTradingStyleHint().getStyle());
    }

    @Test
    void styleHint_intradayWhenMajorityUnder24hButNotScalper() {
        List<TradeDetails> trades = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            LocalDateTime entry = LocalDateTime.of(2024, 1, 8, 10, 0);
            trades.add(trade("s" + i, entry, entry.plusHours(2), "1"));
        }
        assertEquals("INTRADAY", service.calculateMetrics(trades).getTradingStyleHint().getStyle());
    }

    @Test
    void styleHint_mixedWhenNoDominantClass() {
        List<TradeDetails> trades = new ArrayList<>();
        LocalDateTime base = LocalDateTime.of(2024, 1, 8, 10, 0);
        // 2 scalper + 3 longer-intraday + 5 swing = 10 sample.
        // scalper 20%, under-24h 50%, swing 50% → none ≥55% → MIXED
        for (int i = 0; i < 2; i++) {
            trades.add(trade("sc" + i, base, base.plusMinutes(5), "1"));
        }
        for (int i = 0; i < 3; i++) {
            trades.add(trade("in" + i, base, base.plusHours(2), "1"));
        }
        for (int i = 0; i < 5; i++) {
            trades.add(trade("sw" + i, base, base.plusDays(2), "1"));
        }
        assertEquals("MIXED", service.calculateMetrics(trades).getTradingStyleHint().getStyle());
    }

    @Test
    void badTimestamp_exitBeforeEntry() {
        TradeDetails bad = TradeDetails.builder()
                .tradeId("bad")
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(LocalDateTime.of(2024, 1, 8, 12, 0))
                        .build())
                .exitInfo(EntryExitInfo.builder()
                        .timestamp(LocalDateTime.of(2024, 1, 8, 10, 0))
                        .build())
                .metrics(TradeMetrics.builder().profitLoss(BigDecimal.ONE).build())
                .build();

        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(bad));
        assertEquals(1, metrics.getBadTimestampCount());
        assertEquals(0, metrics.getTradingStyleHint().getSampleSize());
    }

    @Test
    void emptyList_emitsSessionSkeleton() {
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of());
        assertEquals(5, metrics.getTradesBySession().size());
        assertEquals(0, metrics.getTradesBySession().get(SESSION_0915_1100));
        assertNull(metrics.getWinRateBySession().get(SESSION_0915_1100));
        assertEquals("UNKNOWN", metrics.getTradingStyleHint().getStyle());
    }

    @Test
    void calculateMetrics_stillFillsHourDayMonth() {
        List<TradeDetails> trades = List.of(
                trade("t1", LocalDateTime.of(2024, 1, 8, 10, 15), LocalDateTime.of(2024, 1, 8, 10, 30), "100"),
                trade("t2", LocalDateTime.of(2024, 1, 8, 10, 45), LocalDateTime.of(2024, 1, 8, 11, 0), "-40"),
                trade("t3", LocalDateTime.of(2024, 2, 14, 14, 0), LocalDateTime.of(2024, 2, 14, 15, 0), "50")
        );

        TradeDistributionMetrics metrics = service.calculateMetrics(trades);

        assertEquals(2, metrics.getTradesByHour().get("10"));
        assertEquals(1, metrics.getTradesByHour().get("14"));
        assertEquals(new BigDecimal("60"), metrics.getProfitByHour().get("10"));
        assertEquals(0, new BigDecimal("50.00").compareTo(metrics.getWinRateByHour().get("10")));
        assertEquals(2, metrics.getTradesByDay().get("MONDAY"));
        assertEquals(2, metrics.getTradesByMonth().get("JANUARY"));
    }

    @Test
    void avgHoldMinutesAndRiskReward_bySession() {
        // Session 09:15–11:00: +100 in 20m, -50 in 10m → avg hold 15m, R:R = 100/50 = 2
        List<TradeDetails> trades = List.of(
                trade("w", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 20), "100"),
                trade("l", LocalDateTime.of(2024, 1, 8, 10, 30),
                        LocalDateTime.of(2024, 1, 8, 10, 40), "-50"),
                // only wins → R:R null
                trade("solo", LocalDateTime.of(2024, 1, 8, 12, 0),
                        LocalDateTime.of(2024, 1, 8, 12, 30), "10")
        );

        TradeDistributionMetrics metrics = service.calculateMetrics(trades);

        assertEquals(0, new BigDecimal("15.0000")
                .compareTo(metrics.getAvgHoldMinutesBySession().get(SESSION_0915_1100)));
        assertEquals(0, new BigDecimal("2.0000")
                .compareTo(metrics.getRiskRewardBySession().get(SESSION_0915_1100)));
        assertNull(metrics.getRiskRewardBySession().get(SESSION_1100_1300));
        assertEquals(0, new BigDecimal("30.0000")
                .compareTo(metrics.getAvgHoldMinutesBySession().get(SESSION_1100_1300)));
    }

    @Test
    void bestSession_requiresMinEligibleThree() {
        List<TradeDetails> trades = new ArrayList<>();
        // 3 trades in 09:15 session avg +10
        for (int i = 0; i < 3; i++) {
            trades.add(trade("a" + i,
                    LocalDateTime.of(2024, 1, 8, 10, i),
                    LocalDateTime.of(2024, 1, 8, 10, i).plusMinutes(5),
                    "10"));
        }
        // 2 trades in 11:00 session with higher avg — not enough sample
        trades.add(trade("b0", LocalDateTime.of(2024, 1, 8, 12, 0),
                LocalDateTime.of(2024, 1, 8, 12, 5), "100"));
        trades.add(trade("b1", LocalDateTime.of(2024, 1, 8, 12, 10),
                LocalDateTime.of(2024, 1, 8, 12, 15), "100"));

        TradeDistributionMetrics metrics = service.calculateMetrics(trades);

        assertEquals(SESSION_0915_1100, metrics.getBestSessionKey());
        assertEquals(0, new BigDecimal("10.0000").compareTo(metrics.getBestSessionAvgPnl()));
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
