package am.trade.dashboard.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradePatternMetrics;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating trade pattern and psychology metrics from trade data
 */
@Service
@Slf4j
public class TradePatternMetricsService {

    private static final int SCALE = 4;
    private static final java.math.RoundingMode ROUNDING_MODE = HALF_UP;

    /**
     * Calculate pattern metrics from a list of trades
     */
    /**
     * Calculate pattern metrics from a list of trades
     */
    public TradePatternMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new TradePatternMetrics();
        }
        
        TradePatternMetrics metrics = new TradePatternMetrics();
        int totalTrades = trades.size();
        
        // Group trades and count psychology factors
        TradeGroupingResult groupingResult = groupTradesByFactors(trades);
        
        // Calculate metrics for each group
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupingResult.getTradesByPattern();
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = groupingResult.getTradesByEntryPsychology();
        Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = groupingResult.getTradesByExitPsychology();
        
        // Calculate pattern metrics
        Map<TradeBehaviorPattern, Integer> patternFrequency = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternProfitLoss = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternWinRate = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternExpectancy = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternRiskRewardRatio = new HashMap<>();
        
        // Calculate entry psychology metrics
        Map<EntryPsychology, Integer> entryPsychologyFrequency = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyProfitLoss = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyWinRate = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyExpectancy = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyRiskRewardRatio = new HashMap<>();
        
        // Calculate exit psychology metrics
        Map<ExitPsychology, Integer> exitPsychologyFrequency = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyProfitLoss = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyWinRate = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyExpectancy = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyRiskRewardRatio = new HashMap<>();
        
        // Process pattern metrics
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            // Calculate frequency
            patternFrequency.put(pattern, patternTrades.size());
            
            // Calculate profit/loss
            BigDecimal totalProfit = patternTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .map(t -> t.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
            patternProfitLoss.put(pattern, totalProfit);
            
            // Calculate win rate
            long winCount = patternTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                       t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
            
            BigDecimal winRateValue = patternTrades.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(winCount * 100.0 / patternTrades.size()).setScale(2, ROUNDING_MODE);
            patternWinRate.put(pattern, winRateValue);
            
            // Calculate expectancy
            patternExpectancy.put(pattern, calculateTradeExpectancy(patternTrades));
            
            // Calculate risk/reward ratio
            patternRiskRewardRatio.put(pattern, calculateRiskRewardRatio(patternTrades));
        }
        
        // Process entry psychology metrics
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            EntryPsychology factor = entry.getKey();
            List<TradeDetails> factorTrades = entry.getValue();
            
            // Calculate frequency
            entryPsychologyFrequency.put(factor, factorTrades.size());
            
            // Calculate profit/loss
            BigDecimal totalProfit = factorTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .map(t -> t.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
            entryPsychologyProfitLoss.put(factor, totalProfit);
            
            // Calculate win rate
            long winCount = factorTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                       t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
            
            BigDecimal winRateValue = factorTrades.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(winCount * 100.0 / factorTrades.size()).setScale(2, ROUNDING_MODE);
            entryPsychologyWinRate.put(factor, winRateValue);
            
            // Calculate expectancy
            entryPsychologyExpectancy.put(factor, calculateTradeExpectancy(factorTrades));
            
            // Calculate risk/reward ratio
            entryPsychologyRiskRewardRatio.put(factor, calculateRiskRewardRatio(factorTrades));
        }
        
        // Process exit psychology metrics
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            ExitPsychology factor = entry.getKey();
            List<TradeDetails> factorTrades = entry.getValue();
            
            // Calculate frequency
            exitPsychologyFrequency.put(factor, factorTrades.size());
            
            // Calculate profit/loss
            BigDecimal totalProfit = factorTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .map(t -> t.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, ROUNDING_MODE);
            exitPsychologyProfitLoss.put(factor, totalProfit);
            
            // Calculate win rate
            long winCount = factorTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                       t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
            
            BigDecimal winRateValue = factorTrades.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(winCount * 100.0 / factorTrades.size()).setScale(2, ROUNDING_MODE);
            exitPsychologyWinRate.put(factor, winRateValue);
            
            // Calculate expectancy
            exitPsychologyExpectancy.put(factor, calculateTradeExpectancy(factorTrades));
            
            // Calculate risk/reward ratio
            exitPsychologyRiskRewardRatio.put(factor, calculateRiskRewardRatio(factorTrades));
        }
        
        // Find top and bottom patterns
        List<Map.Entry<TradeBehaviorPattern, BigDecimal>> sortedPatternsByProfit = new ArrayList<>(patternProfitLoss.entrySet());
        sortedPatternsByProfit.sort(Map.Entry.<TradeBehaviorPattern, BigDecimal>comparingByValue().reversed());
        
        List<TradeBehaviorPattern> mostProfitablePatterns = sortedPatternsByProfit.stream()
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<TradeBehaviorPattern> leastProfitablePatterns = sortedPatternsByProfit.stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        // Get top and bottom entry psychology factors
        List<Map.Entry<EntryPsychology, BigDecimal>> sortedEntryPsychologyByProfit = 
                new ArrayList<>(entryPsychologyProfitLoss.entrySet());
        sortedEntryPsychologyByProfit.sort(Map.Entry.<EntryPsychology, BigDecimal>comparingByValue().reversed());
        
        List<EntryPsychology> mostProfitableEntryPsychology = sortedEntryPsychologyByProfit.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<EntryPsychology> leastProfitableEntryPsychology = sortedEntryPsychologyByProfit.stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
                
        // Get top and bottom exit psychology factors
        List<Map.Entry<ExitPsychology, BigDecimal>> sortedExitPsychologyByProfit = 
                new ArrayList<>(exitPsychologyProfitLoss.entrySet());
        sortedExitPsychologyByProfit.sort(Map.Entry.<ExitPsychology, BigDecimal>comparingByValue().reversed());
        
        List<ExitPsychology> mostProfitableExitPsychology = sortedExitPsychologyByProfit.stream()
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        List<ExitPsychology> leastProfitableExitPsychology = sortedExitPsychologyByProfit.stream()
                .sorted(Map.Entry.comparingByValue())
                .limit(3)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        
        // Calculate emotional control metrics
        BigDecimal fearBasedExitPercentage = totalTrades > 0 ? 
                BigDecimal.valueOf(groupingResult.getFearBasedExitCount() * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        BigDecimal greedBasedEntryPercentage = totalTrades > 0 ? 
                BigDecimal.valueOf(groupingResult.getGreedBasedEntryCount() * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        // Calculate impulsivity metrics
        BigDecimal impulsivityScore = BigDecimal.ZERO;
        if (totalTrades > 0) {
            impulsivityScore = BigDecimal.valueOf(groupingResult.getImpulsiveTradeCount() * 100.0 / totalTrades)
                    .setScale(2, ROUNDING_MODE);
        }
        
        // Set pattern metrics in result object
        metrics.setPatternFrequency(patternFrequency);
        metrics.setPatternProfitLoss(patternProfitLoss);
        metrics.setPatternWinRate(patternWinRate);
        metrics.setPatternExpectancy(patternExpectancy);
        metrics.setPatternRiskRewardRatio(patternRiskRewardRatio);
        metrics.setMostProfitablePatterns(mostProfitablePatterns);
        metrics.setLeastProfitablePatterns(leastProfitablePatterns);
        
        // Set entry psychology metrics
        metrics.setEntryPsychologyFrequency(entryPsychologyFrequency);
        metrics.setEntryPsychologyProfitLoss(entryPsychologyProfitLoss);
        metrics.setEntryPsychologyWinRate(entryPsychologyWinRate);
        metrics.setMostProfitableEntryPsychology(mostProfitableEntryPsychology);
        metrics.setLeastProfitableEntryPsychology(leastProfitableEntryPsychology);
        
        // Set exit psychology metrics
        metrics.setExitPsychologyFrequency(exitPsychologyFrequency);
        //metrics.setExitPsychologyProfitLoss(exitPsychologyProfitLoss);
        //metrics.setExitPsychologyWinRate(exitPsychologyWinRate);
        metrics.setMostProfitableExitPsychology(mostProfitableExitPsychology);
        metrics.setLeastProfitableExitPsychology(leastProfitableExitPsychology);
        
        // Set emotional metrics
        //metrics.setEmotionalControlScore(emotionalControl);
        // metrics.setDisciplineScore(disciplineScore);
        //metrics.setImpulsivityScore(impulsivityScore);
        
        // Calculate and set new strength and weakness metrics
        BigDecimal adaptabilityScore = calculateAdaptabilityScore(trades, tradesByPattern);
        BigDecimal overconfidenceIndex = calculateOverconfidenceIndex(trades, groupingResult);
        
        metrics.setAdaptabilityScore(adaptabilityScore);
        metrics.setOverconfidenceIndex(overconfidenceIndex);
        
        return metrics;
    }
    
    /**
     * Calculate frequency of patterns/factors as percentage of total trades
     */
    private Map<String, BigDecimal> calculateFrequency(Map<String, List<TradeDetails>> tradesByCategory, int totalTrades) {
        Map<String, BigDecimal> frequency = new HashMap<>();
        
        if (totalTrades > 0) {
            for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
                String category = entry.getKey();
                int count = entry.getValue().size();
                
                BigDecimal frequencyPercentage = BigDecimal.valueOf(count * 100.0 / totalTrades)
                        .setScale(2, ROUNDING_MODE);
                
                frequency.put(category, frequencyPercentage);
            }
        }
        
        return frequency;
    }
    
    /**
     * Calculate profitability of patterns/factors
     */
    private Map<String, BigDecimal> calculateProfitability(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> profitability = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            String category = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            BigDecimal totalProfit = trades.stream()
                    .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                    .map(t -> t.getMetrics().getProfitLoss())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            profitability.put(category, totalProfit);
        }
        
        return profitability;
    }
    
    /**
     * Calculate win rate of patterns/factors
     */
    private Map<String, BigDecimal> calculateWinRate(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> winRate = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            String category = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            long winCount = trades.stream()
                    .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                           t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            
            BigDecimal winRatePercentage = trades.isEmpty() ? BigDecimal.ZERO :
                    BigDecimal.valueOf(winCount * 100.0 / trades.size()).setScale(2, ROUNDING_MODE);
            
            winRate.put(category, winRatePercentage);
        }
        
        return winRate;
    }
    
    /**
     * Calculate trade expectancy for a list of trades
     */
    private BigDecimal calculateTradeExpectancy(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate win rate
        long winCount = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                       t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        BigDecimal winProbability = BigDecimal.valueOf((double) winCount / trades.size());
        BigDecimal lossProbability = BigDecimal.ONE.subtract(winProbability);
        
        // Calculate average win and loss
        BigDecimal totalWin = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        int winCounter = 0;
        int lossCounter = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
                
                if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
                    totalWin = totalWin.add(profitLoss);
                    winCounter++;
                } else if (profitLoss.compareTo(BigDecimal.ZERO) < 0) {
                    totalLoss = totalLoss.add(profitLoss.abs());
                    lossCounter++;
                }
            }
        }
        
        BigDecimal averageWin = winCounter > 0 ?
                totalWin.divide(BigDecimal.valueOf(winCounter), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        BigDecimal averageLoss = lossCounter > 0 ?
                totalLoss.divide(BigDecimal.valueOf(lossCounter), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate expectancy
        return winProbability.multiply(averageWin)
                .subtract(lossProbability.multiply(averageLoss))
                .setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate risk-reward ratio for a list of trades
     */
    private BigDecimal calculateRiskRewardRatio(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Calculate average win and loss
        BigDecimal totalWin = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        int winCounter = 0;
        int lossCounter = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
                
                if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
                    totalWin = totalWin.add(profitLoss);
                    winCounter++;
                } else if (profitLoss.compareTo(BigDecimal.ZERO) < 0) {
                    totalLoss = totalLoss.add(profitLoss.abs());
                    lossCounter++;
                }
            }
        }
        
        BigDecimal averageWin = winCounter > 0 ?
                totalWin.divide(BigDecimal.valueOf(winCounter), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        BigDecimal averageLoss = lossCounter > 0 ?
                totalLoss.divide(BigDecimal.valueOf(lossCounter), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate risk/reward ratio
        if (averageLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // Avoid division by zero
        }
        
        return averageWin.divide(averageLoss, SCALE, ROUNDING_MODE);
    }
    
    /**
     * Group trades by behavior patterns and psychology factors
     */
    private TradeGroupingResult groupTradesByFactors(List<TradeDetails> trades) {
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = new HashMap<>();
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = new HashMap<>();
        Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = new HashMap<>();
        
        // Counters for psychology factors
        int fearBasedExitCount = 0;
        int greedBasedEntryCount = 0;
        int impulsiveTradeCount = 0;
        int disciplinedTradeCount = 0;
        
        // Process each trade
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() == null) {
                continue;
            }
            
            // Group by behavior pattern
            if (trade.getPsychologyData().getBehaviorPatterns() != null) {
                for (TradeBehaviorPattern pattern : trade.getPsychologyData().getBehaviorPatterns()) {
                    tradesByPattern.computeIfAbsent(pattern, k -> new ArrayList<>()).add(trade);
                }
            }
            
            // Group by entry psychology factors
            if (trade.getPsychologyData().getEntryPsychologyFactors() != null) {
                for (EntryPsychology factor : trade.getPsychologyData().getEntryPsychologyFactors()) {
                    tradesByEntryPsychology.computeIfAbsent(factor, k -> new ArrayList<>()).add(trade);
                    
                    // Count specific psychology factors for emotional control metrics
                    if (factor.equals(EntryPsychology.OVERCONFIDENCE) || 
                        factor.equals(EntryPsychology.FEAR_OF_MISSING_OUT)) {
                        greedBasedEntryCount++;
                    }
                    
                    if (!factor.equals(EntryPsychology.FOLLOWING_THE_PLAN)) {
                        impulsiveTradeCount++;
                    }
                }
            }
            
            // Group by exit psychology factors
            if (trade.getPsychologyData().getExitPsychologyFactors() != null) {
                for (ExitPsychology factor : trade.getPsychologyData().getExitPsychologyFactors()) {
                    tradesByExitPsychology.computeIfAbsent(factor, k -> new ArrayList<>()).add(trade);
                    
                    // Count specific psychology factors for emotional control metrics
                    if (factor.equals(ExitPsychology.FEAR) || 
                        factor.equals(ExitPsychology.PANIC)) {
                        fearBasedExitCount++;
                    }
                    
                    if (factor.equals(ExitPsychology.DISCIPLINE) || 
                        factor.equals(ExitPsychology.TAKING_PROFITS) || 
                        factor.equals(ExitPsychology.CUTTING_LOSSES)) {
                        disciplinedTradeCount++;
                    }
                }
            }
        }
        
        return new TradeGroupingResult(
                tradesByPattern,
                tradesByEntryPsychology,
                tradesByExitPsychology,
                fearBasedExitCount,
                greedBasedEntryCount,
                impulsiveTradeCount,
                disciplinedTradeCount);
    }
    
    /**
     * Calculate expectancy of patterns/factors (legacy method)
     */
    private Map<String, BigDecimal> calculateExpectancy(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> expectancy = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            String category = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            expectancy.put(category, calculateTradeExpectancy(trades));
        }
        
        return expectancy;
    }
    
    /**
     * Calculate adaptability score - a strength metric that measures how well a trader adapts to changing market conditions
     * Higher score indicates better adaptation to market changes
     * 
     * @param trades List of all trades
     * @param tradesByPattern Map of trades grouped by behavior pattern
     * @return Adaptability score from 0-100
     */
    private BigDecimal calculateAdaptabilityScore(List<TradeDetails> trades, Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Sort trades chronologically
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Factors that contribute to adaptability:
        // 1. Variety of successful patterns used (pattern diversity)
        // 2. Improvement in win rate over time
        // 3. Appropriate changes in strategy during different market conditions
        // 4. Consistency in profitable trades across different market conditions
        
        // Calculate pattern diversity score (more successful patterns = higher adaptability)
        int successfulPatternCount = 0;
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            List<TradeDetails> patternTrades = entry.getValue();
            
            // Count pattern as successful if it has more wins than losses
            long winCount = patternTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
                
            if (winCount > patternTrades.size() / 2) {
                successfulPatternCount++;
            }
        }
        
        // Calculate pattern diversity score (0-40 points)
        BigDecimal patternDiversityScore = BigDecimal.valueOf(Math.min(40, successfulPatternCount * 10));
        
        // Calculate win rate improvement over time (0-30 points)
        BigDecimal winRateImprovementScore = BigDecimal.ZERO;
        if (sortedTrades.size() >= 10) {
            // Divide trades into first half and second half
            int midpoint = sortedTrades.size() / 2;
            List<TradeDetails> firstHalf = sortedTrades.subList(0, midpoint);
            List<TradeDetails> secondHalf = sortedTrades.subList(midpoint, sortedTrades.size());
            
            // Calculate win rates for each half
            double firstHalfWins = firstHalf.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
            double secondHalfWins = secondHalf.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
                
            double firstHalfWinRate = firstHalfWins / firstHalf.size();
            double secondHalfWinRate = secondHalfWins / secondHalf.size();
            
            // Calculate improvement
            double improvement = secondHalfWinRate - firstHalfWinRate;
            
            // Score based on improvement (max 30 points)
            if (improvement > 0) {
                winRateImprovementScore = BigDecimal.valueOf(Math.min(30, improvement * 100));
            }
        }
        
        // Calculate consistency across market conditions (0-30 points)
        // This is a simplified approach - in a real system, you would analyze market conditions
        BigDecimal consistencyScore = BigDecimal.valueOf(30);
        
        // Calculate final adaptability score (0-100)
        BigDecimal adaptabilityScore = patternDiversityScore
            .add(winRateImprovementScore)
            .add(consistencyScore);
            
        // Cap at 100
        return adaptabilityScore.min(BigDecimal.valueOf(100)).setScale(2, ROUNDING_MODE);
    }
    
    /**
     * Calculate overconfidence index - a weakness metric that measures potential overconfidence in trading decisions
     * Higher score indicates higher risk of overconfidence
     * 
     * @param trades List of all trades
     * @param groupingResult Result of grouping trades by factors
     * @return Overconfidence index from 0-100
     */
    private BigDecimal calculateOverconfidenceIndex(List<TradeDetails> trades, TradeGroupingResult groupingResult) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Factors that contribute to overconfidence:
        // 1. Frequency of OVERCONFIDENCE entry psychology
        // 2. Pattern of increasing position sizes after winning streaks
        // 3. Decreasing win rate with increasing position size
        // 4. Ignoring stop losses (if that data is available)
        // 5. High percentage of impulsive trades
        
        // Calculate overconfidence entry frequency (0-25 points)
        BigDecimal overconfidenceEntryScore = BigDecimal.ZERO;
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = groupingResult.getTradesByEntryPsychology();
        List<TradeDetails> overconfidentTrades = tradesByEntryPsychology.getOrDefault(EntryPsychology.OVERCONFIDENCE, new ArrayList<>());
        
        if (!overconfidentTrades.isEmpty()) {
            double overconfidencePercentage = (double) overconfidentTrades.size() / trades.size() * 100;
            overconfidenceEntryScore = BigDecimal.valueOf(Math.min(25, overconfidencePercentage));
        }
        
        // Calculate position sizing pattern score (0-25 points)
        BigDecimal positionSizingScore = BigDecimal.ZERO;
        // Sort trades chronologically
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Look for increasing position sizes after wins
        int increasingAfterWinCount = 0;
        boolean lastTradeWasWin = false;
        BigDecimal lastPositionSize = null;
        
        for (TradeDetails trade : sortedTrades) {
            // Skip trades without necessary data
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null ||
                trade.getEntryInfo() == null || trade.getEntryInfo().getPrice() == null ||
                trade.getEntryInfo().getQuantity() == null) {
                continue;
            }
            
            // Calculate current position size
            BigDecimal currentPositionSize = trade.getEntryInfo().getPrice()
                .multiply(BigDecimal.valueOf(trade.getEntryInfo().getQuantity().doubleValue()));
            
            // Check if position size increased after a win
            if (lastTradeWasWin && lastPositionSize != null && 
                currentPositionSize.compareTo(lastPositionSize) > 0) {
                increasingAfterWinCount++;
            }
            
            // Update for next iteration
            lastTradeWasWin = trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0;
            lastPositionSize = currentPositionSize;
        }
        
        if (sortedTrades.size() > 1) {
            double increasingPercentage = (double) increasingAfterWinCount / (sortedTrades.size() - 1) * 100;
            positionSizingScore = BigDecimal.valueOf(Math.min(25, increasingPercentage));
        }
        
        // Calculate impulsive trade score (0-25 points)
        BigDecimal impulsiveTradeScore = BigDecimal.ZERO;
        int impulsiveTradeCount = groupingResult.getImpulsiveTradeCount();
        
        if (trades.size() > 0) {
            double impulsivePercentage = (double) impulsiveTradeCount / trades.size() * 100;
            impulsiveTradeScore = BigDecimal.valueOf(Math.min(25, impulsivePercentage));
        }
        
        // Calculate stop loss adherence score (0-25 points)
        // This is a placeholder - in a real system, you would analyze stop loss adherence
        BigDecimal stopLossScore = BigDecimal.valueOf(15); // Default moderate score
        
        // Calculate final overconfidence index (0-100)
        BigDecimal overconfidenceIndex = overconfidenceEntryScore
            .add(positionSizingScore)
            .add(impulsiveTradeScore)
            .add(stopLossScore);
            
        // Cap at 100
        return overconfidenceIndex.min(BigDecimal.valueOf(100)).setScale(2, ROUNDING_MODE);
    }
    
    /**
     * Helper class to store trade grouping results
     */
    private static class TradeGroupingResult {
        private final Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern;
        private final Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology;
        private final Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology;
        private final int fearBasedExitCount;
        private final int greedBasedEntryCount;
        private final int impulsiveTradeCount;
        private final int disciplinedTradeCount;
        
        public TradeGroupingResult(
                Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern,
                Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
                Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
                int fearBasedExitCount,
                int greedBasedEntryCount,
                int impulsiveTradeCount,
                int disciplinedTradeCount) {
            this.tradesByPattern = tradesByPattern;
            this.tradesByEntryPsychology = tradesByEntryPsychology;
            this.tradesByExitPsychology = tradesByExitPsychology;
            this.fearBasedExitCount = fearBasedExitCount;
            this.greedBasedEntryCount = greedBasedEntryCount;
            this.impulsiveTradeCount = impulsiveTradeCount;
            this.disciplinedTradeCount = disciplinedTradeCount;
        }
        
        public Map<TradeBehaviorPattern, List<TradeDetails>> getTradesByPattern() {
            return tradesByPattern;
        }
        
        public Map<EntryPsychology, List<TradeDetails>> getTradesByEntryPsychology() {
            return tradesByEntryPsychology;
        }
        
        public Map<ExitPsychology, List<TradeDetails>> getTradesByExitPsychology() {
            return tradesByExitPsychology;
        }
        
        public int getFearBasedExitCount() {
            return fearBasedExitCount;
        }
        
        public int getGreedBasedEntryCount() {
            return greedBasedEntryCount;
        }
        
        public int getImpulsiveTradeCount() {
            return impulsiveTradeCount;
        }
        
        public int getDisciplinedTradeCount() {
            return disciplinedTradeCount;
        }
    }
}
