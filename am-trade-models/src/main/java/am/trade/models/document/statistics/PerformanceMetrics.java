package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * Class for performance metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PerformanceMetrics {
    @Field("win_percentage")
    private BigDecimal winPercentage;
    
    @Field("loss_percentage")
    private BigDecimal lossPercentage;
    
    @Field("break_even_percentage")
    private BigDecimal breakEvenPercentage;
}
