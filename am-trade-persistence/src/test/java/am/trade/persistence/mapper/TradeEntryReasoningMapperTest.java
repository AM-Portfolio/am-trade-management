package am.trade.persistence.mapper;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeEntryExistReasoning;
import am.trade.common.util.JsonConverter;
import am.trade.persistence.entity.TradeEntryExistReasoningEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TradeEntryReasoningMapperTest {

    private TradeEntryReasoningMapper mapper;
    private TradeDetails tradeDetails;
    private JsonConverter jsonConverter;
    private static final String TEST_RESOURCE_PATH = "tradedetails.json";
    
    @BeforeEach
    void setUp() {
        mapper = new TradeEntryReasoningMapper();
        jsonConverter = new JsonConverter();
        tradeDetails = jsonConverter.fromJsonResource(TEST_RESOURCE_PATH, TradeDetails.class);
    }
    
    @Test
    void testToEntityWithValidModel() {
        // Convert to entity
        TradeEntryExistReasoningEntity entity = mapper.toEntity(tradeDetails.getEntryReasoning());
        
        // Verify conversion
        assertNotNull(entity);
        assertEquals(3, entity.getTechnicalReasons().size());
        assertTrue(entity.getTechnicalReasons().contains("SUPPORT_BOUNCE"));
        assertTrue(entity.getTechnicalReasons().contains("MOVING_AVERAGE_CROSSOVER"));
        
        assertEquals(3, entity.getFundamentalReasons().size());
        assertTrue(entity.getFundamentalReasons().contains("ANALYST_UPGRADE"));
        assertTrue(entity.getFundamentalReasons().contains("EARNINGS_BEAT"));
        
        assertEquals(7, entity.getConfidenceLevel());
        assertEquals(3, entity.getSupportingIndicators().size());
        assertEquals(1, entity.getConflictingIndicators().size());
    }
    
    @Test
    void testToModelWithValidEntity() {
        // Create an entity with valid data
        TradeEntryExistReasoningEntity entity = new TradeEntryExistReasoningEntity();
        entity.setTechnicalReasons(Arrays.asList("SUPPORT_BOUNCE", "MOVING_AVERAGE_CROSSOVER"));
        entity.setFundamentalReasons(Arrays.asList("ANALYST_UPGRADE", "EARNINGS_BEAT"));
        entity.setPrimaryReason("TECHNICAL");
        entity.setReasoningSummary("Test reasoning summary");
        entity.setConfidenceLevel(8);
        entity.setSupportingIndicators(Arrays.asList("RSI oversold", "MACD crossover"));
        entity.setConflictingIndicators(Collections.singletonList("Market uncertainty"));
        
        // Convert to model
        TradeEntryExistReasoning model = mapper.toModel(entity);
        
        // Verify conversion
        assertNotNull(model);
        assertEquals(2, model.getTechnicalReasons().size());
        assertTrue(model.getTechnicalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("SUPPORT_BOUNCE")));
        assertTrue(model.getTechnicalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("MOVING_AVERAGE_CROSSOVER")));
        
        assertEquals(2, model.getFundamentalReasons().size());
        assertTrue(model.getFundamentalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("ANALYST_UPGRADE")));
        assertTrue(model.getFundamentalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("EARNINGS_BEAT")));
        
        assertEquals("TECHNICAL", model.getPrimaryReason());
        assertEquals("Test reasoning summary", model.getReasoningSummary());
        assertEquals(8, model.getConfidenceLevel());
        assertEquals(2, model.getSupportingIndicators().size());
        assertEquals(1, model.getConflictingIndicators().size());
    }
    
    // @Test
    // void testHandleNullModel() {
    //     // Test with null model
    //     TradeEntryReasoningEntity entity = mapper.toEntity(null);
        
    //     // Should return empty entity, not null
    //     assertNotNull(entity);
    //     assertTrue(entity.getTechnicalReasons().isEmpty());
    //     assertTrue(entity.getFundamentalReasons().isEmpty());
    // }
    
    // @Test
    // void testHandleNullEntity() {
    //     // Test with null entity
    //     TradeEntryReasoning model = mapper.toModel(null);
        
    //     // Should return empty model, not null
    //     assertNotNull(model);
    //     assertTrue(model.getTechnicalReasons().isEmpty());
    //     assertTrue(model.getFundamentalReasons().isEmpty());
    // }
    
    @Test
    void testHandleCustomEnumCodes() {
        // Create entity with custom codes
        TradeEntryExistReasoningEntity entity = new TradeEntryExistReasoningEntity();
        entity.setTechnicalReasons(Arrays.asList("SUPPORT_BOUNCE", "CUSTOM_TECH_REASON"));
        entity.setFundamentalReasons(Arrays.asList("ANALYST_UPGRADE", "CUSTOM_FUND_REASON"));
        
        // Convert to model
        TradeEntryExistReasoning model = mapper.toModel(entity);
        
        // Verify standard enums are converted correctly
        assertTrue(model.getTechnicalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("SUPPORT_BOUNCE")));
        assertTrue(model.getFundamentalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("ANALYST_UPGRADE")));
        
        // Verify custom enums are created
        assertTrue(model.getTechnicalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("CUSTOM_TECH_REASON")));
        assertTrue(model.getFundamentalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("CUSTOM_FUND_REASON")));
    }
    
    @Test
    void testHandleNonStringObjectInList() {
        // Create entity with a mix of String and Object values (simulating MongoDB behavior)
        TradeEntryExistReasoningEntity entity = new TradeEntryExistReasoningEntity();
        
        // Use raw Lists to bypass type checking
        List<Object> technicalReasons = Arrays.asList("SUPPORT_BOUNCE", 123, new Object());
        List<Object> fundamentalReasons = Arrays.asList("ANALYST_UPGRADE", true, null);
        
        // Set these lists using reflection to bypass type safety
        try {
            java.lang.reflect.Field techField = TradeEntryExistReasoningEntity.class.getDeclaredField("technicalReasons");
            techField.setAccessible(true);
            techField.set(entity, technicalReasons);
            
            java.lang.reflect.Field fundField = TradeEntryExistReasoningEntity.class.getDeclaredField("fundamentalReasons");
            fundField.setAccessible(true);
            fundField.set(entity, fundamentalReasons);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }
        
        // Convert to model - should not throw exceptions
        TradeEntryExistReasoning model = mapper.toModel(entity);
        
        // Verify valid values were converted
        assertTrue(model.getTechnicalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("SUPPORT_BOUNCE")));
        assertTrue(model.getFundamentalReasons().stream()
                .anyMatch(reason -> reason.getCode().equals("ANALYST_UPGRADE")));
        
        // Other values should be skipped without errors
        assertEquals(3, model.getTechnicalReasons().size());
        assertEquals(2, model.getFundamentalReasons().size());
    }
}
