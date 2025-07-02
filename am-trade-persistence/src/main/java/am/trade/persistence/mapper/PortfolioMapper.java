package am.trade.persistence.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import am.trade.common.models.AssetAllocation;
import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.persistence.entity.PortfolioEntity;

/**
 * Mapper class for converting between domain models and persistence entities
 */
@Component
public class PortfolioMapper {

    TradeDetailsMapper tradeDetailsMapper = new TradeDetailsMapper();

    /**
     * Convert a PortfolioModel to a PortfolioEntity
     * @param model The domain model to convert
     * @return The corresponding persistence entity
     */
    public PortfolioEntity toEntity(PortfolioModel model) {
        if (model == null) {
            return null;
        }
        
        
        // Map trades if present    
        List<String> tradeEntities = null;
        if (model.getTradeIds() != null) {
            tradeEntities = model.getTradeIds().stream()
                    .collect(Collectors.toList());
        }
        
        // Map metrics if present
        PortfolioMetrics metricsEntity = null;
        if (model.getMetrics() != null) {
            metricsEntity = PortfolioMetrics.builder()
                    .totalTrades(model.getMetrics().getTotalTrades())
                    .winningTrades(model.getMetrics().getWinningTrades())
                    .losingTrades(model.getMetrics().getLosingTrades())
                    .breakEvenTrades(model.getMetrics().getBreakEvenTrades())
                    .openPositions(model.getMetrics().getOpenPositions())
                    .netProfitLoss(model.getMetrics().getNetProfitLoss())
                    .netProfitLossPercentage(model.getMetrics().getNetProfitLossPercentage())
                    .winRate(model.getMetrics().getWinRate())
                    .lossRate(model.getMetrics().getLossRate())
                    .maxDrawdown(model.getMetrics().getMaxDrawdown())
                    .maxDrawdownPercentage(model.getMetrics().getMaxDrawdownPercentage())
                    .sharpeRatio(model.getMetrics().getSharpeRatio())
                    .sortinoRatio(model.getMetrics().getSortinoRatio())
                    .build();
        }
        
        // Map asset allocations if present
        List<AssetAllocation> assetAllocations = null;
        if (model.getAssetAllocations() != null) {
            assetAllocations = model.getAssetAllocations().stream()
                    .map(this::toAssetAllocationEntity)
                    .collect(Collectors.toList());
        }
        
        // Map winning trades if present
        List<String> winningTradeIds = null;
        if (model.getWinningTradeIds() != null) {
            winningTradeIds = model.getWinningTradeIds().stream()
                    .collect(Collectors.toList());
        }
        
        // Map losing trades if present
        List<String> losingTradeIds = null;
        if (model.getLosingTradeIds() != null) {
            losingTradeIds = model.getLosingTradeIds().stream()
                    .collect(Collectors.toList());
        }
        
        return PortfolioEntity.builder()
                .portfolioId(model.getPortfolioId())
                .name(model.getName())
                .description(model.getDescription())
                .ownerId(model.getOwnerId())
                .active(model.isActive())
                .currency(model.getCurrency())
                .initialCapital(model.getInitialCapital())
                .currentCapital(model.getCurrentCapital())
                .createdDate(model.getCreatedDate())
                .lastUpdatedDate(model.getLastUpdatedDate())
                .metrics(metricsEntity)
                .trades(tradeEntities)
                .winningTrades(winningTradeIds)
                .losingTrades(losingTradeIds)
                .assetAllocations(assetAllocations)
                .build();
    }
    
    /**
     * Convert a PortfolioEntity to a PortfolioModel
     * @param entity The persistence entity to convert
     * @return The corresponding domain model
     */
    public PortfolioModel toModel(PortfolioEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Map trades if present
        List<String> tradeIds = null;
        if (entity.getTrades() != null) {
            tradeIds = entity.getTrades().stream()
                    .collect(Collectors.toList());
        }
        
        // Map winning trades if present
        List<String> winningTradeIds = null;
        if (entity.getWinningTrades() != null) {
            winningTradeIds = entity.getWinningTrades().stream()
                    .collect(Collectors.toList());
        }
        
        // Map losing trades if present
        List<String> losingTradeIds = null;
        if (entity.getLosingTrades() != null) {
            losingTradeIds = entity.getLosingTrades().stream()
                    .collect(Collectors.toList());
        }
        
        // Map asset allocations if present
        List<AssetAllocation> assetAllocations = null;
        if (entity.getAssetAllocations() != null) {
            assetAllocations = entity.getAssetAllocations().stream()
                    .map(this::toAssetAllocationModel)
                    .collect(Collectors.toList());
        }
        
        return PortfolioModel.builder()
                .portfolioId(entity.getPortfolioId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ownerId(entity.getOwnerId())
                .active(entity.isActive())
                .currency(entity.getCurrency())
                .initialCapital(entity.getInitialCapital())
                .currentCapital(entity.getCurrentCapital())
                .createdDate(entity.getCreatedDate())
                .lastUpdatedDate(entity.getLastUpdatedDate())
                .metrics(entity.getMetrics())
                .tradeIds(tradeIds)
                .winningTradeIds(winningTradeIds)
                .losingTradeIds(losingTradeIds)
                .assetAllocations(assetAllocations)
                .build();
    }
    
    /**
     * Convert an AssetAllocation to an AssetAllocation entity
     * @param model The domain model to convert
     * @return The corresponding persistence entity
     */
    public AssetAllocation toAssetAllocationEntity(AssetAllocation model) {
        if (model == null) {
            return null;
        }
        
        return AssetAllocation.builder()
                .assetClass(model.getAssetClass())
                .currentPercentage(model.getCurrentPercentage())
                .targetPercentage(model.getTargetPercentage())
                .variance(model.getVariance())
                .build();
    }
    
    /**
     * Convert an AssetAllocation entity to an AssetAllocation model
     * @param entity The persistence entity to convert
     * @return The corresponding domain model
     */
    public AssetAllocation toAssetAllocationModel(AssetAllocation entity) {
        if (entity == null) {
            return null;
        }
        
        return AssetAllocation.builder()
                .assetClass(entity.getAssetClass())
                .currentPercentage(entity.getCurrentPercentage())
                .targetPercentage(entity.getTargetPercentage())
                .variance(entity.getVariance())
                .build();
    }
}
