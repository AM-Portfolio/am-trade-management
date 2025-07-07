package am.trade.dashboard.service.metrics.feedback;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Results of risk management analysis
 */
@Data
@Builder
public class RiskManagementAnalysis {
    private String riskManagementFeedback;
    private List<String> riskManagementSuggestions;
}
