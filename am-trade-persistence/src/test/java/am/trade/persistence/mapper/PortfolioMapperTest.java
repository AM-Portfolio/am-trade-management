package am.trade.persistence.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import am.trade.common.models.enums.AssetClass;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import am.trade.common.models.AssetAllocation;
import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.persistence.entity.PortfolioEntity;

@ExtendWith(MockitoExtension.class)
public class PortfolioMapperTest {

    @Mock
    private TradeDetailsMapper tradeDetailsMapper;

    private PortfolioMapper mapper;

    @BeforeEach
    public void setup() {
        mapper = new PortfolioMapper(tradeDetailsMapper);
    }

    @Test
    public void testToEntityWithNullModel() {
        assertNull(mapper.toEntity(null));
    }

    @Test
    public void testToModelWithNullEntity() {
        assertNull(mapper.toModel(null));
    }

    @Test
    public void testToEntityWithFullModel() {
        // Create test model
        PortfolioModel model = createTestPortfolioModel();

        // Convert to entity
        PortfolioEntity entity = mapper.toEntity(model);

        // Verify conversion
        assertNotNull(entity);
        assertEquals(model.getPortfolioId(), entity.getPortfolioId());
        assertEquals(model.getName(), entity.getName());
        assertEquals(model.getOwnerId(), entity.getOwnerId());
        assertEquals(model.isActive(), entity.isActive());
        assertEquals(model.getCurrency(), entity.getCurrency());
        assertEquals(model.getInitialCapital(), entity.getInitialCapital());
        assertEquals(model.getCurrentCapital(), entity.getCurrentCapital());

        // Verify metrics
        assertNotNull(entity.getMetrics());
        assertEquals(model.getMetrics().getTotalTrades(), entity.getMetrics().getTotalTrades());
        assertEquals(model.getMetrics().getWinningTrades(), entity.getMetrics().getWinningTrades());
        assertEquals(model.getMetrics().getLosingTrades(), entity.getMetrics().getLosingTrades());
        assertEquals(model.getMetrics().getWinRate(), entity.getMetrics().getWinRate());

        // Verify trades
        assertNotNull(entity.getTrades());
        assertEquals(model.getTradeIds().size(), entity.getTrades().size());
        assertEquals(model.getTradeIds().get(0), entity.getTrades().get(0));

        // Verify asset allocations
        assertNotNull(entity.getAssetAllocations());
        assertEquals(model.getAssetAllocations().size(), entity.getAssetAllocations().size());
        assertEquals(model.getAssetAllocations().get(0).getAssetClass(),
                entity.getAssetAllocations().get(0).getAssetClass());
    }

    @Test
    public void testToModelWithFullEntity() {
        // Create test entity
        PortfolioEntity entity = createTestPortfolioEntity();

        // Convert to model
        PortfolioModel model = mapper.toModel(entity);

        // Verify conversion
        assertNotNull(model);
        assertEquals(entity.getPortfolioId(), model.getPortfolioId());
        assertEquals(entity.getName(), model.getName());
        assertEquals(entity.getOwnerId(), model.getOwnerId());
        assertEquals(entity.isActive(), model.isActive());
        assertEquals(entity.getCurrency(), model.getCurrency());
        assertEquals(entity.getInitialCapital(), model.getInitialCapital());
        assertEquals(entity.getCurrentCapital(), model.getCurrentCapital());

        // Verify metrics
        assertNotNull(model.getMetrics());
        assertEquals(entity.getMetrics().getTotalTrades(), model.getMetrics().getTotalTrades());
        assertEquals(entity.getMetrics().getWinningTrades(), model.getMetrics().getWinningTrades());
        assertEquals(entity.getMetrics().getLosingTrades(), model.getMetrics().getLosingTrades());
        assertEquals(entity.getMetrics().getWinRate(), model.getMetrics().getWinRate());

        // Verify trades
        assertNotNull(model.getTradeIds());
        assertEquals(entity.getTrades().size(), model.getTradeIds().size());
        assertEquals(entity.getTrades().get(0), model.getTradeIds().get(0));

        // Verify asset allocations
        assertNotNull(model.getAssetAllocations());
        assertEquals(entity.getAssetAllocations().size(), model.getAssetAllocations().size());
        assertEquals(entity.getAssetAllocations().get(0).getAssetClass(),
                model.getAssetAllocations().get(0).getAssetClass());
    }

