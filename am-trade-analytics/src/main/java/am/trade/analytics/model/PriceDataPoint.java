package am.trade.analytics.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

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
    
    @Field("timestamp")
    private LocalDateTime timestamp;
    
    @Field("open")
    private BigDecimal open;
    
    @Field("high")
    private BigDecimal high;
    
    @Field("low")
    private BigDecimal low;
    
    @Field("close")
    private BigDecimal close;
    
    @Field("volume")
    private Long volume;
    
    @Field("profit_loss_at_point")
    private BigDecimal profitLossAtPoint;
    
    @Field("profit_loss_percentage_at_point")
    private BigDecimal profitLossPercentageAtPoint;
}
