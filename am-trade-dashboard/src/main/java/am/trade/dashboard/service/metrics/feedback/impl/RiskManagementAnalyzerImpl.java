package am.trade.dashboard.service.metrics.feedback.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.feedback.RiskManagementAnalysis;
import am.trade.dashboard.service.metrics.feedback.RiskManagementAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Implementation of RiskManagementAnalyzer that analyzes risk management aspects of trading
 */
@Component
@Slf4j
public class RiskManagementAnalyzerImpl implements RiskManagementAnalyzer {

    @Override
    public RiskManagementAnalysis analyze(List<TradeDetails> trades) {
        log.debug("Analyzing risk management for {} trades", trades.size());
        
        try {
            // Generate analysis components
            String riskManagementFeedback = generateRiskManagementFeedback(trades);
            List<String> riskManagementSuggestions = generateRiskManagementSuggestions(trades);
            
            log.debug("Successfully analyzed risk management: generated {} suggestions", 
                    riskManagementSuggestions.size());
            
            return RiskManagementAnalysis.builder()
                    .riskManagementFeedback(riskManagementFeedback)
                    .riskManagementSuggestions(riskManagementSuggestions)
                    .build();
        } catch (Exception e) {
            log.error("Error analyzing risk management", e);
            throw new RuntimeException("Failed to analyze risk management", e);
        }
    }
    
    /**
     * Generate risk management feedback
     */
    private String generateRiskManagementFeedback(List<TradeDetails> trades) {
        log.debug("Generating risk management feedback");
        
        if (trades.isEmpty()) {
            return "No trade data available for risk management analysis.";
        }
        
        // Calculate risk metrics
        double avgRiskPerTrade = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .average()
            .orElse(0);
            
        double avgRiskRewardRatio = calculateAverageRiskRewardRatio(trades);
        double maxDrawdown = calculateMaxDrawdown(trades);
        
        // Generate feedback based on risk metrics
        StringBuilder feedback = new StringBuilder();
        
        feedback.append(String.format("Risk Management Analysis: Average risk per trade: %.1f%%, ", avgRiskPerTrade));
        feedback.append(String.format("Risk-reward ratio: %.1f:1, ", avgRiskRewardRatio));
        feedback.append(String.format("Maximum drawdown: %.1f%%. ", maxDrawdown));
        
        // Evaluate overall risk management
        if (avgRiskPerTrade <= 2.0 && avgRiskRewardRatio >= 1.5 && maxDrawdown < 20.0) {
            feedback.append("Your risk management is strong, with appropriate risk per trade and good risk-reward ratios.");
        } else if (avgRiskPerTrade <= 3.0 && avgRiskRewardRatio >= 1.0 && maxDrawdown < 30.0) {
            feedback.append("Your risk management is adequate but could be improved, particularly in ");
            
            if (avgRiskPerTrade > 2.0) {
                feedback.append("reducing risk per trade ");
            }
            
            if (avgRiskRewardRatio < 1.5) {
                feedback.append(avgRiskPerTrade > 2.0 ? "and " : "");
                feedback.append("improving risk-reward ratios ");
            }
            
            if (maxDrawdown >= 20.0) {
                feedback.append((avgRiskPerTrade > 2.0 || avgRiskRewardRatio < 1.5) ? "and " : "");
                feedback.append("managing drawdowns");
            }
            
            feedback.append(".");
        } else {
            feedback.append("Your risk management needs significant improvement. Focus on reducing risk per trade, ");
            feedback.append("improving risk-reward ratios, and implementing strategies to limit drawdowns.");
        }
        
        log.debug("Generated risk management feedback");
        return feedback.toString();
    }
    
    /**
     * Generate risk management suggestions
     */
    private List<String> generateRiskManagementSuggestions(List<TradeDetails> trades) {
        log.debug("Generating risk management suggestions");
        List<String> suggestions = new ArrayList<>();
        
        if (trades.isEmpty()) {
            return suggestions;
        }
        
        // Check risk per trade
        double avgRiskPerTrade = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .average()
            .orElse(0);
            
        if (avgRiskPerTrade > 2.0) {
            suggestions.add(String.format(
                "Consider reducing risk per trade from %.1f%% to 1-2%% of account to improve long-term sustainability", 
                avgRiskPerTrade));
        }
        
        // Check risk consistency
        boolean inconsistentRisk = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() != null)
            .mapToDouble(t -> t.getMetrics().getRiskAmount().doubleValue())
            .distinct()
            .count() > trades.size() * 0.5;
            
        if (inconsistentRisk) {
            suggestions.add("Implement more consistent position sizing to standardize risk across trades");
        }
        
        // Check risk-reward ratio
        double avgRiskRewardRatio = calculateAverageRiskRewardRatio(trades);
        if (avgRiskRewardRatio < 1.0) {
            suggestions.add(String.format(
                "Improve risk-reward ratio from current %.1f:1 to at least 1.5:1 by setting wider profit targets or tighter stop losses", 
                avgRiskRewardRatio));
        }
        
        // Check stop loss usage
        long tradesWithoutStops = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskAmount() == null)
            .count();
            
        if (tradesWithoutStops > 0) {
            double percentWithoutStops = (double) tradesWithoutStops / trades.size() * 100;
            if (percentWithoutStops > 10) {
                suggestions.add(String.format(
                    "Always use stop losses - %.0f%% of your trades were executed without defined stops", 
                    percentWithoutStops));
            }
        }
        
        // Check drawdown management
        double maxDrawdown = calculateMaxDrawdown(trades);
        if (maxDrawdown > 20.0) {
            suggestions.add(String.format(
                "Implement drawdown limits - your maximum drawdown of %.1f%% exceeds recommended levels", 
                maxDrawdown));
        }
        
        // Check consecutive losses
        int maxConsecutiveLosses = calculateMaxConsecutiveLosses(trades);
        if (maxConsecutiveLosses >= 5) {
            suggestions.add(String.format(
                "Consider implementing a 'circuit breaker' rule after %d consecutive losses to pause trading and reassess", 
                maxConsecutiveLosses));
        }
        
        log.debug("Generated {} risk management suggestions", suggestions.size());
        return suggestions;
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
     * Helper method to calculate max drawdown
     */
    private double calculateMaxDrawdown(List<TradeDetails> trades) {
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
                
                double drawdown = peak > 0 ? (peak - balance) / peak * 100 : 0;
                if (drawdown > maxDrawdown) {
                    maxDrawdown = drawdown;
                }
            }
        }
        
        log.trace("Calculated max drawdown: {}%", maxDrawdown);
        return maxDrawdown;
    }
    
    /**
     * Helper method to calculate maximum consecutive losses
     */
    private int calculateMaxConsecutiveLosses(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0;
        }
        
        // Sort trades chronologically
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        int currentStreak = 0;
        int maxStreak = 0;
        
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                if (trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
                    // Loss
                    currentStreak++;
                    maxStreak = Math.max(maxStreak, currentStreak);
                } else {
                    // Win
                    currentStreak = 0;
                }
            }
        }
        
        log.trace("Calculated max consecutive losses: {}", maxStreak);
        return maxStreak;
    }
}
