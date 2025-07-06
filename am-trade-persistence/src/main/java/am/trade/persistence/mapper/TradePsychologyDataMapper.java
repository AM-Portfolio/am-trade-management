package am.trade.persistence.mapper;

import am.trade.common.models.TradePsychologyData;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.persistence.entity.PsychologyFactorEntity;
import am.trade.persistence.entity.TradePsychologyDataEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mapper for converting between TradePsychologyData model and TradePsychologyDataEntity
 */
@Component
public class TradePsychologyDataMapper {

    private static final Logger logger = Logger.getLogger(TradePsychologyDataMapper.class.getName());

    /**
     * Convert from model to entity
     * @param model The TradePsychologyData model
     * @return The TradePsychologyDataEntity
     */
    public TradePsychologyDataEntity toEntity(TradePsychologyData model) {
        if (model == null) {
            return new TradePsychologyDataEntity();
        }

        TradePsychologyDataEntity entity = new TradePsychologyDataEntity();
        
        processEntryPsychologyFactors(model, entity);
        processExitPsychologyFactors(model, entity);
        processBehaviorPatterns(model, entity);
        entity.setPsychologyNotes(model.getPsychologyNotes());
        
        return entity;
    }
    
    /**
     * Process behavior patterns from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void processBehaviorPatterns(TradePsychologyData model, TradePsychologyDataEntity entity) {
        if (model.getBehaviorPatterns() != null) {
            List<String> behaviorPatternCodes = new ArrayList<>();
            List<PsychologyFactorEntity> customBehaviorPatterns = new ArrayList<>();
            
            for (TradeBehaviorPattern pattern : model.getBehaviorPatterns()) {
                processBehaviorPattern(pattern, behaviorPatternCodes, customBehaviorPatterns);
            }
            
            entity.setBehaviorPatterns(behaviorPatternCodes);
            entity.setCustomBehaviorPatterns(customBehaviorPatterns);
        } else {
            entity.setBehaviorPatterns(Collections.emptyList());
            entity.setCustomBehaviorPatterns(Collections.emptyList());
        }
    }
    
    /**
     * Process a single behavior pattern
     * @param pattern The pattern to process
     * @param codes List to add the code to
     * @param customPatterns List to add custom patterns to
     */
    private void processBehaviorPattern(TradeBehaviorPattern pattern, List<String> codes, List<PsychologyFactorEntity> customPatterns) {
        if (pattern != null) {
            codes.add(pattern.getCode());
            
            // Store custom behavior patterns with descriptions
            // Check if it's not one of the predefined patterns by trying to get it from VALUES map
            TradeBehaviorPattern predefined = TradeBehaviorPattern.fromCode(pattern.getCode());
            if (predefined == null || !predefined.equals(pattern)) {
                PsychologyFactorEntity customPattern = new PsychologyFactorEntity();
                customPattern.setCode(pattern.getCode());
                customPattern.setDescription(pattern.getDescription());
                customPatterns.add(customPattern);
            }
        }
    }
    
    /**
     * Process entry psychology factors from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void processEntryPsychologyFactors(TradePsychologyData model, TradePsychologyDataEntity entity) {
        if (model.getEntryPsychologyFactors() != null) {
            List<String> entryFactorCodes = new ArrayList<>();
            List<PsychologyFactorEntity> customEntryFactors = new ArrayList<>();
            
            for (EntryPsychology factor : model.getEntryPsychologyFactors()) {
                processEntryPsychologyFactor(factor, entryFactorCodes, customEntryFactors);
            }
            
            entity.setEntryPsychologyFactors(entryFactorCodes);
            entity.setCustomEntryFactors(customEntryFactors);
        } else {
            entity.setEntryPsychologyFactors(Collections.emptyList());
            entity.setCustomEntryFactors(Collections.emptyList());
        }
    }
    
    /**
     * Process a single entry psychology factor
     * @param factor The factor to process
     * @param codes List to add the code to
     * @param customFactors List to add custom factors to
     */
    private void processEntryPsychologyFactor(
            EntryPsychology factor, 
            List<String> codes, 
            List<PsychologyFactorEntity> customFactors) {
        if (factor != null) {
            codes.add(factor.getCode());
            
            if (!EntryPsychology.isStandardCode(factor.getCode())) {
                customFactors.add(new PsychologyFactorEntity(
                        factor.getCode(), 
                        factor.getDescription()));
            }
        }
    }
    
