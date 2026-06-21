package am.trade.persistence.mapper;

import am.trade.common.models.TradeDetails;
import am.trade.persistence.entity.TradeDetailsEntity;
import org.springframework.stereotype.Component;

/**
 * Mapper class for converting between TradeDetailsEntity and TradeDetails domain model
 */
@Component
public class TradeDetailsMapper {
     
    /**
     * Convert a TradeDetails to a TradeDetailsEntity
     * @param model The domain model to convert
     * @return The corresponding persistence entity
     */
    public TradeDetailsEntity toTradeEntity(TradeDetails model) {
        if (model == null) {
            return null;
        }
        
        return TradeDetailsEntity.builder()
                .tradeId(model.getTradeId())
                .portfolioId(model.getPortfolioId())
                .symbol(model.getSymbol())
                .instrumentInfo(model.getInstrumentInfo())
                .tradePositionType(model.getTradePositionType())
                .status(model.getStatus())
                .entryInfo(model.getEntryInfo())
                .exitInfo(model.getExitInfo())
                .metrics(model.getMetrics())
                .tradeExecutions(model.getTradeExecutions())
                .userId(model.getUserId())
                .attachments(model.getAttachments())
                .notes(model.getNotes())
                .tags(model.getTags())
                .psychologyData(model.getPsychologyData())
                .entryReasoning(model.getEntryReasoning())
                .exitReasoning(model.getExitReasoning())
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
        
        // Provide safe defaults for legacy data missing required fields
        String resolvedTradeId = entity.getTradeId() != null ? entity.getTradeId() : 
                               (entity.getId() != null ? entity.getId() : "unknown-trade");
        String resolvedPortfolioId = entity.getPortfolioId() != null ? entity.getPortfolioId() : "unknown-portfolio";
        
        return TradeDetails.builder()
                .tradeId(resolvedTradeId)
                .portfolioId(resolvedPortfolioId)
                .symbol(entity.getSymbol())
                .instrumentInfo(entity.getInstrumentInfo())
                .tradePositionType(entity.getTradePositionType() != null ? entity.getTradePositionType() : am.trade.common.models.enums.TradePositionType.LONG)
                .status(entity.getStatus() != null ? entity.getStatus() : am.trade.common.models.enums.TradeStatus.OPEN)
                .entryInfo(entity.getEntryInfo())
                .exitInfo(entity.getExitInfo())
                .metrics(entity.getMetrics())
                .tradeExecutions(entity.getTradeExecutions())
                .userId(entity.getUserId())
                .attachments(entity.getAttachments())
                .notes(entity.getNotes())
                .tags(entity.getTags())
                .psychologyData(entity.getPsychologyData())
                .entryReasoning(entity.getEntryReasoning())
                .exitReasoning(entity.getExitReasoning())
                .build();
    }
}
