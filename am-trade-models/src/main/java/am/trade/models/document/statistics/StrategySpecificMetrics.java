package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Class for strategy-specific metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategySpecificMetrics {
    @Field("time_frame")
    private String timeFrame;
    
    @Field("market_condition")
    private String marketCondition;
}
