package am.trade.api.dto.performance;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Response DTO for performance summary – matches the Flutter TradePerformanceSummaryDto.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PerformanceSummaryResponse {

    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private int breakEvenTrades;
    private double winPercentage;
    private double totalProfitLoss;
    private double averageProfitPerTrade;
    private double averageWinAmount;
    private double averageLossAmount;
    private double averageHoldingTimeWin;
    private double averageHoldingTimeLoss;
    private double maxDrawdown;
    private double profitFactor;
    private double largestWin;
    private double largestLoss;

    /** Nested metrics map – matches PerformanceMetricsDto fields. */
    private Map<String, Object> metrics;
}
