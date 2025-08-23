package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculator for best and worst trading day metrics
 * This calculator computes multiple metrics at once and applies them directly to the metrics object
 */
@Component
@Slf4j
public class BestWorstDayCalculator implements MetricsCalculator {

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the best day profit as the primary metric
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Group trades by day and calculate daily P&L
        Map<LocalDate, BigDecimal> dailyProfitLoss = calculateDailyProfitLoss(trades);
        
        // Find the best day
        Optional<Map.Entry<LocalDate, BigDecimal>> bestDay = dailyProfitLoss.entrySet().stream()
                .max(Map.Entry.comparingByValue());
                
        return bestDay.map(Map.Entry::getValue).orElse(BigDecimal.ZERO);
    }
    
    /**
     * Calculate best and worst trading days and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateBestWorstDays(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        try {
            // Group trades by day and calculate daily P&L
            Map<LocalDate, BigDecimal> dailyProfitLoss = calculateDailyProfitLoss(trades);
            
            if (dailyProfitLoss.isEmpty()) {
                return;
            }
            
            // Find the best day
            Optional<Map.Entry<LocalDate, BigDecimal>> bestDay = dailyProfitLoss.entrySet().stream()
                    .max(Map.Entry.comparingByValue());
                    
            // Find the worst day
            Optional<Map.Entry<LocalDate, BigDecimal>> worstDay = dailyProfitLoss.entrySet().stream()
                    .min(Map.Entry.comparingByValue());
            
            // Apply to metrics object
            bestDay.ifPresent(day -> {
                metrics.setBestDayProfit(day.getValue());
                metrics.setBestDayDate(day.getKey().atStartOfDay());
                log.debug("Best day: {} with profit {}", day.getKey(), day.getValue());
            });
            
            worstDay.ifPresent(day -> {
                metrics.setWorstDayLoss(day.getValue().abs());
                metrics.setWorstDayDate(day.getKey().atStartOfDay());
                log.debug("Worst day: {} with loss {}", day.getKey(), day.getValue());
            });
            
        } catch (Exception e) {
            log.error("Error calculating best/worst days", e);
        }
    }
    
    /**
     * Group trades by day and calculate daily profit/loss
     * 
     * @param trades List of trades
     * @return Map of date to profit/loss for that day
     */
    private Map<LocalDate, BigDecimal> calculateDailyProfitLoss(List<TradeDetails> trades) {
        Map<LocalDate, BigDecimal> dailyProfitLoss = new HashMap<>();
        
        // Filter trades with valid data
        List<TradeDetails> validTrades = trades.stream()
                .filter(trade -> trade.getExitInfo() != null && 
                                trade.getExitInfo().getTimestamp() != null &&
                                trade.getMetrics() != null && 
                                trade.getMetrics().getProfitLoss() != null)
                .collect(Collectors.toList());
                
        // Group trades by exit date (when the profit/loss was realized)
        for (TradeDetails trade : validTrades) {
            LocalDateTime exitTime = trade.getExitInfo().getTimestamp();
            LocalDate exitDate = exitTime.toLocalDate();
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            
            // Add to daily total
            dailyProfitLoss.merge(exitDate, profitLoss, BigDecimal::add);
        }
        
        return dailyProfitLoss;
    }

    @Override
    public String getMetricName() {
        return "bestDayProfit";
    }
}
