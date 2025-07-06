package am.trade.persistence.mapper;

import am.trade.common.models.TradePsychologyData;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.persistence.entity.PsychologyFactorEntity;
import am.trade.persistence.entity.TradePsychologyDataEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Mapper for converting between TradePsychologyData model and TradePsychologyDataEntity
 */
@Component
public class TradePsychologyDataMapper {

    private static final String PROCESS_ID = UUID.randomUUID().toString();
    private static final Logger logger = Logger.getLogger(TradePsychologyDataMapper.class.getName());

    /**
     * Convert from model to entity
     * @param model The TradePsychologyData model
     * @return The TradePsychologyDataEntity
     */
    public TradePsychologyDataEntity toEntity(TradePsychologyData model) {
        if (model == null) {
            return null;
        }

        try {
            TradePsychologyDataEntity entity = new TradePsychologyDataEntity();
            
            // Convert entry psychology factors to string codes with error handling
            if (model.getEntryPsychologyFactors() != null) {
                List<String> entryFactorCodes = new ArrayList<>();
                List<PsychologyFactorEntity> customEntryFactors = new ArrayList<>();
                
                for (EntryPsychology factor : model.getEntryPsychologyFactors()) {
                    try {
                        if (factor != null) {
                            entryFactorCodes.add(factor.getCode());
                            
                            // Extract custom entry factors
                            if (!EntryPsychology.isStandardCode(factor.getCode())) {
                                customEntryFactors.add(new PsychologyFactorEntity(factor.getCode(), factor.getDescription()));
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing entry psychology factor: " + e.getMessage());
                    }
                }
                
                entity.setEntryPsychologyFactors(entryFactorCodes);
                entity.setCustomEntryFactors(customEntryFactors);
            } else {
                entity.setEntryPsychologyFactors(Collections.emptyList());
                entity.setCustomEntryFactors(Collections.emptyList());
            }
            
            // Convert exit psychology factors to string codes with error handling
            if (model.getExitPsychologyFactors() != null) {
                List<String> exitFactorCodes = new ArrayList<>();
                List<PsychologyFactorEntity> customExitFactors = new ArrayList<>();
                
                for (ExitPsychology factor : model.getExitPsychologyFactors()) {
                    try {
                        if (factor != null) {
                            exitFactorCodes.add(factor.getCode());
                            
                            // Extract custom exit factors
                            if (!ExitPsychology.isStandardCode(factor.getCode())) {
                                customExitFactors.add(new PsychologyFactorEntity(factor.getCode(), factor.getDescription()));
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing exit psychology factor: " + e.getMessage());
                    }
                }
                
                entity.setExitPsychologyFactors(exitFactorCodes);
                entity.setCustomExitFactors(customExitFactors);
            } else {
                entity.setExitPsychologyFactors(Collections.emptyList());
                entity.setCustomExitFactors(Collections.emptyList());
            }
            
            entity.setPsychologyNotes(model.getPsychologyNotes());
            
            return entity;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to convert TradePsychologyData model to entity: " + e.getMessage(), e);
            return new TradePsychologyDataEntity(); // Return empty entity instead of null
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

        try {
            TradePsychologyData model = new TradePsychologyData();
            
            // Convert entry psychology factor codes to objects with error handling
            List<EntryPsychology> entryFactors = new ArrayList<>();
            if (entity.getEntryPsychologyFactors() != null) {
                for (String code : entity.getEntryPsychologyFactors()) {
                    try {
                        if (code != null) {
                            EntryPsychology factor = EntryPsychology.fromCode(code);
                            if (factor == null && entity.getCustomEntryFactors() != null) {
                                String description = entity.getCustomEntryFactors().stream()
                                        .filter(custom -> custom != null && code.equals(custom.getCode()))
                                        .map(PsychologyFactorEntity::getDescription)
                                        .findFirst()
                                        .orElse("Custom entry psychology: " + code);
                                factor = EntryPsychology.fromCode(code, description);
                            }
                            if (factor == null) {
                                factor = EntryPsychology.fromCode(code, "Custom entry psychology: " + code);
                            }
                            if (factor != null) {
                                entryFactors.add(factor);
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing entry psychology code: " + code + ", " + e.getMessage());
                    }
                }
            }
            model.setEntryPsychologyFactors(entryFactors);
            
            // Convert exit psychology factor codes to objects with error handling
            List<ExitPsychology> exitFactors = new ArrayList<>();
            if (entity.getExitPsychologyFactors() != null) {
                for (String code : entity.getExitPsychologyFactors()) {
                    try {
                        if (code != null) {
                            ExitPsychology factor = ExitPsychology.fromCode(code);
                            if (factor == null && entity.getCustomExitFactors() != null) {
                                String description = entity.getCustomExitFactors().stream()
                                        .filter(custom -> custom != null && code.equals(custom.getCode()))
                                        .map(PsychologyFactorEntity::getDescription)
                                        .findFirst()
                                        .orElse("Custom exit psychology: " + code);
                                factor = ExitPsychology.fromCode(code, description);
                            }
                            if (factor == null) {
                                factor = ExitPsychology.fromCode(code, "Custom exit psychology: " + code);
                            }
                            if (factor != null) {
                                exitFactors.add(factor);
                            }
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Error processing exit psychology code: " + code + ", " + e.getMessage());
                    }
                }
            }
            model.setExitPsychologyFactors(exitFactors);
            
            model.setPsychologyNotes(entity.getPsychologyNotes());
            
            return model;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to convert TradePsychologyDataEntity to model: " + e.getMessage(), e);
            return new TradePsychologyData(); // Return empty model instead of null
        }
    }
}
