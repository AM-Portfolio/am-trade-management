package am.trade.common.models.enums;

/**
 * Enum representing different series types
 */
public enum SeriesType {
    EQ,             // Equity series
    BE,             // Book Entry
    BZ,             // Block Deal
    IL,             // Institutional
    GC,             // Government Securities
    CE,             // Call Option European
    PE,             // Put Option European
    CA,             // Call Option American 
    PA,             // Put Option American
    FUT,            // Futures
    WEEKLY_FUT,     // Weekly Futures
    MONTHLY_FUT,    // Monthly Futures
    QUARTERLY_FUT,   // Quarterly Futures
    UNKNOWN         // Unknown series type
}
