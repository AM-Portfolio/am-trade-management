package am.trade.dashboard.service.metrics.feedback;

import am.trade.dashboard.model.feeback.PatternAnalysis;

/**
 * Interface for analyzing trading patterns
 */
public interface PatternAnalyzer extends TradeAnalyzer<PatternAnalysis> {
    // This interface inherits the analyze method from TradeAnalyzer
    // with PatternAnalysis as the return type
}
