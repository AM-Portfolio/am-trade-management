// package am.trade.persistence.service;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertTrue;
// import static org.mockito.ArgumentMatchers.any;
// import static org.mockito.ArgumentMatchers.anyString;
// import static org.mockito.Mockito.times;
// import static org.mockito.Mockito.verify;
// import static org.mockito.Mockito.when;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;
// import java.util.Optional;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.Mock;
// import org.mockito.junit.jupiter.MockitoExtension;
// import org.springframework.data.domain.Page;
// import org.springframework.data.domain.PageImpl;
// import org.springframework.data.domain.Pageable;

// import am.trade.common.models.PortfolioModel;
// import am.trade.common.models.TradeDetails;
// import am.trade.common.models.enums.TradePositionType;
// import am.trade.common.models.enums.TradeStatus;
// import am.trade.persistence.entity.PortfolioEntity;
// import am.trade.persistence.mapper.PortfolioMapper;
// import am.trade.persistence.repository.PortfolioRepository;

// @ExtendWith(MockitoExtension.class)
// public class PortfolioPersistenceServiceTest {

//     @Mock
//     private PortfolioRepository portfolioRepository;
    
//     @Mock
//     private PortfolioMapper portfolioMapper;
    
//     private PortfolioPersistenceService service;
    
//     private PortfolioModel testModel;
//     private PortfolioEntity testEntity;
    
//     @BeforeEach
//     public void setup() {
//         service = new PortfolioPersistenceService(portfolioRepository, portfolioMapper);
        
//         testModel = createTestPortfolioModel();
//         testEntity = createTestPortfolioEntity();
        
//         // Setup default mapper behavior
//         when(portfolioMapper.toEntity(any(PortfolioModel.class))).thenReturn(testEntity);
//         when(portfolioMapper.toModel(any(PortfolioEntity.class))).thenReturn(testModel);
//     }
    
//     @Test
//     public void testSavePortfolio() {
//         // Setup
//         when(portfolioRepository.save(any(PortfolioEntity.class))).thenReturn(testEntity);
        
//         // Execute
//         PortfolioModel result = service.savePortfolio(testModel);
        
//         // Verify
//         assertNotNull(result);
//         assertEquals(testModel.getPortfolioId(), result.getPortfolioId());
//         verify(portfolioMapper, times(1)).toEntity(testModel);
//         verify(portfolioRepository, times(1)).save(any(PortfolioEntity.class));
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     @Test
//     public void testFindPortfolioById() {
//         // Setup
//         when(portfolioRepository.findById(anyString())).thenReturn(Optional.of(testEntity));
        
//         // Execute
//         Optional<PortfolioModel> result = service.findPortfolioById("portfolio-456");
        
//         // Verify
//         assertTrue(result.isPresent());
//         assertEquals(testModel.getPortfolioId(), result.get().getPortfolioId());
//         verify(portfolioRepository, times(1)).findById("portfolio-456");
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     @Test
//     public void testFindAllPortfolios() {
//         // Setup
//         Page<PortfolioEntity> page = new PageImpl<>(Arrays.asList(testEntity));
//         when(portfolioRepository.findAll(any(Pageable.class))).thenReturn(page);
        
//         // Execute
//         Page<PortfolioModel> result = service.findAllPortfolios(Pageable.unpaged());
        
//         // Verify
//         assertNotNull(result);
//         assertEquals(1, result.getTotalElements());
//         assertEquals(testModel.getPortfolioId(), result.getContent().get(0).getPortfolioId());
//         verify(portfolioRepository, times(1)).findAll(any(Pageable.class));
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     @Test
//     public void testFindPortfoliosByOwnerId() {
//         // Setup
//         when(portfolioRepository.findByOwnerId(anyString(), any(Pageable.class)))
//             .thenReturn(new PageImpl<>(Arrays.asList(testEntity)));
        
//         // Execute
//         Page<PortfolioModel> result = service.findPortfoliosByOwnerId("user-789", Pageable.unpaged());
        
//         // Verify
//         assertNotNull(result);
//         assertEquals(1, result.getTotalElements());
//         assertEquals(testModel.getPortfolioId(), result.getContent().get(0).getPortfolioId());
//         verify(portfolioRepository, times(1)).findByOwnerId("user-789", Pageable.unpaged());
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     @Test
//     public void testFindActivePortfolios() {
//         // Setup
//         when(portfolioRepository.findByActiveTrue(any(Pageable.class)))
//             .thenReturn(new PageImpl<>(Arrays.asList(testEntity)));
        
//         // Execute
//         Page<PortfolioModel> result = service.findActivePortfolios(Pageable.unpaged());
        
