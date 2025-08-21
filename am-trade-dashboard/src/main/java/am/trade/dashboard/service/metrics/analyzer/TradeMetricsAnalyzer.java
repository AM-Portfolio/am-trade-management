package am.trade.dashboard.service.metrics.analyzer;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.grouping.TradeGroupingResult;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for trade metrics analyzers
 * This follows the Strategy Pattern to allow different analysis implementations
 */
public interface TradeMetricsAnalyzer {
    
    /**
     * Analyze trades and calculate a specific metric or score
     * 
     * @param trades List of trades to analyze
     * @param groupingResult Result of grouping trades by factors
     * @return The calculated score or metric
     */
    BigDecimal analyze(List<TradeDetails> trades, TradeGroupingResult groupingResult);
    
    /**
     * Get the name of this analyzer
     * 
     * @return The analyzer name
     */
    String getAnalyzerName();
}
