package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculator for the current winning or losing streak
 * Returns a positive number for winning streak, negative for losing streak
 */
@Component
@Slf4j
public class CurrentStreakCalculator implements MetricsCalculator {

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Sort trades by exit timestamp to ensure chronological order
        List<TradeDetails> sortedTrades = trades.stream()
                .filter(trade -> trade.getExitInfo() != null && 
                                trade.getExitInfo().getTimestamp() != null &&
                                trade.getMetrics() != null && 
                                trade.getMetrics().getProfitLoss() != null)
                .sorted(Comparator.comparing(t -> t.getExitInfo().getTimestamp()))
                .collect(Collectors.toList());
        
        if (sortedTrades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Start from the most recent trade and count backwards
        int currentStreak = 0;
        boolean isWinning = false;
        boolean isFirst = true;
        
        // Iterate from the most recent trade backwards
        for (int i = sortedTrades.size() - 1; i >= 0; i--) {
            TradeDetails trade = sortedTrades.get(i);
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            boolean isTradeWinning = profitLoss.compareTo(BigDecimal.ZERO) > 0;
            
            if (isFirst) {
                // First trade (most recent)
                isWinning = isTradeWinning;
                currentStreak = 1;
                isFirst = false;
            } else if (isTradeWinning == isWinning) {
                // Continuing the streak
                currentStreak++;
            } else {
                // Streak broken
                break;
            }
        }
        
        // Return positive value for winning streak, negative for losing streak
        return BigDecimal.valueOf(isWinning ? currentStreak : -currentStreak);
    }

    @Override
    public String getMetricName() {
        return "currentStreak";
    }
}
