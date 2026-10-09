package am.trade.api.service.impl;

import am.trade.api.client.MarketDataApiClient;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.util.ValidationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioSyncInstrumentResolverImplTest {

    private static final String RELIANCE_ISIN = "INE002A01018";

    @Mock
    private MarketDataApiClient marketDataApiClient;
    @Mock
    private ValidationUtils validationUtils;

    private PortfolioSyncInstrumentResolverImpl resolver;

    @BeforeEach
    void setUp() {
        resolver = new PortfolioSyncInstrumentResolverImpl(marketDataApiClient, validationUtils);
    }

    @Test
    void resolveForSync_isinHit_skipsSymbolNameFallback() {
        TradeDetails trade = TradeDetails.builder()
                .tradeId("t-isin")
                .symbol(RELIANCE_ISIN)
                .instrumentInfo(InstrumentInfo.builder().isin(RELIANCE_ISIN).build())
                .build();

        when(validationUtils.isValidIsin(RELIANCE_ISIN)).thenReturn(true);
        when(validationUtils.isValidIsin("RELIANCE")).thenReturn(false);

        Map<String, String> isinInfo = new HashMap<>();
        isinInfo.put("symbol", "RELIANCE");
        isinInfo.put("description", "Reliance Industries Ltd");
        when(marketDataApiClient.resolveTickersByIsins(List.of(RELIANCE_ISIN)))
                .thenReturn(Map.of(RELIANCE_ISIN, isinInfo));

        resolver.resolveForSync(trade);

        assertEquals("RELIANCE", trade.getSymbol());
        assertEquals("Reliance Industries Ltd", trade.getInstrumentInfo().getDescription());
        verify(marketDataApiClient, never()).resolveTickersByQueries(anyList(), anyList());
    }

    @Test
    void resolveForSync_symbolNoOp_fallsThroughToNameAlias() {
        TradeDetails trade = TradeDetails.builder()
                .tradeId("t-alias")
                .symbol("IDEA")
                .instrumentInfo(InstrumentInfo.builder()
                        .symbol("IDEA")
                        .description("Vodafone Idea Limited")
                        .build())
                .build();

        when(validationUtils.isValidIsin("IDEA")).thenReturn(false);
        when(validationUtils.isValidIsin("VODAFONEIDEA")).thenReturn(false);

        Map<String, String> symbolHit = Map.of("symbol", "IDEA", "description", "Idea Cellular");
        Map<String, String> nameHit = Map.of("symbol", "VODAFONEIDEA", "description", "Vodafone Idea Limited");
        Map<String, Map<String, String>> byQuery = new HashMap<>();
        byQuery.put("IDEA", symbolHit);
        byQuery.put("VODAFONE IDEA LIMITED", nameHit);

        when(marketDataApiClient.resolveTickersByQueries(
                anyList(), eq(List.of("SYMBOL", "NAME")))).thenReturn(byQuery);

        resolver.resolveForSync(trade);

        assertEquals("VODAFONEIDEA", trade.getSymbol());
        assertEquals("Vodafone Idea Limited", trade.getInstrumentInfo().getDescription());
    }

    @Test
    void stripBrokerSeriesSuffix_removesEqAndBe() {
        assertEquals("RELIANCE", PortfolioSyncInstrumentResolverImpl.stripBrokerSeriesSuffix("RELIANCE-EQ"));
        assertEquals("YESBANK", PortfolioSyncInstrumentResolverImpl.stripBrokerSeriesSuffix("YESBANK - BE"));
        assertEquals("TCS", PortfolioSyncInstrumentResolverImpl.stripBrokerSeriesSuffix("TCS"));
    }

    @Test
    void resolveForSync_nullAndEmpty_noop() {
        resolver.resolveForSync((TradeDetails) null);
        resolver.resolveForSync(List.of());
        verify(marketDataApiClient, never()).resolveTickersByIsins(anyList());
    }
}
