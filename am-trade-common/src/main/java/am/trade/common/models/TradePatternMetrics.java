package am.trade.common.models;

import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Model for analyzing trade patterns and psychology metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradePatternMetrics {
    // Trade pattern performance
    private Map<TradeBehaviorPattern, Integer> patternFrequency;
    private Map<TradeBehaviorPattern, BigDecimal> patternProfitLoss;
    private Map<TradeBehaviorPattern, BigDecimal> patternWinRate;
    private Map<TradeBehaviorPattern, BigDecimal> patternExpectancy;
    private Map<TradeBehaviorPattern, BigDecimal> patternRiskRewardRatio;
    private List<TradeBehaviorPattern> mostProfitablePatterns;
    private List<TradeBehaviorPattern> leastProfitablePatterns;
    
    // Psychology impact metrics
    private Map<EntryPsychology, Integer> entryPsychologyFrequency;
    private Map<EntryPsychology, BigDecimal> entryPsychologyProfitLoss;
    private Map<EntryPsychology, BigDecimal> entryPsychologyWinRate;
    private List<EntryPsychology> mostProfitableEntryPsychology;
    private List<EntryPsychology> leastProfitableEntryPsychology;
    
    private Map<ExitPsychology, Integer> exitPsychologyFrequency;
    private Map<ExitPsychology, BigDecimal> exitPsychologyProfitLoss;
    private Map<ExitPsychology, BigDecimal> exitPsychologyWinRate;
    private List<ExitPsychology> mostProfitableExitPsychology;
    private List<ExitPsychology> leastProfitableExitPsychology;
    
    // Emotional control metrics
    private BigDecimal emotionalControlScore; // 0-100 score based on psychology factors
    private BigDecimal planAdherenceScore; // How well trader stuck to trading plan
    private BigDecimal impulsiveTradePercentage; // Percentage of trades marked as impulsive
    private BigDecimal fearBasedExitPercentage; // Percentage of exits driven by fear
    private BigDecimal greedBasedEntryPercentage; // Percentage of entries driven by greed
    private BigDecimal disciplineScore; // Score based on following rules and exit plans
    
    // Pattern consistency metrics
    private BigDecimal patternConsistencyScore; // How consistently patterns are executed
    private Map<TradeBehaviorPattern, BigDecimal> patternExecutionQuality; // Quality score by pattern
    private BigDecimal patternDeviationRate; // Rate of deviating from established patterns
    
    // Psychology improvement metrics
    private BigDecimal psychologyImprovementTrend; // Trend of improvement over time
    private Map<String, BigDecimal> psychologyByTimeframe; // Psychology scores by timeframe
    private BigDecimal emotionalResilience; // Ability to recover from losses
    
    // Combined pattern-psychology metrics
    private Map<String, BigDecimal> patternPsychologyCorrelation; // Correlation between patterns and psychology
    private List<String> psychologyRecommendations; // Recommendations for improvement
    private List<String> patternOptimizationSuggestions; // Suggestions for pattern optimization
    
    // Trade management metrics
    private BigDecimal averageScaleInQuality; // Quality of scaling into positions
    private BigDecimal averageScaleOutQuality; // Quality of scaling out of positions
    private BigDecimal stopLossAdherenceRate; // Rate of adhering to stop losses
    private BigDecimal targetAdherenceRate; // Rate of adhering to profit targets
    private BigDecimal adjustmentEffectivenessScore; // Effectiveness of position adjustments
    
    // Decision quality metrics
    private BigDecimal entryDecisionQuality; // Quality of entry decisions
    private BigDecimal exitDecisionQuality; // Quality of exit decisions
    private BigDecimal overallDecisionQuality; // Overall decision quality
    private BigDecimal decisionConsistency; // Consistency in decision making
    
    // Strength and weakness metrics
    private BigDecimal adaptabilityScore; // Strength: Measures how well a trader adapts to changing market conditions
    private BigDecimal overconfidenceIndex; // Weakness: Measures potential overconfidence in trading decisions
}
