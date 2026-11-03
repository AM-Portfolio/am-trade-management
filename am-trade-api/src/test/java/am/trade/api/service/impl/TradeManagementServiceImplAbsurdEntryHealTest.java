package am.trade.api.service.impl;

import am.trade.api.client.MarketDataApiClient;
import am.trade.common.logger.AppLogger;
import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.market.MarketDataResponse;
import am.trade.common.util.ValidationUtils;
import am.trade.models.enums.TradeStatus;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.PortfolioRepository;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.PortfolioSyncInstrumentResolver;
import am.trade.services.service.TradeDetailsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.CacheManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers PR #80 absurd entry-vs-LTP heal: clear cost only past 50× and only with import-mark fallback.
 */
@ExtendWith(MockitoExtension.class)
class TradeManagementServiceImplAbsurdEntryHealTest {

    @Mock
    private TradeDetailsService tradeDetailsService;
    @Mock
    private PortfolioRepository portfolioRepository;
    @Mock
    private PortfolioService portfolioService;
    @Mock
    private TradeDetailsMapper tradeDetailsMapper;
    @Mock
    private AppLogger appLogger;
    @Mock
    private MarketDataApiClient marketDataApiClient;
    @Mock
    private PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;
    @Mock
    private CacheManager cacheManager;

    private TradeManagementServiceImpl service;

    @BeforeEach
    void setUp() {
        com.am.security.context.UserContext.setUserId("test-user");
        service = new TradeManagementServiceImpl(
                tradeDetailsService,
                portfolioRepository,
                portfolioService,
                tradeDetailsMapper,
                appLogger,
                marketDataApiClient,
                new ValidationUtils(),
                portfolioSyncInstrumentResolver,
                cacheManager);
    }

    @AfterEach
    void tearDown() {
        com.am.security.context.UserContext.clear();
    }

    @Test
    void enrich_clearsAbsurdHundredXEntry_whenImportMarkAlreadyPresent() {
        TradeDetails trade = openHolding(
                "BSE",
                new BigDecimal("36.81"),
                LocalDateTime.of(2024, 1, 10, 10, 0),
                new BigDecimal("3600"));
        stubQuotes("BSE", 3665.0, 3600.0);
        when(tradeDetailsService.findModelsByUserIdAndPortfolioId("test-user", "p1"))
                .thenReturn(List.of(trade));

        List<TradeDetails> result = service.getAllTradesByTradePortfolioId("p1");

        TradeDetails out = result.get(0);
        assertNull(out.getEntryInfo().getPrice(), "absurd entry must clear when mark exists");
        assertEquals(0, new BigDecimal("3600").compareTo(out.getImportMarkPrice()));
        assertEquals("IMPORT_MARK", out.getMetrics().getUnrealizedBasis());
        assertNotNull(out.getMetrics().getProfitLoss());
        verify(marketDataApiClient, never()).getCloseOnOrBefore(any(), any());
        verify(tradeDetailsService).saveAllTradeDetails(anyList());
    }

    @Test
    void enrich_clearsAbsurdHundredXEntry_afterHistImportMarkBackfill() {
        TradeDetails trade = openHolding(
                "BSE",
                new BigDecimal("36.81"),
                LocalDateTime.of(2024, 1, 10, 10, 0),
                null);
        stubQuotes("BSE", 3665.0, 3600.0);
        when(marketDataApiClient.getCloseOnOrBefore(eq("BSE"), eq(LocalDate.of(2024, 1, 10))))
                .thenReturn(3600.0);
        when(tradeDetailsService.findModelsByUserIdAndPortfolioId("test-user", "p1"))
                .thenReturn(List.of(trade));

        List<TradeDetails> result = service.getAllTradesByTradePortfolioId("p1");

        TradeDetails out = result.get(0);
        assertNull(out.getEntryInfo().getPrice());
        assertEquals(0, new BigDecimal("3600.0").compareTo(out.getImportMarkPrice()));
        assertEquals("IMPORT_MARK", out.getMetrics().getUnrealizedBasis());
        verify(marketDataApiClient).getCloseOnOrBefore("BSE", LocalDate.of(2024, 1, 10));
    }

