package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * Class for value-based metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValueMetrics {
    @Field("total_value")
    private BigDecimal totalValue;
    
    @Field("average_value")
    private BigDecimal averageValue;
    
    @Field("max_value")
    private BigDecimal maxValue;
    
    @Field("min_value")
    private BigDecimal minValue;
    
    @Field("total_profit")
    private BigDecimal totalProfit;
    
    @Field("total_loss")
    private BigDecimal totalLoss;
    
    @Field("net_profit_loss")
    private BigDecimal netProfitLoss;
}
