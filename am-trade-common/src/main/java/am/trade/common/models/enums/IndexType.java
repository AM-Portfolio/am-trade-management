package am.trade.common.models.enums;

/**
 * Enum representing different index types in the Indian market
 */
public enum IndexType {
    // Major indices
    NIFTY("Nifty 50", "NSE's benchmark index for the Indian stock market"),
    BANKNIFTY("Bank Nifty", "Index of the most liquid and large capitalized Indian banking stocks"),
    MIDCAPNIFTY("Midcap Nifty", "Index representing the mid-cap segment of the market"),
    FINNIFTY("Fin Nifty", "Index representing the financial services sector"),
    
    // Other important indices
    NIFTY_NEXT_50("Nifty Next 50", "Index representing the 50 companies after Nifty 50"),
    NIFTY_100("Nifty 100", "Index representing the top 100 companies based on market capitalization"),
    NIFTY_200("Nifty 200", "Index representing the top 200 companies based on market capitalization"),
    NIFTY_500("Nifty 500", "Index representing the top 500 companies based on market capitalization"),
    
    // Sector indices
    NIFTY_AUTO("Nifty Auto", "Index representing the auto sector"),
    NIFTY_FMCG("Nifty FMCG", "Index representing the FMCG sector"),
    NIFTY_IT("Nifty IT", "Index representing the IT sector"),
    NIFTY_METAL("Nifty Metal", "Index representing the metal sector"),
    NIFTY_PHARMA("Nifty Pharma", "Index representing the pharmaceutical sector"),
    NIFTY_REALTY("Nifty Realty", "Index representing the real estate sector"),
    
    // BSE indices
    SENSEX("Sensex", "BSE's benchmark index for the Indian stock market"),
    BSE_100("BSE 100", "Index representing the top 100 companies listed on BSE"),
    BSE_200("BSE 200", "Index representing the top 200 companies listed on BSE"),
    BSE_500("BSE 500", "Index representing the top 500 companies listed on BSE"),
    
    // Other indices
    INDIA_VIX("India VIX", "Volatility index based on the NIFTY Index options"),
    
    // Default
    UNKNOWN("Unknown", "Unknown index type");
    
    private final String displayName;
    private final String description;
    
    IndexType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Find an IndexType by its symbol name (case-insensitive)
     * 
     * @param symbol The symbol to look for
     * @return The matching IndexType or UNKNOWN if no match
     */
    public static IndexType fromSymbol(String symbol) {
        if (symbol == null || symbol.isEmpty()) {
            return UNKNOWN;
        }
        
        String upperSymbol = symbol.toUpperCase();
        
        for (IndexType indexType : values()) {
            if (indexType.name().equals(upperSymbol) || 
                indexType.getDisplayName().toUpperCase().equals(upperSymbol)) {
                return indexType;
            }
        }
        
        // Handle special cases with different naming conventions
        if (upperSymbol.equals("NIFTY")) {
            return NIFTY;
        } else if (upperSymbol.equals("BANKNIFTY") || upperSymbol.equals("BANK NIFTY")) {
            return BANKNIFTY;
        } else if (upperSymbol.equals("MIDCAPNIFTY") || upperSymbol.equals("MIDCAP NIFTY")) {
            return MIDCAPNIFTY;
        } else if (upperSymbol.equals("FINNIFTY") || upperSymbol.equals("FIN NIFTY")) {
            return FINNIFTY;
        }
        
        return UNKNOWN;
    }
    
    /**
     * Check if a symbol represents an index
     * 
     * @param symbol The symbol to check
     * @return true if the symbol represents an index, false otherwise
     */
    public static boolean isIndex(String symbol) {
        return fromSymbol(symbol) != UNKNOWN;
    }
}
