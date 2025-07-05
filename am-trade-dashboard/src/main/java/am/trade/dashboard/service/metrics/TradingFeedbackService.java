package am.trade.dashboard.service.metrics;

import am.trade.common.models.*;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating personalized trading feedback based on trade psychology and entry/exit reasoning
 */
@Service
@Slf4j
public class TradingFeedbackService {

    /**
     * Generate trading feedback based on trade details
     * 
     * @param trades List of trade details
     * @return TradingFeedback object with personalized insights
     */
    public TradingFeedback generateFeedback(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new TradingFeedback();
        }
        
        // Analyze psychological patterns
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = groupTradesByEntryPsychology(trades);
        Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = groupTradesByExitPsychology(trades);
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupTradesByPattern(trades);
        
        // Generate feedback components
        List<String> strengthAreas = identifyStrengthAreas(trades, tradesByEntryPsychology, tradesByExitPsychology, tradesByPattern);
        List<String> improvementAreas = identifyImprovementAreas(trades, tradesByEntryPsychology, tradesByExitPsychology, tradesByPattern);
        Map<String, List<String>> psychologyInsights = generatePsychologyInsights(tradesByEntryPsychology, tradesByExitPsychology);
        List<String> emotionalPatternObservations = identifyEmotionalPatterns(trades);
        List<String> recommendedBehaviorChanges = generateBehaviorRecommendations(trades, tradesByPattern);
        List<String> tradingHabitsToReinforce = identifyPositiveHabits(trades);
        Map<String, String> patternSpecificFeedback = generatePatternFeedback(tradesByPattern);
        List<String> successPatternRecommendations = identifySuccessPatterns(tradesByPattern);
        
        // Generate decision quality feedback
        String entryDecisionFeedback = generateEntryDecisionFeedback(trades);
        String exitDecisionFeedback = generateExitDecisionFeedback(trades);
        String overallDecisionQualityFeedback = generateOverallDecisionFeedback(trades);
        
        // Generate risk management feedback
        String riskManagementFeedback = generateRiskManagementFeedback(trades);
        List<String> riskManagementSuggestions = generateRiskManagementSuggestions(trades);
        
