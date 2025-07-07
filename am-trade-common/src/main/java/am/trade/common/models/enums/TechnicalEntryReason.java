package am.trade.common.models.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing technical analysis reasons for entering a trade
 * Supports both predefined values and custom user-defined values
 */
@Getter
public class TechnicalEntryReason {
    // Predefined common technical entry reasons
    public static final TechnicalEntryReason TREND_FOLLOWING = new TechnicalEntryReason("TREND_FOLLOWING", "Entry based on established price trend");
    public static final TechnicalEntryReason SUPPORT_BOUNCE = new TechnicalEntryReason("SUPPORT_BOUNCE", "Price bouncing off support level");
    public static final TechnicalEntryReason RESISTANCE_BREAKOUT = new TechnicalEntryReason("RESISTANCE_BREAKOUT", "Price breaking above resistance level");
    public static final TechnicalEntryReason MOVING_AVERAGE_CROSSOVER = new TechnicalEntryReason("MOVING_AVERAGE_CROSSOVER", "Crossover of moving averages");
    public static final TechnicalEntryReason OVERSOLD_RSI = new TechnicalEntryReason("OVERSOLD_RSI", "RSI indicating oversold conditions");
    public static final TechnicalEntryReason OVERBOUGHT_RSI = new TechnicalEntryReason("OVERBOUGHT_RSI", "RSI indicating overbought conditions");
    public static final TechnicalEntryReason MACD_SIGNAL = new TechnicalEntryReason("MACD_SIGNAL", "MACD signal line crossover");
    public static final TechnicalEntryReason BOLLINGER_BAND_TOUCH = new TechnicalEntryReason("BOLLINGER_BAND_TOUCH", "Price touching Bollinger Band");
    public static final TechnicalEntryReason VOLUME_SPIKE = new TechnicalEntryReason("VOLUME_SPIKE", "Unusual increase in trading volume");
    public static final TechnicalEntryReason CHART_PATTERN = new TechnicalEntryReason("CHART_PATTERN", "Recognized chart pattern formation");
    public static final TechnicalEntryReason FIBONACCI_LEVEL = new TechnicalEntryReason("FIBONACCI_LEVEL", "Price at key Fibonacci retracement/extension level");
    public static final TechnicalEntryReason DIVERGENCE = new TechnicalEntryReason("DIVERGENCE", "Price and indicator moving in opposite directions");
    
    private static final Map<String, TechnicalEntryReason> VALUES = new HashMap<>();
    
    static {
        Arrays.asList(
            TREND_FOLLOWING, SUPPORT_BOUNCE, RESISTANCE_BREAKOUT, MOVING_AVERAGE_CROSSOVER,
            OVERSOLD_RSI, OVERBOUGHT_RSI, MACD_SIGNAL, BOLLINGER_BAND_TOUCH,
            VOLUME_SPIKE, CHART_PATTERN, FIBONACCI_LEVEL, DIVERGENCE
        ).forEach(value -> VALUES.put(value.getCode(), value));
    }
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined enum values
     */
    private TechnicalEntryReason(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Factory method to get an existing technical entry reason by code or create a new custom one
     * 
     * @param code The code for the technical entry reason
     * @param description Optional description for new custom values
     * @return A TechnicalEntryReason instance
     */
    public static TechnicalEntryReason fromCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Technical entry reason code cannot be null or empty");
        }
        
        // Return existing value if available
        TechnicalEntryReason existing = VALUES.get(code);
        if (existing != null) {
            return existing;
        }
        
        // Create new custom value
        TechnicalEntryReason custom = new TechnicalEntryReason(code, description != null ? description : code);
        VALUES.put(code, custom);
        return custom;
    }
    
    /**
     * Get an existing technical entry reason by code
     * 
     * @param code The code for the technical entry reason
     * @return A TechnicalEntryReason instance or null if not found
     */
    public static TechnicalEntryReason fromCode(String code) {
        return VALUES.get(code);
    }
    
    /**
     * Get all available technical entry reason values
     * 
     * @return Array of all values
     */
    public static TechnicalEntryReason[] values() {
        return VALUES.values().toArray(new TechnicalEntryReason[0]);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TechnicalEntryReason that = (TechnicalEntryReason) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