    /**
     * Process exit psychology factors from model to entity
     * @param model Source model
     * @param entity Target entity
     */
    private void processExitPsychologyFactors(TradePsychologyData model, TradePsychologyDataEntity entity) {
        if (model.getExitPsychologyFactors() != null) {
            List<String> exitFactorCodes = new ArrayList<>();
            List<PsychologyFactorEntity> customExitFactors = new ArrayList<>();
            
            for (ExitPsychology factor : model.getExitPsychologyFactors()) {
                processExitPsychologyFactor(factor, exitFactorCodes, customExitFactors);
            }
            
            entity.setExitPsychologyFactors(exitFactorCodes);
            entity.setCustomExitFactors(customExitFactors);
        } else {
            entity.setExitPsychologyFactors(Collections.emptyList());
            entity.setCustomExitFactors(Collections.emptyList());
        }
    }
    
    /**
     * Process a single exit psychology factor
     * @param factor The factor to process
     * @param codes List to add the code to
     * @param customFactors List to add custom factors to
     */
    private void processExitPsychologyFactor(
            ExitPsychology factor, 
            List<String> codes, 
            List<PsychologyFactorEntity> customFactors) {
        if (factor != null) {
            codes.add(factor.getCode());
            
            if (!ExitPsychology.isStandardCode(factor.getCode())) {
                customFactors.add(new PsychologyFactorEntity(
                        factor.getCode(), 
                        factor.getDescription()));
            }
        }
    }

    /**
     * Convert from entity to model
     * @param entity The TradePsychologyDataEntity
     * @return The TradePsychologyData model
     */
    public TradePsychologyData toModel(TradePsychologyDataEntity entity) {
        if (entity == null) {
            return null;
        }

        TradePsychologyData model = new TradePsychologyData();
        
        model.setEntryPsychologyFactors(processEntryPsychologyFactorCodes(entity));
        model.setExitPsychologyFactors(processExitPsychologyFactorCodes(entity));
        model.setBehaviorPatterns(processBehaviorPatternCodes(entity));
        model.setPsychologyNotes(entity.getPsychologyNotes());
        
        return model;
    }
    
