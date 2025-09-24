package am.trade.common.models.enums;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Flexible enum-like class for asset classes in portfolios
 * Supports both predefined values and custom user-defined values
 */
@Getter
@EqualsAndHashCode
public class AssetClass {
    // Predefined asset classes
    public static final AssetClass STOCK = new AssetClass("STOCK", "Individual Stock");
    public static final AssetClass ETF = new AssetClass("ETF", "Exchange Traded Fund");
    public static final AssetClass MUTUAL_FUND = new AssetClass("MUTUAL_FUND", "Mutual Fund");
    public static final AssetClass BOND = new AssetClass("BOND", "Bond");
    public static final AssetClass OPTION = new AssetClass("OPTION", "Option Contract");
    public static final AssetClass FUTURES = new AssetClass("FUTURES", "Futures Contract");
    public static final AssetClass FOREX = new AssetClass("FOREX", "Foreign Exchange");
    public static final AssetClass CRYPTO = new AssetClass("CRYPTO", "Cryptocurrency");
    public static final AssetClass COMMODITY = new AssetClass("COMMODITY", "Physical Commodity");
    public static final AssetClass REIT = new AssetClass("REIT", "Real Estate Investment Trust");
    public static final AssetClass CASH = new AssetClass("CASH", "Cash or Cash Equivalent");
    public static final AssetClass OTHER = new AssetClass("OTHER", "Other Asset Type");

    // Set of predefined values for validation
    private static final Set<String> PREDEFINED_VALUES = new HashSet<>(Arrays.asList(
            "STOCK", "ETF", "MUTUAL_FUND", "BOND", "OPTION", "FUTURES", 
            "FOREX", "CRYPTO", "COMMODITY", "REIT", "CASH", "OTHER"
    ));

    private final String code;
    private final String description;
    
    /**
     * Constructor for predefined asset classes
     * 
     * @param code The code representing the asset class
     * @param description A human-readable description of the asset class
     */
    private AssetClass(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * Create a custom asset class with the given code and description
     * 
     * @param code The code for the custom asset class
     * @param description A human-readable description of the custom asset class
     * @return A new AssetClass instance
     */
    public static AssetClass custom(String code, String description) {
        if (PREDEFINED_VALUES.contains(code)) {
            throw new IllegalArgumentException("Cannot create custom asset class with predefined code: " + code);
        }
        return new AssetClass(code, description);
    }
    
    /**
     * Get a predefined asset class by its code, or create a custom one if not found
     * 
     * @param code The code to look up
     * @param description Optional description for custom asset class (can be null for predefined)
     * @return The matching predefined asset class or a new custom one
     */
    public static AssetClass fromCode(String code, String description) {
        if (code == null) {
            return null;
        }
        
        switch (code) {
            case "STOCK": return STOCK;
            case "ETF": return ETF;
            case "MUTUAL_FUND": return MUTUAL_FUND;
            case "BOND": return BOND;
            case "OPTION": return OPTION;
            case "FUTURES": return FUTURES;
            case "FOREX": return FOREX;
            case "CRYPTO": return CRYPTO;
            case "COMMODITY": return COMMODITY;
            case "REIT": return REIT;
            case "CASH": return CASH;
            case "OTHER": return OTHER;
            default: return custom(code, description);
        }
    }
    
    @Override
    public String toString() {
        return code;
    }
}
