package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculator for average winning and losing trade metrics
 * This calculator computes multiple metrics at once and applies them directly to the metrics object
 */
@Component
@Slf4j
public class AverageTradeCalculator extends AbstractBigDecimalMetricCalculator {

    public AverageTradeCalculator() {
        super(2, RoundingMode.HALF_UP);
    }

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        // This method returns the average trade value as the primary metric
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        int tradeCount = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                totalProfitLoss = totalProfitLoss.add(trade.getMetrics().getProfitLoss());
                tradeCount++;
            }
        }
        
        if (tradeCount > 0) {
            return safeDivide(totalProfitLoss, new BigDecimal(tradeCount));
        }
        
        return BigDecimal.ZERO;
    }
    
    /**
     * Calculate average winning and losing trades and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateAverageTrades(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        try {
            // Filter trades with valid profit/loss data
            List<TradeDetails> validTrades = trades.stream()
                    .filter(trade -> trade.getMetrics() != null && 
                                    trade.getMetrics().getProfitLoss() != null)
                    .collect(Collectors.toList());
                    
            if (validTrades.isEmpty()) {
                return;
            }
            
            // Separate winning and losing trades
            List<TradeDetails> winningTrades = validTrades.stream()
                    .filter(trade -> trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());
                    
            List<TradeDetails> losingTrades = validTrades.stream()
                    .filter(trade -> trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0)
                    .collect(Collectors.toList());
            
            // Calculate average winning trade
            if (!winningTrades.isEmpty()) {
                BigDecimal totalWinnings = winningTrades.stream()
                        .map(trade -> trade.getMetrics().getProfitLoss())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                BigDecimal averageWinningTrade = safeDivide(totalWinnings, new BigDecimal(winningTrades.size()));
                metrics.setAverageWinningTrade(averageWinningTrade);
                log.debug("Average winning trade: {}", averageWinningTrade);
            }
            
            // Calculate average losing trade (as a positive number for consistency)
            if (!losingTrades.isEmpty()) {
                BigDecimal totalLosses = losingTrades.stream()
                        .map(trade -> trade.getMetrics().getProfitLoss().abs())
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                        
                BigDecimal averageLosingTrade = safeDivide(totalLosses, new BigDecimal(losingTrades.size()));
                metrics.setAverageLosingTrade(averageLosingTrade);
                log.debug("Average losing trade: {}", averageLosingTrade);
            }
            
        } catch (Exception e) {
            log.error("Error calculating average trades", e);
        }
    }

    @Override
    public String getMetricName() {
        return "averageTrade";
    }
}
