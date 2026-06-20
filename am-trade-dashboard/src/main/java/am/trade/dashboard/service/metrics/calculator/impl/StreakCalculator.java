package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Calculator for winning and losing streaks.
 * This calculator is special as it calculates multiple metrics at once.
 */
@Component
@Slf4j
public class StreakCalculator implements MetricsCalculator {

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method is not used directly as this calculator sets multiple metrics
        // Return a placeholder value
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate streak metrics and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateStreaks(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        int currentWinStreak = 0;
        int currentLossStreak = 0;
        int maxWinStreak = 0;
        int maxLossStreak = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null) {
                continue;
            }
            
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            int comparison = profitLoss.compareTo(BigDecimal.ZERO);
            
            if (comparison > 0) {
                // Win
                currentWinStreak++;
                currentLossStreak = 0;
                maxWinStreak = Math.max(maxWinStreak, currentWinStreak);
            } else if (comparison < 0) {
                // Loss
                currentLossStreak++;
                currentWinStreak = 0;
                maxLossStreak = Math.max(maxLossStreak, currentLossStreak);
            } else {
                // Break even
                currentWinStreak = 0;
                currentLossStreak = 0;
            }
        }
        
        // Set streak metrics
        metrics.setLongestWinningStreak(maxWinStreak);
        metrics.setLongestLosingStreak(maxLossStreak);
        
        // Also set current streaks if needed
        // Note: These fields would need to be added to the PerformanceMetrics class
        // metrics.setCurrentWinStreak(currentWinStreak);
        // metrics.setCurrentLossStreak(currentLossStreak);
    }

    @Override
    public String getMetricName() {
        return "longestWinningStreak";
    }
    
    /**
     * Get all metrics calculated by this calculator
     * 
     * @return Map of metric name to setter function
     */
    public Map<String, BiConsumer<PerformanceMetrics, BigDecimal>> getMetricSetters() {
        return Map.of(
            "longestWinningStreak", (metrics, value) -> metrics.setLongestWinningStreak(value.intValue()),
            "longestLosingStreak", (metrics, value) -> metrics.setLongestLosingStreak(value.intValue())
        );
    }
}
