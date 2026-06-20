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
 * Enum representing psychological factors affecting trade exit decisions
 * Supports both predefined values and custom user-defined values
 */
@Getter
@Schema(
    description = "Psychological factor influencing trade exit decision. Supports both predefined and custom values.",
    example = "DISCIPLINE",
    allowableValues = {
        "FEAR", "GREED", "DISCIPLINE", "PANIC",
        "REGRET_AVOIDANCE", "SUNK_COST_FALLACY", 
        "TAKING_PROFITS", "CUTTING_LOSSES"
    }
)
public class ExitPsychology {
    // Predefined common exit psychology factors
    @Schema(description = "Exiting due to fear of losing gains")
    public static final ExitPsychology FEAR = new ExitPsychology("FEAR", "Exiting due to fear of losing gains");
    
    @Schema(description = "Holding too long hoping for more gains")
    public static final ExitPsychology GREED = new ExitPsychology("GREED", "Holding too long hoping for more gains");
    
    @Schema(description = "Exiting according to predefined plan")
    public static final ExitPsychology DISCIPLINE = new ExitPsychology("DISCIPLINE", "Exiting according to predefined plan");
    
    @Schema(description = "Exiting hastily due to market volatility")
    public static final ExitPsychology PANIC = new ExitPsychology("PANIC", "Exiting hastily due to market volatility");
    
    @Schema(description = "Exiting to avoid feeling regret later")
    public static final ExitPsychology REGRET_AVOIDANCE = new ExitPsychology("REGRET_AVOIDANCE", "Exiting to avoid feeling regret later");
    
    @Schema(description = "Holding losing position too long due to sunk cost fallacy")
    public static final ExitPsychology SUNK_COST_FALLACY = new ExitPsychology("SUNK_COST_FALLACY", "Holding losing position too long");
    
    @Schema(description = "Disciplined profit-taking at target")
    public static final ExitPsychology TAKING_PROFITS = new ExitPsychology("TAKING_PROFITS", "Disciplined profit-taking at target");
    
    @Schema(description = "Disciplined exit at stop-loss")
    public static final ExitPsychology CUTTING_LOSSES = new ExitPsychology("CUTTING_LOSSES", "Disciplined exit at stop-loss");
    
    private static final Map<String, ExitPsychology> VALUES = new HashMap<>();
    
    static {
        Arrays.asList(
            FEAR, GREED, DISCIPLINE, PANIC, REGRET_AVOIDANCE, 
            SUNK_COST_FALLACY, TAKING_PROFITS, CUTTING_LOSSES
        ).forEach(value -> VALUES.put(value.getCode(), value));
    }
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined enum values
     */
    private ExitPsychology(String code, String description) {
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
     * @param code The code for the exit psychology
     * @param description Optional description for new custom values
     */
    @JsonCreator
    public static ExitPsychology fromJson(
            @JsonProperty("code") String code, 
            @JsonProperty("description") String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Exit psychology code cannot be null or empty");
        }
        return fromCode(code, description);
    }
    
    /**
     * Factory method to get an existing exit psychology by code or create a new custom one
     * 
     * @param code The code for the exit psychology
     * @param description Optional description for new custom values
     * @return An ExitPsychology instance
     */
    public static ExitPsychology fromCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Exit psychology code cannot be null or empty");
        }
        
        // Return existing value if available
        ExitPsychology existing = VALUES.get(code);
        if (existing != null) {
            return existing;
        }
        
        // Create new custom value
        ExitPsychology custom = new ExitPsychology(code, description != null ? description : code);
        VALUES.put(code, custom);
        return custom;
    }
    
    /**
     * Get an existing exit psychology by code
     * 
     * @param code The code for the exit psychology
     * @return An ExitPsychology instance or null if not found
     */
    public static ExitPsychology fromCode(String code) {
        return VALUES.get(code);
    }
    
    /**
     * Get all available exit psychology values
     * 
     * @return Array of all values
     */
    public static ExitPsychology[] values() {
        return VALUES.values().toArray(new ExitPsychology[0]);
    }

    public static boolean isStandardCode(String code) {
        if (code == null) {
            return false;
        }
        return Arrays.asList(
            FEAR.getCode(), 
            GREED.getCode(), 
            DISCIPLINE.getCode(), 
            PANIC.getCode(), 
            REGRET_AVOIDANCE.getCode(), 
            SUNK_COST_FALLACY.getCode(), 
            TAKING_PROFITS.getCode(),
            CUTTING_LOSSES.getCode()
        ).contains(code);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ExitPsychology that = (ExitPsychology) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
