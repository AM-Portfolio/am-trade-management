package am.trade.persistence.mapper;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradePsychologyData;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.util.JsonConverter;
import am.trade.persistence.entity.PsychologyFactorEntity;
import am.trade.persistence.entity.TradePsychologyDataEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradePsychologyDataMapperTest {

    private TradePsychologyDataMapper mapper;
    private TradeDetails tradeDetails;
    private JsonConverter jsonConverter;
    private static final String TEST_RESOURCE_PATH = "tradedetails.json";
    
    @BeforeEach
    void setUp() {
        mapper = new TradePsychologyDataMapper();
        jsonConverter = new JsonConverter();
        tradeDetails = jsonConverter.fromJsonResource(TEST_RESOURCE_PATH, TradeDetails.class);
    }
    
    @Test
    void testToEntityWithValidModel() {
        // Create a model with valid data
        TradePsychologyData model = tradeDetails.getPsychologyData();
        
        // Convert to entity
        TradePsychologyDataEntity entity = mapper.toEntity(model);
        
        // Verify conversion
        assertNotNull(entity);
        assertEquals(3, entity.getEntryPsychologyFactors().size());
        assertTrue(entity.getEntryPsychologyFactors().contains("FOLLOWING_THE_PLAN"));
        assertTrue(entity.getEntryPsychologyFactors().contains("INTUITION"));
        assertTrue(entity.getEntryPsychologyFactors().contains("OVERCONFIDENCE"));
        
        assertEquals(2, entity.getExitPsychologyFactors().size());
        assertTrue(entity.getExitPsychologyFactors().contains("DISCIPLINE"));
        assertTrue(entity.getExitPsychologyFactors().contains("TAKING_PROFITS"));

        assertEquals(2, entity.getBehaviorPatterns().size());
        assertTrue(entity.getBehaviorPatterns().contains("DISCIPLINED_EXECUTION"));
        assertTrue(entity.getBehaviorPatterns().contains("POSITION_SIZING_ISSUES"));
        
        assertEquals("Felt confident in the setup and maintained discipline throughout the trade. No emotional decision-making.", entity.getPsychologyNotes());
    }
    
    @Test
    void testToModelWithValidEntity() {
        // Create an entity with valid data
        TradePsychologyDataEntity entity = new TradePsychologyDataEntity();
        entity.setEntryPsychologyFactors(Arrays.asList("CONFIDENCE", "DISCIPLINE", "CUSTOM_ENTRY"));
        entity.setExitPsychologyFactors(Arrays.asList("SATISFACTION", "OBJECTIVITY", "CUSTOM_EXIT"));
        entity.setPsychologyNotes("Felt confident in the setup and maintained discipline throughout the trade. No emotional decision-making.");
        
        // Add custom factors
        PsychologyFactorEntity customEntry = new PsychologyFactorEntity();
        customEntry.setCode("CUSTOM_ENTRY");
        customEntry.setDescription("Custom Entry Factor");
        
        PsychologyFactorEntity customExit = new PsychologyFactorEntity();
        customExit.setCode("CUSTOM_EXIT");
        customExit.setDescription("Custom Exit Factor");
        
        entity.setCustomEntryFactors(Collections.singletonList(customEntry));
        entity.setCustomExitFactors(Collections.singletonList(customExit));
        
        // Convert to model
        TradePsychologyData model = mapper.toModel(entity);
        
        // Verify conversion
        assertNotNull(model);
        assertEquals(3, model.getEntryPsychologyFactors().size());
        assertTrue(model.getEntryPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("CONFIDENCE")));
        assertTrue(model.getEntryPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("DISCIPLINE")));
        assertTrue(model.getEntryPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("CUSTOM_ENTRY")));
        
        assertEquals(3, model.getExitPsychologyFactors().size());
        assertTrue(model.getExitPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("SATISFACTION")));
        assertTrue(model.getExitPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("OBJECTIVITY")));
        assertTrue(model.getExitPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("CUSTOM_EXIT")));
        
        assertEquals("Felt confident in the setup and maintained discipline throughout the trade. No emotional decision-making.", model.getPsychologyNotes());
        
        // Verify custom factors have descriptions
        EntryPsychology customEntryFactor = model.getEntryPsychologyFactors().stream()
                .filter(factor -> factor.getCode().equals("CUSTOM_ENTRY"))
                .findFirst()
                .orElse(null);
        assertNotNull(customEntryFactor);
        assertEquals("Custom Entry Factor", customEntryFactor.getDescription());
        
        ExitPsychology customExitFactor = model.getExitPsychologyFactors().stream()
                .filter(factor -> factor.getCode().equals("CUSTOM_EXIT"))
                .findFirst()
                .orElse(null);
        assertNotNull(customExitFactor);
        assertEquals("Custom Exit Factor", customExitFactor.getDescription());
    }
    
    @Test
    void testHandleNonStringObjectInList() {
        // Create entity with a mix of String and Object values (simulating MongoDB behavior)
        TradePsychologyDataEntity entity = new TradePsychologyDataEntity();
        
        // Use raw Lists to bypass type checking
        List<Object> entryFactors = Arrays.asList("CONFIDENCE", 123, new Object());
        List<Object> exitFactors = Arrays.asList("SATISFACTION", true, null);
        List<Object> behaviorPatterns = Arrays.asList("DISCIPLINED_EXECUTION", false, new HashMap<>());
        
        // Set these lists using reflection to bypass type safety
        try {
            java.lang.reflect.Field entryField = TradePsychologyDataEntity.class.getDeclaredField("entryPsychologyFactors");
            entryField.setAccessible(true);
            entryField.set(entity, entryFactors);
            
            java.lang.reflect.Field exitField = TradePsychologyDataEntity.class.getDeclaredField("exitPsychologyFactors");
            exitField.setAccessible(true);
            exitField.set(entity, exitFactors);
            
            java.lang.reflect.Field behaviorField = TradePsychologyDataEntity.class.getDeclaredField("behaviorPatterns");
            behaviorField.setAccessible(true);
            behaviorField.set(entity, behaviorPatterns);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        // Convert to model - should not throw exceptions
        TradePsychologyData model = mapper.toModel(entity);
        
        // Verify valid values were converted
        assertTrue(model.getEntryPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("CONFIDENCE")));
        assertTrue(model.getExitPsychologyFactors().stream()
                .anyMatch(factor -> factor.getCode().equals("SATISFACTION")));
        assertTrue(model.getBehaviorPatterns().stream()
                .anyMatch(pattern -> pattern.getCode().equals("DISCIPLINED_EXECUTION")));
        
        // Non-string objects are attempted to be converted to strings
        // The mapper tries to convert all objects to strings, so we need to check how many were successfully converted
        // For entry factors: "CONFIDENCE" and 123.toString() should be processed, but new Object() might fail
        // For exit factors: "SATISFACTION" should be processed, true.toString() might be processed, null is skipped
        // For behavior patterns: "DISCIPLINED_EXECUTION" should be processed, false.toString() might be processed, HashMap might fail
        
        // We should verify that at least the string values were processed correctly
        assertTrue(model.getEntryPsychologyFactors().size() >= 1);
        assertTrue(model.getExitPsychologyFactors().size() >= 1);
        assertTrue(model.getBehaviorPatterns().size() >= 1);
    }
}
