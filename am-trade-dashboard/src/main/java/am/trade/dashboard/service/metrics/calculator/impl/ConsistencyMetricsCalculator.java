package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculator for trade consistency metrics including standard deviation of returns,
 * profit consistency, and drawdown metrics
 */
@Component
@Slf4j
public class ConsistencyMetricsCalculator implements MetricsCalculator {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the standard deviation of returns as the primary metric
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        List<BigDecimal> returns = extractReturns(trades);
        if (returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return calculateStandardDeviation(returns);
    }
    
    /**
     * Calculate consistency metrics and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateConsistencyMetrics(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        try {
            // Sort trades by exit timestamp
            List<TradeDetails> sortedTrades = trades.stream()
                    .filter(trade -> trade.getExitInfo() != null && 
                                    trade.getExitInfo().getTimestamp() != null &&
                                    trade.getMetrics() != null && 
                                    trade.getMetrics().getProfitLoss() != null)
                    .sorted(Comparator.comparing(t -> t.getExitInfo().getTimestamp()))
                    .collect(Collectors.toList());
                    
            if (sortedTrades.isEmpty()) {
                return;
            }
            
            // Extract returns
            List<BigDecimal> returns = extractReturns(sortedTrades);
            
            // Calculate standard deviation of returns
            BigDecimal stdDeviation = calculateStandardDeviation(returns);
            metrics.setReturnStandardDeviation(stdDeviation);
            log.debug("Standard deviation of returns: {}", stdDeviation);
            
            // Calculate profit consistency (percentage of profitable periods)
            BigDecimal profitConsistency = calculateProfitConsistency(returns);
            metrics.setProfitConsistency(profitConsistency);
            log.debug("Profit consistency: {}", profitConsistency);
            
            // Calculate maximum drawdown
            BigDecimal maxDrawdown = calculateMaxDrawdown(sortedTrades);
            metrics.setMaxDrawdown(maxDrawdown);
            log.debug("Maximum drawdown: {}", maxDrawdown);
            
        } catch (Exception e) {
            log.error("Error calculating consistency metrics", e);
        }
    }
    
    /**
     * Extract returns from trades
     * 
     * @param trades List of trades
     * @return List of returns as BigDecimal
     */
    private List<BigDecimal> extractReturns(List<TradeDetails> trades) {
        List<BigDecimal> returns = new ArrayList<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && 
                trade.getMetrics().getProfitLoss() != null && 
                trade.getMetrics().getProfitLossPercentage() != null) {
                
                // Use the profitLossPercentage which is already calculated as return
                returns.add(trade.getMetrics().getProfitLossPercentage().divide(new BigDecimal(100), SCALE, ROUNDING_MODE));
            }
        }
        
        return returns;
    }
    
    /**
     * Calculate standard deviation of a list of BigDecimal values
     * 
     * @param values List of values
     * @return Standard deviation as BigDecimal
     */
    private BigDecimal calculateStandardDeviation(List<BigDecimal> values) {
        if (values.isEmpty() || values.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Calculate mean
        BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(new BigDecimal(values.size()), SCALE, ROUNDING_MODE);
        
        // Calculate sum of squared differences
        BigDecimal sumSquaredDiff = values.stream()
                .map(value -> value.subtract(mean))
                .map(diff -> diff.multiply(diff))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate variance (sum of squared differences divided by n-1)
        BigDecimal variance = sumSquaredDiff.divide(new BigDecimal(values.size() - 1), SCALE, ROUNDING_MODE);
        
        // Calculate standard deviation (square root of variance)
        return new BigDecimal(Math.sqrt(variance.doubleValue()))
                .setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate profit consistency (percentage of profitable periods)
     * 
     * @param returns List of returns
     * @return Profit consistency as BigDecimal (0-1)
     */
    private BigDecimal calculateProfitConsistency(List<BigDecimal> returns) {
        if (returns.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        long profitablePeriods = returns.stream()
                .filter(r -> r.compareTo(BigDecimal.ZERO) > 0)
                .count();
                
        return new BigDecimal(profitablePeriods)
                .divide(new BigDecimal(returns.size()), SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate maximum drawdown from peak equity
     * 
     * @param trades List of trades sorted by exit time
     * @return Maximum drawdown as BigDecimal (positive value)
     */
    private BigDecimal calculateMaxDrawdown(List<TradeDetails> trades) {
        if (trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cumulativePnL = BigDecimal.ZERO;
        BigDecimal peak = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                // Add trade P&L to cumulative P&L
                cumulativePnL = cumulativePnL.add(trade.getMetrics().getProfitLoss());
                
                // Update peak if we have a new high
                if (cumulativePnL.compareTo(peak) > 0) {
                    peak = cumulativePnL;
                } 
                // Calculate drawdown if we're below peak
                else if (peak.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal drawdown = peak.subtract(cumulativePnL);
                    // Update max drawdown if this is larger
                    if (drawdown.compareTo(maxDrawdown) > 0) {
                        maxDrawdown = drawdown;
                    }
                }
            }
        }
        
        return maxDrawdown;
    }

    @Override
    public String getMetricName() {
        return "returnStandardDeviation";
    }
}
