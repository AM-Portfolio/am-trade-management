package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Model for analyzing the timing aspects of trades
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeTimingMetrics {
    // Entry timing metrics
    private BigDecimal averageEntryEfficiency; // How close to optimal entry (0-100%)
    private BigDecimal entryTimingScore; // Composite score of entry timing quality
    private Map<String, Integer> earlyEntries; // Count of entries that were too early
    private Map<String, Integer> lateEntries; // Count of entries that were too late
    private Map<String, Integer> optimalEntries; // Count of well-timed entries
    
    // Exit timing metrics
    private BigDecimal averageExitEfficiency; // How close to optimal exit (0-100%)
    private BigDecimal exitTimingScore; // Composite score of exit timing quality
    private Map<String, Integer> earlyExits; // Count of exits that were too early
    private Map<String, Integer> lateExits; // Count of exits that were too late
    private Map<String, Integer> optimalExits; // Count of well-timed exits
    
    // Market timing metrics
    private BigDecimal marketDirectionAccuracy; // Accuracy in predicting market direction
    private BigDecimal trendAlignmentPercentage; // Percentage of trades aligned with trend
    private BigDecimal counterTrendSuccessRate; // Success rate of counter-trend trades
    
    // Seasonal timing
    private Map<String, BigDecimal> seasonalPerformance; // Performance by season
    private Map<String, BigDecimal> monthlyPerformance; // Performance by month
    private Map<String, BigDecimal> dayOfWeekPerformance; // Performance by day of week
    private Map<String, BigDecimal> timeOfDayPerformance; // Performance by time of day
    
    // Volatility timing
    private BigDecimal highVolatilityWinRate; // Win rate during high volatility
    private BigDecimal lowVolatilityWinRate; // Win rate during low volatility
    private BigDecimal volatilityAdaptationScore; // How well strategy adapts to volatility
    
    // News and event timing
    private BigDecimal preEventPerformance; // Performance before major events
    private BigDecimal postEventPerformance; // Performance after major events
    private BigDecimal newsReactionScore; // How well trades capitalize on news
    
    // Timing improvement
    private BigDecimal timingImprovementTrend; // Trend of timing improvement over time
    private LocalDateTime lastTimingAssessment; // When timing was last assessed
    private Map<String, BigDecimal> timingByStrategy; // Timing effectiveness by strategy
}
