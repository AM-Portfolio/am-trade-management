// package am.trade.persistence.mapper;

// import static org.junit.jupiter.api.Assertions.assertEquals;
// import static org.junit.jupiter.api.Assertions.assertNotNull;
// import static org.junit.jupiter.api.Assertions.assertNull;

// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.util.Arrays;
// import java.util.List;

// import org.junit.jupiter.api.BeforeEach;
// import org.junit.jupiter.api.Test;

// import am.trade.common.models.PortfolioModel;
// import am.trade.common.models.TradeDetails;
// import am.trade.common.models.enums.TradePositionType;
// import am.trade.common.models.enums.TradeStatus;
// import am.trade.persistence.entity.PortfolioEntity;
// import am.trade.persistence.entity.TradeDetailsEntity;

// public class PortfolioMapperTest {

//     private PortfolioMapper mapper;

//     @BeforeEach
//     public void setup() {
//         mapper = new PortfolioMapper();
//     }

//     @Test
//     public void testToEntityWithNullModel() {
//         assertNull(mapper.toEntity(null));
//     }

//     @Test
//     public void testToModelWithNullEntity() {
//         assertNull(mapper.toModel(null));
//     }

//     @Test
//     public void testToEntityWithFullModel() {
//         // Create test model
//         PortfolioModel model = createTestPortfolioModel();
        
//         // Convert to entity
//         PortfolioEntity entity = mapper.toEntity(model);
        
//         // Verify conversion
//         assertNotNull(entity);
//         assertEquals(model.getPortfolioId(), entity.getPortfolioId());
//         assertEquals(model.getName(), entity.getName());
//         assertEquals(model.getDescription(), entity.getDescription());
//         assertEquals(model.getOwnerId(), entity.getOwnerId());
//         assertEquals(model.isActive(), entity.isActive());
//         assertEquals(model.getCurrency(), entity.getCurrency());
//         assertEquals(model.getInitialCapital(), entity.getInitialCapital());
//         assertEquals(model.getCurrentCapital(), entity.getCurrentCapital());
//         assertEquals(model.getCreatedDate(), entity.getCreatedDate());
//         assertEquals(model.getLastUpdatedDate(), entity.getLastUpdatedDate());
        
//         // Verify metrics
//         assertNotNull(entity.getMetrics());
//         assertEquals(model.getMetrics().getTotalTrades(), entity.getMetrics().getTotalTrades());
//         assertEquals(model.getMetrics().getWinningTrades(), entity.getMetrics().getWinningTrades());
//         assertEquals(model.getMetrics().getLosingTrades(), entity.getMetrics().getLosingTrades());
//         assertEquals(model.getMetrics().getWinRate(), entity.getMetrics().getWinRate());
//         assertEquals(model.getMetrics().getAverageWin(), entity.getMetrics().getAverageWin());
        
//         // Verify trades
//         assertNotNull(entity.getTrades());
//         assertEquals(model.getTrades().size(), entity.getTrades().size());
//         assertEquals(model.getTrades().get(0).getTradeId(), entity.getTrades().get(0).getTradeId());
//         assertEquals(model.getTrades().get(0).getSymbol(), entity.getTrades().get(0).getSymbol());
        
//         // Verify asset allocations
//         assertNotNull(entity.getAssetAllocations());
//         assertEquals(model.getAssetAllocations().size(), entity.getAssetAllocations().size());
//         assertEquals(model.getAssetAllocations().get(0).getAssetClass(), 
//                 entity.getAssetAllocations().get(0).getAssetClass());
//     }

//     @Test
//     public void testToModelWithFullEntity() {
//         // Create test entity
//         PortfolioEntity entity = createTestPortfolioEntity();
        
//         // Convert to model
//         PortfolioModel model = mapper.toModel(entity);
        
//         // Verify conversion
//         assertNotNull(model);
//         assertEquals(entity.getPortfolioId(), model.getPortfolioId());
//         assertEquals(entity.getName(), model.getName());
//         assertEquals(entity.getDescription(), model.getDescription());
//         assertEquals(entity.getOwnerId(), model.getOwnerId());
//         assertEquals(entity.isActive(), model.isActive());
//         assertEquals(entity.getCurrency(), model.getCurrency());
//         assertEquals(entity.getInitialCapital(), model.getInitialCapital());
//         assertEquals(entity.getCurrentCapital(), model.getCurrentCapital());
//         assertEquals(entity.getCreatedDate(), model.getCreatedDate());
//         assertEquals(entity.getLastUpdatedDate(), model.getLastUpdatedDate());
        
