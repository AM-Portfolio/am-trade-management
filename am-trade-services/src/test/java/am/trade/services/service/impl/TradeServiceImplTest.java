package am.trade.services.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import am.trade.models.document.Trade;
import am.trade.models.dto.TradeDTO;
import am.trade.models.mapper.TradeMapper;
import am.trade.persistence.repository.TradeRepository;

@ExtendWith(MockitoExtension.class)
public class TradeServiceImplTest {

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TradeMapper tradeMapper;

    @InjectMocks
    private TradeServiceImpl tradeService;

    private TradeDTO testTradeDTO;
    private Trade testTrade;

    @BeforeEach
    void setUp() {
        testTradeDTO = TradeDTO.builder()
                .id("1")
                .symbol("AAPL")
                .quantity(100)
                .price(new BigDecimal("150.00"))
                .build();

        testTrade = new Trade();
        testTrade.setId("1");
        testTrade.setSymbol("AAPL");
        testTrade.setQuantity(100);
        testTrade.setPrice(new BigDecimal("150.00"));
    }

    @Test
    void createTrade_ShouldReturnSavedTradeDTO() {
        // Arrange
        when(tradeMapper.toEntity(testTradeDTO)).thenReturn(testTrade);
        when(tradeRepository.save(testTrade)).thenReturn(testTrade);
        when(tradeMapper.toDto(testTrade)).thenReturn(testTradeDTO);

        // Act
        TradeDTO result = tradeService.createTrade(testTradeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testTradeDTO.getSymbol(), result.getSymbol());
        verify(tradeRepository, times(1)).save(testTrade);
        verify(tradeMapper, times(1)).toEntity(testTradeDTO);
        verify(tradeMapper, times(1)).toDto(testTrade);
    }

    @Test
    void updateTrade_WhenTradeExists_ShouldReturnUpdatedTradeDTO() {
        // Arrange
        String id = "1";
        when(tradeRepository.findById(id)).thenReturn(Optional.of(testTrade));
        when(tradeMapper.toEntity(testTradeDTO)).thenReturn(testTrade);
        when(tradeRepository.save(testTrade)).thenReturn(testTrade);
        when(tradeMapper.toDto(testTrade)).thenReturn(testTradeDTO);

        // Act
        TradeDTO result = tradeService.updateTrade(id, testTradeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(tradeRepository, times(1)).findById(id);
        verify(tradeRepository, times(1)).save(testTrade);
    }

    @Test
    void updateTrade_WhenTradeDoesNotExist_ShouldThrowException() {
        // Arrange
        String id = "non-existent";
        when(tradeRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            tradeService.updateTrade(id, testTradeDTO);
        });

        assertEquals("Trade not found with id: " + id, exception.getMessage());
        verify(tradeRepository, times(1)).findById(id);
        verify(tradeRepository, times(0)).save(any());
    }

    @Test
    void findById_WhenTradeExists_ShouldReturnOptionalWithDTO() {
        // Arrange
        String id = "1";
        when(tradeRepository.findById(id)).thenReturn(Optional.of(testTrade));
        when(tradeMapper.toDto(testTrade)).thenReturn(testTradeDTO);

        // Act
        Optional<TradeDTO> result = tradeService.findById(id);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTradeDTO.getSymbol(), result.get().getSymbol());
        verify(tradeRepository, times(1)).findById(id);
    }

    @Test
    void deleteTrade_ShouldCallRepositoryDelete() {
        // Arrange
        String id = "1";

        // Act
        tradeService.deleteTrade(id);

        // Assert
        verify(tradeRepository, times(1)).deleteById(id);
    }
}
