package am.trade.dashboard.service.metrics.feedback;

/**
 * Interface for analyzing trading decisions
 */
public interface DecisionAnalyzer extends TradeAnalyzer<DecisionAnalysis> {
    // This interface inherits the analyze method from TradeAnalyzer
    // with DecisionAnalysis as the return type
}
