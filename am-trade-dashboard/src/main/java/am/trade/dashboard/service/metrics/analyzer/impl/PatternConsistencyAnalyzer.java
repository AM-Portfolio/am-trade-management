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
 * Analyzer for pattern consistency metrics including pattern consistency score,
 * pattern execution quality, and pattern deviation rate.
 */
@Component
public class PatternConsistencyAnalyzer implements TradeMetricsAnalyzer {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal analyze(List<TradeDetails> trades, TradeGroupingResult groupingResult) {
        // This method returns the overall pattern consistency score
        if (trades == null || trades.isEmpty() || groupingResult == null) {
            return BigDecimal.ZERO;
        }

        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupingResult.getTradesByPattern();
        if (tradesByPattern == null || tradesByPattern.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Calculate pattern consistency score
        return calculatePatternConsistencyScore(trades, tradesByPattern);
    }

    /**
     * Calculate pattern consistency score based on how consistently patterns are executed
     */
    public BigDecimal calculatePatternConsistencyScore(
            List<TradeDetails> trades,
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        
        if (trades == null || trades.isEmpty() || tradesByPattern == null || tradesByPattern.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Calculate execution quality for each pattern
        Map<TradeBehaviorPattern, BigDecimal> executionQuality = calculatePatternExecutionQuality(tradesByPattern);
        
        // Calculate overall consistency as weighted average of execution quality
        BigDecimal totalQuality = BigDecimal.ZERO;
        BigDecimal totalWeight = BigDecimal.ZERO;
        
        for (Map.Entry<TradeBehaviorPattern, BigDecimal> entry : executionQuality.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            BigDecimal quality = entry.getValue();
            BigDecimal weight = new BigDecimal(tradesByPattern.get(pattern).size());
            
            totalQuality = totalQuality.add(quality.multiply(weight));
            totalWeight = totalWeight.add(weight);
        }
        
        if (totalWeight.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        
        return totalQuality.divide(totalWeight, SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate execution quality for each pattern
     */
    public Map<TradeBehaviorPattern, BigDecimal> calculatePatternExecutionQuality(
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        
        if (tradesByPattern == null || tradesByPattern.isEmpty()) {
            return Collections.emptyMap();
        }
        
        Map<TradeBehaviorPattern, BigDecimal> executionQuality = new HashMap<>();
        
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            if (patternTrades.size() < 2) {
                executionQuality.put(pattern, new BigDecimal("50")); // Default for patterns with few trades
                continue;
            }
            
            // Calculate consistency metrics for this pattern
            BigDecimal entryConsistency = calculateEntryConsistency(patternTrades);
            BigDecimal exitConsistency = calculateExitConsistency(patternTrades);
            BigDecimal riskConsistency = calculateRiskConsistency(patternTrades);
            
            // Calculate overall execution quality for this pattern
            BigDecimal patternQuality = entryConsistency.multiply(new BigDecimal("0.4"))
                    .add(exitConsistency.multiply(new BigDecimal("0.4")))
                    .add(riskConsistency.multiply(new BigDecimal("0.2")));
            
            executionQuality.put(pattern, patternQuality);
        }
        
        return executionQuality;
    }

    /**
     * Calculate pattern deviation rate - how often trader deviates from established patterns
     */
    public BigDecimal calculatePatternDeviationRate(List<TradeDetails> trades) {
        if (trades == null || trades.size() < 3) {
            return BigDecimal.ZERO;
        }
        
        // Sort trades by date
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Count pattern changes
        int patternChanges = 0;
        TradeBehaviorPattern previousPattern = sortedTrades.get(0).getPsychologyData().getBehaviorPatterns().get(0);
        
        for (int i = 1; i < sortedTrades.size(); i++) {
            TradeBehaviorPattern currentPattern = sortedTrades.get(i).getPsychologyData().getBehaviorPatterns().get(0);
            if (currentPattern != previousPattern) {
                patternChanges++;
                previousPattern = currentPattern;
            }
        }
        
        // Calculate deviation rate
        return new BigDecimal(patternChanges)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(sortedTrades.size() - 1), SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate consistency of entry execution for a pattern
     */
    private BigDecimal calculateEntryConsistency(List<TradeDetails> patternTrades) {
        if (patternTrades == null || patternTrades.size() < 2) {
            return new BigDecimal("50");
        }
        
        // Calculate standard deviation of entry prices
        List<BigDecimal> entryPrices = patternTrades.stream()
                .filter(trade -> trade.getEntryInfo() != null && trade.getEntryInfo().getPrice() != null)
                .map(trade -> trade.getEntryInfo().getPrice())
                .collect(Collectors.toList());
        
        // Normalize prices by dividing by the average price
        if (entryPrices.size() < 2) {
            return new BigDecimal("50");
        }
        
        BigDecimal avgPrice = entryPrices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(entryPrices.size()), SCALE, ROUNDING_MODE);
                
        List<BigDecimal> normalizedEntryPrices = entryPrices.stream()
                .map(price -> price.divide(avgPrice, SCALE, ROUNDING_MODE))
                .collect(Collectors.toList());
        
        if (normalizedEntryPrices.size() < 2) {
            return new BigDecimal("50");
        }
        
        BigDecimal stdDev = calculateStandardDeviation(normalizedEntryPrices);
        
        // Convert to consistency score (100 - normalized std dev)
        BigDecimal maxExpectedStdDev = new BigDecimal("0.1"); // 10% variation is max expected
        BigDecimal normalizedStdDev = stdDev.divide(maxExpectedStdDev, SCALE, ROUNDING_MODE)
                .multiply(new BigDecimal("100"));
        
        // Cap at 100
        BigDecimal consistencyScore = new BigDecimal("100").subtract(normalizedStdDev);
        return consistencyScore.max(BigDecimal.ZERO);
    }
    
    /**
     * Calculate consistency of exit execution for a pattern
     */
    private BigDecimal calculateExitConsistency(List<TradeDetails> patternTrades) {
        if (patternTrades == null || patternTrades.size() < 2) {
            return new BigDecimal("50");
        }
        
        // Calculate standard deviation of exit prices
        List<BigDecimal> exitPrices = patternTrades.stream()
                .filter(trade -> trade.getExitInfo() != null && trade.getExitInfo().getPrice() != null)
                .map(trade -> trade.getExitInfo().getPrice())
                .collect(Collectors.toList());
                
        // Normalize prices by dividing by the average price
        if (exitPrices.size() < 2) {
            return new BigDecimal("50");
        }
        
        BigDecimal avgPrice = exitPrices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(new BigDecimal(exitPrices.size()), SCALE, ROUNDING_MODE);
                
        List<BigDecimal> normalizedExitPrices = exitPrices.stream()
                .map(price -> price.divide(avgPrice, SCALE, ROUNDING_MODE))
                .collect(Collectors.toList());
        
        if (normalizedExitPrices.size() < 2) {
            return new BigDecimal("50");
        }
        
        BigDecimal stdDev = calculateStandardDeviation(normalizedExitPrices);
        
        // Convert to consistency score (100 - normalized std dev)
        BigDecimal maxExpectedStdDev = new BigDecimal("0.1"); // 10% variation is max expected
        BigDecimal normalizedStdDev = stdDev.divide(maxExpectedStdDev, SCALE, ROUNDING_MODE)
                .multiply(new BigDecimal("100"));
        
        // Cap at 100
        BigDecimal consistencyScore = new BigDecimal("100").subtract(normalizedStdDev);
        return consistencyScore.max(BigDecimal.ZERO);
    }
    
    /**
     * Calculate consistency of risk management for a pattern
     */
    private BigDecimal calculateRiskConsistency(List<TradeDetails> patternTrades) {
        if (patternTrades == null || patternTrades.size() < 2) {
            return new BigDecimal("50");
        }
        
        // Calculate standard deviation of risk percentages
        List<BigDecimal> riskPercentages = patternTrades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null)
                .map(trade -> {
                    // Calculate risk percentage from risk amount and entry value
                    BigDecimal riskAmount = trade.getMetrics().getRiskAmount();
                    BigDecimal entryValue = trade.getEntryInfo() != null && trade.getEntryInfo().getTotalValue() != null ?
                            trade.getEntryInfo().getTotalValue() : BigDecimal.ONE;
                    return riskAmount.divide(entryValue, SCALE, ROUNDING_MODE).multiply(new BigDecimal("100"));
                })
                .collect(Collectors.toList());

        if (riskPercentages.size() < 2) {
            return new BigDecimal("50");
        }
        
        BigDecimal stdDev = calculateStandardDeviation(riskPercentages);
        
        // Convert to consistency score (100 - normalized std dev)
        BigDecimal maxExpectedStdDev = new BigDecimal("1"); // 1% variation is max expected
        BigDecimal normalizedStdDev = stdDev.divide(maxExpectedStdDev, SCALE, ROUNDING_MODE)
                .multiply(new BigDecimal("100"));
        
        // Cap at 100
        BigDecimal consistencyScore = new BigDecimal("100").subtract(normalizedStdDev);
        return consistencyScore.max(BigDecimal.ZERO);
    }
    
    /**
     * Calculate standard deviation of a list of BigDecimal values
     */
    private BigDecimal calculateStandardDeviation(List<BigDecimal> values) {
        if (values == null || values.size() < 2) {
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
    public String getAnalyzerName() {
        return "Pattern Consistency";
    }
}
