package am.trade.dashboard.service.metrics;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.TradeMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_0915_1100;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_1100_1300;
import static am.trade.dashboard.service.metrics.TradeDistributionMetricsService.SESSION_KEYS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimingDistributionEdgeCasesSpec {

    private final TradeDistributionMetricsService service =
            new TradeDistributionMetricsService();

    @Test
    void avgHold_nullWhenNoValidExit() {
        TradeDetails open = TradeDetails.builder()
                .tradeId("open")
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(LocalDateTime.of(2024, 1, 8, 10, 0))
                        .build())
                .metrics(TradeMetrics.builder().profitLoss(BigDecimal.TEN).build())
                .build();

        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(open));
        assertNull(metrics.getAvgHoldMinutesBySession().get(SESSION_0915_1100));
        assertNull(metrics.getRiskRewardBySession().get(SESSION_0915_1100));
    }

    @Test
    void avgHold_skipsNegativeDuration() {
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
        TradeDetails ok = trade("ok",
                LocalDateTime.of(2024, 1, 8, 10, 0),
                LocalDateTime.of(2024, 1, 8, 10, 30),
                "5");

        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(bad, ok));
        assertEquals(0, new BigDecimal("30.0000")
                .compareTo(metrics.getAvgHoldMinutesBySession().get(SESSION_0915_1100)));
        assertEquals(1, metrics.getBadTimestampCount());
    }

    @Test
    void riskReward_nullWhenOnlyWinsOrOnlyLosses() {
        TradeDistributionMetrics winsOnly = service.calculateMetrics(List.of(
                trade("w1", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "10"),
                trade("w2", LocalDateTime.of(2024, 1, 8, 10, 15),
                        LocalDateTime.of(2024, 1, 8, 10, 25), "20")
        ));
        assertNull(winsOnly.getRiskRewardBySession().get(SESSION_0915_1100));

        TradeDistributionMetrics lossesOnly = service.calculateMetrics(List.of(
                trade("l1", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "-10"),
                trade("l2", LocalDateTime.of(2024, 1, 8, 10, 15),
                        LocalDateTime.of(2024, 1, 8, 10, 25), "-20")
        ));
        assertNull(lossesOnly.getRiskRewardBySession().get(SESSION_0915_1100));
    }

    @Test
    void riskReward_ignoresBreakEven() {
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(
                trade("w", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 10), "100"),
                trade("l", LocalDateTime.of(2024, 1, 8, 10, 15),
                        LocalDateTime.of(2024, 1, 8, 10, 25), "-50"),
                trade("be", LocalDateTime.of(2024, 1, 8, 10, 30),
                        LocalDateTime.of(2024, 1, 8, 10, 40), "0")
        ));
        assertEquals(0, new BigDecimal("2.0000")
                .compareTo(metrics.getRiskRewardBySession().get(SESSION_0915_1100)));
    }

    @Test
    void bestSession_nullWhenNoBucketMeetsMinEligible() {
        List<TradeDetails> trades = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            trades.add(trade("a" + i,
                    LocalDateTime.of(2024, 1, 8, 10, i),
                    LocalDateTime.of(2024, 1, 8, 10, i).plusMinutes(5),
                    "100"));
        }
        TradeDistributionMetrics metrics = service.calculateMetrics(trades);
        assertNull(metrics.getBestSessionKey());
        assertNull(metrics.getBestSessionAvgPnl());
    }

    @Test
    void bestSession_picksHighestAvgAmongQualifying() {
        List<TradeDetails> trades = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            trades.add(trade("m" + i,
                    LocalDateTime.of(2024, 1, 8, 10, i),
                    LocalDateTime.of(2024, 1, 8, 10, i).plusMinutes(5),
                    "10"));
            trades.add(trade("a" + i,
                    LocalDateTime.of(2024, 1, 8, 12, i),
                    LocalDateTime.of(2024, 1, 8, 12, i).plusMinutes(5),
                    "50"));
        }
        TradeDistributionMetrics metrics = service.calculateMetrics(trades);
        assertEquals(SESSION_1100_1300, metrics.getBestSessionKey());
        assertEquals(0, new BigDecimal("50.0000").compareTo(metrics.getBestSessionAvgPnl()));
    }

    @Test
    void dayAndMonth_avgHoldAndRiskRewardPopulated() {
        List<TradeDetails> trades = List.of(
                trade("w", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 20), "100"),
                trade("l", LocalDateTime.of(2024, 1, 8, 14, 0),
                        LocalDateTime.of(2024, 1, 8, 14, 10), "-50")
        );
        TradeDistributionMetrics metrics = service.calculateMetrics(trades);

        assertEquals(0, new BigDecimal("15.0000")
                .compareTo(metrics.getAvgHoldMinutesByDay().get("MONDAY")));
        assertEquals(0, new BigDecimal("2.0000")
                .compareTo(metrics.getRiskRewardByDay().get("MONDAY")));
        assertEquals(0, new BigDecimal("15.0000")
                .compareTo(metrics.getAvgHoldMinutesByMonth().get("JANUARY")));
        assertEquals(0, new BigDecimal("2.0000")
                .compareTo(metrics.getRiskRewardByMonth().get("JANUARY")));
    }

    @Test
    void emptySessionMaps_alwaysContainAllKeysIncludingHoldAndRr() {
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of());
        for (String key : SESSION_KEYS) {
            assertTrue(metrics.getAvgHoldMinutesBySession().containsKey(key));
            assertTrue(metrics.getRiskRewardBySession().containsKey(key));
            assertNull(metrics.getAvgHoldMinutesBySession().get(key));
            assertNull(metrics.getRiskRewardBySession().get(key));
        }
        assertNull(metrics.getBestSessionKey());
    }

    @Test
    void fractionalHoldMinutes_subMinutePrecision() {
        TradeDetails trade = trade("t",
                LocalDateTime.of(2024, 1, 8, 10, 0, 0),
                LocalDateTime.of(2024, 1, 8, 10, 1, 30),
                "1");
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(trade));
        assertEquals(0, new BigDecimal("1.5000")
                .compareTo(metrics.getAvgHoldMinutesBySession().get(SESSION_0915_1100)));
    }

    @ParameterizedTest
    @CsvSource({
            "100,-50,2.0000",
            "50,-50,1.0000",
            "30,-90,0.3333"
    })
    void riskReward_goldenRatios(String win, String loss, String expected) {
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(
                trade("w", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 5), win),
                trade("l", LocalDateTime.of(2024, 1, 8, 10, 10),
                        LocalDateTime.of(2024, 1, 8, 10, 15), loss)
        ));
        assertEquals(0, new BigDecimal(expected)
                .compareTo(metrics.getRiskRewardBySession().get(SESSION_0915_1100)));
    }

    @Test
    void timezoneNote_alwaysEntryLocalAsStored() {
        TradeDistributionMetrics metrics = service.calculateMetrics(List.of(
                trade("t", LocalDateTime.of(2024, 1, 8, 10, 0),
                        LocalDateTime.of(2024, 1, 8, 10, 5), "1")
        ));
        assertEquals("entry_local_as_stored", metrics.getTimezoneNote());
        assertFalse(metrics.getTimezoneNote().toLowerCase().contains("ist"));
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
