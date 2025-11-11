package am.trade.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the type of a trade order
 */
@Schema(description = "Type of order for execution")
public enum OrderType {
    @Schema(description = "Market order - Execute at current market price")
    MARKET,
    
    @Schema(description = "Limit order - Execute at specified price or better")
    LIMIT,
    
    @Schema(description = "Stop order - Becomes market order when stop price reached")
    STOP,
    
    @Schema(description = "Stop-limit order - Becomes limit order when stop price reached")
    STOP_LIMIT,
    
    @Schema(description = "Trailing stop - Stop price follows market at specified distance")
    TRAILING_STOP,
    
    @Schema(description = "Fill or kill - Execute immediately and completely or cancel")
    FILL_OR_KILL,
    
    @Schema(description = "Good till cancel - Remains active until filled or cancelled")
    GOOD_TILL_CANCEL,
    
    @Schema(description = "Good till date - Remains active until specified date")
    GOOD_TILL_DATE,
    
    @Schema(description = "At the open - Execute at market opening")
    AT_THE_OPEN,
    
    @Schema(description = "At the close - Execute at market closing")
    AT_THE_CLOSE
}
