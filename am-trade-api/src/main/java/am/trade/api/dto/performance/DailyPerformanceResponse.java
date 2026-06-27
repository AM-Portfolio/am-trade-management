package am.trade.api.dto.performance;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * One day of performance data – matches the Flutter DailyPerformanceDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DailyPerformanceResponse {

    /** ISO date string (yyyy-MM-dd) */
    private String date;
    private double totalProfitLoss;
    private int tradeCount;
    private int winCount;
    private int lossCount;
    private double winRate;
    private String bestTradeSymbol;
    private Double bestTradePnL;

    /** Nested metrics map – matches PerformanceMetricsDto fields. */
    private Map<String, Object> metrics;
}
