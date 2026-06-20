package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum representing different stock exchanges
 */
@Schema(description = "Stock exchange where the trade was executed")
public enum Exchange {
    @Schema(description = "National Stock Exchange (India)")
    NSE,
    
    @Schema(description = "Bombay Stock Exchange (India)")
    BSE,
    
    @Schema(description = "New York Stock Exchange")
    NYSE,
    
    @Schema(description = "NASDAQ Stock Market")
    NASDAQ,
    
    @Schema(description = "London Stock Exchange")
    LSE,
    
    @Schema(description = "Hong Kong Stock Exchange")
    HKEX,
    
    @Schema(description = "Tokyo Stock Exchange")
    TSE,
    
    @Schema(description = "Singapore Exchange")
    SGX,
    
    @Schema(description = "European Exchange")
    EUREX,
    
    @Schema(description = "Chicago Mercantile Exchange")
    CME,
    
    @Schema(description = "Other exchanges")
    OTHER
}
