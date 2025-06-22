package am.trade.persistence.mapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.persistence.entity.PortfolioEntity;
import am.trade.persistence.entity.TradeDetailsEntity;

/**
 * Mapper class for converting between domain models and persistence entities
 */
@Component
public class PortfolioMapper {

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
        List<TradeDetailsEntity> tradeEntities = null;
        if (model.getTrades() != null) {
            tradeEntities = model.getTrades().stream()
                    .map(this::toTradeEntity)
                    .collect(Collectors.toList());
        }
        
        // Map metrics if present
        PortfolioEntity.PortfolioMetrics metricsEntity = null;
        if (model.getMetrics() != null) {
            metricsEntity = PortfolioEntity.PortfolioMetrics.builder()
                    .totalTrades(model.getMetrics().getTotalTrades())
                    .winningTrades(model.getMetrics().getWinningTrades())
                    .losingTrades(model.getMetrics().getLosingTrades())
                    .breakEvenTrades(model.getMetrics().getBreakEvenTrades())
                    .openPositions(model.getMetrics().getOpenPositions())
                    .totalProfitLoss(model.getMetrics().getNetProfitLoss())
                    .totalProfitLossPercentage(model.getMetrics().getNetProfitLossPercentage())
                    .winRate(model.getMetrics().getWinRate())
                    .lossRate(model.getMetrics().getLossRate())
                    .averageWin(model.getMetrics().getTotalProfit() != null ? 
                        model.getMetrics().getTotalProfit().divide(BigDecimal.valueOf(model.getMetrics().getWinningTrades()), 
                        java.math.RoundingMode.HALF_UP) : null)
                    .averageLoss(model.getMetrics().getTotalLoss() != null ? 
                        model.getMetrics().getTotalLoss().divide(BigDecimal.valueOf(model.getMetrics().getLosingTrades()), 
                        java.math.RoundingMode.HALF_UP) : null)
                    .largestWin(null) // Not available in domain model
                    .largestLoss(null) // Not available in domain model
                    .maxDrawdown(model.getMetrics().getMaxDrawdown())
                    .maxDrawdownPercentage(model.getMetrics().getMaxDrawdownPercentage())
                    .sharpeRatio(model.getMetrics().getSharpeRatio())
                    .sortinoRatio(model.getMetrics().getSortinoRatio())
                    .calmarRatio(null) // Not available in domain model
                    .build();
        }
        
        // Map asset allocations if present
        List<PortfolioEntity.AssetAllocation> assetAllocations = null;
        if (model.getAssetAllocations() != null) {
            assetAllocations = model.getAssetAllocations().stream()
                    .map(this::toAssetAllocationEntity)
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
        List<TradeDetails> tradeDetails = null;
        if (entity.getTrades() != null) {
            tradeDetails = entity.getTrades().stream()
                    .map(this::toTradeDetails)
                    .collect(Collectors.toList());
        }
        
        // Map metrics if present
        PortfolioModel.PortfolioMetrics metrics = null;
        if (entity.getMetrics() != null) {
            metrics = PortfolioModel.PortfolioMetrics.builder()
                    .totalTrades(entity.getMetrics().getTotalTrades())
                    .winningTrades(entity.getMetrics().getWinningTrades())
                    .losingTrades(entity.getMetrics().getLosingTrades())
                    .breakEvenTrades(entity.getMetrics().getBreakEvenTrades())
                    .openPositions(entity.getMetrics().getOpenPositions())
                    .netProfitLoss(entity.getMetrics().getTotalProfitLoss())
                    .netProfitLossPercentage(entity.getMetrics().getTotalProfitLossPercentage())
                    .winRate(entity.getMetrics().getWinRate())
                    .lossRate(entity.getMetrics().getLossRate())
                    .profitFactor(null) // Not available in entity
                    .expectancy(null) // Not available in entity
                    .totalValue(null) // Not available in entity
                    .totalProfit(entity.getMetrics().getAverageWin() != null && entity.getMetrics().getWinningTrades() > 0 ? 
                        entity.getMetrics().getAverageWin().multiply(BigDecimal.valueOf(entity.getMetrics().getWinningTrades())) : null)
                    .totalLoss(entity.getMetrics().getAverageLoss() != null && entity.getMetrics().getLosingTrades() > 0 ? 
                        entity.getMetrics().getAverageLoss().multiply(BigDecimal.valueOf(entity.getMetrics().getLosingTrades())) : null)
                    .maxDrawdown(entity.getMetrics().getMaxDrawdown())
                    .maxDrawdownPercentage(entity.getMetrics().getMaxDrawdownPercentage())
                    .sharpeRatio(entity.getMetrics().getSharpeRatio())
                    .sortinoRatio(entity.getMetrics().getSortinoRatio())
                    .monthlyReturns(null) // Not available in entity
                    .weeklyReturns(null) // Not available in entity
                    .build();
        }
        
        // Map asset allocations if present
        List<PortfolioModel.AssetAllocation> assetAllocations = null;
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
                .metrics(metrics)
                .trades(tradeDetails)
                .assetAllocations(assetAllocations)
                .build();
    }
    
