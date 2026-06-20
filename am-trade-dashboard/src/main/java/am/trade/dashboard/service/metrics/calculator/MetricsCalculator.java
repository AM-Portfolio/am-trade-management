package am.trade.dashboard.service.metrics.calculator;

import am.trade.common.models.TradeDetails;

import java.math.BigDecimal;
import java.util.List;

/**
 * Interface for all metrics calculators
 * This follows the Strategy Pattern to allow different calculation implementations
 */
public interface MetricsCalculator {
    
    /**
     * Calculate a specific metric from a list of trades
     * 
     * @param trades List of trades to analyze
     * @return The calculated metric value
     */
    BigDecimal calculate(List<TradeDetails> trades);
    
    /**
     * Get the name of this calculator
     * 
     * @return The calculator name
     */
    String getMetricName();
}
