package am.trade.dashboard.model.feeback;

import lombok.Builder;
import lombok.Data;

/**
 * Results of trading decision analysis
 */
@Data
@Builder
public class DecisionAnalysis {
    private String entryDecisionFeedback;
    private String exitDecisionFeedback;
    private String overallDecisionQualityFeedback;
}
