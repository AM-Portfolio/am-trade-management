package am.trade.dashboard.service.metrics.analyzer.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.dashboard.service.metrics.analyzer.TradeMetricsAnalyzer;
import am.trade.dashboard.service.metrics.grouping.TradeGroupingResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Analyzer for calculating overconfidence index
 * Measures potential overconfidence in trading decisions
 */
@Component
public class OverconfidenceAnalyzer implements TradeMetricsAnalyzer {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal analyze(List<TradeDetails> trades, TradeGroupingResult groupingResult) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate overconfidence entry score (0-25 points)
        BigDecimal overconfidenceEntryScore = calculateOverconfidenceEntryScore(groupingResult);
        
        // Calculate position sizing score (0-25 points)
        BigDecimal positionSizingScore = calculatePositionSizingScore(trades);
        
        // Calculate impulsive trade score (0-25 points)
        BigDecimal impulsiveTradeScore = calculateImpulsiveTradeScore(trades, groupingResult);
        
        // Calculate stop loss adherence score (0-25 points)
        BigDecimal stopLossScore = calculateStopLossScore(trades);
        
        // Calculate final overconfidence index (0-100)
        BigDecimal overconfidenceIndex = overconfidenceEntryScore
            .add(positionSizingScore)
            .add(impulsiveTradeScore)
            .add(stopLossScore);
            
        // Cap at 100
        return overconfidenceIndex.min(BigDecimal.valueOf(100)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate overconfidence entry score
     * Higher score indicates more entries based on overconfidence
     */
    private BigDecimal calculateOverconfidenceEntryScore(TradeGroupingResult groupingResult) {
        int greedBasedEntryCount = groupingResult.getGreedBasedEntryCount();
        int totalEntryFactors = 0;
        
        for (List<TradeDetails> trades : groupingResult.getTradesByEntryPsychology().values()) {
            totalEntryFactors += trades.size();
        }
        
        if (totalEntryFactors == 0) {
            return BigDecimal.ZERO;
        }
        
        double overconfidenceRate = (double) greedBasedEntryCount / totalEntryFactors * 100;
        return BigDecimal.valueOf(Math.min(25, overconfidenceRate / 4)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate position sizing score
     * Higher score indicates increasing position sizes after wins (potential overconfidence)
     */
    private BigDecimal calculatePositionSizingScore(List<TradeDetails> trades) {
        if (trades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Sort trades by date
        List<TradeDetails> sortedTrades = trades.stream()
            .filter(t -> t.getTradeDate() != null && t.getMetrics() != null && 
                   t.getMetrics().getProfitLoss() != null && t.getEntryInfo().getQuantity() != null)
            .sorted(Comparator.comparing(TradeDetails::getTradeDate))
            .collect(Collectors.toList());
        
        if (sortedTrades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Count instances of increasing position size after winning trades
        int increasingAfterWinCount = 0;
        boolean lastTradeWasWin = false;
        BigDecimal lastPositionSize = null;
        
        for (TradeDetails trade : sortedTrades) {
            // Calculate position size from entry info
            BigDecimal currentPositionSize = null;
            if (trade.getEntryInfo() != null && trade.getEntryInfo().getQuantity() != null) {
                // Convert Integer to BigDecimal if needed
                Object quantityObj = trade.getEntryInfo().getQuantity();
                if (quantityObj instanceof Integer) {
                    currentPositionSize = new BigDecimal((Integer) quantityObj);
                } else if (quantityObj instanceof BigDecimal) {
                    currentPositionSize = (BigDecimal) quantityObj;
                }
            }
            
            if (lastTradeWasWin && lastPositionSize != null && currentPositionSize != null && 
                currentPositionSize.compareTo(lastPositionSize) > 0) {
                increasingAfterWinCount++;
            }
            
            // Update for next iteration
            lastTradeWasWin = trade.getMetrics() != null && 
                              trade.getMetrics().getProfitLoss() != null && 
                              trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0;
            lastPositionSize = currentPositionSize;
        }
        
        if (sortedTrades.size() <= 1) {
            return BigDecimal.ZERO;
        }
        
        double increasingPercentage = (double) increasingAfterWinCount / (sortedTrades.size() - 1) * 100;
        return BigDecimal.valueOf(Math.min(25, increasingPercentage / 4)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate impulsive trade score
     * Higher score indicates more impulsive trading decisions
     */
    private BigDecimal calculateImpulsiveTradeScore(List<TradeDetails> trades, TradeGroupingResult groupingResult) {
        int impulsiveTradeCount = groupingResult.getImpulsiveTradeCount();
        
        if (trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        double impulsivePercentage = (double) impulsiveTradeCount / trades.size() * 100;
        return BigDecimal.valueOf(Math.min(25, impulsivePercentage / 4)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate stop loss adherence score
     * Higher score indicates poor adherence to stop loss rules
     */
    private BigDecimal calculateStopLossScore(List<TradeDetails> trades) {
        // This is a placeholder implementation
        // In a real system, you would analyze stop loss adherence
        return BigDecimal.valueOf(15); // Default moderate score
    }
    
    @Override
    public String getAnalyzerName() {
        return "Overconfidence Index";
    }
}