//         // Verify
//         assertNotNull(result);
//         assertEquals(1, result.getTotalElements());
//         assertEquals(testModel.getPortfolioId(), result.getContent().get(0).getPortfolioId());
//         verify(portfolioRepository, times(1)).findByActiveTrue(Pageable.unpaged());
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     @Test
//     public void testFindPortfoliosWithTradesBySymbol() {
//         // Setup
//         when(portfolioRepository.findByTradesSymbol(anyString(), any(Pageable.class)))
//             .thenReturn(new PageImpl<>(Arrays.asList(testEntity)));
        
//         // Execute
//         Page<PortfolioModel> result = service.findPortfoliosWithTradesBySymbol("AAPL", Pageable.unpaged());
        
//         // Verify
//         assertNotNull(result);
//         assertEquals(1, result.getTotalElements());
//         assertEquals(testModel.getPortfolioId(), result.getContent().get(0).getPortfolioId());
//         verify(portfolioRepository, times(1)).findByTradesSymbol("AAPL", Pageable.unpaged());
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     @Test
//     public void testDeletePortfolio() {
//         // Execute
//         service.deletePortfolio("portfolio-456");
        
//         // Verify
//         verify(portfolioRepository, times(1)).deleteById("portfolio-456");
//     }
    
//     @Test
//     public void testUpdatePortfolioTrades() {
//         // Setup
//         TradeDetails tradeDetails = createTestTradeDetails();
//         when(portfolioRepository.findById(anyString())).thenReturn(Optional.of(testEntity));
//         when(portfolioRepository.save(any(PortfolioEntity.class))).thenReturn(testEntity);
        
//         // Execute
//         Optional<PortfolioModel> result = service.updatePortfolioTrades("portfolio-456", Arrays.asList(tradeDetails));
        
//         // Verify
//         assertTrue(result.isPresent());
//         verify(portfolioRepository, times(1)).findById("portfolio-456");
//         verify(portfolioRepository, times(1)).save(any(PortfolioEntity.class));
//         verify(portfolioMapper, times(1)).toModel(testEntity);
//     }
    
//     private PortfolioModel createTestPortfolioModel() {
//         return PortfolioModel.builder()
//                 .portfolioId("portfolio-456")
//                 .name("Tech Growth Portfolio")
//                 .description("Portfolio focused on growth tech stocks")
//                 .ownerId("user-789")
//                 .active(true)
//                 .currency("USD")
//                 .initialCapital(new BigDecimal("10000.00"))
//                 .currentCapital(new BigDecimal("11500.00"))
//                 .createdDate(LocalDateTime.now().minusDays(30))
//                 .lastUpdatedDate(LocalDateTime.now())
//                 .metrics(PortfolioModel.PortfolioMetrics.builder()
//                         .totalTrades(10)
//                         .winningTrades(7)
//                         .losingTrades(3)
//                         .build())
//                 .trades(Arrays.asList(createTestTradeDetails()))
//                 .build();
//     }
    
//     private PortfolioEntity createTestPortfolioEntity() {
//         return PortfolioEntity.builder()
//                 .portfolioId("portfolio-456")
//                 .name("Tech Growth Portfolio")
//                 .description("Portfolio focused on growth tech stocks")
//                 .ownerId("user-789")
//                 .active(true)
//                 .currency("USD")
//                 .initialCapital(new BigDecimal("10000.00"))
//                 .currentCapital(new BigDecimal("11500.00"))
//                 .createdDate(LocalDateTime.now().minusDays(30))
//                 .lastUpdatedDate(LocalDateTime.now())
//                 .metrics(PortfolioEntity.PortfolioMetrics.builder()
//                         .totalTrades(10)
//                         .winningTrades(7)
//                         .losingTrades(3)
//                         .build())
//                 .build();
//     }
    
//     private TradeDetails createTestTradeDetails() {
//         return TradeDetails.builder()
//                 .tradeId("trade-123")
//                 .portfolioId("portfolio-456")
//                 .symbol("AAPL")
//                 .tradePositionType(TradePositionType.LONG)
//                 .status(TradeStatus.CLOSED)
//                 .entryInfo(TradeDetails.EntryExitInfo.builder()
//                         .timestamp(LocalDateTime.now().minusDays(5))
//                         .price(new BigDecimal("150.00"))
//                         .quantity(10)
//                         .build())
//                 .exitInfo(TradeDetails.EntryExitInfo.builder()
//                         .timestamp(LocalDateTime.now())
//                         .price(new BigDecimal("165.00"))
//                         .quantity(10)
//                         .build())
//                 .metrics(TradeDetails.TradeMetrics.builder()
//                         .profitLoss(new BigDecimal("140.01"))
//                         .profitLossPercentage(new BigDecimal("9.27"))
//                         .build())
//                 .build();
//     }
// }
