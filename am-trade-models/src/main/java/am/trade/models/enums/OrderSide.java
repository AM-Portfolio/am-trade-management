package am.trade.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the side of a trade order
 */
@Schema(description = "Order side/direction for trade execution")
public enum OrderSide {
    @Schema(description = "Buy order - Opening long position")
    BUY,
    
    @Schema(description = "Sell order - Closing long position or opening short")
    SELL,
    
    @Schema(description = "Short sell order - Opening short position")
    SHORT,
    
    @Schema(description = "Cover order - Closing short position")
    COVER
}
