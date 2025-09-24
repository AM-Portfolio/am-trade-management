package am.trade.dashboard.service.metrics.feedback.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.dashboard.model.feeback.PsychologyAnalysis;
import am.trade.dashboard.service.metrics.feedback.PsychologyAnalyzer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PsychologyAnalyzer that analyzes psychological aspects of trading
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PsychologyAnalyzerImpl implements PsychologyAnalyzer {

    @Override
    public PsychologyAnalysis analyze(List<TradeDetails> trades) {
        log.debug("Analyzing psychological aspects of {} trades", trades.size());
        
        try {
            // Group trades by psychology factors
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = groupTradesByEntryPsychology(trades);
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = groupTradesByExitPsychology(trades);
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupTradesByPattern(trades);
            
            // Generate analysis components
            List<String> strengthAreas = identifyStrengthAreas(trades, tradesByEntryPsychology, 
                    tradesByExitPsychology, tradesByPattern);
            List<String> improvementAreas = identifyImprovementAreas(trades, tradesByEntryPsychology, 
                    tradesByExitPsychology, tradesByPattern);
            Map<String, List<String>> psychologyInsights = generatePsychologyInsights(
                    tradesByEntryPsychology, tradesByExitPsychology);
            List<String> emotionalPatternObservations = identifyEmotionalPatterns(trades);
            
            log.debug("Successfully analyzed psychological aspects: found {} strength areas, {} improvement areas", 
                    strengthAreas.size(), improvementAreas.size());
            
            return PsychologyAnalysis.builder()
                    .strengthAreas(strengthAreas)
                    .improvementAreas(improvementAreas)
                    .psychologyInsights(psychologyInsights)
                    .emotionalPatternObservations(emotionalPatternObservations)
                    .build();
        } catch (Exception e) {
            log.error("Error analyzing psychological aspects of trading", e);
            throw new RuntimeException("Failed to analyze psychological aspects of trading", e);
        }
    }
    
    /**
     * Group trades by entry psychology factors
     */
    private Map<EntryPsychology, List<TradeDetails>> groupTradesByEntryPsychology(List<TradeDetails> trades) {
        log.trace("Grouping {} trades by entry psychology", trades.size());
        Map<EntryPsychology, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getEntryPsychologyFactors() != null && !trade.getPsychologyData().getEntryPsychologyFactors().isEmpty()) {
                EntryPsychology psychology = trade.getPsychologyData().getEntryPsychologyFactors().get(0);
                result.computeIfAbsent(psychology, k -> new ArrayList<>()).add(trade);
            }
        }
        
        log.trace("Grouped trades into {} entry psychology categories", result.size());
        return result;
    }
    
    /**
     * Group trades by exit psychology factors
     */
    private Map<ExitPsychology, List<TradeDetails>> groupTradesByExitPsychology(List<TradeDetails> trades) {
        log.trace("Grouping {} trades by exit psychology", trades.size());
        Map<ExitPsychology, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getExitPsychologyFactors() != null && !trade.getPsychologyData().getExitPsychologyFactors().isEmpty()) {
                ExitPsychology psychology = trade.getPsychologyData().getExitPsychologyFactors().get(0);
                result.computeIfAbsent(psychology, k -> new ArrayList<>()).add(trade);
            }
        }
        
        log.trace("Grouped trades into {} exit psychology categories", result.size());
        return result;
    }
    
    /**
     * Group trades by behavior patterns
     */
    private Map<TradeBehaviorPattern, List<TradeDetails>> groupTradesByPattern(List<TradeDetails> trades) {
        log.trace("Grouping {} trades by behavior pattern", trades.size());
        Map<TradeBehaviorPattern, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getBehaviorPatterns() != null) {
                for (TradeBehaviorPattern pattern : trade.getPsychologyData().getBehaviorPatterns()) {
                    result.computeIfAbsent(pattern, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        log.trace("Grouped trades into {} behavior pattern categories", result.size());
        return result;
    }
    
    /**
     * Identify areas of strength based on trade data
     */
    private List<String> identifyStrengthAreas(
            List<TradeDetails> trades,
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        
        log.debug("Identifying strength areas from trade data");
        List<String> strengthAreas = new ArrayList<>();
        
        // Check for profitable entry psychology factors
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            if (isProfitable(entry.getValue()) && entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                if (winRate > 60) {
                    strengthAreas.add(String.format("Strong performance when entering trades with %s mindset (%.0f%% win rate)",
                            entry.getKey().toString().toLowerCase().replace('_', ' '), winRate));
                }
            }
        }
        
        // Check for profitable exit psychology factors
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            if (isProfitable(entry.getValue()) && entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                if (winRate > 60) {
                    strengthAreas.add(String.format("Effective at exiting trades with %s approach (%.0f%% win rate)",
                            entry.getKey().toString().toLowerCase().replace('_', ' '), winRate));
                }
            }
        }
        
        // Check for profitable behavior patterns
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            if (isProfitable(entry.getValue()) && entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                if (winRate > 60) {
                    strengthAreas.add(String.format("Strong performance with %s trading pattern (%.0f%% win rate)",
                            entry.getKey().toString().toLowerCase().replace('_', ' '), winRate));
                }
            }
        }
        
        // Check overall risk management
        double avgRiskReward = calculateAverageRiskRewardRatio(trades);
        if (avgRiskReward >= 1.5) {
            strengthAreas.add(String.format("Good risk management with average risk-reward ratio of %.1f", avgRiskReward));
        }
        
        log.debug("Identified {} strength areas", strengthAreas.size());
        return strengthAreas;
    }
    
    /**
     * Identify areas needing improvement based on trade data
     */
    private List<String> identifyImprovementAreas(
            List<TradeDetails> trades,
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        
        log.debug("Identifying improvement areas from trade data");
        List<String> improvementAreas = new ArrayList<>();
        
        // Check for unprofitable entry psychology factors
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            if (!isProfitable(entry.getValue()) && entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                if (winRate < 40) {
                    improvementAreas.add(String.format("Consider avoiding trades with %s entry mindset (%.0f%% win rate)",
                            entry.getKey().toString().toLowerCase().replace('_', ' '), winRate));
                }
            }
        }
        
        // Check for unprofitable exit psychology factors
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            if (!isProfitable(entry.getValue()) && entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                if (winRate < 40) {
                    improvementAreas.add(String.format("Work on improving exits with %s approach (%.0f%% win rate)",
                            entry.getKey().toString().toLowerCase().replace('_', ' '), winRate));
                }
            }
        }
        
        // Check for unprofitable behavior patterns
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            if (!isProfitable(entry.getValue()) && entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                if (winRate < 40) {
                    improvementAreas.add(String.format("Address issues with %s trading pattern (%.0f%% win rate)",
                            entry.getKey().toString().toLowerCase().replace('_', ' '), winRate));
                }
            }
        }
        
        // Check overall risk management
        double avgRiskReward = calculateAverageRiskRewardRatio(trades);
        if (avgRiskReward < 1.0) {
            improvementAreas.add(String.format("Improve risk management - current average risk-reward ratio is only %.1f", avgRiskReward));
        }
        
        log.debug("Identified {} improvement areas", improvementAreas.size());
        return improvementAreas;
    }
    
    /**
     * Generate insights based on psychology factors
     */
    private Map<String, List<String>> generatePsychologyInsights(
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology) {
        
        log.debug("Generating psychology insights");
        Map<String, List<String>> insights = new HashMap<>();
        List<String> entryInsights = new ArrayList<>();
        List<String> exitInsights = new ArrayList<>();
        
        // Entry psychology insights
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            if (entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                String insight = String.format("%s mindset: %.0f%% win rate across %d trades",
                        entry.getKey().toString().toLowerCase().replace('_', ' '),
                        winRate, entry.getValue().size());
                entryInsights.add(insight);
            }
        }
        
        // Exit psychology insights
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            if (entry.getValue().size() >= 3) {
                double winRate = calculateWinRate(entry.getValue());
                String insight = String.format("%s approach: %.0f%% win rate across %d trades",
                        entry.getKey().toString().toLowerCase().replace('_', ' '),
                        winRate, entry.getValue().size());
                exitInsights.add(insight);
            }
        }
        
        insights.put("Entry Psychology", entryInsights);
        insights.put("Exit Psychology", exitInsights);
        
        log.debug("Generated {} entry insights and {} exit insights", 
                entryInsights.size(), exitInsights.size());
        return insights;
    }
    
    /**
     * Identify emotional patterns in trading
     */
    private List<String> identifyEmotionalPatterns(List<TradeDetails> trades) {
        log.debug("Identifying emotional patterns from {} trades", trades.size());
        List<String> observations = new ArrayList<>();
        
        // Check for FOMO (Fear of Missing Out)
        long fomoCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getEntryPsychologyFactors() != null && !t.getPsychologyData().getEntryPsychologyFactors().isEmpty() && t.getPsychologyData().getEntryPsychologyFactors().get(0) == EntryPsychology.FEAR_OF_MISSING_OUT)
                .count();
        
        if (fomoCount > trades.size() * 0.2) {  // More than 20% of trades show FOMO
            observations.add(String.format("FOMO influenced %.0f%% of your trades. Consider waiting for proper setups.",
                    (double) fomoCount / trades.size() * 100));
        }
        
        // Check for early exits due to fear
        long fearfulExitCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getExitPsychologyFactors() != null && !t.getPsychologyData().getExitPsychologyFactors().isEmpty() && t.getPsychologyData().getExitPsychologyFactors().get(0) == ExitPsychology.FEAR)
                .count();
        
        if (fearfulExitCount > trades.size() * 0.2) {  // More than 20% of trades show fearful exits
            observations.add(String.format("Fear led to early exits in %.0f%% of your trades. Consider using predetermined exit points.",
                    (double) fearfulExitCount / trades.size() * 100));
        }
        
        // Check for revenge trading
        long revengeCount = trades.stream()
                .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getBehaviorPatterns() != null && !t.getPsychologyData().getBehaviorPatterns().isEmpty() && t.getPsychologyData().getBehaviorPatterns().get(0) == TradeBehaviorPattern.REVENGE_TRADING)
                .count();
        
        if (revengeCount > 0) {
            observations.add(String.format("Detected %d instances of revenge trading. Consider taking breaks after losses.",
                    revengeCount));
        }
        
        log.debug("Identified {} emotional pattern observations", observations.size());
        return observations;
    }
    
    /**
     * Helper method to check if a list of trades is profitable overall
     */
    private boolean isProfitable(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return false;
        }
        
        double totalProfitLoss = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
            .sum();
            
        return totalProfitLoss > 0;
    }
    
    /**
     * Helper method to calculate win rate for a list of trades
     */
    private double calculateWinRate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        long winCount = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
            .count();
            
        return Math.round((double) winCount / trades.size() * 100);
    }
    
    /**
     * Helper method to calculate average risk-reward ratio
     */
    private double calculateAverageRiskRewardRatio(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        return trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskRewardRatio() != null)
            .mapToDouble(t -> t.getMetrics().getRiskRewardRatio().doubleValue())
            .average()
            .orElse(0.0);
    }
}