        // Build and return the feedback object
        return TradingFeedback.builder()
                .strengthAreas(strengthAreas)
                .improvementAreas(improvementAreas)
                .psychologyInsights(psychologyInsights)
                .emotionalPatternObservations(emotionalPatternObservations)
                .recommendedBehaviorChanges(recommendedBehaviorChanges)
                .tradingHabitsToReinforce(tradingHabitsToReinforce)
                .patternSpecificFeedback(patternSpecificFeedback)
                .successPatternRecommendations(successPatternRecommendations)
                .entryDecisionFeedback(entryDecisionFeedback)
                .exitDecisionFeedback(exitDecisionFeedback)
                .overallDecisionQualityFeedback(overallDecisionQualityFeedback)
                .riskManagementFeedback(riskManagementFeedback)
                .riskManagementSuggestions(riskManagementSuggestions)
                .build();
    }
    
    /**
     * Group trades by entry psychology factors
     */
    private Map<EntryPsychology, List<TradeDetails>> groupTradesByEntryPsychology(List<TradeDetails> trades) {
        Map<EntryPsychology, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getEntryPsychologyFactors() != null) {
                for (EntryPsychology factor : trade.getPsychologyData().getEntryPsychologyFactors()) {
                    result.computeIfAbsent(factor, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Group trades by exit psychology factors
     */
    private Map<ExitPsychology, List<TradeDetails>> groupTradesByExitPsychology(List<TradeDetails> trades) {
        Map<ExitPsychology, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getExitPsychologyFactors() != null) {
                for (ExitPsychology factor : trade.getPsychologyData().getExitPsychologyFactors()) {
                    result.computeIfAbsent(factor, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Group trades by behavior patterns
     */
    private Map<TradeBehaviorPattern, List<TradeDetails>> groupTradesByPattern(List<TradeDetails> trades) {
        Map<TradeBehaviorPattern, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            if (trade.getPsychologyData() != null && trade.getPsychologyData().getBehaviorPatterns() != null) {
                for (TradeBehaviorPattern pattern : trade.getPsychologyData().getBehaviorPatterns()) {
                    result.computeIfAbsent(pattern, k -> new ArrayList<>()).add(trade);
                }
            }
        }
        
        return result;
    }
    
    /**
     * Identify areas of strength based on trade data
     */
    private List<String> identifyStrengthAreas(List<TradeDetails> trades, 
                                              Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
                                              Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
                                              Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        List<String> strengths = new ArrayList<>();
        
        // Check for disciplined entries
        if (tradesByEntryPsychology.containsKey(EntryPsychology.FOLLOWING_THE_PLAN) && 
            isProfitable(tradesByEntryPsychology.get(EntryPsychology.FOLLOWING_THE_PLAN))) {
            strengths.add("Disciplined entries lead to profitable trades");
        }
        
        // Check for patient entries - using ANALYSIS_PARALYSIS as a proxy for patience
        if (tradesByEntryPsychology.containsKey(EntryPsychology.ANALYSIS_PARALYSIS) && 
            isProfitable(tradesByEntryPsychology.get(EntryPsychology.ANALYSIS_PARALYSIS))) {
            strengths.add("Patient entries showing good results");
        }
        
        // Check for disciplined exits
        if (tradesByExitPsychology.containsKey(ExitPsychology.fromCode("DISCIPLINE")) && 
            isProfitable(tradesByExitPsychology.get(ExitPsychology.fromCode("DISCIPLINE")))) {
            strengths.add("Disciplined exits preserving profits");
        }
        
        // Check for taking profits
        if (tradesByExitPsychology.containsKey(ExitPsychology.fromCode("TAKING_PROFITS")) && 
            isProfitable(tradesByExitPsychology.get(ExitPsychology.fromCode("TAKING_PROFITS")))) {
            strengths.add("Effective profit-taking strategy");
        }
        
        // Check for cutting losses
        if (tradesByExitPsychology.containsKey(ExitPsychology.fromCode("CUTTING_LOSSES"))) {
            strengths.add("Good discipline in cutting losses");
        }
        
        // Check for adaptability (new strength metric)
        boolean hasAdaptability = trades.stream()
            .filter(t -> t.getPsychologyData() != null)
            .filter(t -> t.getPsychologyData().getBehaviorPatterns() != null)
            .flatMap(t -> t.getPsychologyData().getBehaviorPatterns().stream())
            .anyMatch(p -> p.getCode().equals("ADAPTABLE") || p.getCode().equals("FLEXIBLE"));
            
        if (hasAdaptability) {
            strengths.add("Demonstrates adaptability to changing market conditions");
        }
        
        return strengths;
    }
    
    /**
     * Identify areas needing improvement based on trade data
     */
    private List<String> identifyImprovementAreas(List<TradeDetails> trades, 
                                                 Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
                                                 Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
                                                 Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        List<String> improvements = new ArrayList<>();
        
        // Check for impulsive entries
        EntryPsychology impulsiveEntry = EntryPsychology.fromCode("IMPULSIVE");
        if (tradesByEntryPsychology.containsKey(impulsiveEntry) && 
            !isProfitable(tradesByEntryPsychology.get(impulsiveEntry))) {
            improvements.add("Impulsive entries leading to losses");
        }
        
        // Check for FOMO entries
        if (tradesByEntryPsychology.containsKey(EntryPsychology.FEAR_OF_MISSING_OUT) && 
            !isProfitable(tradesByEntryPsychology.get(EntryPsychology.FEAR_OF_MISSING_OUT))) {
            improvements.add("FOMO-based entries resulting in poor outcomes");
        }
        
        // Check for fear-based exits
        ExitPsychology fearExit = ExitPsychology.fromCode("FEAR");
        if (tradesByExitPsychology.containsKey(fearExit) && 
            !isProfitable(tradesByExitPsychology.get(fearExit))) {
            improvements.add("Fear-based exits potentially leaving money on the table");
        }
        
        // Check for panic exits
        ExitPsychology panicExit = ExitPsychology.fromCode("PANIC");
        if (tradesByExitPsychology.containsKey(panicExit)) {
            improvements.add("Panic exits leading to suboptimal results");
        }
        
        // Check for overconfidence (new weakness metric)
        boolean hasOverconfidence = trades.stream()
            .filter(t -> t.getPsychologyData() != null)
            .filter(t -> t.getPsychologyData().getEntryPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getEntryPsychologyFactors().stream())
            .anyMatch(p -> p.equals(EntryPsychology.OVERCONFIDENCE));
            
        if (hasOverconfidence) {
            improvements.add("Overconfidence potentially leading to excessive risk-taking");
        }
        
        return improvements;
    }
    
    /**
     * Generate insights based on psychology factors
     */
    private Map<String, List<String>> generatePsychologyInsights(
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology) {
        Map<String, List<String>> insights = new HashMap<>();
        
        // Entry psychology insights
        List<String> entryInsights = new ArrayList<>();
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            EntryPsychology factor = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            if (factor.equals(EntryPsychology.FOLLOWING_THE_PLAN)) {
                entryInsights.add("Disciplined entries make up " + trades.size() + " trades with " + 
                                 calculateWinRate(trades) + "% win rate");
            } else if (factor.getCode().equals("IMPULSIVE")) {
                entryInsights.add("Impulsive entries make up " + trades.size() + " trades with " + 
                                 calculateWinRate(trades) + "% win rate");
            } else if (factor.equals(EntryPsychology.FEAR_OF_MISSING_OUT)) {
                entryInsights.add("FOMO-driven entries make up " + trades.size() + " trades with " + 
                                 calculateWinRate(trades) + "% win rate");
            }
        }
        insights.put("Entry Psychology", entryInsights);
        
        // Exit psychology insights
        List<String> exitInsights = new ArrayList<>();
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            ExitPsychology factor = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            if (factor.getCode().equals("DISCIPLINE")) {
                exitInsights.add("Disciplined exits make up " + trades.size() + " trades with " + 
                                calculateWinRate(trades) + "% win rate");
            } else if (factor.getCode().equals("FEAR")) {
                exitInsights.add("Fear-based exits make up " + trades.size() + " trades with " + 
                                calculateWinRate(trades) + "% win rate");
            } else if (factor.getCode().equals("TAKING_PROFITS")) {
                exitInsights.add("Profit-taking exits make up " + trades.size() + " trades with " + 
                                calculateWinRate(trades) + "% win rate");
            }
        }
        insights.put("Exit Psychology", exitInsights);
        
        return insights;
    }
    
    /**
     * Identify emotional patterns in trading
     */
    private List<String> identifyEmotionalPatterns(List<TradeDetails> trades) {
        List<String> patterns = new ArrayList<>();
        
        // Check for patterns of emotional trading
        long fearBasedExits = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getExitPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getExitPsychologyFactors().stream())
            .filter(p -> p.getCode().equals("FEAR") || p.getCode().equals("PANIC"))
            .count();
            
        if (fearBasedExits > trades.size() * 0.2) { // If more than 20% of trades have fear-based exits
            patterns.add("Pattern of fear-based decision making in exits");
        }
        
        long greedBasedEntries = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getEntryPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getEntryPsychologyFactors().stream())
            .filter(p -> p.getCode().equals("GREED") || p.equals(EntryPsychology.FEAR_OF_MISSING_OUT))
            .count();
            
        if (greedBasedEntries > trades.size() * 0.2) { // If more than 20% of trades have greed-based entries
            patterns.add("Pattern of greed-driven entries");
        }
        
        return patterns;
    }
    
    /**
     * Generate behavior change recommendations
     */
    private List<String> generateBehaviorRecommendations(List<TradeDetails> trades,
                                                       Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        List<String> recommendations = new ArrayList<>();
        
        // Check for specific behavior patterns and make recommendations
        TradeBehaviorPattern revengePattern = TradeBehaviorPattern.fromCode("REVENGE_TRADING");
        if (tradesByPattern.containsKey(revengePattern) && 
            !isProfitable(tradesByPattern.get(revengePattern))) {
            recommendations.add("Take a break after losses to avoid revenge trading");
        }
        
        TradeBehaviorPattern overtradingPattern = TradeBehaviorPattern.fromCode("OVERTRADING");
        if (tradesByPattern.containsKey(overtradingPattern)) {
            recommendations.add("Reduce trading frequency and focus on quality setups");
        }
        
        // Check for impulsive entries
        long impulsiveEntries = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getEntryPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getEntryPsychologyFactors().stream())
            .filter(p -> p.getCode().equals("IMPULSIVE"))
            .count();
            
        if (impulsiveEntries > trades.size() * 0.2) { // If more than 20% of trades have impulsive entries
            recommendations.add("Implement a pre-trade checklist to reduce impulsive entries");
        }
        
        return recommendations;
    }
    
    /**
     * Identify positive trading habits to reinforce
     */
    private List<String> identifyPositiveHabits(List<TradeDetails> trades) {
        List<String> habits = new ArrayList<>();
        
        // Check for disciplined entries and exits
        long disciplinedEntries = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getEntryPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getEntryPsychologyFactors().stream())
            .filter(p -> p.equals(EntryPsychology.FOLLOWING_THE_PLAN))
            .count();
            
        long disciplinedExits = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getExitPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getExitPsychologyFactors().stream())
            .filter(p -> p.equals(ExitPsychology.DISCIPLINE) || p.equals(ExitPsychology.TAKING_PROFITS) || p.equals(ExitPsychology.CUTTING_LOSSES))
            .count();
            
        if (disciplinedEntries > trades.size() * 0.3) { // If more than 30% of trades have disciplined entries
            habits.add("Continue using pre-trade analysis and planning");
        }
        
        if (disciplinedExits > trades.size() * 0.3) { // If more than 30% of trades have disciplined exits
            habits.add("Maintain disciplined exit strategies");
        }
        
        return habits;
    }
    
    /**
     * Generate feedback specific to trading patterns
     */
    private Map<String, String> generatePatternFeedback(Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        Map<String, String> feedback = new HashMap<>();
        
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            double winRate = calculateWinRate(trades);
            
            if (pattern.getCode().equals("TREND_FOLLOWING")) {
                feedback.put("Trend Following", 
                            "Win rate: " + winRate + "%. " + 
                            (winRate > 50 ? "Effective strategy, continue to refine entries and exits." : 
                                           "Consider improving trend identification or timing."));
            } else if (pattern.getCode().equals("COUNTER_TREND")) {
                feedback.put("Counter-Trend Trading", 
                            "Win rate: " + winRate + "%. " + 
                            (winRate > 50 ? "Effective at identifying reversals." : 
                                           "Higher risk strategy, be more selective with setups."));
            } else if (pattern.getCode().equals("BREAKOUT_TRADING")) {
                feedback.put("Breakout Trading", 
                            "Win rate: " + winRate + "%. " + 
                            (winRate > 50 ? "Good at capturing momentum moves." : 
                                           "Watch for false breakouts, consider adding confirmation indicators."));
            }
        }
        
        return feedback;
    }
    
    /**
     * Identify successful trading patterns
     */
    private List<String> identifySuccessPatterns(Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        List<String> successPatterns = new ArrayList<>();
        
        // Find patterns with high win rates
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            if (trades.size() >= 5 && calculateWinRate(trades) >= 60) {
                successPatterns.add(pattern.getDescription() + " (Win rate: " + calculateWinRate(trades) + "%)");
            }
        }
        
        return successPatterns;
    }
    
    /**
     * Generate feedback on entry decisions
     */
    private String generateEntryDecisionFeedback(List<TradeDetails> trades) {
        long totalTrades = trades.size();
        if (totalTrades == 0) return "Insufficient data for entry decision analysis";
        
        long disciplinedEntries = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getEntryPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getEntryPsychologyFactors().stream())
            .filter(p -> p.equals(EntryPsychology.DISCIPLINED))
            .count();
            
        double disciplinedPercentage = (double) disciplinedEntries / totalTrades * 100;
        
        if (disciplinedPercentage >= 70) {
            return "Excellent entry discipline. Consistently following trading plan for entries.";
        } else if (disciplinedPercentage >= 50) {
            return "Good entry discipline, but room for improvement. Consider refining entry criteria.";
        } else {
            return "Entry decisions need improvement. Consider implementing a more structured entry approach.";
        }
    }
    
    /**
     * Generate feedback on exit decisions
     */
    private String generateExitDecisionFeedback(List<TradeDetails> trades) {
        long totalTrades = trades.size();
        if (totalTrades == 0) return "Insufficient data for exit decision analysis";
        
        long disciplinedExits = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getExitPsychologyFactors() != null)
            .flatMap(t -> t.getPsychologyData().getExitPsychologyFactors().stream())
            .filter(p -> p.equals(ExitPsychology.DISCIPLINE) || p.equals(ExitPsychology.TAKING_PROFITS) || p.equals(ExitPsychology.CUTTING_LOSSES))
            .count();
            
        double disciplinedPercentage = (double) disciplinedExits / totalTrades * 100;
        
        if (disciplinedPercentage >= 70) {
            return "Excellent exit execution. Consistently following exit plans and managing trades well.";
        } else if (disciplinedPercentage >= 50) {
            return "Good exit management, but room for improvement. Consider setting clearer exit criteria.";
        } else {
            return "Exit decisions need improvement. Consider implementing more structured exit rules.";
        }
    }
    
    /**
     * Generate overall decision quality feedback
     */
    private String generateOverallDecisionFeedback(List<TradeDetails> trades) {
        double winRate = calculateWinRate(trades);
        double avgRiskRewardRatio = calculateAverageRiskRewardRatio(trades);
        
        if (winRate >= 60 && avgRiskRewardRatio >= 1.5) {
            return "Excellent decision quality. High win rate combined with favorable risk-reward ratio.";
        } else if (winRate >= 50 || avgRiskRewardRatio >= 1.5) {
            return "Good decision quality. Either win rate or risk-reward ratio is strong, focus on improving the other.";
        } else {
            return "Decision quality needs improvement. Focus on both win rate and risk-reward ratio.";
        }
    }
    
    /**
     * Generate risk management feedback
     */
    private String generateRiskManagementFeedback(List<TradeDetails> trades) {
        double avgRiskPerTrade = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .average()
            .orElse(0);
            
        double maxDrawdown = calculateMaxDrawdown(trades);
        
        if (avgRiskPerTrade <= 2.0 && maxDrawdown <= 10.0) {
            return "Excellent risk management. Consistent position sizing and controlled drawdowns.";
        } else if (avgRiskPerTrade <= 3.0 || maxDrawdown <= 15.0) {
            return "Good risk management, but room for improvement. Consider tightening position sizing rules.";
        } else {
            return "Risk management needs attention. Consider reducing position sizes and implementing stricter stop losses.";
        }
    }
    
    /**
     * Generate risk management suggestions
     */
    private List<String> generateRiskManagementSuggestions(List<TradeDetails> trades) {
        List<String> suggestions = new ArrayList<>();
        
        double avgRiskPerTrade = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .average()
            .orElse(0);
            
        if (avgRiskPerTrade > 2.0) {
            suggestions.add("Consider reducing risk per trade to 1-2% of account");
        }
        
        boolean inconsistentRisk = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .distinct()
            .count() > trades.size() * 0.5;
            
        if (inconsistentRisk) {
            suggestions.add("Implement more consistent position sizing");
        }
        
        return suggestions;
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
    
    /**
     * Helper method to calculate max drawdown (simplified)
     */
    private double calculateMaxDrawdown(List<TradeDetails> trades) {
        // This is a simplified calculation - in a real system you would use a more sophisticated approach
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        double maxDrawdown = 0.0;
        double peak = 0.0;
        double balance = 0.0;
        
        // Sort trades chronologically
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                balance += trade.getMetrics().getProfitLoss().doubleValue();
                
                if (balance > peak) {
                    peak = balance;
                }
                
                double drawdown = (peak - balance) / peak * 100;
                if (drawdown > maxDrawdown) {
                    maxDrawdown = drawdown;
                }
            }
        }
        
        return maxDrawdown;
    }
}
