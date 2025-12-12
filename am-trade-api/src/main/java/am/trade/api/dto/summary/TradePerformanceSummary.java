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
public class TradePerformanceSummary {
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private int breakEvenTrades;
    private double winPercentage;
    private BigDecimal totalProfitLoss;
    private BigDecimal averageProfitPerTrade;
    private BigDecimal averageWinAmount;
    private BigDecimal averageLossAmount;
    private double averageHoldingTimeWin; // In hours
    private double averageHoldingTimeLoss; // In hours
    private BigDecimal maxDrawdown;
    private double profitFactor;
    private BigDecimal largestWin;
    private BigDecimal largestLoss;
    private PerformanceMetrics metrics;
}
