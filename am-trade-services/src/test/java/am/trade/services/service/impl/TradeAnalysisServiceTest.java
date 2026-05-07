package am.trade.services.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeEntryExistReasoning;
import am.trade.common.models.TradePsychologyData;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.services.service.TradeDetailsService;

@ExtendWith(MockitoExtension.class)
public class TradeAnalysisServiceTest {

    @Mock
    private TradeDetailsService tradeDetailsService;

    @InjectMocks
    private TradeAnalysisService tradeAnalysisService;

    private TradeDetails testTrade;

    @BeforeEach
    void setUp() {
        testTrade = TradeDetails.builder()
                .tradeId("t1")
                .symbol("AAPL")
                .build();
    }

    @Test
    void addTradeAnalysis_WhenTradeExists_ShouldUpdateAndSave() {
        // Arrange
        TradePsychologyData psychology = new TradePsychologyData();
        TradeEntryExistReasoning reasoning = new TradeEntryExistReasoning();
        
        when(tradeDetailsService.findModelByTradeId("t1")).thenReturn(Optional.of(testTrade));
        when(tradeDetailsService.saveTradeDetails(any(TradeDetails.class))).thenReturn(testTrade);

        // Act
        TradeDetails result = tradeAnalysisService.addTradeAnalysis("t1", psychology, reasoning);

        // Assert
        assertNotNull(result);
        assertEquals(psychology, result.getPsychologyData());
        assertEquals(reasoning, result.getEntryReasoning());
        verify(tradeDetailsService).saveTradeDetails(testTrade);
    }

    @Test
    void addTradeAnalysis_WhenTradeDoesNotExist_ShouldThrowException() {
        // Arrange
        when(tradeDetailsService.findModelByTradeId("unknown")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            tradeAnalysisService.addTradeAnalysis("unknown", null, null);
        });
    }

    @Test
    void analyzeBehaviorPatterns_ShouldReturnCorrectFrequencies() {
        // Arrange
        TradePsychologyData data1 = new TradePsychologyData();
        data1.setBehaviorPatterns(Arrays.asList(TradeBehaviorPattern.CHASING_MOMENTUM, TradeBehaviorPattern.REVENGE_TRADING));
        
        TradePsychologyData data2 = new TradePsychologyData();
        data2.setBehaviorPatterns(Collections.singletonList(TradeBehaviorPattern.CHASING_MOMENTUM));

        TradeDetails t1 = TradeDetails.builder().psychologyData(data1).build();
        TradeDetails t2 = TradeDetails.builder().psychologyData(data2).build();

        when(tradeDetailsService.findByPortfolioIdIn(anyList())).thenReturn(Arrays.asList(t1, t2));

        // Act
        Map<TradeBehaviorPattern, Integer> result = tradeAnalysisService.analyzeBehaviorPatterns(Arrays.asList("p1"));

        // Assert
        assertEquals(2, result.get(TradeBehaviorPattern.CHASING_MOMENTUM));
        assertEquals(1, result.get(TradeBehaviorPattern.REVENGE_TRADING));
    }
}
