package am.trade.dashboard.model.feeback;

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