    /**
     * Convert a TradeDetails to a TradeDetailsEntity
     * @param model The domain model to convert
     * @return The corresponding persistence entity
     */
    public TradeDetailsEntity toTradeEntity(TradeDetails model) {
        if (model == null) {
            return null;
        }
        
        // Map entry info if present
        TradeDetailsEntity.EntryExitInfo entryInfoEntity = null;
        if (model.getEntryInfo() != null) {
            entryInfoEntity = TradeDetailsEntity.EntryExitInfo.builder()
                    .timestamp(model.getEntryInfo().getTimestamp())
                    .price(model.getEntryInfo().getPrice())
                    .quantity(model.getEntryInfo().getQuantity())
                    .fees(model.getEntryInfo().getFees())
                    .totalValue(model.getEntryInfo().getTotalValue())
                    .build();
        }
        
        // Map exit info if present
        TradeDetailsEntity.EntryExitInfo exitInfoEntity = null;
        if (model.getExitInfo() != null) {
            exitInfoEntity = TradeDetailsEntity.EntryExitInfo.builder()
                    .timestamp(model.getExitInfo().getTimestamp())
                    .price(model.getExitInfo().getPrice())
                    .quantity(model.getExitInfo().getQuantity())
                    .fees(model.getExitInfo().getFees())
                    .totalValue(model.getExitInfo().getTotalValue())
                    .build();
        }
        
        // Map metrics if present
        TradeDetailsEntity.TradeMetrics metricsEntity = null;
        if (model.getMetrics() != null) {
            metricsEntity = TradeDetailsEntity.TradeMetrics.builder()
                    .profitLoss(model.getMetrics().getProfitLoss())
                    .profitLossPercentage(model.getMetrics().getProfitLossPercentage())
                    .returnOnEquity(model.getMetrics().getReturnOnEquity())
                    .riskAmount(model.getMetrics().getRiskAmount())
                    .rewardAmount(model.getMetrics().getRewardAmount())
                    .riskRewardRatio(model.getMetrics().getRiskRewardRatio())
                    .holdingTimeDays(model.getMetrics().getHoldingTimeDays())
                    .holdingTimeHours(model.getMetrics().getHoldingTimeHours())
                    .holdingTimeMinutes(model.getMetrics().getHoldingTimeMinutes())
                    .build();
        }
        
        return TradeDetailsEntity.builder()
                .tradeId(model.getTradeId())
                .portfolioId(model.getPortfolioId())
                .symbol(model.getSymbol())
                .tradePositionType(model.getTradePositionType())
                .status(model.getStatus())
                .entryInfo(entryInfoEntity)
                .exitInfo(exitInfoEntity)
                .metrics(metricsEntity)
                .tradeExecutions(model.getTradeExecutions())
                .build();
    }
    
