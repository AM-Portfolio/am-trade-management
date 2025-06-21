package am.trade.kafka.service.metrics.calculator;

import am.trade.models.document.Trade;

import java.util.List;

/**
 * Generic interface for metrics calculators
 * 
 * @param <T> The type of metrics object this calculator produces
 */
public interface MetricsCalculator<T> {
    
    /**
     * Calculate metrics based on a list of trades
     * 
     * @param trades List of trades to analyze
     * @return Calculated metrics object of type T
     */
    T calculate(List<Trade> trades);
}
