package am.trade.services.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;

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

import am.trade.common.models.TradeDetails;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;

@ExtendWith(MockitoExtension.class)
public class PortfolioTradeServiceTest {

    @Mock
    private TradeDetailsRepository tradeDetailsRepository;

    @Mock
    private TradeDetailsMapper tradeDetailsMapper;

    @InjectMocks
    private PortfolioTradeService portfolioTradeService;

    private TradeDetailsEntity testEntity;
    private TradeDetails testModel;

    @BeforeEach
    void setUp() {
        testEntity = new TradeDetailsEntity();
        testEntity.setTradeId("1");
        testEntity.setPortfolioId("p1");

        testModel = TradeDetails.builder()
                .tradeId("1")
                .portfolioId("p1")
                .build();
    }

    @Test
    void getTradeDetailsForPortfolios_ShouldReturnMappedList() {
        // Arrange
        List<String> ids = Arrays.asList("p1", "p2");
        when(tradeDetailsRepository.findByPortfolioIdIn(ids)).thenReturn(Arrays.asList(testEntity));
        when(tradeDetailsMapper.toTradeDetails(testEntity)).thenReturn(testModel);

        // Act
        List<TradeDetails> result = portfolioTradeService.getTradeDetailsForPortfolios(ids);

        // Assert
        assertEquals(1, result.size());
        assertEquals("p1", result.get(0).getPortfolioId());
        verify(tradeDetailsRepository).findByPortfolioIdIn(ids);
    }

    @Test
    void getTradeDetailsForPortfoliosPaginated_ShouldReturnPage() {
        // Arrange
        List<String> ids = Arrays.asList("p1");
        Pageable pageable = PageRequest.of(0, 10);
        Page<TradeDetailsEntity> entityPage = new PageImpl<>(Arrays.asList(testEntity));

        when(tradeDetailsRepository.findByPortfolioIdIn(ids, pageable)).thenReturn(entityPage);
        when(tradeDetailsMapper.toTradeDetails(testEntity)).thenReturn(testModel);

        // Act
        Page<TradeDetails> result = portfolioTradeService.getTradeDetailsForPortfoliosPaginated(ids, pageable);

        // Assert
        assertEquals(1, result.getTotalElements());
        assertEquals("p1", result.getContent().get(0).getPortfolioId());
    }
}