//         // Verify metrics
//         assertNotNull(model.getMetrics());
//         assertEquals(entity.getMetrics().getTotalTrades(), model.getMetrics().getTotalTrades());
//         assertEquals(entity.getMetrics().getWinningTrades(), model.getMetrics().getWinningTrades());
//         assertEquals(entity.getMetrics().getLosingTrades(), model.getMetrics().getLosingTrades());
//         assertEquals(entity.getMetrics().getWinRate(), model.getMetrics().getWinRate());
//         assertEquals(entity.getMetrics().getAverageWin(), model.getMetrics().getAverageWin());
        
//         // Verify trades
//         assertNotNull(model.getTrades());
//         assertEquals(entity.getTrades().size(), model.getTrades().size());
//         assertEquals(entity.getTrades().get(0).getTradeId(), model.getTrades().get(0).getTradeId());
//         assertEquals(entity.getTrades().get(0).getSymbol(), model.getTrades().get(0).getSymbol());
        
//         // Verify asset allocations
//         assertNotNull(model.getAssetAllocations());
//         assertEquals(entity.getAssetAllocations().size(), model.getAssetAllocations().size());
//         assertEquals(entity.getAssetAllocations().get(0).getAssetClass(), 
//                 model.getAssetAllocations().get(0).getAssetClass());
//     }

//     @Test
//     public void testToTradeEntityWithNullModel() {
//         assertNull(mapper.toTradeEntity(null));
//     }

//     @Test
//     public void testToTradeDetailsWithNullEntity() {
//         assertNull(mapper.toTradeDetails(null));
//     }

//     private PortfolioModel createTestPortfolioModel() {
//         // Create metrics
//         PortfolioModel.PortfolioMetrics metrics = PortfolioModel.PortfolioMetrics.builder()
//                 .totalTrades(10)
//                 .winningTrades(7)
//                 .losingTrades(3)
//                 .breakEvenTrades(0)
//                 .openPositions(2)
//                 .totalProfitLoss(new BigDecimal("1500.00"))
//                 .totalProfitLossPercentage(new BigDecimal("15.00"))
//                 .winRate(new BigDecimal("70.00"))
//                 .lossRate(new BigDecimal("30.00"))
//                 .averageWin(new BigDecimal("300.00"))
//                 .averageLoss(new BigDecimal("150.00"))
//                 .largestWin(new BigDecimal("500.00"))
//                 .largestLoss(new BigDecimal("200.00"))
//                 .maxDrawdown(new BigDecimal("300.00"))
//                 .maxDrawdownPercentage(new BigDecimal("3.00"))
//                 .sharpeRatio(new BigDecimal("1.5"))
//                 .sortinoRatio(new BigDecimal("2.0"))
//                 .calmarRatio(new BigDecimal("0.8"))
//                 .build();
        
//         // Create trade details
//         TradeDetails trade = TradeDetails.builder()
//                 .tradeId("trade-123")
//                 .portfolioId("portfolio-456")
//                 .symbol("AAPL")
//                 .tradePositionType(TradePositionType.LONG)
//                 .status(TradeStatus.CLOSED)
//                 .entryInfo(TradeDetails.EntryExitInfo.builder()
//                         .timestamp(LocalDateTime.now().minusDays(5))
//                         .price(new BigDecimal("150.00"))
//                         .quantity(10)
//                         .fees(new BigDecimal("9.99"))
//                         .totalValue(new BigDecimal("1509.99"))
//                         .build())
//                 .exitInfo(TradeDetails.EntryExitInfo.builder()
//                         .timestamp(LocalDateTime.now())
//                         .price(new BigDecimal("165.00"))
//                         .quantity(10)
//                         .fees(new BigDecimal("9.99"))
//                         .totalValue(new BigDecimal("1650.00"))
//                         .build())
//                 .metrics(TradeDetails.TradeMetrics.builder()
//                         .profitLoss(new BigDecimal("140.01"))
//                         .profitLossPercentage(new BigDecimal("9.27"))
//                         .returnOnEquity(new BigDecimal("14.00"))
//                         .riskAmount(new BigDecimal("100.00"))
//                         .rewardAmount(new BigDecimal("140.01"))
//                         .riskRewardRatio(new BigDecimal("1.40"))
//                         .holdingTimeDays(5)
//                         .holdingTimeHours(120)
//                         .holdingTimeMinutes(7200)
//                         .build())
//                 .build();
        
