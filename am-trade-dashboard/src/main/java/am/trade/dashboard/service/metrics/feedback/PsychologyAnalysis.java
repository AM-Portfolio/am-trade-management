package am.trade.dashboard.service.metrics.feedback;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * Results of psychological analysis of trading behavior
 */
@Data
@Builder
public class PsychologyAnalysis {
    private List<String> strengthAreas;
    private List<String> improvementAreas;
    private Map<String, List<String>> psychologyInsights;
    private List<String> emotionalPatternObservations;
}
