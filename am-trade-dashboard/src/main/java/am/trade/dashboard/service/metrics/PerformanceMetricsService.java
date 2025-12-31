package am.trade.dashboard.service.metrics;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsRegistry;
import am.trade.dashboard.service.metrics.preprocessor.TradeDataPreprocessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for calculating performance metrics from trade data.
 * Uses a registry of metric calculators to compute various metrics.
 * This design allows for easy addition of new metrics without modifying this service.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PerformanceMetricsService {

    private final MetricsRegistry metricsRegistry;
    private final List<TradeDataPreprocessor> preprocessors;

    /**
     * Calculate performance metrics from a list of trades.
     * The calculation is delegated to specialized metric calculators registered in the metrics registry.
     * 
     * @param trades List of trade details to analyze
     * @return PerformanceMetrics object populated with calculated metrics
     */
    public PerformanceMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            log.info("No trades provided for metrics calculation");
            return new PerformanceMetrics();
        }
        
        log.info("Calculating performance metrics for {} trades", trades.size());
        
        // Create metrics object to populate
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        try {
            // Preprocess trade data if needed
            List<TradeDetails> processedTrades = preprocessTradeData(trades);
            
            // Calculate and apply all metrics using the registry
            metricsRegistry.calculateAndApplyMetrics(processedTrades, metrics);
            
            log.info("Successfully calculated performance metrics");
            return metrics;
            
        } catch (Exception e) {
            log.error("Error calculating performance metrics", e);
            return new PerformanceMetrics(); // Return empty metrics on error
        }
    }
    
    /**
     * Apply all registered preprocessors to the trade data
     * 
     * @param trades Original trade data
     * @return Processed trade data
     */
    private List<TradeDetails> preprocessTradeData(List<TradeDetails> trades) {
        List<TradeDetails> processedTrades = trades;
        
        for (TradeDataPreprocessor preprocessor : preprocessors) {
            try {
                processedTrades = preprocessor.process(processedTrades);
                log.debug("Applied preprocessor: {}", preprocessor.getClass().getSimpleName());
            } catch (Exception e) {
                log.warn("Error in preprocessor: {}", preprocessor.getClass().getSimpleName(), e);
            }
        }
        
        return processedTrades;
    }
}
