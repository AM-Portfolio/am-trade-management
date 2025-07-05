package am.trade.common.models.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing psychological factors affecting trade entry decisions
 * Supports both predefined values and custom user-defined values
 */
@Getter
public class EntryPsychology {
    // Predefined common entry psychology factors
    public static final EntryPsychology FEAR_OF_MISSING_OUT = new EntryPsychology("FEAR_OF_MISSING_OUT", "Fear of missing out on potential gains");
    public static final EntryPsychology OVERCONFIDENCE = new EntryPsychology("OVERCONFIDENCE", "Excessive confidence in analysis or prediction");
    public static final EntryPsychology REVENGE_TRADING = new EntryPsychology("REVENGE_TRADING", "Entering a trade to recover previous losses");
    public static final EntryPsychology ANALYSIS_PARALYSIS = new EntryPsychology("ANALYSIS_PARALYSIS", "Overthinking leading to delayed entry");
    public static final EntryPsychology FOLLOWING_THE_PLAN = new EntryPsychology("FOLLOWING_THE_PLAN", "Disciplined entry according to trading plan");
    public static final EntryPsychology INTUITION = new EntryPsychology("INTUITION", "Gut feeling or market intuition");
    public static final EntryPsychology PEER_PRESSURE = new EntryPsychology("PEER_PRESSURE", "Influenced by others' opinions or actions");
    public static final EntryPsychology DISCIPLINED = new EntryPsychology("DISCIPLINED", "Disciplined entry according to trading plan");
    
    private static final Map<String, EntryPsychology> VALUES = new HashMap<>();
    
    static {
        Arrays.asList(
            FEAR_OF_MISSING_OUT, OVERCONFIDENCE, REVENGE_TRADING, ANALYSIS_PARALYSIS, 
            FOLLOWING_THE_PLAN, INTUITION, PEER_PRESSURE
        ).forEach(value -> VALUES.put(value.getCode(), value));
    }
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined enum values
     */
    private EntryPsychology(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Factory method to get an existing entry psychology by code or create a new custom one
     * 
     * @param code The code for the entry psychology
     * @param description Optional description for new custom values
     * @return An EntryPsychology instance
     */
    public static EntryPsychology fromCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Entry psychology code cannot be null or empty");
        }
        
        // Return existing value if available
        EntryPsychology existing = VALUES.get(code);
        if (existing != null) {
            return existing;
        }
        
        // Create new custom value
        EntryPsychology custom = new EntryPsychology(code, description != null ? description : code);
        VALUES.put(code, custom);
        return custom;
    }
    
    /**
     * Get an existing entry psychology by code
     * 
     * @param code The code for the entry psychology
     * @return An EntryPsychology instance or null if not found
     */
    public static EntryPsychology fromCode(String code) {
        return VALUES.get(code);
    }
    
    /**
     * Get all available entry psychology values
     * 
     * @return Array of all values
     */
    public static EntryPsychology[] values() {
        return VALUES.values().toArray(new EntryPsychology[0]);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EntryPsychology that = (EntryPsychology) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
