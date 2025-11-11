package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing different types of F&O (Futures and Options) trades
 */
@Schema(description = "Type of Futures and Options (F&O) instrument")
public enum FNOTradeType {
    @Schema(description = "Futures on Index (e.g., NIFTY, BANKNIFTY)")
    FUTIDX,
    
    @Schema(description = "Options on Index (e.g., NIFTY, BANKNIFTY)")
    OPTIDX,
    
    @Schema(description = "Futures on Equity/Stock")
    FUTEQ,
    
    @Schema(description = "Options on Equity/Stock")
    OPTEQ,
    
    @Schema(description = "Futures on Stock")
    FUTSTK,
    
    @Schema(description = "Options on Stock")
    OPTSTK
}
