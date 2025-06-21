package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Class for holding time metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoldingTimeMetrics {
    @Field("average_holding_minutes")
    private Double averageHoldingMinutes;
    
    @Field("average_winning_holding_minutes")
    private Double averageWinningHoldingMinutes;
    
    @Field("average_losing_holding_minutes")
    private Double averageLosingHoldingMinutes;
    
    @Field("max_holding_minutes")
    private Double maxHoldingMinutes;
    
    @Field("min_holding_minutes")
    private Double minHoldingMinutes;
}
