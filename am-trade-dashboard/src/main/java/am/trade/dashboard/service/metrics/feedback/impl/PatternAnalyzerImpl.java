package am.trade.dashboard.service.metrics.feedback.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.dashboard.service.metrics.feedback.PatternAnalysis;
import am.trade.dashboard.service.metrics.feedback.PatternAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of PatternAnalyzer that analyzes trading patterns
 */
@Component
@Slf4j
public class PatternAnalyzerImpl implements PatternAnalyzer {

    @Override
    public PatternAnalysis analyze(List<TradeDetails> trades) {
        log.debug("Analyzing trading patterns for {} trades", trades.size());
        
        try {
            // Group trades by behavior patterns
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupTradesByPattern(trades);
            
            // Generate analysis components
            List<String> recommendedBehaviorChanges = generateBehaviorRecommendations(trades, tradesByPattern);
            List<String> positiveHabits = identifyPositiveHabits(trades);
            Map<String, String> patternSpecificFeedback = generatePatternFeedback(tradesByPattern);
            List<String> successPatternRecommendations = identifySuccessPatterns(tradesByPattern);
            
            log.debug("Successfully analyzed trading patterns: found {} behavior recommendations, {} positive habits", 
                    recommendedBehaviorChanges.size(), positiveHabits.size());
            
            return PatternAnalysis.builder()
                    .recommendedBehaviorChanges(recommendedBehaviorChanges)
                    .positiveHabits(positiveHabits)
                    .patternSpecificFeedback(patternSpecificFeedback)
                    .successPatternRecommendations(successPatternRecommendations)
                    .build();
        } catch (Exception e) {
            log.error("Error analyzing trading patterns", e);
            throw new RuntimeException("Failed to analyze trading patterns", e);
        }
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
     * Generate behavior change recommendations
     */
    private List<String> generateBehaviorRecommendations(
            List<TradeDetails> trades,
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        
        log.debug("Generating behavior change recommendations");
        List<String> recommendations = new ArrayList<>();
        
        // Check for specific behavior patterns that need addressing
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            if (patternTrades.size() >= 3) {  // Only consider patterns with enough data
                boolean isPatternProfitable = isProfitable(patternTrades);
                double winRate = calculateWinRate(patternTrades);
                
                String patternCode = pattern.getCode();
                switch (patternCode) {
                    case "REVENGE_TRADING":
                        recommendations.add("Avoid revenge trading - take a break after losses to reset emotionally");
                        break;
                    case "OVERTRADING":
                        recommendations.add("Reduce trading frequency - focus on quality setups rather than quantity");
                        break;
                    case "HESITATION":
                        if (!isPatternProfitable) {
                            recommendations.add("Work on reducing hesitation - develop clear entry criteria and stick to them");
                        }
                        break;
                    case "FEAR_OF_MISSING_OUT":
                        if (!isPatternProfitable) {
                            recommendations.add("Avoid FOMO-based entries - wait for proper setups that meet your criteria");
                        }
                        break;
                    case "EARLY_EXIT":
                        if (!isPatternProfitable) {
                            recommendations.add("Hold winning trades longer - consider using trailing stops to capture more profit");
                        }
                        break;
                    default:
                        // No specific recommendation for other patterns
                }
            }
        }
        
        // Check overall consistency in position sizing using entry quantity as a proxy
        boolean inconsistentSizing = trades.stream()
            .filter(t -> t.getEntryInfo() != null && t.getEntryInfo().getQuantity() != null)
            .map(t -> t.getEntryInfo().getQuantity())
            .distinct()
            .count() > trades.size() * 0.7;  // More than 70% of trades have different position sizes
            
        if (inconsistentSizing) {
            recommendations.add("Implement more consistent position sizing strategy");
        }
        
        log.debug("Generated {} behavior change recommendations", recommendations.size());
        return recommendations;
    }
    
    /**
     * Identify positive trading habits to reinforce
     */
    private List<String> identifyPositiveHabits(List<TradeDetails> trades) {
        log.debug("Identifying positive trading habits");
        List<String> habits = new ArrayList<>();
        
        // Check for consistent risk management
        boolean consistentRisk = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .distinct()
            .count() <= 3;  // Only a few distinct risk percentages used
            
        if (consistentRisk) {
            habits.add("Consistent risk management - continue using fixed risk percentages");
        }
        
        // Check for good risk-reward ratio
        double avgRiskReward = calculateAverageRiskRewardRatio(trades);
        if (avgRiskReward >= 2.0) {
            habits.add(String.format("Excellent risk-reward ratio (%.1f) - continue seeking high probability setups", avgRiskReward));
        }
        
        // Check for patience in entries
        long patientEntries = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getBehaviorPatterns() != null)
            .flatMap(t -> t.getPsychologyData().getBehaviorPatterns().stream())
            .filter(pattern -> "PATIENT".equals(pattern.getCode()))
            .count();
            
