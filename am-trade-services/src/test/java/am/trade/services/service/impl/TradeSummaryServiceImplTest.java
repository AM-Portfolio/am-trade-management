package am.trade.services.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;
import am.trade.persistence.repository.TradeSummaryBasicRepository;
import am.trade.persistence.repository.TradeSummaryDetailedRepository;

@ExtendWith(MockitoExtension.class)
public class TradeSummaryServiceImplTest {

    @Mock
    private TradeSummaryBasicRepository basicRepository;

    @Mock
    private TradeSummaryDetailedRepository detailedRepository;

    @InjectMocks
    private TradeSummaryServiceImpl tradeSummaryService;

    private TradeSummaryBasic testBasic;
    private TradeSummaryDetailed testDetailed;

    @BeforeEach
    void setUp() {
        testBasic = new TradeSummaryBasic();
        testBasic.setId("basic-123");
        testBasic.setOwnerId("user-1");

        testDetailed = new TradeSummaryDetailed();
        testDetailed.setId("detailed-123");
        testDetailed.setTradeSummaryBasicId("basic-123");
    }

    @Test
    void saveTradeSummary_ShouldLinkAndSaveBoth() {
        // Arrange
        when(basicRepository.save(any(TradeSummaryBasic.class))).thenReturn(testBasic);
        when(detailedRepository.save(any(TradeSummaryDetailed.class))).thenReturn(testDetailed);

        // Act
        TradeSummary result = tradeSummaryService.saveTradeSummary(testBasic, testDetailed);

        // Assert
        assertNotNull(result);
        verify(basicRepository, times(2)).save(any(TradeSummaryBasic.class)); // Saved twice: once initially, once after getting detailed ID
        verify(detailedRepository, times(1)).save(any(TradeSummaryDetailed.class));
        assertEquals("basic-123", testDetailed.getTradeSummaryBasicId());
        assertEquals("detailed-123", testBasic.getDetailedMetricsId());
    }

    @Test
    void findTradeSummaryById_WhenExists_ShouldReturnComposite() {
        // Arrange
        String id = "basic-123";
        testBasic.setDetailedMetricsId("detailed-123");
        when(basicRepository.findById(id)).thenReturn(Optional.of(testBasic));
        when(detailedRepository.findById("detailed-123")).thenReturn(Optional.of(testDetailed));

        // Act
        Optional<TradeSummary> result = tradeSummaryService.findTradeSummaryById(id);

        // Assert
        assertTrue(result.isPresent());
        verify(basicRepository, times(1)).findById(id);
        verify(detailedRepository, times(1)).findById("detailed-123");
    }

    @Test
    void deleteTradeSummary_ShouldDeleteBoth() {
        // Arrange
        String id = "basic-123";
        testBasic.setDetailedMetricsId("detailed-123");
        when(basicRepository.findById(id)).thenReturn(Optional.of(testBasic));

        // Act
        tradeSummaryService.deleteTradeSummary(id);

        // Assert
        verify(detailedRepository, times(1)).deleteById("detailed-123");
        verify(basicRepository, times(1)).deleteById(id);
    }

    @Test
    void updateTradeSummaryBasic_WhenDoesNotExist_ShouldThrowException() {
        // Arrange
        when(basicRepository.existsById(anyString())).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            tradeSummaryService.updateTradeSummaryBasic(testBasic);
        });
    }
}
