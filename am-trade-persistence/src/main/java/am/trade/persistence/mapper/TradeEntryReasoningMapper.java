package am.trade.persistence.mapper;

import am.trade.common.models.TradeEntryReasoning;
import am.trade.common.models.enums.FundamentalEntryReason;
import am.trade.common.models.enums.TechnicalEntryReason;
import am.trade.persistence.entity.TradeEntryReasoningEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mapper for converting between TradeEntryReasoning model and TradeEntryReasoningEntity
 */
@Component
public class TradeEntryReasoningMapper {

    private static final String PROCESS_ID = UUID.randomUUID().toString();
    private static final Logger logger = Logger.getLogger(TradeEntryReasoningMapper.class.getName());

    /**
     * Convert from model to entity
     * @param model The TradeEntryReasoning model
     * @return The TradeEntryReasoningEntity
     */
    public TradeEntryReasoningEntity toEntity(TradeEntryReasoning model) {
        if (model == null) {
            return null;
        }

        try {
            TradeEntryReasoningEntity entity = new TradeEntryReasoningEntity();
            
            // Set basic fields
            entity.setPrimaryReason(model.getPrimaryReason());
            entity.setReasoningSummary(model.getReasoningSummary());
            entity.setReasoningNotes(model.getReasoningSummary()); // Set reasoningNotes to match reasoningSummary
            entity.setConfidenceLevel(model.getConfidenceLevel());
            
            // Handle technical reasons with null check and error handling
            if (model.getTechnicalReasons() != null) {
                List<String> technicalReasonCodes = new ArrayList<>();
                for (TechnicalEntryReason reason : model.getTechnicalReasons()) {
                    try {
                        if (reason != null) {
                            technicalReasonCodes.add(reason.getCode());
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing technical reason: " + e.getMessage());
                    }
                }
                entity.setTechnicalReasons(technicalReasonCodes);
            } else {
                entity.setTechnicalReasons(Collections.emptyList());
            }
            
            // Handle fundamental reasons with null check and error handling
            if (model.getFundamentalReasons() != null) {
                List<String> fundamentalReasonCodes = new ArrayList<>();
                for (FundamentalEntryReason reason : model.getFundamentalReasons()) {
                    try {
                        if (reason != null) {
                            fundamentalReasonCodes.add(reason.getCode());
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing fundamental reason: " + e.getMessage());
                    }
                }
                entity.setFundamentalReasons(fundamentalReasonCodes);
            } else {
                entity.setFundamentalReasons(Collections.emptyList());
            }
            
            // Handle supporting indicators with null check
            entity.setSupportingIndicators(model.getSupportingIndicators() != null ? 
                    model.getSupportingIndicators() : Collections.emptyList());
            
            // Handle conflicting indicators with null check
            entity.setConflictingIndicators(model.getConflictingIndicators() != null ? 
                    model.getConflictingIndicators() : Collections.emptyList());
            
            return entity;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to convert TradeEntryReasoning model to entity: " + e.getMessage(), e);
            return new TradeEntryReasoningEntity(); // Return empty entity instead of null
        }
    }

    /**
     * Convert from entity to model
     * @param entity The TradeEntryReasoningEntity
     * @return The TradeEntryReasoning model
     */
    public TradeEntryReasoning toModel(TradeEntryReasoningEntity entity) {
        if (entity == null) {
            return null;
        }

        try {
            TradeEntryReasoning model = new TradeEntryReasoning();
            
            // Set basic fields
            model.setReasoningSummary(entity.getReasoningSummary());
            model.setPrimaryReason(entity.getPrimaryReason());
            model.setConfidenceLevel(entity.getConfidenceLevel());
            
            // Handle technical reasons with null check and error handling
            if (entity.getTechnicalReasons() != null) {
                List<TechnicalEntryReason> technicalReasons = new ArrayList<>();
                for (String code : entity.getTechnicalReasons()) {
                    try {
                        if (code != null) {
                            TechnicalEntryReason reason = TechnicalEntryReason.fromCode(code);
                            if (reason != null) {
                                technicalReasons.add(reason);
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing technical reason code: " + code + ", " + e.getMessage());
                    }
                }
                model.setTechnicalReasons(technicalReasons);
            } else {
                model.setTechnicalReasons(new ArrayList<>());
            }
            
            // Handle fundamental reasons with null check and error handling
            if (entity.getFundamentalReasons() != null) {
                List<FundamentalEntryReason> fundamentalReasons = new ArrayList<>();
                for (String code : entity.getFundamentalReasons()) {
                    try {
                        if (code != null) {
                            FundamentalEntryReason reason = FundamentalEntryReason.fromCode(code);
                            if (reason != null) {
                                fundamentalReasons.add(reason);
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing fundamental reason code: " + code + ", " + e.getMessage());
                    }
                }
                model.setFundamentalReasons(fundamentalReasons);
            } else {
                model.setFundamentalReasons(new ArrayList<>());
            }
            
            // Handle supporting indicators with null check
            model.setSupportingIndicators(entity.getSupportingIndicators() != null ? 
                    entity.getSupportingIndicators() : new ArrayList<>());
            
            // Handle conflicting indicators with null check
            model.setConflictingIndicators(entity.getConflictingIndicators() != null ? 
                    entity.getConflictingIndicators() : new ArrayList<>());
            
            return model;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to convert TradeEntryReasoningEntity to model: " + e.getMessage(), e);
            return new TradeEntryReasoning(); // Return empty model instead of null
        }
    }
}
