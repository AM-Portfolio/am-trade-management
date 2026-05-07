package am.trade.services.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.enums.TradeStatus;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.TradeDetailsService;

@ExtendWith(MockitoExtension.class)
public class TradeProcessingServiceImplTest {

    @Mock
    private TradeDetailsService tradeDetailsService;

    @Mock
    private PortfolioPersistenceService portfolioPersistenceService;

    @InjectMocks
    private TradeProcessingServiceImpl tradeProcessingService;

    @Test
    void calculatePortfolioMetrics_ShouldCalculateCorrectly() {
        // Arrange
        List<String> tradeIds = Arrays.asList("t1", "t2", "t3");
        
        TradeDetails winTrade = TradeDetails.builder()
                .status(TradeStatus.WIN)
                .metrics(TradeMetrics.builder().profitLoss(new BigDecimal("100.00")).build())
                .build();
                
        TradeDetails lossTrade = TradeDetails.builder()
                .status(TradeStatus.LOSS)
                .metrics(TradeMetrics.builder().profitLoss(new BigDecimal("-50.00")).build())
                .build();
                
        TradeDetails openTrade = TradeDetails.builder()
                .status(TradeStatus.OPEN)
                .metrics(TradeMetrics.builder().profitLoss(BigDecimal.ZERO).build())
                .build();

        when(tradeDetailsService.findModelsByTradeIds(tradeIds))
                .thenReturn(Arrays.asList(winTrade, lossTrade, openTrade));

        // Act
        // We use reflection or call a public method that triggers this. 
        // Since calculatePortfolioMetrics is private, we test it through processTradeDetails
        when(portfolioPersistenceService.findByPortfolioId("p1")).thenReturn(Optional.empty());
        when(portfolioPersistenceService.savePortfolio(any(PortfolioModel.class))).thenAnswer(i -> i.getArguments()[0]);

        tradeProcessingService.processTradeDetails(tradeIds, "p1", "u1");

        // Assert
        verify(portfolioPersistenceService).savePortfolio(argThat(portfolio -> {
            PortfolioMetrics metrics = portfolio.getMetrics();
            assertEquals(3, metrics.getTotalTrades());
            assertEquals(1, metrics.getWinningTrades());
            assertEquals(1, metrics.getLosingTrades());
            assertEquals(1, metrics.getOpenPositions());
            assertEquals(new BigDecimal("100.00"), metrics.getTotalProfit());
            assertEquals(new BigDecimal("50.00"), metrics.getTotalLoss()); // abs value
            assertEquals(new BigDecimal("50.00"), metrics.getNetProfitLoss());
            return true;
        }));
    }

    @Test
    void processTradeDetails_WhenPortfolioExists_ShouldUpdateExisting() {
        // Arrange
        String portfolioId = "p1";
        List<String> newTradeIds = Arrays.asList("t2");
        PortfolioModel existingPortfolio = PortfolioModel.builder()
                .portfolioId(portfolioId)
                .tradeIds(Arrays.asList("t1"))
                .build();

        when(portfolioPersistenceService.findByPortfolioId(portfolioId)).thenReturn(Optional.of(existingPortfolio));
        when(tradeDetailsService.findModelsByTradeIds(anyList())).thenReturn(Arrays.asList());
        when(portfolioPersistenceService.savePortfolio(any(PortfolioModel.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        tradeProcessingService.processTradeDetails(newTradeIds, portfolioId, "u1");

        // Assert
        verify(portfolioPersistenceService).savePortfolio(argThat(portfolio -> {
            assertEquals(2, portfolio.getTradeIds().size());
            assertTrue(portfolio.getTradeIds().contains("t1"));
            assertTrue(portfolio.getTradeIds().contains("t2"));
            return true;
        }));
    }
}
