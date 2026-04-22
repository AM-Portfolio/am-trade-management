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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

    private TradeManagementServiceImpl tradeManagementService;

    @BeforeEach
    void setUp() {
        tradeManagementService = new TradeManagementServiceImpl(tradeDetailsService, portfolioRepository,
                tradeDetailsMapper, appLogger);
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

        when(portfolioRepository.findByPortfolioId(portfolioId)).thenReturn(java.util.Optional.empty());
        when(tradeDetailsService.findModelsByPortfolioId(portfolioId))
                .thenReturn(Arrays.asList(trade1, trade2, trade3, trade4, trade5));

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

    private TradeDetails createTrade(String id, String portfolioId, LocalDateTime entryTime) {
        return TradeDetails.builder()
                .tradeId(id)
                .portfolioId(portfolioId)
                .entryInfo(EntryExitInfo.builder().timestamp(entryTime).build())
                .build();
    }
}
