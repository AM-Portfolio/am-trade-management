package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model for detailed performance metrics of trading activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceMetrics {
    // Core performance metrics
    private BigDecimal totalProfitLoss;
    private BigDecimal totalProfitLossPercentage;
    private BigDecimal winRate;
    private BigDecimal profitFactor; // Total profit / Total loss (>1 is profitable)
    private BigDecimal expectancy; // Average amount you can expect to win/lose per trade
    
    // Return metrics
    private BigDecimal annualizedReturn;
    private BigDecimal monthlyReturn;
    private BigDecimal quarterlyReturn;
    private BigDecimal yearToDateReturn;
    
    // Consistency metrics
    private BigDecimal averageWinningTrade;
    private BigDecimal averageLosingTrade;
    private BigDecimal largestWinningTrade;
    private BigDecimal largestLosingTrade;
    private BigDecimal winLossRatio; // Average win / Average loss
    
    // Streak information
    private int longestWinningStreak;
    private int longestLosingStreak;
    private int currentStreak; // Positive for winning, negative for losing
    
    // Time metrics
    private BigDecimal averageHoldingTimeWinning; // In days
    private BigDecimal averageHoldingTimeLosing; // In days
    private BigDecimal averageHoldingTimeOverall; // In days
    
    // Efficiency metrics
    private BigDecimal returnOnCapital; // Profit / Capital used
    private BigDecimal returnPerUnit; // Return per unit of risk
    
    // Timestamp information
    private LocalDateTime lastWinningTradeDate;
    private LocalDateTime lastLosingTradeDate;
    private LocalDateTime bestDayDate;
    private LocalDateTime worstDayDate;
    private BigDecimal bestDayProfit;
    private BigDecimal worstDayLoss;
}
