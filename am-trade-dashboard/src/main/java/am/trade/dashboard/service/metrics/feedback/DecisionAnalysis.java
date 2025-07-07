package am.trade.dashboard.service.metrics.feedback;

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
