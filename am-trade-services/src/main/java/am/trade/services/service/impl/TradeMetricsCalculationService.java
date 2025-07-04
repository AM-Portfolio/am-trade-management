package am.trade.services.service.impl;

import am.trade.common.models.*;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Service for calculating various trade metrics including pattern and psychology metrics
 */
@Service
public class TradeMetricsCalculationService {

    private final TradeDetailsRepository tradeDetailsRepository;
    private final TradeDetailsMapper tradeDetailsMapper;
    
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    public TradeMetricsCalculationService(TradeDetailsRepository tradeDetailsRepository, 
                                         TradeDetailsMapper tradeDetailsMapper) {
        this.tradeDetailsRepository = tradeDetailsRepository;
        this.tradeDetailsMapper = tradeDetailsMapper;
    }
    
    /**
     * Calculate all metrics for a list of portfolio IDs
     * 
     * @param portfolioIds List of portfolio IDs to calculate metrics for
     * @return A complete trade summary with all metrics calculated
     */
    public TradeSummary calculateAllMetrics(List<String> portfolioIds) {
        List<TradeDetailsEntity> tradeEntities = tradeDetailsRepository.findByPortfolioIdIn(portfolioIds);
        List<TradeDetails> trades = tradeEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        
        if (trades.isEmpty()) {
            return createEmptyTradeSummary(portfolioIds);
        }
        
        TradeSummary summary = new TradeSummary();
        summary.setPortfolioIds(portfolioIds);
        summary.setTradeDetails(trades);
        
        // Calculate and set all metrics
        summary.setPerformanceMetrics(calculatePerformanceMetrics(trades));
        summary.setRiskMetrics(calculateRiskMetrics(trades));
        summary.setDistributionMetrics(calculateDistributionMetrics(trades));
        summary.setTimingMetrics(calculateTimingMetrics(trades));
        summary.setPatternMetrics(calculatePatternMetrics(trades));
        
        // Set legacy metrics for backward compatibility
        setLegacyMetrics(summary, trades);
        
        return summary;
    }
    
