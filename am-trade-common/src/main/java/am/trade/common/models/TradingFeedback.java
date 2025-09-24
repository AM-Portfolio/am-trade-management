package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Model for providing personalized feedback based on trade psychology and behavior patterns
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradingFeedback {
    // General feedback
    private List<String> strengthAreas;           // Areas where the trader shows strength
    private List<String> improvementAreas;        // Areas where improvement is needed
    
    // Psychology-based feedback
    private Map<String, List<String>> psychologyInsights;  // Insights based on entry/exit psychology
    private List<String> emotionalPatternObservations;     // Observations about emotional patterns
    
    // Behavioral recommendations
    private List<String> recommendedBehaviorChanges;       // Suggested behavior changes
    private List<String> tradingHabitsToReinforce;         // Positive habits to reinforce
    
    // Pattern-based feedback
    private Map<String, String> patternSpecificFeedback;   // Feedback for specific trading patterns
    private List<String> successPatternRecommendations;    // Recommendations based on successful patterns
    
    // Decision quality feedback
    private String entryDecisionFeedback;                  // Feedback on entry decisions
    private String exitDecisionFeedback;                   // Feedback on exit decisions
    private String overallDecisionQualityFeedback;         // Overall decision quality feedback
    
    // Risk management feedback
    private String riskManagementFeedback;                 // Feedback on risk management approach
    private List<String> riskManagementSuggestions;        // Suggestions for improving risk management
}
