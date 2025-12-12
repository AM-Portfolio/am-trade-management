package am.trade.api.dto.summary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyPerformance {
    private String month; // JANUARY, FEBRUARY...
    private int monthOrder; // 1 = January, 12 = December
    private BigDecimal totalProfitLoss;
    private int tradeCount;
    private int winCount;
    private int lossCount;
    private double winRate;
    private BigDecimal averageWinAmount;
    private BigDecimal averageLossAmount;
    private double averageHoldingTime;
    private PerformanceMetrics metrics;
}
