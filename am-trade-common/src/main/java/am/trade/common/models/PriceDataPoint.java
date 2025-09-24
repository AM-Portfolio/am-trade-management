package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Represents a price data point for trade replay analysis
 * Used to store historical price data during the holding period
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceDataPoint {
    
    private LocalDateTime timestamp;
    
    private BigDecimal open;
    
    private BigDecimal high;
    
    private BigDecimal low;
    
    private BigDecimal close;
    
    private Long volume;
    
    private BigDecimal profitLossAtPoint;
    
    private BigDecimal profitLossPercentageAtPoint;
}
