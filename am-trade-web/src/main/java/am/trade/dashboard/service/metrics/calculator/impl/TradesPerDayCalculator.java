package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculator for average trades per day metric
 */
@Component
public class TradesPerDayCalculator extends AbstractBigDecimalMetricCalculator {

    public TradesPerDayCalculator() {
        super(2, RoundingMode.HALF_UP); // Use 2 decimal places for this metric
    }

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        // Filter trades with valid timestamps
        List<TradeDetails> validTrades = trades.stream()
                .filter(trade -> trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null)
                .collect(Collectors.toList());
                
        if (validTrades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Sort by timestamp
        validTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Get first and last trade dates
        LocalDateTime firstTradeDate = validTrades.get(0).getEntryInfo().getTimestamp();
        LocalDateTime lastTradeDate = validTrades.get(validTrades.size() - 1).getEntryInfo().getTimestamp();
        
        // Calculate total trading days (minimum 1 day)
        long totalTradingDays = Math.max(1, Duration.between(firstTradeDate, lastTradeDate).toDays() + 1);
        
        // Calculate trades per day
        return BigDecimal.valueOf((double) validTrades.size() / totalTradingDays)
                .setScale(scale, roundingMode);
    }

    @Override
    public String getMetricName() {
        return "tradesPerDay";
    }
}
