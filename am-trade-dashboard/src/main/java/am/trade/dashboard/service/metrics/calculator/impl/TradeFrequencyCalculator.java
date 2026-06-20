package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calculator for trade frequency metrics including trades per day/week/month
 * and volume metrics
 */
@Component
@Slf4j
public class TradeFrequencyCalculator implements MetricsCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the average trades per day as the primary metric
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return calculateAverageTradesPerDay(trades);
    }
    
    /**
     * Calculate trade frequency metrics and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateFrequencyMetrics(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        try {
            // Filter trades with valid data
            List<TradeDetails> validTrades = trades.stream()
                    .filter(trade -> trade.getEntryInfo() != null && 
                                    trade.getEntryInfo().getTimestamp() != null)
                    .sorted(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()))
                    .collect(Collectors.toList());
                    
            if (validTrades.isEmpty()) {
                return;
            }
            
            // Calculate average trades per day
            BigDecimal tradesPerDay = calculateAverageTradesPerDay(validTrades);
            metrics.setTradesPerDay(tradesPerDay);
            log.debug("Average trades per day: {}", tradesPerDay);
            
            // Calculate average trades per week
            BigDecimal tradesPerWeek = tradesPerDay.multiply(new BigDecimal(7))
                    .setScale(SCALE, ROUNDING_MODE);
            metrics.setTradesPerWeek(tradesPerWeek);
            log.debug("Average trades per week: {}", tradesPerWeek);
            
            // Calculate average trades per month
            BigDecimal tradesPerMonth = tradesPerDay.multiply(new BigDecimal(30))
                    .setScale(SCALE, ROUNDING_MODE);
            metrics.setTradesPerMonth(tradesPerMonth);
            log.debug("Average trades per month: {}", tradesPerMonth);
            
            // Calculate total trading days
            int tradingDays = calculateTradingDays(validTrades);
            metrics.setTradingDays(tradingDays);
            log.debug("Total trading days: {}", tradingDays);
            
            // Calculate average volume per trade
            BigDecimal avgVolumePerTrade = calculateAverageVolumePerTrade(validTrades);
            metrics.setAverageVolumePerTrade(avgVolumePerTrade);
            log.debug("Average volume per trade: {}", avgVolumePerTrade);
            
        } catch (Exception e) {
            log.error("Error calculating trade frequency metrics", e);
        }
    }
    
    /**
     * Calculate average trades per day
     * 
     * @param trades List of trades sorted by entry time
     * @return Average trades per day as BigDecimal
     */
    private BigDecimal calculateAverageTradesPerDay(List<TradeDetails> trades) {
        if (trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Get first and last trade dates
        LocalDate firstTradeDate = trades.get(0).getEntryInfo().getTimestamp().toLocalDate();
        LocalDate lastTradeDate = trades.get(trades.size() - 1).getEntryInfo().getTimestamp().toLocalDate();
        
        // Calculate total days between first and last trade
        long totalDays = ChronoUnit.DAYS.between(firstTradeDate, lastTradeDate) + 1;
        
        if (totalDays <= 0) {
            totalDays = 1; // Avoid division by zero
        }
        
        // Calculate average trades per day
        return new BigDecimal(trades.size())
                .divide(new BigDecimal(totalDays), SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate total number of trading days (days with at least one trade)
     * 
     * @param trades List of trades
     * @return Number of trading days
     */
    private int calculateTradingDays(List<TradeDetails> trades) {
        if (trades.isEmpty()) {
            return 0;
        }
        
        // Group trades by date
        Set<LocalDate> tradingDates = trades.stream()
                .map(trade -> trade.getEntryInfo().getTimestamp().toLocalDate())
                .collect(Collectors.toSet());
                
        return tradingDates.size();
    }
    
    /**
     * Calculate average volume per trade
     * 
     * @param trades List of trades
     * @return Average volume per trade as BigDecimal
     */
    private BigDecimal calculateAverageVolumePerTrade(List<TradeDetails> trades) {
        if (trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Sum up volumes from all trades
        BigDecimal totalVolume = BigDecimal.ZERO;
        int tradeCount = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getEntryInfo() != null && trade.getEntryInfo().getQuantity() != null) {
                totalVolume = totalVolume.add(new BigDecimal(trade.getEntryInfo().getQuantity()));
                tradeCount++;
            }
        }
        
        if (tradeCount > 0) {
            return totalVolume.divide(new BigDecimal(tradeCount), SCALE, ROUNDING_MODE);
        }
        
        return BigDecimal.ZERO;
    }

    @Override
    public String getMetricName() {
        return "tradesPerDay";
    }
}
