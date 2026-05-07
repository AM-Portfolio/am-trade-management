package am.trade.services.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeDetails;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;

@ExtendWith(MockitoExtension.class)
public class TradeDetailsServiceImplTest {

    @Mock
    private TradeDetailsRepository tradeDetailsRepository;

    @Mock
    private TradeDetailsMapper tradeDetailsMapper;

    @InjectMocks
    private TradeDetailsServiceImpl tradeDetailsService;

    private TradeDetailsEntity testEntity;
    private TradeDetails testModel;

    @BeforeEach
    void setUp() {
        testEntity = new TradeDetailsEntity();
        testEntity.setTradeId("1");
        testEntity.setSymbol("AAPL");

        testModel = TradeDetails.builder()
                .tradeId("1")
                .symbol("AAPL")
                .build();
    }

    @Test
    void findModelById_WhenExists_ShouldReturnModel() {
        // Arrange
        when(tradeDetailsRepository.findById("1")).thenReturn(Optional.of(testEntity));
        when(tradeDetailsMapper.toTradeDetails(testEntity)).thenReturn(testModel);

        // Act
        Optional<TradeDetails> result = tradeDetailsService.findModelById("1");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("AAPL", result.get().getSymbol());
    }

    @Test
    void findModelsByPortfolioId_WithPagination_ShouldReturnPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<TradeDetailsEntity> entityPage = new PageImpl<>(Arrays.asList(testEntity));
        
        when(tradeDetailsRepository.findByPortfolioId("p1", pageable)).thenReturn(entityPage);
        when(tradeDetailsMapper.toTradeDetails(testEntity)).thenReturn(testModel);

        // Act
        Page<TradeDetails> result = tradeDetailsService.findModelsByPortfolioId("p1", pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("AAPL", result.getContent().get(0).getSymbol());
    }

    @Test
    void findByUserIdAndEntryInfoTimestampBetween_ShouldFilterInMemory() {
        // Arrange
        LocalDateTime start = LocalDateTime.now().minusDays(10);
        LocalDateTime end = LocalDateTime.now();
        
        TradeDetailsEntity inRange = new TradeDetailsEntity();
        inRange.setEntryInfo(EntryExitInfo.builder().timestamp(start.plusDays(5)).build());
        
        TradeDetailsEntity outOfRange = new TradeDetailsEntity();
        outOfRange.setEntryInfo(EntryExitInfo.builder().timestamp(start.minusDays(5)).build());

        when(tradeDetailsRepository.findByUserId("u1")).thenReturn(Arrays.asList(inRange, outOfRange));
        when(tradeDetailsMapper.toTradeDetails(inRange)).thenReturn(testModel);

        // Act
        List<TradeDetails> result = tradeDetailsService.findByUserIdAndEntryInfoTimestampBetween("u1", start, end);

        // Assert
        assertEquals(1, result.size());
        verify(tradeDetailsMapper, times(1)).toTradeDetails(inRange);
        verify(tradeDetailsMapper, times(0)).toTradeDetails(outOfRange);
    }
}