    /**
     * Process entry psychology factor codes from entity
     * @param entity Source entity
     * @return List of entry psychology factors
     */
    private List<EntryPsychology> processEntryPsychologyFactorCodes(TradePsychologyDataEntity entity) {
        List<EntryPsychology> entryFactors = new ArrayList<>();
        if (entity.getEntryPsychologyFactors() == null) {
            return entryFactors;
        }
        
        List<?> rawFactors = entity.getEntryPsychologyFactors();
        for (Object codeObj : rawFactors) {
            if (codeObj instanceof String) {
                EntryPsychology factor = createEntryPsychologyFactor((String) codeObj, entity);
                if (factor != null) {
                    entryFactors.add(factor);
                }
            } else if (codeObj != null) {
                // Try to convert non-string objects to string
                try {
                    EntryPsychology factor = createEntryPsychologyFactor(codeObj.toString(), entity);
                    if (factor != null) {
                        entryFactors.add(factor);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not process non-string entry psychology factor: " + codeObj);
                }
            }
        }
        
        return entryFactors;
    }
    
    /**
     * Create an entry psychology factor from a code
     * @param code The code to create the factor from
     * @param entity Source entity for custom descriptions
     * @return The created entry psychology factor
     */
    private EntryPsychology createEntryPsychologyFactor(String code, TradePsychologyDataEntity entity) {
        if (code == null) {
            return null;
        }
        
        EntryPsychology factor = EntryPsychology.fromCode(code);
        if (factor != null) {
            return factor;
        }
        
        // Try to find a custom description
        String description = findCustomEntryDescription(code, entity);
        return EntryPsychology.fromCode(code, description);
    }
    
    /**
     * Find a custom description for an entry psychology factor
     * @param code The code to find a description for
     * @param entity The source entity
     * @return The description or a default one
     */
    private String findCustomEntryDescription(String code, TradePsychologyDataEntity entity) {
        if (entity.getCustomEntryFactors() == null) {
            return "Custom entry psychology: " + code;
        }
        
        return entity.getCustomEntryFactors().stream()
                .filter(custom -> custom != null && code.equals(custom.getCode()))
                .map(PsychologyFactorEntity::getDescription)
                .findFirst()
                .orElse("Custom entry psychology: " + code);
    }
    
    /**
     * Process exit psychology factor codes from entity
     * @param entity Source entity
     * @return List of exit psychology factors
     */
    private List<ExitPsychology> processExitPsychologyFactorCodes(TradePsychologyDataEntity entity) {
        List<ExitPsychology> exitFactors = new ArrayList<>();
        if (entity.getExitPsychologyFactors() == null) {
            return exitFactors;
        }
        
        List<?> rawFactors = entity.getExitPsychologyFactors();
        for (Object codeObj : rawFactors) {
            if (codeObj instanceof String) {
                ExitPsychology factor = createExitPsychologyFactor((String) codeObj, entity);
                if (factor != null) {
                    exitFactors.add(factor);
                }
            } else if (codeObj != null) {
                // Try to convert non-string objects to string
                try {
                    ExitPsychology factor = createExitPsychologyFactor(codeObj.toString(), entity);
                    if (factor != null) {
                        exitFactors.add(factor);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not process non-string exit psychology factor: " + codeObj);
                }
            }
        }
        
        return exitFactors;
    }
    
    /**
     * Create an exit psychology factor from a code
     * @param code The code to create the factor from
     * @param entity Source entity for custom descriptions
     * @return The created exit psychology factor
     */
    private ExitPsychology createExitPsychologyFactor(String code, TradePsychologyDataEntity entity) {
        if (code == null) {
            return null;
        }
        
        ExitPsychology factor = ExitPsychology.fromCode(code);
        if (factor != null) {
            return factor;
        }
        
        // Try to find a custom description
        String description = findCustomExitDescription(code, entity);
        return ExitPsychology.fromCode(code, description);
    }
    
    /**
     * Find a custom description for an exit psychology factor
     * @param code The code to find a description for
     * @param entity The source entity
     * @return The description or a default one
     */
    private String findCustomExitDescription(String code, TradePsychologyDataEntity entity) {
        if (entity.getCustomExitFactors() == null) {
            return "Custom exit psychology: " + code;
        }
        
        return entity.getCustomExitFactors().stream()
                .filter(custom -> custom != null && code.equals(custom.getCode()))
                .map(PsychologyFactorEntity::getDescription)
                .findFirst()
                .orElse("Custom exit psychology: " + code);
    }
    
    /**
     * Process behavior pattern codes from entity
     * @param entity Source entity
     * @return List of behavior patterns
     */
    private List<TradeBehaviorPattern> processBehaviorPatternCodes(TradePsychologyDataEntity entity) {
        List<TradeBehaviorPattern> behaviorPatterns = new ArrayList<>();
        if (entity.getBehaviorPatterns() == null) {
            return behaviorPatterns;
        }
        
        List<?> rawPatterns = entity.getBehaviorPatterns();
        for (Object codeObj : rawPatterns) {
            if (codeObj instanceof String) {
                TradeBehaviorPattern pattern = createBehaviorPattern((String) codeObj, entity);
                if (pattern != null) {
                    behaviorPatterns.add(pattern);
                }
            } else if (codeObj != null) {
                // Try to convert non-string objects to string
                try {
                    TradeBehaviorPattern pattern = createBehaviorPattern(codeObj.toString(), entity);
                    if (pattern != null) {
                        behaviorPatterns.add(pattern);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Could not process non-string behavior pattern: " + codeObj);
                }
            }
        }
        
        return behaviorPatterns;
    }
    
    /**
     * Create a behavior pattern from a code
     * @param code The code to create from
     * @param entity The source entity
     * @return The created behavior pattern
     */
    private TradeBehaviorPattern createBehaviorPattern(String code, TradePsychologyDataEntity entity) {
        // Try to get existing pattern
        TradeBehaviorPattern pattern = TradeBehaviorPattern.fromCode(code);
        if (pattern != null) {
            return pattern;
        }
        
        // Try to find a custom description
        String description = findCustomBehaviorPatternDescription(code, entity);
        return TradeBehaviorPattern.fromCode(code, description);
    }
    
    /**
     * Find a custom description for a behavior pattern
     * @param code The code to find a description for
     * @param entity The source entity
     * @return The description or a default one
     */
    private String findCustomBehaviorPatternDescription(String code, TradePsychologyDataEntity entity) {
        if (entity.getCustomBehaviorPatterns() == null) {
            return "Custom behavior pattern: " + code;
        }
        
        return entity.getCustomBehaviorPatterns().stream()
                .filter(custom -> custom != null && code.equals(custom.getCode()))
                .map(PsychologyFactorEntity::getDescription)
                .findFirst()
                .orElse("Custom behavior pattern: " + code);
    }
}
