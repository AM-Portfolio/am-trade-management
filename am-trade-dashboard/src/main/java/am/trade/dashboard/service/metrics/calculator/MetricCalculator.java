package am.trade.dashboard.service.metrics.calculator;

import am.trade.common.models.TradeDetails;

import java.util.List;

/**
 * Interface for metric calculators that compute specific trading metrics
 * from a list of trades.
 */
public interface MetricCalculator<T> {
    
    /**
     * Calculate a specific metric from trade data
     * 
     * @param trades List of trade details to analyze
     * @return The calculated metric value
     */
    T calculate(List<TradeDetails> trades);
    
    /**
     * Get the name of this metric calculator
     * 
     * @return The name of the metric
     */
    String getMetricName();
}
