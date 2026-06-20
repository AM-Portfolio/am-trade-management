package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Calculator for largest winning and losing trades
 * This is a special calculator that calculates multiple metrics at once
 */
@Component
@Slf4j
public class LargestTradeCalculator implements MetricsCalculator {

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the largest winning trade as the primary metric
        // Both largest winning and losing trades are calculated in calculateLargestTrades
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        Optional<TradeDetails> largestWinningTrade = trades.stream()
                .filter(trade -> trade.getMetrics() != null && 
                                trade.getMetrics().getProfitLoss() != null &&
                                trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .max(Comparator.comparing(trade -> trade.getMetrics().getProfitLoss()));
                
        return largestWinningTrade.map(trade -> trade.getMetrics().getProfitLoss())
                .orElse(BigDecimal.ZERO);
    }
    
    /**
     * Calculate largest winning and losing trades and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateLargestTrades(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        try {
            // Find largest winning trade
            Optional<TradeDetails> largestWinningTrade = trades.stream()
                    .filter(trade -> trade.getMetrics() != null && 
                                    trade.getMetrics().getProfitLoss() != null &&
                                    trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                    .max(Comparator.comparing(trade -> trade.getMetrics().getProfitLoss()));
                    
            // Find largest losing trade (most negative)
            Optional<TradeDetails> largestLosingTrade = trades.stream()
                    .filter(trade -> trade.getMetrics() != null && 
                                    trade.getMetrics().getProfitLoss() != null &&
                                    trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0)
                    .min(Comparator.comparing(trade -> trade.getMetrics().getProfitLoss()));
            
            // Apply to metrics object
            largestWinningTrade.ifPresent(trade -> {
                metrics.setLargestWinningTrade(trade.getMetrics().getProfitLoss());
                log.debug("Largest winning trade: {}", trade.getMetrics().getProfitLoss());
            });
            
            largestLosingTrade.ifPresent(trade -> {
                // Store as absolute value for consistency
                metrics.setLargestLosingTrade(trade.getMetrics().getProfitLoss().abs());
                log.debug("Largest losing trade: {}", trade.getMetrics().getProfitLoss());
            });
            
        } catch (Exception e) {
            log.error("Error calculating largest trades", e);
        }
    }

    @Override
    public String getMetricName() {
        return "largestWinningTrade";
    }
}