    @Test
    void enrich_keepsMultiBaggerFifteenXEntry_withoutWipingCost() {
        TradeDetails trade = openHolding(
                "WINNER",
                new BigDecimal("100"),
                LocalDateTime.of(2020, 6, 1, 10, 0),
                null);
        stubQuotes("WINNER", 1500.0, 1490.0);
        when(tradeDetailsService.findModelsByUserIdAndPortfolioId("test-user", "p1"))
                .thenReturn(List.of(trade));

        List<TradeDetails> result = service.getAllTradesByTradePortfolioId("p1");

        TradeDetails out = result.get(0);
        assertEquals(0, new BigDecimal("100").compareTo(out.getEntryInfo().getPrice()));
        assertEquals("COST", out.getMetrics().getUnrealizedBasis());
        verify(marketDataApiClient, never()).getCloseOnOrBefore(any(), any());
        verify(tradeDetailsService, never()).saveAllTradeDetails(anyList());
    }

    @Test
    void enrich_keepsAbsurdEntry_whenNoImportMarkFallbackAvailable() {
        TradeDetails trade = openHolding(
                "ORPHAN",
                new BigDecimal("10"),
                LocalDateTime.of(2024, 3, 1, 10, 0),
                null);
        stubQuotes("ORPHAN", 1200.0, 1190.0);
        when(marketDataApiClient.getCloseOnOrBefore(eq("ORPHAN"), eq(LocalDate.of(2024, 3, 1))))
                .thenReturn(null);
        when(tradeDetailsService.findModelsByUserIdAndPortfolioId("test-user", "p1"))
                .thenReturn(List.of(trade));

        List<TradeDetails> result = service.getAllTradesByTradePortfolioId("p1");

        TradeDetails out = result.get(0);
        assertEquals(0, new BigDecimal("10").compareTo(out.getEntryInfo().getPrice()),
                "must keep cost basis when heal has no mark fallback");
        assertNull(out.getImportMarkPrice());
        assertEquals("COST", out.getMetrics().getUnrealizedBasis());
        verify(tradeDetailsService, never()).saveAllTradeDetails(anyList());
    }

    @Test
    void enrich_keepsEntryJustUnderFiftyXThreshold() {
        // 49× stays under ABSURD_ENTRY_RATIO_HIGH (50)
        TradeDetails trade = openHolding(
                "NEAR",
                new BigDecimal("100"),
                LocalDateTime.of(2019, 1, 1, 10, 0),
                null);
        stubQuotes("NEAR", 4900.0, 4800.0);
        when(tradeDetailsService.findModelsByUserIdAndPortfolioId("test-user", "p1"))
                .thenReturn(List.of(trade));

        List<TradeDetails> result = service.getAllTradesByTradePortfolioId("p1");

        assertEquals(0, new BigDecimal("100").compareTo(result.get(0).getEntryInfo().getPrice()));
        assertEquals("COST", result.get(0).getMetrics().getUnrealizedBasis());
    }

    private void stubQuotes(String symbol, double lastPrice, double previousClose) {
        when(marketDataApiClient.getQuotes(anyList())).thenReturn(Map.of(
                symbol,
                MarketDataResponse.builder()
                        .lastPrice(lastPrice)
                        .previousClose(previousClose)
                        .build()));
        // Avoid NPE in refreshPortfolioTotalValues when Mockito returns null Optional.
        when(portfolioService.findByPortfolioId("p1")).thenReturn(java.util.Optional.empty());
    }

    private static TradeDetails openHolding(
            String symbol,
            BigDecimal entryPrice,
            LocalDateTime entryAt,
            BigDecimal importMark) {
        return TradeDetails.builder()
                .tradeId("t-" + symbol)
                .portfolioId("p1")
                .symbol(symbol)
                .status(TradeStatus.OPEN)
                .importMarkPrice(importMark)
                .metrics(new TradeMetrics())
                .entryInfo(EntryExitInfo.builder()
                        .price(entryPrice)
                        .quantity(10)
                        .timestamp(entryAt)
                        .totalValue(entryPrice.multiply(BigDecimal.TEN))
                        .build())
                .build();
    }
}
