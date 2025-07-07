package am.trade.common.models.enums;

import lombok.Getter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing fundamental analysis reasons for entering a trade
 * Supports both predefined values and custom user-defined values
 */
@Getter
public class FundamentalEntryReason {
    // Predefined common fundamental entry reasons
    public static final FundamentalEntryReason EARNINGS_BEAT = new FundamentalEntryReason("EARNINGS_BEAT", "Company reported better than expected earnings");
    public static final FundamentalEntryReason EARNINGS_MISS = new FundamentalEntryReason("EARNINGS_MISS", "Company reported worse than expected earnings");
    public static final FundamentalEntryReason REVENUE_GROWTH = new FundamentalEntryReason("REVENUE_GROWTH", "Strong revenue growth reported");
    public static final FundamentalEntryReason VALUATION = new FundamentalEntryReason("VALUATION", "Attractive price relative to intrinsic value");
    public static final FundamentalEntryReason DIVIDEND_INCREASE = new FundamentalEntryReason("DIVIDEND_INCREASE", "Company increased dividend payment");
    public static final FundamentalEntryReason ANALYST_UPGRADE = new FundamentalEntryReason("ANALYST_UPGRADE", "Positive change in analyst recommendation");
    public static final FundamentalEntryReason SECTOR_ROTATION = new FundamentalEntryReason("SECTOR_ROTATION", "Capital flowing into this market sector");
    public static final FundamentalEntryReason PRODUCT_LAUNCH = new FundamentalEntryReason("PRODUCT_LAUNCH", "Company launched significant new product");
    public static final FundamentalEntryReason MERGER_ACQUISITION = new FundamentalEntryReason("MERGER_ACQUISITION", "Merger, acquisition or takeover activity");
    public static final FundamentalEntryReason ECONOMIC_DATA = new FundamentalEntryReason("ECONOMIC_DATA", "Favorable economic indicators");
    public static final FundamentalEntryReason POLICY_CHANGE = new FundamentalEntryReason("POLICY_CHANGE", "Beneficial regulatory or policy changes");
    public static final FundamentalEntryReason MANAGEMENT_CHANGE = new FundamentalEntryReason("MANAGEMENT_CHANGE", "Changes in company leadership");
    
    private static final Map<String, FundamentalEntryReason> VALUES = new HashMap<>();
    
    static {
        Arrays.asList(
            EARNINGS_BEAT, EARNINGS_MISS, REVENUE_GROWTH, VALUATION,
            DIVIDEND_INCREASE, ANALYST_UPGRADE, SECTOR_ROTATION, PRODUCT_LAUNCH,
            MERGER_ACQUISITION, ECONOMIC_DATA, POLICY_CHANGE, MANAGEMENT_CHANGE
        ).forEach(value -> VALUES.put(value.getCode(), value));
    }
    
    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined enum values
     */
    private FundamentalEntryReason(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Get the code for this reason
     * @return The code
     */
    public String getCode() {
        return code;
    }
    
    /**
     * Get the description for this reason
     * @return The description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Factory method to get an existing fundamental entry reason by code or create a new custom one
     * 
     * @param code The code for the fundamental entry reason
     * @param description Optional description for new custom values
     * @return A FundamentalEntryReason instance
     */
    public static FundamentalEntryReason fromCode(String code, String description) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Fundamental entry reason code cannot be null or empty");
        }
        
        // Return existing value if available
        FundamentalEntryReason existing = VALUES.get(code);
        if (existing != null) {
            return existing;
        }
        
        // Create new custom value
        FundamentalEntryReason custom = new FundamentalEntryReason(code, description != null ? description : code);
        VALUES.put(code, custom);
        return custom;
    }
    
    /**
     * Get an existing fundamental entry reason by code
     * 
     * @param code The code for the fundamental entry reason
     * @return A FundamentalEntryReason instance or null if not found
     */
    public static FundamentalEntryReason fromCode(String code) {
        return VALUES.get(code);
    }
    
    /**
     * Get all available fundamental entry reason values
     * 
     * @return Array of all values
     */
    public static FundamentalEntryReason[] values() {
        return VALUES.values().toArray(new FundamentalEntryReason[0]);
    }
    
    @Override
    public String toString() {
        return code;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        FundamentalEntryReason that = (FundamentalEntryReason) obj;
        return code.equals(that.code);
    }
    
    @Override
    public int hashCode() {
        return code.hashCode();
    }
}