        if (patientEntries > trades.size() * 0.3) {  // More than 30% of trades show patience
            habits.add("Good patience in waiting for proper setups - continue this discipline");
        }
        
        // Check for letting winners run
        long runningWinners = trades.stream()
            .filter(t -> t.getPsychologyData() != null && t.getPsychologyData().getBehaviorPatterns() != null)
            .flatMap(t -> t.getPsychologyData().getBehaviorPatterns().stream())
            .filter(pattern -> "LETTING_WINNERS_RUN".equals(pattern.getCode()))
            .count();
            
        if (runningWinners > trades.size() * 0.2) {  // More than 20% of trades let winners run
            habits.add("Effectively letting winning trades run - continue using trailing stops");
        }
        
        log.debug("Identified {} positive trading habits", habits.size());
        return habits;
    }
    
    /**
     * Generate feedback specific to trading patterns
     */
    private Map<String, String> generatePatternFeedback(Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        log.debug("Generating pattern-specific feedback");
        Map<String, String> feedback = new HashMap<>();
        
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            if (patternTrades.size() >= 3) {  // Only provide feedback for patterns with enough data
                double winRate = calculateWinRate(patternTrades);
                double avgProfitLoss = patternTrades.stream()
                    .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                    .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
                    .average()
                    .orElse(0.0);
                    
                String patternName = pattern.toString().toLowerCase().replace('_', ' ');
                String feedbackText = String.format(
                    "%s pattern: %.0f%% win rate, average P/L: $%.2f across %d trades. %s",
                    capitalizeWords(patternName), 
                    winRate, 
                    avgProfitLoss,
                    patternTrades.size(),
                    getPatternAdvice(pattern, winRate, avgProfitLoss)
                );
                
                feedback.put(capitalizeWords(patternName), feedbackText);
            }
        }
        
        log.debug("Generated feedback for {} trading patterns", feedback.size());
        return feedback;
    }
    
    /**
     * Get specific advice for a trading pattern based on its performance
     */
    private String getPatternAdvice(TradeBehaviorPattern pattern, double winRate, double avgProfitLoss) {
        String patternCode = pattern.getCode();
        switch (patternCode) {
            case "REVENGE_TRADING":
                return "This pattern typically leads to poor decisions. Take breaks after losses.";
            case "OVERTRADING":
                return "Excessive trading often dilutes performance. Focus on quality over quantity.";
            case "HESITATION":
                return winRate < 50 ? 
                    "Hesitation is hurting your results. Develop clear entry rules." : 
                    "Your cautious approach is working well. Continue being selective.";
            case "FEAR_OF_MISSING_OUT":
                return "FOMO-based entries typically underperform. Wait for proper setups.";
            case "EARLY_EXIT":
                return avgProfitLoss > 0 ?
                    "While profitable, consider holding winners longer to maximize gains." :
                    "Early exits are limiting your profitability. Use predetermined exit points.";
            case "PATIENT":
                return winRate > 50 ?
                    "Your patience is paying off. Continue waiting for high-quality setups." :
                    "While patience is good, ensure your entry criteria are effective.";
            case "LETTING_WINNERS_RUN":
                return "This approach is maximizing your gains. Continue using trailing stops.";
            default:
                return "";
        }
    }
    
    /**
     * Identify successful trading patterns
     */
    private List<String> identifySuccessPatterns(Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern) {
        log.debug("Identifying successful trading patterns");
        List<String> recommendations = new ArrayList<>();
        
        // Find the most profitable patterns
        List<Map.Entry<TradeBehaviorPattern, List<TradeDetails>>> profitablePatterns = tradesByPattern.entrySet().stream()
            .filter(entry -> entry.getValue().size() >= 3)  // Only consider patterns with enough data
            .filter(entry -> isProfitable(entry.getValue()))
            .sorted((e1, e2) -> {
                // Sort by average profit per trade (descending)
                double avg1 = calculateAverageProfitLoss(e1.getValue());
                double avg2 = calculateAverageProfitLoss(e2.getValue());
                return Double.compare(avg2, avg1);
            })
            .limit(3)  // Top 3 patterns
            .collect(Collectors.toList());
            
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : profitablePatterns) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            double winRate = calculateWinRate(patternTrades);
            double avgProfit = calculateAverageProfitLoss(patternTrades);
            
            String patternName = pattern.toString().toLowerCase().replace('_', ' ');
            recommendations.add(String.format(
                "Continue to focus on %s approach (%.0f%% win rate, avg profit: $%.2f)",
                patternName, winRate, avgProfit
            ));
        }
        
        log.debug("Identified {} successful trading patterns", recommendations.size());
        return recommendations;
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
     * Helper method to calculate average profit/loss
     */
    private double calculateAverageProfitLoss(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        return trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
            .average()
            .orElse(0.0);
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
     * Helper method to capitalize each word in a string
     */
    private String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        String[] words = input.split("\\s");
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
}
