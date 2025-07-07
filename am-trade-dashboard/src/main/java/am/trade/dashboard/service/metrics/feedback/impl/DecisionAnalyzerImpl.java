package am.trade.dashboard.service.metrics.feedback.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.feedback.DecisionAnalysis;
import am.trade.dashboard.service.metrics.feedback.DecisionAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of DecisionAnalyzer that analyzes trading decisions
 */
@Component
@Slf4j
public class DecisionAnalyzerImpl implements DecisionAnalyzer {

    @Override
    public DecisionAnalysis analyze(List<TradeDetails> trades) {
        log.debug("Analyzing trading decisions for {} trades", trades.size());
        
        try {
            // Generate analysis components
            String entryDecisionFeedback = generateEntryDecisionFeedback(trades);
            String exitDecisionFeedback = generateExitDecisionFeedback(trades);
            String overallDecisionQualityFeedback = generateOverallDecisionFeedback(trades);
            
            log.debug("Successfully analyzed trading decisions");
            
            return DecisionAnalysis.builder()
                    .entryDecisionFeedback(entryDecisionFeedback)
                    .exitDecisionFeedback(exitDecisionFeedback)
                    .overallDecisionQualityFeedback(overallDecisionQualityFeedback)
                    .build();
        } catch (Exception e) {
            log.error("Error analyzing trading decisions", e);
            throw new RuntimeException("Failed to analyze trading decisions", e);
        }
    }
    
    /**
     * Generate feedback on entry decisions
     */
    private String generateEntryDecisionFeedback(List<TradeDetails> trades) {
        log.debug("Generating entry decision feedback");
        
        if (trades.isEmpty()) {
            return "No trade data available for entry decision analysis.";
        }
        
        // Calculate entry quality metrics
        long goodEntryCount = trades.stream()
            .filter(t -> t.getEntryReasoning() != null && t.getEntryReasoning().getConfidenceLevel() != null)
            .filter(t -> t.getEntryReasoning().getConfidenceLevel() >= 7)
            .count();
            
        double goodEntryPercentage = (double) goodEntryCount / trades.size() * 100;
        
        long badEntryCount = trades.stream()
            .filter(t -> t.getEntryReasoning() != null && t.getEntryReasoning().getConfidenceLevel() != null)
            .filter(t -> t.getEntryReasoning().getConfidenceLevel() <= 3)
            .count();
            
        double badEntryPercentage = (double) badEntryCount / trades.size() * 100;
        
        // Generate feedback based on entry quality
        StringBuilder feedback = new StringBuilder();
        
        if (goodEntryPercentage >= 70) {
            feedback.append(String.format("Your entry decisions are generally strong, with %.0f%% of entries rated as high quality. ", 
                    goodEntryPercentage));
            feedback.append("Continue to focus on your established entry criteria that are working well.");
        } else if (goodEntryPercentage >= 50) {
            feedback.append(String.format("Your entry decisions are above average, with %.0f%% of entries rated as high quality. ", 
                    goodEntryPercentage));
            feedback.append("There is room for improvement in entry precision and timing.");
        } else {
            feedback.append(String.format("Your entry decisions need improvement, with only %.0f%% of entries rated as high quality. ", 
                    goodEntryPercentage));
            feedback.append(String.format("%.0f%% of your entries were rated as poor quality. ", badEntryPercentage));
            feedback.append("Consider refining your entry criteria and being more selective with trade setups.");
        }
        
        // Add specific advice based on entry timing
        boolean hasEarlyEntries = trades.stream()
            .filter(t -> t.getEntryReasoning() != null && t.getEntryReasoning().getPrimaryReason() != null)
            .anyMatch(t -> t.getEntryReasoning().getPrimaryReason().contains("EARLY"));
            
        boolean hasLateEntries = trades.stream()
            .filter(t -> t.getEntryReasoning() != null && t.getEntryReasoning().getPrimaryReason() != null)
            .anyMatch(t -> t.getEntryReasoning().getPrimaryReason().contains("LATE"));
            
        if (hasEarlyEntries) {
            feedback.append(" Watch for premature entries before confirmation signals are present.");
        }
        
        if (hasLateEntries) {
            feedback.append(" Be cautious about chasing trades after the optimal entry point has passed.");
        }
        
        log.debug("Generated entry decision feedback");
        return feedback.toString();
    }
    
