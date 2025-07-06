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
            return new TradeEntryReasoningEntity();
        }

        TradeEntryReasoningEntity entity = new TradeEntryReasoningEntity();
        
        setBasicEntityFields(model, entity);
        processTechnicalReasons(model, entity);
        processFundamentalReasons(model, entity);
        processIndicators(model, entity);
        
        return entity;
    }
    
    /**
     * Set basic fields from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void setBasicEntityFields(TradeEntryReasoning model, TradeEntryReasoningEntity entity) {
        entity.setPrimaryReason(model.getPrimaryReason());
        entity.setReasoningSummary(model.getReasoningSummary());
        entity.setReasoningNotes(model.getReasoningSummary()); // Set reasoningNotes to match reasoningSummary
        entity.setConfidenceLevel(model.getConfidenceLevel());
    }
    
    /**
     * Process technical reasons from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void processTechnicalReasons(TradeEntryReasoning model, TradeEntryReasoningEntity entity) {
        if (model.getTechnicalReasons() != null) {
            List<String> technicalReasonCodes = new ArrayList<>();
            
            // Handle potential mixed type list (due to deserialization issues)
            for (Object reasonObj : model.getTechnicalReasons()) {
                if (reasonObj instanceof TechnicalEntryReason) {
                    // Normal case - process TechnicalEntryReason object
                    TechnicalEntryReason reason = (TechnicalEntryReason) reasonObj;
                    if (reason != null) {
                        technicalReasonCodes.add(reason.getCode());
                    }
                } else if (reasonObj instanceof String) {
                    // Handle String case directly
                    String code = (String) reasonObj;
                    if (code != null && !code.isEmpty()) {
                        technicalReasonCodes.add(code);
                    }
                } else if (reasonObj != null) {
                    // Try to handle other object types by converting to string
                    try {
                        String code = reasonObj.toString();
                        if (code != null && !code.isEmpty()) {
                            technicalReasonCodes.add(code);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Could not process technical reason: " + reasonObj, e);
                    }
                }
            }
            
            entity.setTechnicalReasons(technicalReasonCodes);
        } else {
            entity.setTechnicalReasons(Collections.emptyList());
        }
    }
    
    /**
     * Process fundamental reasons from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void processFundamentalReasons(TradeEntryReasoning model, TradeEntryReasoningEntity entity) {
        if (model.getFundamentalReasons() != null) {
            List<String> fundamentalReasonCodes = new ArrayList<>();
            
            // Handle potential mixed type list (due to deserialization issues)
            for (Object reasonObj : model.getFundamentalReasons()) {
                if (reasonObj instanceof FundamentalEntryReason) {
                    // Normal case - process FundamentalEntryReason object
                    processFundamentalReason((FundamentalEntryReason) reasonObj, fundamentalReasonCodes);
                } else if (reasonObj instanceof String) {
                    // Handle String case directly
                    String code = (String) reasonObj;
                    if (code != null && !code.isEmpty()) {
                        fundamentalReasonCodes.add(code);
                    }
                } else if (reasonObj != null) {
                    // Try to handle other object types by converting to string
                    try {
                        String code = reasonObj.toString();
                        if (code != null && !code.isEmpty()) {
                            fundamentalReasonCodes.add(code);
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Could not process fundamental reason: " + reasonObj, e);
                    }
                }
            }
            
            entity.setFundamentalReasons(fundamentalReasonCodes);
        } else {
            entity.setFundamentalReasons(Collections.emptyList());
        }
    }
    
    /**
     * Process a single fundamental reason
     * @param reason The reason to process
     * @param codes List to add the code to
     */
    private void processFundamentalReason(FundamentalEntryReason reason, List<String> codes) {
        try {
            if (reason != null) {
                codes.add(reason.getCode());
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing fundamental reason: " + e.getMessage());
        }
    }
    
    /**
     * Process indicators from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void processIndicators(TradeEntryReasoning model, TradeEntryReasoningEntity entity) {
        // Handle supporting indicators with null check
        entity.setSupportingIndicators(model.getSupportingIndicators() != null ? 
                model.getSupportingIndicators() : Collections.emptyList());
        
        // Handle conflicting indicators with null check
        entity.setConflictingIndicators(model.getConflictingIndicators() != null ? 
                model.getConflictingIndicators() : Collections.emptyList());
    }

    /**
     * Convert from entity to model
     * @param entity The TradeEntryReasoningEntity
     * @return The TradeEntryReasoning model
     */
    public TradeEntryReasoning toModel(TradeEntryReasoningEntity entity) {
        if (entity == null) {
            return new TradeEntryReasoning();
        }

        TradeEntryReasoning model = new TradeEntryReasoning();
        
        setBasicModelFields(entity, model);
        model.setTechnicalReasons(processTechnicalReasonCodes(entity));
        model.setFundamentalReasons(processFundamentalReasonCodes(entity));
        processModelIndicators(entity, model);
        
        return model;
    }
    
    /**
     * Set basic fields from entity to model
     * @param entity Source entity
     * @param model Target model
     */
    private void setBasicModelFields(TradeEntryReasoningEntity entity, TradeEntryReasoning model) {
        model.setReasoningSummary(entity.getReasoningSummary());
        model.setPrimaryReason(entity.getPrimaryReason());
        model.setConfidenceLevel(entity.getConfidenceLevel());
    }
    
    /**
     * Process technical reason codes from entity
     * @param entity Source entity
     * @return List of technical entry reasons
     */
    private List<TechnicalEntryReason> processTechnicalReasonCodes(TradeEntryReasoningEntity entity) {
        List<TechnicalEntryReason> technicalReasons = new ArrayList<>();
        if (entity.getTechnicalReasons() == null) {
            return technicalReasons;
        }
        
        List<?> rawReasons = entity.getTechnicalReasons();
        for (Object codeObj : rawReasons) {
            if (codeObj instanceof String) {
                processTechnicalReasonCode((String) codeObj, technicalReasons);
            } else if (codeObj != null) {
                // Try to convert non-string objects to string
                try {
                    processTechnicalReasonCode(codeObj.toString(), technicalReasons);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not process non-string technical reason: " + codeObj);
                }
            }
        }
        
        return technicalReasons;
    }
    
    /**
     * Process a single technical reason code
     * @param code The code to process
     * @param reasons List to add the reason to
     */
    private void processTechnicalReasonCode(String code, List<TechnicalEntryReason> reasons) {
        try {
            if (code != null) {
                TechnicalEntryReason reason = TechnicalEntryReason.fromCode(code);
                if (reason != null) {
                    reasons.add(reason);
                } else {
                    // Create a custom reason if it doesn't exist
                    reason = TechnicalEntryReason.fromCode(code, code);
                    reasons.add(reason);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing technical reason code: " + code + ", " + e.getMessage());
        }
    }
    
    /**
     * Process fundamental reason codes from entity
     * @param entity Source entity
     * @return List of fundamental entry reasons
     */
    private List<FundamentalEntryReason> processFundamentalReasonCodes(TradeEntryReasoningEntity entity) {
        List<FundamentalEntryReason> fundamentalReasons = new ArrayList<>();
        if (entity.getFundamentalReasons() == null) {
            return fundamentalReasons;
        }
        
        List<?> rawReasons = entity.getFundamentalReasons();
        for (Object codeObj : rawReasons) {
            if (codeObj instanceof String) {
                processFundamentalReasonCode((String) codeObj, fundamentalReasons);
            } else if (codeObj != null) {
                // Try to convert non-string objects to string
                try {
                    processFundamentalReasonCode(codeObj.toString(), fundamentalReasons);
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not process non-string fundamental reason: " + codeObj);
                }
            }
        }
        
        return fundamentalReasons;
    }
    
    /**
     * Process a single fundamental reason code
     * @param code The code to process
     * @param reasons List to add the reason to
     */
    private void processFundamentalReasonCode(String code, List<FundamentalEntryReason> reasons) {
        try {
            if (code != null) {
                FundamentalEntryReason reason = FundamentalEntryReason.fromCode(code);
                if (reason != null) {
                    reasons.add(reason);
                } else {
                    // Create a custom reason if it doesn't exist
                    reason = FundamentalEntryReason.fromCode(code, code);
                    reasons.add(reason);
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error processing fundamental reason code: " + code + ", " + e.getMessage());
        }
    }
    
    /**
     * Process indicators from entity to model
     * @param entity Source entity
     * @param model Target model
     */
    private void processModelIndicators(TradeEntryReasoningEntity entity, TradeEntryReasoning model) {
        // Handle supporting indicators with null check
        model.setSupportingIndicators(entity.getSupportingIndicators() != null ? 
                entity.getSupportingIndicators() : new ArrayList<>());
        
        // Handle conflicting indicators with null check
        model.setConflictingIndicators(entity.getConflictingIndicators() != null ? 
                entity.getConflictingIndicators() : new ArrayList<>());
    }
}