    /**
     * Calculate pattern metrics focusing on trade psychology and behavior patterns
     * 
     * @param trades List of trades to analyze
     * @return Calculated pattern metrics
     */
    public TradePatternMetrics calculatePatternMetrics(List<TradeDetails> trades) {
        TradePatternMetrics metrics = new TradePatternMetrics();
        
        // Extract trades with psychology data
        List<TradeDetails> tradesWithPsychology = trades.stream()
                .filter(t -> t.getPsychologyData() != null)
                .collect(Collectors.toList());
        
        if (tradesWithPsychology.isEmpty()) {
            return metrics;
        }
        
        // Calculate pattern frequency and performance
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = new HashMap<>();
        
        // Group trades by behavior pattern
        for (TradeDetails trade : tradesWithPsychology) {
            if (trade.getPsychologyData().getBehaviorPatterns() != null) {
                for (TradeBehaviorPattern pattern : trade.getPsychologyData().getBehaviorPatterns()) {
                    tradesByPattern.computeIfAbsent(pattern, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        // Calculate metrics for each pattern
        Map<TradeBehaviorPattern, Integer> patternFrequency = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternProfitLoss = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternWinRate = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternExpectancy = new HashMap<>();
        
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            // Calculate frequency
            patternFrequency.put(pattern, patternTrades.size());
            
            // Calculate profit/loss
            BigDecimal totalPL = patternTrades.stream()
                    .map(t -> t.getMetrics() != null ? t.getMetrics().getProfitLoss() : null)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            patternProfitLoss.put(pattern, totalPL);
            
            // Calculate win rate
            long winCount = patternTrades.stream()
                    .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            BigDecimal winRate = BigDecimal.valueOf(winCount)
                    .divide(BigDecimal.valueOf(patternTrades.size()), SCALE, ROUNDING_MODE);
            patternWinRate.put(pattern, winRate);
            
            // Calculate expectancy (average profit/loss per trade)
            BigDecimal expectancy = totalPL.divide(BigDecimal.valueOf(patternTrades.size()), SCALE, ROUNDING_MODE);
            patternExpectancy.put(pattern, expectancy);
        }
        
        // Set calculated metrics
        metrics.setPatternFrequency(patternFrequency);
        metrics.setPatternProfitLoss(patternProfitLoss);
        metrics.setPatternWinRate(patternWinRate);
        metrics.setPatternExpectancy(patternExpectancy);
        
        // Find most and least profitable patterns
        List<TradeBehaviorPattern> sortedPatterns = new ArrayList<>(patternProfitLoss.keySet());
        sortedPatterns.sort((p1, p2) -> patternProfitLoss.get(p2).compareTo(patternProfitLoss.get(p1)));
        
        metrics.setMostProfitablePatterns(sortedPatterns.isEmpty() ? Collections.emptyList() 
                : sortedPatterns.subList(0, Math.min(3, sortedPatterns.size())));
        
        Collections.reverse(sortedPatterns);
        metrics.setLeastProfitablePatterns(sortedPatterns.isEmpty() ? Collections.emptyList() 
                : sortedPatterns.subList(0, Math.min(3, sortedPatterns.size())));
        
        // Calculate psychology metrics
        calculateEntryPsychologyMetrics(tradesWithPsychology, metrics);
        calculateExitPsychologyMetrics(tradesWithPsychology, metrics);
        calculateEmotionalControlMetrics(tradesWithPsychology, metrics);
        
        return metrics;
    }
    
    /**
     * Calculate entry psychology metrics
     */
    private void calculateEntryPsychologyMetrics(List<TradeDetails> trades, TradePatternMetrics metrics) {
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = new HashMap<>();
        
        // Group trades by entry psychology
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getEntryPsychologyFactors() != null) {
                for (EntryPsychology psychology : trade.getPsychologyData().getEntryPsychologyFactors()) {
                    tradesByEntryPsychology.computeIfAbsent(psychology, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        // Calculate metrics for each entry psychology factor
        Map<EntryPsychology, Integer> entryFrequency = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryProfitLoss = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryWinRate = new HashMap<>();
        
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            EntryPsychology psychology = entry.getKey();
            List<TradeDetails> psychologyTrades = entry.getValue();
            
            // Calculate frequency
            entryFrequency.put(psychology, psychologyTrades.size());
            
            // Calculate profit/loss
            BigDecimal totalPL = psychologyTrades.stream()
                    .map(t -> t.getMetrics() != null ? t.getMetrics().getProfitLoss() : null)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            entryProfitLoss.put(psychology, totalPL);
            
            // Calculate win rate
            long winCount = psychologyTrades.stream()
                    .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            BigDecimal winRate = BigDecimal.valueOf(winCount)
                    .divide(BigDecimal.valueOf(psychologyTrades.size()), SCALE, ROUNDING_MODE);
            entryWinRate.put(psychology, winRate);
        }
        
        // Set calculated metrics
        metrics.setEntryPsychologyFrequency(entryFrequency);
        metrics.setEntryPsychologyProfitLoss(entryProfitLoss);
        metrics.setEntryPsychologyWinRate(entryWinRate);
        
        // Find most and least profitable entry psychology factors
        List<EntryPsychology> sortedPsychology = new ArrayList<>(entryProfitLoss.keySet());
        sortedPsychology.sort((p1, p2) -> entryProfitLoss.get(p2).compareTo(entryProfitLoss.get(p1)));
        
        metrics.setMostProfitableEntryPsychology(sortedPsychology.isEmpty() ? Collections.emptyList() 
                : sortedPsychology.subList(0, Math.min(3, sortedPsychology.size())));
        
        Collections.reverse(sortedPsychology);
        metrics.setLeastProfitableEntryPsychology(sortedPsychology.isEmpty() ? Collections.emptyList() 
                : sortedPsychology.subList(0, Math.min(3, sortedPsychology.size())));
    }
    
    /**
     * Calculate exit psychology metrics
     */
    private void calculateExitPsychologyMetrics(List<TradeDetails> trades, TradePatternMetrics metrics) {
        Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = new HashMap<>();
        
        // Group trades by exit psychology
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getExitPsychologyFactors() != null) {
                for (ExitPsychology psychology : trade.getPsychologyData().getExitPsychologyFactors()) {
                    tradesByExitPsychology.computeIfAbsent(psychology, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        // Calculate metrics for each exit psychology factor
        Map<ExitPsychology, Integer> exitFrequency = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitProfitLoss = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitWinRate = new HashMap<>();
        
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            ExitPsychology psychology = entry.getKey();
            List<TradeDetails> psychologyTrades = entry.getValue();
            
            // Calculate frequency
            exitFrequency.put(psychology, psychologyTrades.size());
            
            // Calculate profit/loss
            BigDecimal totalPL = psychologyTrades.stream()
                    .map(t -> t.getMetrics() != null ? t.getMetrics().getProfitLoss() : null)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            exitProfitLoss.put(psychology, totalPL);
            
            // Calculate win rate
            long winCount = psychologyTrades.stream()
                    .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                    .count();
            BigDecimal winRate = BigDecimal.valueOf(winCount)
                    .divide(BigDecimal.valueOf(psychologyTrades.size()), SCALE, ROUNDING_MODE);
            exitWinRate.put(psychology, winRate);
        }
        
        // Set calculated metrics
        metrics.setExitPsychologyFrequency(exitFrequency);
        metrics.setExitPsychologyProfitLoss(exitProfitLoss);
        metrics.setExitPsychologyWinRate(exitWinRate);
        
        // Find most and least profitable exit psychology factors
        List<ExitPsychology> sortedPsychology = new ArrayList<>(exitProfitLoss.keySet());
        sortedPsychology.sort((p1, p2) -> exitProfitLoss.get(p2).compareTo(exitProfitLoss.get(p1)));
        
        metrics.setMostProfitableExitPsychology(sortedPsychology.isEmpty() ? Collections.emptyList() 
                : sortedPsychology.subList(0, Math.min(3, sortedPsychology.size())));
        
        Collections.reverse(sortedPsychology);
        metrics.setLeastProfitableExitPsychology(sortedPsychology.isEmpty() ? Collections.emptyList() 
                : sortedPsychology.subList(0, Math.min(3, sortedPsychology.size())));
    }
    
    /**
     * Calculate emotional control metrics
     */
    private void calculateEmotionalControlMetrics(List<TradeDetails> trades, TradePatternMetrics metrics) {
        // Count trades with specific psychology factors
        long fearBasedExitCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && 
                       t.getPsychologyData().getExitPsychologyFactors() != null &&
                       t.getPsychologyData().getExitPsychologyFactors().stream()
                            .anyMatch(p -> "FEAR".equals(p.getCode()) || 
                                          "PANIC".equals(p.getCode()) || 
                                          "ANXIETY".equals(p.getCode())))
                .count();
        
        long greedBasedEntryCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && 
                       t.getPsychologyData().getEntryPsychologyFactors() != null &&
                       t.getPsychologyData().getEntryPsychologyFactors().stream()
                            .anyMatch(p -> "GREED".equals(p.getCode()) || 
                                          "FEAR_OF_MISSING_OUT".equals(p.getCode())))
                .count();
        
        long impulsiveTradeCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && 
                       t.getPsychologyData().getBehaviorPatterns() != null &&
                       t.getPsychologyData().getBehaviorPatterns().stream()
                            .anyMatch(p -> "IMPULSIVE_ENTRY".equals(p.getCode()) || 
                                          "REVENGE_TRADING".equals(p.getCode())))
                .count();
        
        long disciplinedTradeCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && 
                       (t.getPsychologyData().getEntryPsychologyFactors() != null &&
                        t.getPsychologyData().getEntryPsychologyFactors().stream()
                            .anyMatch(p -> "FOLLOWING_THE_PLAN".equals(p.getCode()))) ||
                       (t.getPsychologyData().getExitPsychologyFactors() != null &&
                        t.getPsychologyData().getExitPsychologyFactors().stream()
                            .anyMatch(p -> "DISCIPLINE".equals(p.getCode()) || 
                                          "FOLLOWING_STOP_LOSS".equals(p.getCode()))))
                .count();
        
        // Calculate percentages
        int totalTrades = trades.size();
        BigDecimal fearBasedExitPercentage = totalTrades > 0 ? 
                BigDecimal.valueOf(fearBasedExitCount * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        BigDecimal greedBasedEntryPercentage = totalTrades > 0 ? 
                BigDecimal.valueOf(greedBasedEntryCount * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        BigDecimal impulsiveTradePercentage = totalTrades > 0 ? 
                BigDecimal.valueOf(impulsiveTradeCount * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        // Calculate discipline score (0-100)
        BigDecimal disciplineScore = totalTrades > 0 ? 
                BigDecimal.valueOf(disciplinedTradeCount * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        // Calculate emotional control score (0-100)
        // Higher score means better emotional control
        BigDecimal emotionalControlScore = BigDecimal.valueOf(100)
                .subtract(fearBasedExitPercentage.add(greedBasedEntryPercentage).add(impulsiveTradePercentage)
                        .divide(BigDecimal.valueOf(3), ROUNDING_MODE))
                .setScale(2, ROUNDING_MODE);
        
        // Set calculated metrics
        metrics.setFearBasedExitPercentage(fearBasedExitPercentage);
        metrics.setGreedBasedEntryPercentage(greedBasedEntryPercentage);
        metrics.setImpulsiveTradePercentage(impulsiveTradePercentage);
        metrics.setDisciplineScore(disciplineScore);
        metrics.setEmotionalControlScore(emotionalControlScore);
    }
    
    /**
     * Calculate performance metrics
     */
    public PerformanceMetrics calculatePerformanceMetrics(List<TradeDetails> trades) {
        // Implementation would go here
        // For brevity, returning an empty object
        return new PerformanceMetrics();
    }
    
    /**
     * Calculate risk metrics
     */
    public RiskMetrics calculateRiskMetrics(List<TradeDetails> trades) {
        // Implementation would go here
        // For brevity, returning an empty object
        return new RiskMetrics();
    }
    
    /**
     * Calculate distribution metrics
     */
    public TradeDistributionMetrics calculateDistributionMetrics(List<TradeDetails> trades) {
        // Implementation would go here
        // For brevity, returning an empty object
        return new TradeDistributionMetrics();
    }
    
    /**
     * Calculate timing metrics
     */
    public TradeTimingMetrics calculateTimingMetrics(List<TradeDetails> trades) {
        // Implementation would go here
        // For brevity, returning an empty object
        return new TradeTimingMetrics();
    }
    
    /**
     * Set legacy metrics for backward compatibility
     */
    private void setLegacyMetrics(TradeSummary summary, List<TradeDetails> trades) {
        // Calculate basic metrics
        BigDecimal totalProfitLoss = trades.stream()
                .map(t -> t.getMetrics() != null ? t.getMetrics().getProfitLoss() : null)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long winCount = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        BigDecimal winRate = trades.isEmpty() ? BigDecimal.ZERO : 
                BigDecimal.valueOf(winCount * 100.0 / trades.size()).setScale(2, ROUNDING_MODE);
        
        // Set legacy metrics
        summary.setTotalProfitLoss(totalProfitLoss);
        summary.setWinRate(winRate);
        
        // Categorize winning and losing trades
        List<TradeDetails> winningTrades = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .sorted((t1, t2) -> t2.getMetrics().getProfitLoss().compareTo(t1.getMetrics().getProfitLoss()))
                .collect(Collectors.toList());
        
        List<TradeDetails> losingTrades = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0)
                .sorted((t1, t2) -> t1.getMetrics().getProfitLoss().compareTo(t2.getMetrics().getProfitLoss()))
                .collect(Collectors.toList());
        
        summary.setWinningTrades(winningTrades);
        summary.setLosingTrades(losingTrades);
    }
    
    /**
     * Create an empty trade summary when no trades are found
     */
    private TradeSummary createEmptyTradeSummary(List<String> portfolioIds) {
        TradeSummary summary = new TradeSummary();
        summary.setPortfolioIds(portfolioIds);
        summary.setTradeDetails(Collections.emptyList());
        summary.setWinningTrades(Collections.emptyList());
        summary.setLosingTrades(Collections.emptyList());
        summary.setTotalProfitLoss(BigDecimal.ZERO);
        summary.setWinRate(BigDecimal.ZERO);
        
        return summary;
    }
}
