package am.trade.dashboard.service.metrics.preprocessor.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.preprocessor.TradeDataPreprocessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Preprocessor that validates trade data and filters out invalid trades.
 * This ensures that only valid trades are used for metric calculations.
 */
@Component
@Slf4j
public class DataValidationPreprocessor implements TradeDataPreprocessor {

    @Override
    public List<TradeDetails> process(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return trades;
        }
        
        log.debug("Validating {} trades", trades.size());
        
        List<TradeDetails> validTrades = trades.stream()
                .filter(this::isValidTrade)
                .collect(Collectors.toList());
        
        int invalidCount = trades.size() - validTrades.size();
        if (invalidCount > 0) {
            log.warn("Filtered out {} invalid trades", invalidCount);
        }
        
        return validTrades;
    }
    
    /**
     * Check if a trade has the minimum required data for metric calculations
     * 
     * @param trade The trade to validate
     * @return True if the trade is valid, false otherwise
     */
    private boolean isValidTrade(TradeDetails trade) {
        if (trade == null) {
            return false;
        }
        
        // Check if trade has metrics data
        if (trade.getMetrics() == null) {
            return false;
        }
        
        // Check if trade has profit/loss data
        if (trade.getMetrics().getProfitLoss() == null) {
            return false;
        }
        
        // Check if trade has entry information
        if (trade.getEntryInfo() == null) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int getOrder() {
        // Execute this preprocessor first
        return 0;
    }
}
