package am.trade.common.models.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing categories for trade tags
 * Supports both predefined values and custom user-defined values
 */
@Getter
public class TradeTagCategories {
    // Predefined common trade tag categories
    public static final TradeTagCategories STRATEGY = new TradeTagCategories("STRATEGY", "Trading strategy used");
    public static final TradeTagCategories MARKET_CONDITION = new TradeTagCategories("MARKET_CONDITION", "Market conditions during trade");
    public static final TradeTagCategories SETUP_TYPE = new TradeTagCategories("SETUP_TYPE", "Type of trade setup");
    public static final TradeTagCategories TIME_FRAME = new TradeTagCategories("TIME_FRAME", "Time frame of analysis");
    public static final TradeTagCategories RISK_LEVEL = new TradeTagCategories("RISK_LEVEL", "Level of risk taken");
    public static final TradeTagCategories SECTOR = new TradeTagCategories("SECTOR", "Market sector");
    public static final TradeTagCategories TECHNICAL_PATTERN = new TradeTagCategories("TECHNICAL_PATTERN", "Technical analysis pattern");
    public static final TradeTagCategories FUNDAMENTAL_FACTOR = new TradeTagCategories("FUNDAMENTAL_FACTOR", "Fundamental analysis factor");
    public static final TradeTagCategories SENTIMENT = new TradeTagCategories("SENTIMENT", "Market sentiment");
    public static final TradeTagCategories PERFORMANCE = new TradeTagCategories("PERFORMANCE", "Trade performance category");
    
    private static final Map<String, TradeTagCategories> VALUES = new HashMap<>();
    
    static {
        Arrays.asList(
            STRATEGY, MARKET_CONDITION, SETUP_TYPE, TIME_FRAME, RISK_LEVEL,
            SECTOR, TECHNICAL_PATTERN, FUNDAMENTAL_FACTOR, SENTIMENT, PERFORMANCE
        ).forEach(value -> VALUES.put(value.getCode(), value));
    }
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined enum values
     */
    private TradeTagCategories(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Factory method to get an existing tag category by code or create a new custom one
     * 
     * @param code The code for the tag category
     * @param description Optional description for new custom values
     * @return A TradeTagCategories instance
     */
    public static TradeTagCategories fromCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Trade tag category code cannot be null or empty");
        }
        
        // Return existing value if available
        TradeTagCategories existing = VALUES.get(code);
        if (existing != null) {
            return existing;
        }
        
        // Create new custom value
        TradeTagCategories custom = new TradeTagCategories(code, description != null ? description : code);
        VALUES.put(code, custom);
        return custom;
    }
    
    /**
     * Get an existing tag category by code
     * 
     * @param code The code for the tag category
     * @return A TradeTagCategories instance or null if not found
     */
    public static TradeTagCategories fromCode(String code) {
        return VALUES.get(code);
    }
    
    /**
     * Get all available tag category values
     * 
     * @return Array of all values
     */
    public static TradeTagCategories[] values() {
        return VALUES.values().toArray(new TradeTagCategories[0]);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TradeTagCategories that = (TradeTagCategories) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
