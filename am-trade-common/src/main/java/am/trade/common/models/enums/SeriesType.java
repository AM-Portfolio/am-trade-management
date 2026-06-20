package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing different series types
 */
@Schema(description = "Trading series type for the instrument")
public enum SeriesType {
    @Schema(description = "Equity series")
    EQ,
    
    @Schema(description = "Book Entry series")
    BE,
    
    @Schema(description = "Block Deal series")
    BZ,
    
    @Schema(description = "Institutional series")
    IL,
    
    @Schema(description = "Government Securities series")
    GC,
    
    @Schema(description = "Call Option European style")
    CE,
    
    @Schema(description = "Put Option European style")
    PE,
    
    @Schema(description = "Call Option American style")
    CA,
    
    @Schema(description = "Put Option American style")
    PA,
    
    @Schema(description = "Futures contract")
    FUT,
    
    @Schema(description = "Weekly Futures contract")
    WEEKLY_FUT,
    
    @Schema(description = "Monthly Futures contract")
    MONTHLY_FUT,
    
    @Schema(description = "Quarterly Futures contract")
    QUARTERLY_FUT,
    
    @Schema(description = "Unknown series type")
    UNKNOWN
}