    /**
     * Convert a TradeDetailsEntity to a TradeDetails
     * @param entity The persistence entity to convert
     * @return The corresponding domain model
     */
    public TradeDetails toTradeDetails(TradeDetailsEntity entity) {
        if (entity == null) {
            return null;
        }
        
        // Map entry info if present
        TradeDetails.EntryExitInfo entryInfo = null;
        if (entity.getEntryInfo() != null) {
            entryInfo = TradeDetails.EntryExitInfo.builder()
                    .timestamp(entity.getEntryInfo().getTimestamp())
                    .price(entity.getEntryInfo().getPrice())
                    .quantity(entity.getEntryInfo().getQuantity())
                    .fees(entity.getEntryInfo().getFees())
                    .totalValue(entity.getEntryInfo().getTotalValue())
                    .build();
        }
        
        // Map exit info if present
        TradeDetails.EntryExitInfo exitInfo = null;
        if (entity.getExitInfo() != null) {
            exitInfo = TradeDetails.EntryExitInfo.builder()
                    .timestamp(entity.getExitInfo().getTimestamp())
                    .price(entity.getExitInfo().getPrice())
                    .quantity(entity.getExitInfo().getQuantity())
                    .fees(entity.getExitInfo().getFees())
                    .totalValue(entity.getExitInfo().getTotalValue())
                    .build();
        }
        
        // Map metrics if present
        TradeDetails.TradeMetrics metrics = null;
        if (entity.getMetrics() != null) {
            metrics = TradeDetails.TradeMetrics.builder()
                    .profitLoss(entity.getMetrics().getProfitLoss())
                    .profitLossPercentage(entity.getMetrics().getProfitLossPercentage())
                    .returnOnEquity(entity.getMetrics().getReturnOnEquity())
                    .riskAmount(entity.getMetrics().getRiskAmount())
                    .rewardAmount(entity.getMetrics().getRewardAmount())
                    .riskRewardRatio(entity.getMetrics().getRiskRewardRatio())
                    .holdingTimeDays(entity.getMetrics().getHoldingTimeDays())
                    .holdingTimeHours(entity.getMetrics().getHoldingTimeHours())
                    .holdingTimeMinutes(entity.getMetrics().getHoldingTimeMinutes())
                    .build();
        }
        
        return TradeDetails.builder()
                .tradeId(entity.getTradeId())
                .portfolioId(entity.getPortfolioId())
                .symbol(entity.getSymbol())
                .tradePositionType(entity.getTradePositionType())
                .status(entity.getStatus())
                .entryInfo(entryInfo)
                .exitInfo(exitInfo)
                .metrics(metrics)
                .tradeExecutions(entity.getTradeExecutions())
                .build();
    }
    
    /**
     * Convert a PortfolioModel.AssetAllocation to a PortfolioEntity.AssetAllocation
     * @param model The domain model to convert
     * @return The corresponding persistence entity
     */
    public PortfolioEntity.AssetAllocation toAssetAllocationEntity(PortfolioModel.AssetAllocation model) {
        if (model == null) {
            return null;
        }
        
        return PortfolioEntity.AssetAllocation.builder()
                .assetClass(model.getAssetClass())
                .sector(null) // Not available in domain model
                .industry(null) // Not available in domain model
                .allocation(model.getCurrentPercentage()) // Map currentPercentage to allocation
                .currentValue(null) // Not available in domain model
                .profitLoss(null) // Not available in domain model
                .profitLossPercentage(model.getVariance()) // Map variance to profitLossPercentage
                .build();
    }
    
    /**
     * Convert a PortfolioEntity.AssetAllocation to a PortfolioModel.AssetAllocation
     * @param entity The persistence entity to convert
     * @return The corresponding domain model
     */
    public PortfolioModel.AssetAllocation toAssetAllocationModel(PortfolioEntity.AssetAllocation entity) {
        if (entity == null) {
            return null;
        }
        
        return PortfolioModel.AssetAllocation.builder()
                .assetClass(entity.getAssetClass())
                .currentPercentage(entity.getAllocation()) // Map allocation to currentPercentage
                .targetPercentage(null) // Not available in entity
                .variance(entity.getProfitLossPercentage()) // Map profitLossPercentage to variance
                .build();
    }
}
