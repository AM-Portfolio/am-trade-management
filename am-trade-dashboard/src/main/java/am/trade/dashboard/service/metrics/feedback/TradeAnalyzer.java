package am.trade.dashboard.service.metrics.feedback;

import am.trade.common.models.TradeDetails;

import java.util.List;

/**
 * Base interface for all trade analyzers
 * @param <T> The type of analysis result produced by this analyzer
 */
public interface TradeAnalyzer<T> {
    
    /**
     * Analyze a list of trades and produce analysis results
     * 
     * @param trades List of trade details to analyze
     * @return Analysis results of type T
     */
    T analyze(List<TradeDetails> trades);
}
