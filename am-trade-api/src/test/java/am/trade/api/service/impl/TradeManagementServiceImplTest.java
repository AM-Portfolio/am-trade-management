package am.trade.api.service.impl;

import am.trade.api.service.impl.TradeManagementServiceImpl;
import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import am.trade.services.service.TradeDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import am.trade.common.models.CalendarLatestActivity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeManagementServiceImplTest {

    @Mock
    private TradeDetailsService tradeDetailsService;
    @Mock
    private am.trade.persistence.repository.PortfolioRepository portfolioRepository;
    @Mock
    private am.trade.persistence.mapper.TradeDetailsMapper tradeDetailsMapper;
    @Mock
    private am.trade.common.logger.AppLogger appLogger;
    @Mock
    private am.trade.api.client.MarketDataApiClient marketDataApiClient;
    @Mock
    private am.trade.services.service.PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;

    private TradeManagementServiceImpl tradeManagementService;

    @BeforeEach
    void setUp() {
        com.am.security.context.UserContext.setUserId("test-user");
        tradeManagementService = new TradeManagementServiceImpl(tradeDetailsService, portfolioRepository,
                tradeDetailsMapper, appLogger, marketDataApiClient, new am.trade.common.util.ValidationUtils(),
                portfolioSyncInstrumentResolver);
    }

    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        com.am.security.context.UserContext.clear();
    }

    @Test
    void getTradeDetailsByMonth_shouldFilterTradesCorrectly() {
        // Given
        String portfolioId = "test-portfolio";
        int year = 2020;
        int month = 7;

        // Trade 1: Inside July 2020 (Beginning)
        TradeDetails trade1 = createTrade("t1", portfolioId, LocalDateTime.of(2020, 7, 1, 0, 0));

        // Trade 2: Inside July 2020 (Middle)
        TradeDetails trade2 = createTrade("t2", portfolioId, LocalDateTime.of(2020, 7, 15, 12, 0));

        // Trade 3: Inside July 2020 (End)
        TradeDetails trade3 = createTrade("t3", portfolioId, LocalDateTime.of(2020, 7, 31, 23, 59, 59));

        // Trade 4: Outside (June)
        TradeDetails trade4 = createTrade("t4", portfolioId, LocalDateTime.of(2020, 6, 30, 23, 59, 59));

        // Trade 5: Outside (August)
        TradeDetails trade5 = createTrade("t5", portfolioId, LocalDateTime.of(2020, 8, 1, 0, 0));

        when(tradeDetailsService.findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(
                org.mockito.ArgumentMatchers.eq("test-user"),
                org.mockito.ArgumentMatchers.eq(portfolioId), 
                org.mockito.ArgumentMatchers.any(LocalDateTime.class), 
                org.mockito.ArgumentMatchers.any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(trade1, trade2, trade3));

        // When
        Map<String, List<TradeDetails>> result = tradeManagementService.getTradeDetailsByMonth(year, month,
                portfolioId);

        // Then
        assertTrue(result.containsKey(portfolioId));
        List<TradeDetails> trades = result.get(portfolioId);
        assertEquals(3, trades.size());
        assertTrue(trades.contains(trade1));
        assertTrue(trades.contains(trade2));
        assertTrue(trades.contains(trade3));
        assertFalse(trades.contains(trade4));
        assertFalse(trades.contains(trade5));
    }

    @Test
    void ownerWideDateRangeOmitsPaperPortfolios() {
        TradeDetails broker = createTrade("t1", "broker", LocalDateTime.of(2020, 7, 15, 12, 0));
        TradeDetails paper = createTrade("t2", "paper", LocalDateTime.of(2020, 7, 15, 12, 0));
        when(tradeDetailsService.findModelsByUserIdAndEntryInfoTimestampBetween(
                eq("test-user"),
                any(LocalDateTime.class),
                any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(broker, paper));
        when(portfolioRepository.findByOwnerId("test-user")).thenReturn(List.of(
                am.trade.persistence.entity.PortfolioEntity.builder().portfolioId("paper").kind("PAPER").build(),
                am.trade.persistence.entity.PortfolioEntity.builder().portfolioId("broker").kind("BROKER").build()));

        Map<String, List<TradeDetails>> result = tradeManagementService.getTradeDetailsByMonth(2020, 7, null);

        assertTrue(result.containsKey("broker"));
        assertFalse(result.containsKey("paper"));
        assertEquals(1, result.get("broker").size());
    }

    @Test
    void getLatestCalendarActivity_withPortfolio_returnsYearMonthFromNewestTrade() {
        TradeDetails latest = createTrade("t1", "pf-1", LocalDateTime.of(2024, 3, 18, 10, 0));
        when(tradeDetailsService.findLatestByUserIdAndPortfolioId("test-user", "pf-1"))
                .thenReturn(Optional.of(latest));

        Optional<CalendarLatestActivity> activity =
                tradeManagementService.getLatestCalendarActivity("pf-1");

        assertTrue(activity.isPresent());
        assertEquals(LocalDate.of(2024, 3, 18), activity.get().getLastTradeDate());
        assertEquals(2024, activity.get().getYear());
        assertEquals(3, activity.get().getMonth());
    }

    @Test
    void getLatestCalendarActivity_userWide_skipsPaperNewestAndPicksLive() {
        TradeDetails paperNewest = createTrade("paper-1", "paper", LocalDateTime.of(2025, 1, 10, 9, 0));
        TradeDetails liveOlder = createTrade("live-1", "broker", LocalDateTime.of(2024, 11, 5, 14, 0));
        when(portfolioRepository.findByOwnerId("test-user")).thenReturn(List.of(
                am.trade.persistence.entity.PortfolioEntity.builder().portfolioId("paper").kind("PAPER").build(),
                am.trade.persistence.entity.PortfolioEntity.builder().portfolioId("broker").kind("BROKER").build()));
        when(tradeDetailsService.findLatestByUserId("test-user")).thenReturn(Optional.of(paperNewest));
        when(tradeDetailsService.findPageByUserId(eq("test-user"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(paperNewest, liveOlder)));

        Optional<CalendarLatestActivity> activity =
                tradeManagementService.getLatestCalendarActivity(null);

        assertTrue(activity.isPresent());
        assertEquals(LocalDate.of(2024, 11, 5), activity.get().getLastTradeDate());
        assertEquals(2024, activity.get().getYear());
        assertEquals(11, activity.get().getMonth());
    }

    @Test
    void getLatestCalendarActivity_empty_returnsEmptyOptional() {
        when(tradeDetailsService.findLatestByUserIdAndPortfolioId("test-user", "pf-empty"))
                .thenReturn(Optional.empty());
        when(portfolioRepository.findByPortfolioId("pf-empty")).thenReturn(Optional.empty());

        Optional<CalendarLatestActivity> activity =
                tradeManagementService.getLatestCalendarActivity("pf-empty");

        assertTrue(activity.isEmpty());
    }

    private TradeDetails createTrade(String id, String portfolioId, LocalDateTime entryTime) {
        return TradeDetails.builder()
                .tradeId(id)
                .portfolioId(portfolioId)
                .entryInfo(EntryExitInfo.builder().timestamp(entryTime).build())
                .build();
    }
}
