package am.trade.dashboard.service.metrics.feedback;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Results of trading pattern analysis
 */
@Data
@Builder
public class PatternAnalysis {
    private List<String> recommendedBehaviorChanges;
    private List<String> positiveHabits;
    private Map<String, String> patternSpecificFeedback;
    private List<String> successPatternRecommendations;
}
