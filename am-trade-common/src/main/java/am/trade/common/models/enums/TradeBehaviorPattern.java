package am.trade.common.models.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing common trading behavior patterns
 * Supports both predefined values and custom user-defined values
 */
@Getter
@Schema(
    description = "Trading behavior pattern observed during the trade. Supports both predefined and custom values.",
    example = "DISCIPLINED_EXECUTION",
    allowableValues = {
        "OVERTRADING", "HESITATION", "AVERAGING_DOWN", 
        "CUTTING_WINNERS_SHORT", "HOLDING_LOSERS", "CHASING_MOMENTUM",
        "POSITION_SIZING_ISSUES", "REVENGE_TRADING", "DISCIPLINED_EXECUTION"
    }
)
public class TradeBehaviorPattern {
    // Predefined common trading behavior patterns
    @Schema(description = "Trading too frequently")
    public static final TradeBehaviorPattern OVERTRADING = new TradeBehaviorPattern("OVERTRADING", "Trading too frequently");
    
    @Schema(description = "Delayed decision making")
    public static final TradeBehaviorPattern HESITATION = new TradeBehaviorPattern("HESITATION", "Delayed decision making");
    
    @Schema(description = "Adding to losing positions")
    public static final TradeBehaviorPattern AVERAGING_DOWN = new TradeBehaviorPattern("AVERAGING_DOWN", "Adding to losing positions");
    
    @Schema(description = "Exiting profitable trades too early")
    public static final TradeBehaviorPattern CUTTING_WINNERS_SHORT = new TradeBehaviorPattern("CUTTING_WINNERS_SHORT", "Exiting profitable trades too early");
    
    @Schema(description = "Keeping losing positions too long")
    public static final TradeBehaviorPattern HOLDING_LOSERS = new TradeBehaviorPattern("HOLDING_LOSERS", "Keeping losing positions too long");
    
    @Schema(description = "Entering after significant price movement")
    public static final TradeBehaviorPattern CHASING_MOMENTUM = new TradeBehaviorPattern("CHASING_MOMENTUM", "Entering after significant price movement");
    
    @Schema(description = "Inconsistent or improper position sizing")
    public static final TradeBehaviorPattern POSITION_SIZING_ISSUES = new TradeBehaviorPattern("POSITION_SIZING_ISSUES", "Inconsistent or improper position sizing");
    
    @Schema(description = "Trading to recover losses")
    public static final TradeBehaviorPattern REVENGE_TRADING = new TradeBehaviorPattern("REVENGE_TRADING", "Trading to recover losses");
    
    @Schema(description = "Following trading plan consistently")
    public static final TradeBehaviorPattern DISCIPLINED_EXECUTION = new TradeBehaviorPattern("DISCIPLINED_EXECUTION", "Following trading plan consistently");
    
    private static final Map<String, TradeBehaviorPattern> VALUES = new HashMap<>();
    
    static {
        Arrays.asList(
            OVERTRADING, HESITATION, AVERAGING_DOWN, CUTTING_WINNERS_SHORT, 
            HOLDING_LOSERS, CHASING_MOMENTUM, POSITION_SIZING_ISSUES, 
            REVENGE_TRADING, DISCIPLINED_EXECUTION
        ).forEach(value -> VALUES.put(value.getCode(), value));
    }
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined enum values
     */
    private TradeBehaviorPattern(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * JSON serialization - returns object with code and description
     * This ensures proper JSON structure when serializing to MongoDB or REST responses
     */
    @JsonValue
    public Map<String, String> toJson() {
        Map<String, String> json = new HashMap<>();
        json.put("code", code);
        json.put("description", description);
        return json;
    }
    
    /**
     * JSON deserialization constructor - handles both string code and object with code/description
     * 
     * @param code The code for the behavior pattern
     * @param description Optional description for new custom values
     */
    @JsonCreator
    public static TradeBehaviorPattern fromJson(
            @JsonProperty("code") String code, 
            @JsonProperty("description") String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Trade behavior pattern code cannot be null or empty");
        }
        return fromCode(code, description);
    }
    
    /**
     * Factory method to get an existing behavior pattern by code or create a new custom one
     * 
     * @param code The code for the behavior pattern
     * @param description Optional description for new custom values
     * @return A TradeBehaviorPattern instance
     */
    public static TradeBehaviorPattern fromCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Trade behavior pattern code cannot be null or empty");
        }
        
        // Return existing value if available
        TradeBehaviorPattern existing = VALUES.get(code);
        if (existing != null) {
            return existing;
        }
        
        // Create new custom value
        TradeBehaviorPattern custom = new TradeBehaviorPattern(code, description != null ? description : code);
        VALUES.put(code, custom);
        return custom;
    }
    
    /**
     * Get an existing behavior pattern by code
     * 
     * @param code The code for the behavior pattern
     * @return A TradeBehaviorPattern instance or null if not found
     */
    public static TradeBehaviorPattern fromCode(String code) {
        return VALUES.get(code);
    }
    
    /**
     * Get all available behavior pattern values
     * 
     * @return Array of all values
     */
    public static TradeBehaviorPattern[] values() {
        return VALUES.values().toArray(new TradeBehaviorPattern[0]);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        TradeBehaviorPattern that = (TradeBehaviorPattern) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
