package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing the type of trade (buy or sell)
 */
@Schema(description = "Trade direction - Buy or Sell")
public enum TradeType {
    @Schema(description = "Buy/Long position")
    BUY,
    
    @Schema(description = "Sell/Short position")
    SELL
}
