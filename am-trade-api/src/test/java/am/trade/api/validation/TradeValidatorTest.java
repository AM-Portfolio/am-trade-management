package am.trade.api.validation;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeEntryExistReasoning;
import am.trade.common.models.TradeMetrics;
import am.trade.models.enums.Exchange;
import am.trade.models.enums.MarketSegment;
import am.trade.models.enums.TradePositionType;
import am.trade.models.enums.TradeStatus;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TradeValidatorTest {

    private final TradeValidator validator = new TradeValidator();

    @BeforeEach
    void setUp() {
        UserContext.setUserId("test-user");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void openTradeWithoutExit_passes() {
        TradeDetails trade = baseOpenTrade();
        assertDoesNotThrow(() -> validator.validateTrade(trade));
        assertEquals("test-user", trade.getUserId());
    }

    @Test
    void closedTradeWithoutExit_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.setStatus(TradeStatus.WIN);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Exit information is required for closed trades", ex.getMessage());
    }

    @Test
    void negativeEntryQuantity_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.getEntryInfo().setQuantity(0);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Entry quantity must be greater than 0", ex.getMessage());
    }

    @Test
    void exitBeforeEntry_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.setStatus(TradeStatus.LOSS);
        LocalDateTime entry = trade.getEntryInfo().getTimestamp();
        trade.setExitInfo(EntryExitInfo.builder()
                .timestamp(entry.minusMinutes(5))
                .price(new BigDecimal("90"))
                .quantity(10)
                .build());
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Exit timestamp must be on or after entry timestamp", ex.getMessage());
    }

    @Test
    void exitQuantityExceedsEntry_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.setStatus(TradeStatus.WIN);
        trade.setExitInfo(EntryExitInfo.builder()
                .timestamp(trade.getEntryInfo().getTimestamp().plusHours(1))
                .price(new BigDecimal("120"))
                .quantity(50)
                .build());
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Exit quantity cannot exceed entry quantity", ex.getMessage());
    }

    @Test
    void closedTradeWithValidExit_passes() {
        TradeDetails trade = baseOpenTrade();
        trade.setStatus(TradeStatus.WIN);
        trade.setExitInfo(EntryExitInfo.builder()
                .timestamp(trade.getEntryInfo().getTimestamp().plusHours(2))
                .price(new BigDecimal("110"))
                .quantity(10)
                .build());
        assertDoesNotThrow(() -> validator.validateTrade(trade));
    }

    @Test
    void missingSymbol_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.setSymbol(null);
        trade.setInstrumentInfo(null);
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Symbol is required", ex.getMessage());
    }

    @Test
    void instrumentWithoutExchange_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.setInstrumentInfo(InstrumentInfo.builder()
                .symbol("RELIANCE")
                .segment(MarketSegment.EQ)
                .build());
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Exchange is required when instrument info is provided", ex.getMessage());
    }

    @Test
    void confidenceOutOfRange_fails() {
        TradeDetails trade = baseOpenTrade();
        trade.setEntryReasoning(TradeEntryExistReasoning.builder()
                .confidenceLevel(11)
                .build());
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class, () -> validator.validateTrade(trade));
        assertEquals("Entry confidence level must be between 1 and 10", ex.getMessage());
    }

    @Test
    void clientMetricsDoNotAffectValidation() {
        TradeDetails trade = baseOpenTrade();
        trade.setMetrics(TradeMetrics.builder()
                .profitLoss(new BigDecimal("99999"))
                .build());
        assertDoesNotThrow(() -> validator.validateTrade(trade));
    }

    private static TradeDetails baseOpenTrade() {
        LocalDateTime now = LocalDateTime.of(2026, 11, 2, 10, 0);
        return TradeDetails.builder()
                .portfolioId("pf-1")
                .symbol("RELIANCE")
                .status(TradeStatus.OPEN)
                .tradePositionType(TradePositionType.LONG)
                .instrumentInfo(InstrumentInfo.builder()
                        .symbol("RELIANCE")
                        .exchange(Exchange.NSE)
                        .segment(MarketSegment.EQ)
                        .build())
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(now)
                        .price(new BigDecimal("100.50"))
                        .quantity(10)
                        .build())
                .build();
    }
}
