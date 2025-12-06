package am.trade.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the status of a trade order
 */
@Schema(description = "Current status of the trade order")
public enum OrderStatus {
    @Schema(description = "Order submitted but not yet active")
    PENDING,
    
    @Schema(description = "Order is active and working")
    OPEN,
    
    @Schema(description = "Order partially executed")
    PARTIALLY_FILLED,
    
    @Schema(description = "Order completely executed")
    FILLED,
    
    @Schema(description = "Order cancelled by user")
    CANCELLED,
    
    @Schema(description = "Order rejected by exchange/broker")
    REJECTED,
    
    @Schema(description = "Order expired without execution")
    EXPIRED,
    
    @Schema(description = "Order settled and finalized")
    SETTLED
}
