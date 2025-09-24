package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model for analyzing the performance of different trading strategies
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StrategyPerformanceMetrics {
    // Strategy identification
    private String strategyName;
    private String strategyDescription;
    private List<String> strategyTags;
    private LocalDateTime strategyInceptionDate;
    
    // Core performance metrics by strategy
    private BigDecimal totalProfitLoss;
    private BigDecimal winRate;
    private BigDecimal profitFactor;
    private BigDecimal expectancy;
    private BigDecimal sharpeRatio;
    
    // Strategy consistency
    private BigDecimal consistencyScore; // How consistent the strategy performs
    private BigDecimal performanceStability; // Stability of returns over time
    private List<LocalDateTime> strategyDrawdownPeriods; // Periods of underperformance
    private List<LocalDateTime> strategyOutperformancePeriods; // Periods of outperformance
    
    // Market condition effectiveness
    private Map<String, BigDecimal> performanceByMarketType; // Bull, bear, sideways
    private Map<String, BigDecimal> performanceByVolatility; // High, medium, low volatility
    private BigDecimal marketNeutralScore; // How market-neutral the strategy is
    
    // Strategy adaptation
    private BigDecimal adaptabilityScore; // How well strategy adapts to changing conditions
    private LocalDateTime lastStrategyAdjustment; // When strategy was last adjusted
    private List<String> strategyEvolutionNotes; // Notes on how strategy has evolved
    
    // Comparative metrics
    private BigDecimal benchmarkOutperformance; // Performance vs benchmark
    private BigDecimal peerGroupRanking; // Percentile ranking vs peer strategies
    private Map<String, BigDecimal> correlationWithOtherStrategies; // Correlation matrix
    
    // Risk metrics by strategy
    private BigDecimal maxDrawdown;
    private BigDecimal recoveryFactor; // Return / Max drawdown
    private BigDecimal ulcerIndex; // Measure of drawdown severity
    private BigDecimal painIndex; // Alternative measure of drawdown severity
    
    // Strategy optimization
    private Map<String, BigDecimal> parameterSensitivity; // How sensitive to parameter changes
    private List<String> optimizationSuggestions; // Suggestions for improvement
    private BigDecimal optimizationPotential; // Estimated improvement potential
    
    // Strategy execution
    private BigDecimal executionQuality; // How well strategy is executed
    private BigDecimal planAdherenceScore; // How well trader sticks to strategy
    private BigDecimal signalResponseTime; // Average time to act on signals
}
