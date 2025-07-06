package am.trade.common.models.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing psychological factors affecting trade exit decisions
 * Supports both predefined values and custom user-defined values
 */
@Getter
public class ExitPsychology {
    // Predefined common exit psychology factors
    public static final ExitPsychology FEAR = new ExitPsychology("FEAR", "Exiting due to fear of losing gains");
    public static final ExitPsychology GREED = new ExitPsychology("GREED", "Holding too long hoping for more gains");
    public static final ExitPsychology DISCIPLINE = new ExitPsychology("DISCIPLINE", "Exiting according to predefined plan");
    public static final ExitPsychology PANIC = new ExitPsychology("PANIC", "Exiting hastily due to market volatility");
    public static final ExitPsychology REGRET_AVOIDANCE = new ExitPsychology("REGRET_AVOIDANCE", "Exiting to avoid feeling regret later");
    public static final ExitPsychology SUNK_COST_FALLACY = new ExitPsychology("SUNK_COST_FALLACY", "Holding losing position too long");
    public static final ExitPsychology TAKING_PROFITS = new ExitPsychology("TAKING_PROFITS", "Disciplined profit-taking at target");
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