    /**
     * Generate feedback on exit decisions
     */
    private String generateExitDecisionFeedback(List<TradeDetails> trades) {
        log.debug("Generating exit decision feedback");
        
        if (trades.isEmpty()) {
            return "No trade data available for exit decision analysis.";
        }
        
        // Calculate exit quality metrics using the exitReasoning field
        long goodExitCount = trades.stream()
            .filter(t -> t.getExitReasoning() != null && t.getExitReasoning().getConfidenceLevel() != null)
            .filter(t -> t.getExitReasoning().getConfidenceLevel() >= 7) // High confidence level indicates good exit
            .count();
            
        double goodExitPercentage = trades.isEmpty() ? 0 : (double) goodExitCount / trades.size() * 100;
        
        long badExitCount = trades.stream()
            .filter(t -> t.getExitReasoning() != null && t.getExitReasoning().getConfidenceLevel() != null)
            .filter(t -> t.getExitReasoning().getConfidenceLevel() <= 3) // Low confidence level indicates bad exit
            .count();
            
        double badExitPercentage = (double) badExitCount / trades.size() * 100;
        
        // Calculate average profit realization using exitReasoning
        // Using the confidence level as a proxy for how well the exit was executed
        double avgProfitRealization = trades.stream()
            .filter(t -> t.getExitReasoning() != null && 
                   t.getExitReasoning().getConfidenceLevel() != null)
            .mapToDouble(t -> t.getExitReasoning().getConfidenceLevel() / 10.0) // Convert to a 0-1 scale
            .average()
            .orElse(0.0);
        
        // Generate feedback based on exit quality
        StringBuilder feedback = new StringBuilder();
        
        if (goodExitPercentage >= 70) {
            feedback.append(String.format("Your exit decisions are generally strong, with %.0f%% of exits rated as high quality. ", 
                    goodExitPercentage));
        } else if (goodExitPercentage >= 50) {
            feedback.append(String.format("Your exit decisions are above average, with %.0f%% of exits rated as high quality. ", 
                    goodExitPercentage));
        } else {
            feedback.append(String.format("Your exit decisions need improvement, with only %.0f%% of exits rated as high quality. ", 
                    goodExitPercentage));
            feedback.append(String.format("%.0f%% of your exits were rated as poor quality. ", badExitPercentage));
        }
        
        // Add profit realization feedback
        feedback.append(String.format("On average, you capture %.0f%% of the potential profit in your trades. ", 
                avgProfitRealization * 100));
                
        if (avgProfitRealization < 0.5) {
            feedback.append("Consider holding winning trades longer or using trailing stops to capture more profit.");
        } else if (avgProfitRealization >= 0.8) {
            feedback.append("You're effectively maximizing profit potential in your trades.");
        }
        
        log.debug("Generated exit decision feedback");
        return feedback.toString();
    }
    
    /**
     * Generate overall decision quality feedback
     */
    private String generateOverallDecisionFeedback(List<TradeDetails> trades) {
        log.debug("Generating overall decision quality feedback");
        
        if (trades.isEmpty()) {
            return "No trade data available for decision quality analysis.";
        }
        
        // Calculate overall metrics
        double winRate = calculateWinRate(trades);
        double profitFactor = calculateProfitFactor(trades);
        
        // Generate feedback based on overall decision quality
        StringBuilder feedback = new StringBuilder();
        
        feedback.append(String.format("Overall decision quality: Win rate: %.0f%%, Profit factor: %.2f. ", 
                winRate, profitFactor));
                
        if (winRate >= 60 && profitFactor >= 2.0) {
            feedback.append("Your trading decisions are excellent, showing both consistency and strong risk management.");
        } else if (winRate >= 50 && profitFactor >= 1.5) {
            feedback.append("Your trading decisions are good, but there's room for improvement in consistency and/or risk management.");
        } else if (profitFactor >= 1.0) {
            feedback.append("Your trading decisions are profitable but need refinement to improve consistency and risk management.");
        } else {
            feedback.append("Your trading decisions need significant improvement. Focus on developing a more structured approach to entries and exits.");
        }
        
        log.debug("Generated overall decision quality feedback");
        return feedback.toString();
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
     * Helper method to calculate profit factor (gross profit / gross loss)
     */
    private double calculateProfitFactor(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        double grossProfit = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
            .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
            .sum();
            
        double grossLoss = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0)
            .mapToDouble(t -> Math.abs(t.getMetrics().getProfitLoss().doubleValue()))
            .sum();
            
        return grossLoss > 0 ? grossProfit / grossLoss : grossProfit > 0 ? Double.POSITIVE_INFINITY : 0.0;
    }
}
