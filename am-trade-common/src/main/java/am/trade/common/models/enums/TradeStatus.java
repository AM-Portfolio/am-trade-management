package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the status of a trade
 */
@Schema(description = "Trade status indicating the outcome or current state of the trade")
public enum TradeStatus {
    @Schema(description = "Trade resulted in profit")
    WIN,
    
    @Schema(description = "Trade resulted in loss")
    LOSS,
    
    @Schema(description = "Position is still open")
    OPEN,
    
    @Schema(description = "Trade resulted in no profit or loss")
    BREAK_EVEN
}
