package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

/**
 * Class for time-related metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TimeMetrics {
    @Field("first_trade_date")
    private LocalDateTime firstTradeDate;
    
    @Field("last_trade_date")
    private LocalDateTime lastTradeDate;
    
    @Field("period_days")
    private Integer periodDays;
    
    @Field("trades_per_day")
    private Double tradesPerDay;
    
    @Field("trades_per_week")
    private Double tradesPerWeek;
    
    @Field("trades_per_month")
    private Double tradesPerMonth;
}
