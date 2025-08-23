package am.trade.dashboard.service.metrics.analyzer.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.dashboard.service.metrics.analyzer.TradeMetricsAnalyzer;
import am.trade.dashboard.service.metrics.grouping.TradeGroupingResult;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Analyzer for calculating adaptability score
 * Measures how well a trader adapts to changing market conditions
 */
@Component
public class AdaptabilityAnalyzer implements TradeMetricsAnalyzer {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal analyze(List<TradeDetails> trades, TradeGroupingResult groupingResult) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate pattern diversity score (0-25 points)
        BigDecimal patternDiversityScore = calculatePatternDiversityScore(groupingResult);
        
        // Calculate pattern switching score (0-25 points)
        BigDecimal patternSwitchingScore = calculatePatternSwitchingScore(trades);
        
        // Calculate profitable adaptation score (0-25 points)
        BigDecimal profitableAdaptationScore = calculateProfitableAdaptationScore(trades);
        
        // Calculate market condition response score (0-25 points)
        BigDecimal marketConditionScore = calculateMarketConditionScore(trades);
        
        // Calculate final adaptability score (0-100)
        BigDecimal adaptabilityScore = patternDiversityScore
            .add(patternSwitchingScore)
            .add(profitableAdaptationScore)
            .add(marketConditionScore);
            
        // Cap at 100
        return adaptabilityScore.min(BigDecimal.valueOf(100)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate pattern diversity score
     * Higher score for using a diverse set of trading patterns
     */
    private BigDecimal calculatePatternDiversityScore(TradeGroupingResult groupingResult) {
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupingResult.getTradesByPattern();
        
        // Count number of different patterns used
        int patternCount = tradesByPattern.size();
        
        // Calculate diversity score (max 25 points)
        // More patterns = higher score, with diminishing returns
        return BigDecimal.valueOf(Math.min(25, patternCount * 5)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate pattern switching score
     * Higher score for effectively switching between patterns
     */
    private BigDecimal calculatePatternSwitchingScore(List<TradeDetails> trades) {
        if (trades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Sort trades by date
        List<TradeDetails> sortedTrades = trades.stream()
            .filter(t -> t.getTradeDate() != null && t.getPsychologyData() != null && 
                   t.getPsychologyData().getBehaviorPatterns() != null &&
                   !t.getPsychologyData().getBehaviorPatterns().isEmpty())
            .sorted(Comparator.comparing(TradeDetails::getTradeDate))
            .collect(Collectors.toList());
        
        if (sortedTrades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Count pattern switches
        int patternSwitches = 0;
        TradeBehaviorPattern lastPattern = sortedTrades.get(0).getPsychologyData().getBehaviorPatterns().get(0);
        
        for (int i = 1; i < sortedTrades.size(); i++) {
            List<TradeBehaviorPattern> currentPatterns = sortedTrades.get(i).getPsychologyData().getBehaviorPatterns();
            if (!currentPatterns.isEmpty() && !currentPatterns.contains(lastPattern)) {
                patternSwitches++;
                lastPattern = currentPatterns.get(0);
            }
        }
        
        // Calculate switch score (max 25 points)
        double switchRate = (double) patternSwitches / (sortedTrades.size() - 1);
        return BigDecimal.valueOf(Math.min(25, switchRate * 50)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate profitable adaptation score
     * Higher score for making profitable trades after losses
     */
    private BigDecimal calculateProfitableAdaptationScore(List<TradeDetails> trades) {
        if (trades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Sort trades by date
        List<TradeDetails> sortedTrades = trades.stream()
            .filter(t -> t.getTradeDate() != null && t.getMetrics() != null && 
                   t.getMetrics().getProfitLoss() != null)
            .sorted(Comparator.comparing(TradeDetails::getTradeDate))
            .collect(Collectors.toList());
        
        if (sortedTrades.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Count recoveries (profitable trade after losing trade)
        int recoveryCount = 0;
        boolean lastTradeWasLoss = false;
        
        for (TradeDetails trade : sortedTrades) {
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            
            if (lastTradeWasLoss && profitLoss.compareTo(BigDecimal.ZERO) > 0) {
                recoveryCount++;
            }
            
            lastTradeWasLoss = profitLoss.compareTo(BigDecimal.ZERO) < 0;
        }
        
        // Calculate recovery score (max 25 points)
        int possibleRecoveries = 0;
        for (int i = 0; i < sortedTrades.size() - 1; i++) {
            if (sortedTrades.get(i).getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
                possibleRecoveries++;
            }
        }
        
        if (possibleRecoveries == 0) {
            return BigDecimal.valueOf(25); // Perfect score if no losses to recover from
        }
        
        double recoveryRate = (double) recoveryCount / possibleRecoveries;
        return BigDecimal.valueOf(Math.min(25, recoveryRate * 25)).setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate market condition response score
     * Higher score for adapting to different market conditions
     */
    private BigDecimal calculateMarketConditionScore(List<TradeDetails> trades) {
        // This is a placeholder implementation
        // In a real system, you would analyze market conditions and how the trader responded
        return BigDecimal.valueOf(15); // Default moderate score
    }
    
    @Override
    public String getAnalyzerName() {
        return "Adaptability Score";
    }
}