    private PortfolioModel createTestPortfolioModel() {
        // Create metrics
        PortfolioMetrics metrics = PortfolioMetrics.builder()
                .totalTrades(10)
                .winningTrades(7)
                .losingTrades(3)
                .breakEvenTrades(0)
                .openPositions(2)
                .netProfitLoss(new BigDecimal("1500.00"))
                .netProfitLossPercentage(new BigDecimal("15.00"))
                .winRate(new BigDecimal("70.00"))
                .lossRate(new BigDecimal("30.00"))
                .maxDrawdown(new BigDecimal("300.00"))
                .maxDrawdownPercentage(new BigDecimal("3.00"))
                .sharpeRatio(new BigDecimal("1.5"))
                .sortinoRatio(new BigDecimal("2.0"))
                .build();

        // Create asset allocations
        AssetAllocation allocation = AssetAllocation.builder()
                .assetClass(AssetClass.STOCK)
                .currentPercentage(new BigDecimal("25.00"))
                .targetPercentage(new BigDecimal("30.00"))
                .variance(new BigDecimal("-5.00"))
                .build();

        // Create portfolio model
        return PortfolioModel.builder()
                .portfolioId("portfolio-456")
                .name("Tech Growth Portfolio")
                .description("Portfolio focused on growth tech stocks")
                .ownerId("user-789")
                .active(true)
                .currency("USD")
                .initialCapital(new BigDecimal("10000.00"))
                .currentCapital(new BigDecimal("11500.00"))
                .createdDate(LocalDateTime.now().minusDays(30))
                .lastUpdatedDate(LocalDateTime.now())
                .metrics(metrics)
                .tradeIds(Arrays.asList("trade-123"))
                .assetAllocations(Arrays.asList(allocation))
                .build();
    }

    private PortfolioEntity createTestPortfolioEntity() {
        // Create metrics
        PortfolioMetrics metrics = PortfolioMetrics.builder()
                .totalTrades(10)
                .winningTrades(7)
                .losingTrades(3)
                .breakEvenTrades(0)
                .openPositions(2)
                .netProfitLoss(new BigDecimal("1500.00"))
                .netProfitLossPercentage(new BigDecimal("15.00"))
                .winRate(new BigDecimal("70.00"))
                .lossRate(new BigDecimal("30.00"))
                .maxDrawdown(new BigDecimal("300.00"))
                .maxDrawdownPercentage(new BigDecimal("3.00"))
                .sharpeRatio(new BigDecimal("1.5"))
                .sortinoRatio(new BigDecimal("2.0"))
                .build();

        // Create asset allocations
        AssetAllocation allocation = AssetAllocation.builder()
                .assetClass(AssetClass.STOCK)
                .currentPercentage(new BigDecimal("25.00"))
                .targetPercentage(new BigDecimal("30.00"))
                .variance(new BigDecimal("-5.00"))
                .build();

        // Create portfolio entity
        return PortfolioEntity.builder()
                .portfolioId("portfolio-456")
                .name("Tech Growth Portfolio")
                .description("Portfolio focused on growth tech stocks")
                .ownerId("user-789")
                .active(true)
                .currency("USD")
                .initialCapital(new BigDecimal("10000.00"))
                .currentCapital(new BigDecimal("11500.00"))
                .createdDate(LocalDateTime.now().minusDays(30))
                .lastUpdatedDate(LocalDateTime.now())
                .metrics(metrics)
                .trades(Arrays.asList("trade-123"))
                .assetAllocations(Arrays.asList(allocation))
                .build();
    }
}
