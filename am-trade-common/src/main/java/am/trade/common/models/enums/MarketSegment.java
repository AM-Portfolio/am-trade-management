package am.trade.common.models.enums;

/**
 * Enum representing different market segments
 */
public enum MarketSegment {
    // Primary instrument types
    EQUITY("Equity", "Regular equity/stock segment", false),
    INDEX("Index", "Market index segment", false),
    
    // Derivative types
    EQUITY_FUTURES("Equity Futures", "Futures contracts on individual stocks", true),
    EQUITY_OPTIONS("Equity Options", "Options contracts on individual stocks", true),
    INDEX_FUTURES("Index Futures", "Futures contracts on market indices", true),
    INDEX_OPTIONS("Index Options", "Options contracts on market indices", true),
    
    // Other segments
    CURRENCY("Currency", "Currency segment", false),
    COMMODITY("Commodity", "Commodity segment", false),
    DEBT("Debt", "Debt/Bond segment", false),
    MUTUAL_FUND("Mutual Fund", "Mutual fund segment", false),
    ETF("ETF", "Exchange Traded Fund segment", false),
    
    // Legacy codes (kept for backward compatibility)
    FUT("Futures", "Generic futures segment", true),
    OPT("Options", "Generic options segment", true),
    EQ("Equity", "Equity segment", false),
    FO("F&O", "Futures and Options segment", true),
    
    // Default
    UNKNOWN("Unknown", "Unknown market segment", false);
    
    private final String displayName;
    private final String description;
    private final boolean isDerivative;
    
    MarketSegment(String displayName, String description, boolean isDerivative) {
        this.displayName = displayName;
        this.description = description;
        this.isDerivative = isDerivative;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public boolean isDerivative() {
        return isDerivative;
    }
    
    /**
     * Determine the appropriate market segment based on the base symbol and derivative type
     * 
     * @param baseSymbol The base symbol
     * @param isOptions True if it's an options contract, false for futures
     * @return The appropriate market segment
     */
    public static MarketSegment determineSegment(String baseSymbol, boolean isOptions) {
        boolean isIndexSymbol = IndexType.isIndex(baseSymbol);
        
        if (isOptions) {
            return isIndexSymbol ? INDEX_OPTIONS : EQUITY_OPTIONS;
        } else {
            return isIndexSymbol ? INDEX_FUTURES : EQUITY_FUTURES;
        }
    }
    
    /**
     * Get the base instrument type for a derivative segment
     * 
     * @return The base instrument type (EQUITY or INDEX) or null if not a derivative
     */
    public MarketSegment getBaseInstrumentType() {
        if (!isDerivative) {
            return this;
        }
        
        switch (this) {
            case EQUITY_FUTURES:
            case EQUITY_OPTIONS:
                return EQUITY;
            case INDEX_FUTURES:
            case INDEX_OPTIONS:
                return INDEX;
            case FUT:
            case OPT:
            case FO:
                return null; // Can't determine without additional context
            default:
                return null;
        }
    }
}
