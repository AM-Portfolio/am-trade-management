package am.trade.services.service.impl;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResolvePositionValueTest {

    @Test
    void prefersCostBasisWhenPresent() {
        TradeDetails trade = TradeDetails.builder()
                .entryInfo(EntryExitInfo.builder()
                        .quantity(10)
                        .price(new BigDecimal("50"))
                        .totalValue(new BigDecimal("500"))
                        .build())
                .currentPrice(new BigDecimal("80"))
                .importMarkPrice(new BigDecimal("60"))
                .build();
        assertEquals(0, new BigDecimal("500")
                .compareTo(TradeProcessingServiceImpl.resolvePositionMarketOrCostValue(trade)));
    }

    @Test
    void usesLiveMarkWhenCostUnknown() {
        TradeDetails trade = TradeDetails.builder()
                .entryInfo(EntryExitInfo.builder().quantity(10).build())
                .currentPrice(new BigDecimal("80"))
                .importMarkPrice(new BigDecimal("60"))
                .build();
        assertEquals(0, new BigDecimal("800")
                .compareTo(TradeProcessingServiceImpl.resolvePositionMarketOrCostValue(trade)));
    }

    @Test
    void usesImportMarkWhenLiveMissing() {
        TradeDetails trade = TradeDetails.builder()
                .entryInfo(EntryExitInfo.builder().quantity(10).build())
                .importMarkPrice(new BigDecimal("60"))
                .build();
        assertEquals(0, new BigDecimal("600")
                .compareTo(TradeProcessingServiceImpl.resolvePositionMarketOrCostValue(trade)));
    }

    @Test
    void zeroCostEntryDoesNotCountAsCost() {
        TradeDetails trade = TradeDetails.builder()
                .entryInfo(EntryExitInfo.builder()
                        .quantity(10)
                        .price(BigDecimal.ZERO)
                        .totalValue(BigDecimal.ZERO)
                        .build())
                .importMarkPrice(new BigDecimal("12.5"))
                .build();
        assertEquals(0, new BigDecimal("125")
                .compareTo(TradeProcessingServiceImpl.resolvePositionMarketOrCostValue(trade)));
    }
}
