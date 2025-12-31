package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for trade decision quality metrics including entry decision quality,
 * exit decision quality, and overall decision quality.
 */
@Component
public class DecisionQualityCalculator implements MetricsCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the overall decision quality
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal entryQuality = calculateEntryDecisionQuality(trades);
        BigDecimal exitQuality = calculateExitDecisionQuality(trades);
        
        // Overall decision quality is the average of entry and exit quality
        return entryQuality.add(exitQuality).divide(new BigDecimal("2"), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate entry decision quality based on entry price relative to optimal entry
     * and alignment with market conditions
     */
    public BigDecimal calculateEntryDecisionQuality(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalQualityScore = BigDecimal.ZERO;
        
        for (TradeDetails trade : trades) {
            // Calculate entry quality score (0-100) based on:
            // 1. How close entry was to optimal entry (if available)
            // 2. Whether entry aligned with market conditions
            // 3. Whether entry had proper risk management
            
            BigDecimal entryScore = new BigDecimal("50"); // Default middle score
            
            // If trade was profitable, entry decision was likely better
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null && 
                trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
                entryScore = entryScore.add(new BigDecimal("25"));
            }
            
            // If trade had proper risk management (stop loss set)
            if (trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null) {
                entryScore = entryScore.add(new BigDecimal("25"));
            }
            
            totalQualityScore = totalQualityScore.add(entryScore);
        }
        
        return totalQualityScore.divide(new BigDecimal(trades.size()), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate exit decision quality based on exit price relative to optimal exit
     * and profit target achievement
     */
    public BigDecimal calculateExitDecisionQuality(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalQualityScore = BigDecimal.ZERO;
        
        for (TradeDetails trade : trades) {
            // Calculate exit quality score (0-100) based on:
            // 1. How close exit was to optimal exit (if available)
            // 2. Whether profit target was achieved
            // 3. Whether stop loss was honored
            
            BigDecimal exitScore = new BigDecimal("50"); // Default middle score
            
            // If trade was profitable, exit decision was likely better
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null && 
                trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
                exitScore = exitScore.add(new BigDecimal("15"));
            }
            
            // If trade hit profit target (assuming rewardAmount in metrics represents the profit target)
            if (trade.getMetrics() != null && trade.getMetrics().getRewardAmount() != null && 
                trade.getExitInfo() != null && trade.getExitInfo().getPrice() != null && 
                trade.getMetrics().getProfitLoss() != null && 
                trade.getMetrics().getProfitLoss().compareTo(trade.getMetrics().getRewardAmount()) >= 0) {
                exitScore = exitScore.add(new BigDecimal("25"));
            }
            
            // If trade honored stop loss (didn't exit below stop loss)
            if (trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null && 
                trade.getExitInfo() != null && trade.getExitInfo().getPrice() != null && 
                trade.getMetrics().getProfitLoss() != null && 
                trade.getMetrics().getProfitLoss().compareTo(trade.getMetrics().getRiskAmount().negate()) > 0) {
                exitScore = exitScore.add(new BigDecimal("10"));
            }
            
            totalQualityScore = totalQualityScore.add(exitScore);
        }
        
        return totalQualityScore.divide(new BigDecimal(trades.size()), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate decision consistency across trades
     */
    public BigDecimal calculateDecisionConsistency(List<TradeDetails> trades) {
        if (trades == null || trades.size() < 2) {
            return BigDecimal.ZERO;
        }

        // Calculate standard deviation of decision quality scores
        // Lower standard deviation means higher consistency
        List<BigDecimal> entryScores = trades.stream()
                .map(this::calculateSingleTradeEntryQuality)
                .toList();
                
        List<BigDecimal> exitScores = trades.stream()
                .map(this::calculateSingleTradeExitQuality)
                .toList();
                
        BigDecimal entryStdDev = calculateStandardDeviation(entryScores);
        BigDecimal exitStdDev = calculateStandardDeviation(exitScores);
        
        // Convert standard deviation to consistency score (100 - normalized std dev)
        BigDecimal avgStdDev = entryStdDev.add(exitStdDev).divide(new BigDecimal("2"), SCALE, ROUNDING_MODE);
        BigDecimal maxPossibleStdDev = new BigDecimal("50"); // Max possible std dev on 0-100 scale
        
        BigDecimal normalizedStdDev = avgStdDev
                .multiply(new BigDecimal("100"))
                .divide(maxPossibleStdDev, SCALE, ROUNDING_MODE);
                
        return new BigDecimal("100").subtract(normalizedStdDev);
    }
    
    private BigDecimal calculateSingleTradeEntryQuality(TradeDetails trade) {
        BigDecimal entryScore = new BigDecimal("50");
        
        if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null && 
            trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
            entryScore = entryScore.add(new BigDecimal("25"));
        }
        
        if (trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null) {
            entryScore = entryScore.add(new BigDecimal("25"));
        }
        
        return entryScore;
    }
    
    private BigDecimal calculateSingleTradeExitQuality(TradeDetails trade) {
        BigDecimal exitScore = new BigDecimal("50");
        
        if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null && 
            trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
            exitScore = exitScore.add(new BigDecimal("15"));
        }
        
        if (trade.getMetrics() != null && trade.getMetrics().getRewardAmount() != null && 
            trade.getExitInfo() != null && trade.getExitInfo().getPrice() != null && 
            trade.getMetrics().getProfitLoss() != null && 
            trade.getMetrics().getProfitLoss().compareTo(trade.getMetrics().getRewardAmount()) >= 0) {
            exitScore = exitScore.add(new BigDecimal("25"));
        }
        
        if (trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null && 
            trade.getExitInfo() != null && trade.getExitInfo().getPrice() != null && 
            trade.getMetrics().getProfitLoss() != null && 
            trade.getMetrics().getProfitLoss().compareTo(trade.getMetrics().getRiskAmount().negate()) > 0) {
            exitScore = exitScore.add(new BigDecimal("10"));
        }
        
        return exitScore;
    }
    
    private BigDecimal calculateStandardDeviation(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate mean
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal mean = sum.divide(new BigDecimal(values.size()), SCALE, ROUNDING_MODE);
        
        // Calculate sum of squared differences
        BigDecimal sumSquaredDiff = values.stream()
                .map(value -> value.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate variance
        BigDecimal variance = sumSquaredDiff.divide(new BigDecimal(values.size()), SCALE, ROUNDING_MODE);
        
        // Calculate standard deviation (square root of variance)
        return new BigDecimal(Math.sqrt(variance.doubleValue()))
                .setScale(SCALE, ROUNDING_MODE);
    }

    @Override
    public String getMetricName() {
        return "Decision Quality";
    }
}
