package am.trade.analytics.mapper;

import am.trade.analytics.model.TradeReplay;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between persistence entities and analytics models
 */
@Component
public class PersistenceModelMapper {

    /**
     * Convert persistence TradeReplay entity to analytics TradeReplay model
     * 
     * @param entity Persistence TradeReplay entity
     * @return Analytics TradeReplay model
     */
    public TradeReplay toAnalyticsModel(am.trade.persistence.entity.TradeReplay entity) {
        if (entity == null) {
            return null;
        }
        
        return TradeReplay.builder()
                .id(entity.getId())
                .replayId(entity.getReplayId())
                .symbol(entity.getSymbol())
                .entryDate(entity.getEntryDate())
                .exitDate(entity.getExitDate())
                .entryPrice(entity.getEntryPrice())
                .exitPrice(entity.getExitPrice())
                .side(entity.getSide())
                .positionSize(entity.getPositionSize())
                .profitLoss(entity.getProfitLoss())
                .profitLossPercentage(entity.getProfitLossPercentage())
                .maxDrawdown(entity.getMaxDrawdown())
                .maxDrawdownPercentage(entity.getMaxDrawdownPercentage())
                .maxProfit(entity.getMaxProfit())
                .maxProfitPercentage(entity.getMaxProfitPercentage())
                .holdingPeriodDays(entity.getHoldingPeriodDays())
                .volatility(entity.getVolatility())
                .averageDailyMovement(entity.getAverageDailyMovement())
                .priceDataPoints(entity.getPriceDataPoints())
                .replayNotes(entity.getReplayNotes())
                .originalTradeId(entity.getOriginalTradeId())
                .strategyId(entity.getStrategyId())
                .portfolioId(entity.getPortfolioId())
                .createdDate(entity.getCreatedDate())
                .lastModifiedDate(entity.getLastModifiedDate())
                .build();
    }

    /**
     * Convert analytics TradeReplay model to persistence TradeReplay entity
     * 
     * @param model Analytics TradeReplay model
     * @return Persistence TradeReplay entity
     */
    public am.trade.persistence.entity.TradeReplay toPersistenceEntity(TradeReplay model) {
        if (model == null) {
            return null;
        }
        
        return am.trade.persistence.entity.TradeReplay.builder()
                .id(model.getId())
                .replayId(model.getReplayId())
                .symbol(model.getSymbol())
                .entryDate(model.getEntryDate())
                .exitDate(model.getExitDate())
                .entryPrice(model.getEntryPrice())
                .exitPrice(model.getExitPrice())
                .side(model.getSide())
                .positionSize(model.getPositionSize())
                .profitLoss(model.getProfitLoss())
                .profitLossPercentage(model.getProfitLossPercentage())
                .maxDrawdown(model.getMaxDrawdown())
                .maxDrawdownPercentage(model.getMaxDrawdownPercentage())
                .maxProfit(model.getMaxProfit())
                .maxProfitPercentage(model.getMaxProfitPercentage())
                .holdingPeriodDays(model.getHoldingPeriodDays())
                .volatility(model.getVolatility())
                .averageDailyMovement(model.getAverageDailyMovement())
                .priceDataPoints(model.getPriceDataPoints())
                .replayNotes(model.getReplayNotes())
                .originalTradeId(model.getOriginalTradeId())
                .strategyId(model.getStrategyId())
                .portfolioId(model.getPortfolioId())
                .createdDate(model.getCreatedDate())
                .lastModifiedDate(model.getLastModifiedDate())
                .build();
    }

    /**
     * Convert list of persistence TradeReplay entities to analytics TradeReplay models
     * 
     * @param entities List of persistence TradeReplay entities
     * @return List of analytics TradeReplay models
     */
    public List<TradeReplay> toAnalyticsModelList(List<am.trade.persistence.entity.TradeReplay> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toAnalyticsModel)
                .collect(Collectors.toList());
    }
}
