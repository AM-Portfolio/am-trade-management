package am.trade.api.service.impl;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HoldingStyleMatchSpec {

    @ParameterizedTest
    @CsvSource({
            "SCALPER, 0, true",
            "SCALPER, 14, true",
            "SCALPER, 15, false",
            "INTRADAY, 14, false",
            "INTRADAY, 15, true",
            "INTRADAY, 1439, true",
            "INTRADAY, 1440, false",
            "SWING, 1439, false",
            "SWING, 1440, true",
            "SWING, 10000, true"
    })
    void matchesHoldingStyle_boundaries(String style, long holdMinutes, boolean expected) {
        LocalDateTime entry = LocalDateTime.of(2024, 1, 8, 10, 0);
        TradeDetails trade = TradeDetails.builder()
                .tradeId("t")
                .entryInfo(EntryExitInfo.builder().timestamp(entry).build())
                .exitInfo(EntryExitInfo.builder().timestamp(entry.plusMinutes(holdMinutes)).build())
                .build();
        assertTrue(expected == TradeMetricsServiceImpl.matchesHoldingStyle(trade, style),
                () -> style + " @ " + holdMinutes + "m expected " + expected);
    }

    @Test
    void matchesHoldingStyle_falseWhenMissingExit() {
        TradeDetails trade = TradeDetails.builder()
                .tradeId("t")
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(LocalDateTime.of(2024, 1, 8, 10, 0))
                        .build())
                .build();
        assertFalse(TradeMetricsServiceImpl.matchesHoldingStyle(trade, "SCALPER"));
    }

    @Test
    void matchesHoldingStyle_falseWhenNegativeDuration() {
        LocalDateTime entry = LocalDateTime.of(2024, 1, 8, 12, 0);
        TradeDetails trade = TradeDetails.builder()
                .tradeId("t")
                .entryInfo(EntryExitInfo.builder().timestamp(entry).build())
                .exitInfo(EntryExitInfo.builder().timestamp(entry.minusMinutes(5)).build())
                .build();
        assertFalse(TradeMetricsServiceImpl.matchesHoldingStyle(trade, "SCALPER"));
    }

    @Test
    void matchesHoldingStyle_unknownStylePasses() {
        LocalDateTime entry = LocalDateTime.of(2024, 1, 8, 10, 0);
        TradeDetails trade = TradeDetails.builder()
                .tradeId("t")
                .entryInfo(EntryExitInfo.builder().timestamp(entry).build())
                .exitInfo(EntryExitInfo.builder().timestamp(entry.plusMinutes(5)).build())
                .build();
        assertTrue(TradeMetricsServiceImpl.matchesHoldingStyle(trade, "CUSTOM"));
    }
}
