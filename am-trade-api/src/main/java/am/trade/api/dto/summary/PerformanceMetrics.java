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
public class PerformanceMetrics {

    // Time Analysis
    private double avgHoldTime; // in hours or days
    private double longestTradeDuration;
    private double maxTradingWeeksDuration;
    private double avgTradingWeeksDuration;

    // Profitability
    private BigDecimal avgGrossTradePnL;
    private BigDecimal avgLoss;
    private BigDecimal avgMaxTradeLoss;
    private BigDecimal avgMaxTradeProfit;
    private BigDecimal avgTradeWinLossRatio; // Avg trade win/loss
    private BigDecimal avgWeeklyGrossPnL;
    private BigDecimal avgWeeklyWinLossRatio;
    private BigDecimal avgWin;
    private BigDecimal grossPnL;
    private BigDecimal largestLosingTrade;
    private BigDecimal largestProfitableTrade;
    private double profitFactor;

    // Risk & Drawdown
    private BigDecimal avgWeeklyGrossDrawdown;
    private double avgPlannedRMultiple;
    private double avgRealizedRMultiple;
    private int breakevenDays;
    private int breakevenTrades;
    private int losingDays;
    private BigDecimal maxWeeklyGrossDrawdown;

    // Streaks & Consistency
    private double avgWeeklyWinPercentage;
    private double longsWinPercentage;
    private int maxConsecutiveLosingWeeks;
    private int maxConsecutiveLosses;
    private int maxConsecutiveWinningWeeks;
    private int maxConsecutiveWins;
    private double shortsWinPercentage;
    private double winPercentage;
    private int winningDays;
}
