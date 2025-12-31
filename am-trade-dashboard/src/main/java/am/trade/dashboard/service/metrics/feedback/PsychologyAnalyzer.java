package am.trade.dashboard.service.metrics.feedback;

import am.trade.dashboard.model.feeback.PsychologyAnalysis;

/**
 * Interface for analyzing psychological aspects of trading
 */
public interface PsychologyAnalyzer extends TradeAnalyzer<PsychologyAnalysis> {
    // This interface inherits the analyze method from TradeAnalyzer
    // with PsychologyAnalysis as the return type
}