//         // Create asset allocations
//         PortfolioModel.AssetAllocation allocation = PortfolioModel.AssetAllocation.builder()
//                 .assetClass("Equity")
//                 .sector("Technology")
//                 .industry("Software")
//                 .allocation(new BigDecimal("25.00"))
//                 .currentValue(new BigDecimal("2500.00"))
//                 .profitLoss(new BigDecimal("500.00"))
//                 .profitLossPercentage(new BigDecimal("20.00"))
//                 .build();
        
//         // Create portfolio model
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
//                 .metrics(metrics)
//                 .trades(Arrays.asList(trade))
//                 .assetAllocations(Arrays.asList(allocation))
//                 .build();
//     }
    
//     private PortfolioEntity createTestPortfolioEntity() {
//         // Create metrics
//         PortfolioEntity.PortfolioMetrics metrics = PortfolioEntity.PortfolioMetrics.builder()
//                 .totalTrades(10)
//                 .winningTrades(7)
//                 .losingTrades(3)
//                 .breakEvenTrades(0)
//                 .openPositions(2)
//                 .totalProfitLoss(new BigDecimal("1500.00"))
//                 .totalProfitLossPercentage(new BigDecimal("15.00"))
//                 .winRate(new BigDecimal("70.00"))
//                 .lossRate(new BigDecimal("30.00"))
//                 .averageWin(new BigDecimal("300.00"))
//                 .averageLoss(new BigDecimal("150.00"))
//                 .largestWin(new BigDecimal("500.00"))
//                 .largestLoss(new BigDecimal("200.00"))
//                 .maxDrawdown(new BigDecimal("300.00"))
//                 .maxDrawdownPercentage(new BigDecimal("3.00"))
//                 .sharpeRatio(new BigDecimal("1.5"))
//                 .sortinoRatio(new BigDecimal("2.0"))
//                 .calmarRatio(new BigDecimal("0.8"))
//                 .build();
        
//         // Create trade details
//         TradeDetailsEntity trade = TradeDetailsEntity.builder()
//                 .tradeId("trade-123")
//                 .portfolioId("portfolio-456")
//                 .symbol("AAPL")
//                 .tradePositionType(TradePositionType.LONG)
//                 .status(TradeStatus.CLOSED)
//                 .entryInfo(TradeDetailsEntity.EntryExitInfo.builder()
//                         .timestamp(LocalDateTime.now().minusDays(5))
//                         .price(new BigDecimal("150.00"))
//                         .quantity(10)
//                         .fees(new BigDecimal("9.99"))
//                         .totalValue(new BigDecimal("1509.99"))
//                         .build())
//                 .exitInfo(TradeDetailsEntity.EntryExitInfo.builder()
//                         .timestamp(LocalDateTime.now())
//                         .price(new BigDecimal("165.00"))
//                         .quantity(10)
//                         .fees(new BigDecimal("9.99"))
//                         .totalValue(new BigDecimal("1650.00"))
//                         .build())
//                 .metrics(TradeDetailsEntity.TradeMetrics.builder()
//                         .profitLoss(new BigDecimal("140.01"))
//                         .profitLossPercentage(new BigDecimal("9.27"))
//                         .returnOnEquity(new BigDecimal("14.00"))
//                         .riskAmount(new BigDecimal("100.00"))
//                         .rewardAmount(new BigDecimal("140.01"))
//                         .riskRewardRatio(new BigDecimal("1.40"))
//                         .holdingTimeDays(5)
//                         .holdingTimeHours(120)
//                         .holdingTimeMinutes(7200)
//                         .build())
//                 .build();
        
//         // Create asset allocations
//         PortfolioEntity.AssetAllocation allocation = PortfolioEntity.AssetAllocation.builder()
//                 .assetClass("Equity")
//                 .sector("Technology")
//                 .industry("Software")
//                 .allocation(new BigDecimal("25.00"))
//                 .currentValue(new BigDecimal("2500.00"))
//                 .profitLoss(new BigDecimal("500.00"))
//                 .profitLossPercentage(new BigDecimal("20.00"))
//                 .build();
        
//         // Create portfolio entity
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
//                 .metrics(metrics)
//                 .trades(Arrays.asList(trade))
//                 .assetAllocations(Arrays.asList(allocation))
//                 .build();
//     }
// }
