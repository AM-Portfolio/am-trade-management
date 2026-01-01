package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the type of trade position (long or short)
 */
@Schema(description = "Trade position type - Long or Short")
public enum TradePositionType {
    @Schema(description = "Long position - Expecting price to increase")
    LONG,
    
    @Schema(description = "Short position - Expecting price to decrease")
    SHORT
}
