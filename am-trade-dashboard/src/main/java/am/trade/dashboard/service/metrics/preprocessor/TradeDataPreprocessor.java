package am.trade.dashboard.service.metrics.preprocessor;

import am.trade.common.models.TradeDetails;

import java.util.List;

/**
 * Interface for trade data preprocessors that prepare trade data for metric calculation.
 * Preprocessors can filter, sort, transform, or enrich trade data before metrics are calculated.
 */
public interface TradeDataPreprocessor {
    
    /**
     * Process a list of trade details
     * 
     * @param trades The original list of trade details
     * @return The processed list of trade details
     */
    List<TradeDetails> process(List<TradeDetails> trades);
    
    /**
     * Get the order in which this preprocessor should be executed.
     * Lower values are executed first.
     * 
     * @return The order value
     */
    default int getOrder() {
        return 0;
    }
}
