package am.trade.dashboard.service.metrics.preprocessor.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.preprocessor.TradeDataPreprocessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Preprocessor that sorts trades chronologically by entry timestamp.
 * This ensures that time-based metrics like streaks are calculated correctly.
 */
@Component
@Slf4j
public class ChronologicalSortPreprocessor implements TradeDataPreprocessor {

    @Override
    public List<TradeDetails> process(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return trades;
        }
        
        log.debug("Sorting {} trades chronologically", trades.size());
        
        // Create a new list to avoid modifying the original
        List<TradeDetails> validTrades = trades.stream()
                .filter(trade -> trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null)
                .collect(Collectors.toList());
        
        if (validTrades.isEmpty()) {
            log.warn("No trades with valid entry timestamps found");
            return trades;
        }
        
        // Sort by entry timestamp
        validTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Add any trades that didn't have timestamps at the end
        List<TradeDetails> result = new ArrayList<>(validTrades);
        trades.stream()
                .filter(trade -> trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null)
                .forEach(result::add);
        
        return result;
    }
    
    @Override
    public int getOrder() {
        // Execute this preprocessor early
        return 10;
    }
}
