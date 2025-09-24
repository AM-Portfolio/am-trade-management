package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for emotional metrics such as fear-based exits, greed-based entries,
 * and impulsive trade percentages.
 */
@Component
public class EmotionalMetricsCalculator implements MetricsCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the emotional control score
        // A composite score based on fear, greed, and impulsivity metrics
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal fearBasedExitPercentage = calculateFearBasedExitPercentage(trades);
        BigDecimal greedBasedEntryPercentage = calculateGreedBasedEntryPercentage(trades);
        BigDecimal impulsiveTradePercentage = calculateImpulsiveTradePercentage(trades);

        // Calculate emotional control score (100 - average of negative factors)
        BigDecimal negativeFactorsAvg = fearBasedExitPercentage
                .add(greedBasedEntryPercentage)
                .add(impulsiveTradePercentage)
                .divide(new BigDecimal("3"), SCALE, ROUNDING_MODE);

        // Higher score means better emotional control (less fear, greed, impulsivity)
        return new BigDecimal("100").subtract(negativeFactorsAvg);
    }

    /**
     * Calculate percentage of trades with fear-based exits
     */
    public BigDecimal calculateFearBasedExitPercentage(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long fearBasedExits = trades.stream()
                .filter(trade -> trade.getPsychologyData() != null && trade.getPsychologyData().getExitPsychologyFactors() != null)
                .filter(trade -> trade.getPsychologyData().getExitPsychologyFactors().stream()
                        .anyMatch(factor -> "FEAR".equals(factor.getCode()) || "PANIC".equals(factor.getCode())))
                .count();

        return new BigDecimal(fearBasedExits)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(trades.size()), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate percentage of trades with greed-based entries
     */
    public BigDecimal calculateGreedBasedEntryPercentage(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long greedBasedEntries = trades.stream()
                .filter(trade -> trade.getPsychologyData() != null && trade.getPsychologyData().getEntryPsychologyFactors() != null)
                .filter(trade -> trade.getPsychologyData().getEntryPsychologyFactors().stream()
                        .anyMatch(factor -> "GREED".equals(factor.getCode()) || "FEAR_OF_MISSING_OUT".equals(factor.getCode())))
                .count();

        return new BigDecimal(greedBasedEntries)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(trades.size()), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate percentage of trades marked as impulsive
     */
    public BigDecimal calculateImpulsiveTradePercentage(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        long impulsiveTrades = trades.stream()
                .filter(trade -> trade.getPsychologyData() != null && trade.getPsychologyData().getEntryPsychologyFactors() != null)
                .filter(trade -> trade.getPsychologyData().getEntryPsychologyFactors().stream()
                        .anyMatch(factor -> "IMPULSIVE".equals(factor.getCode())))
                .count();

        return new BigDecimal(impulsiveTrades)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(trades.size()), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate discipline score based on adherence to trading plan
     */
    public BigDecimal calculateDisciplineScore(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Count trades that followed the plan (not impulsive, not FOMO)
        long disciplinedTrades = trades.stream()
                .filter(trade -> trade.getPsychologyData() != null && 
                        trade.getPsychologyData().getEntryPsychologyFactors() != null && 
                        trade.getPsychologyData().getExitPsychologyFactors() != null)
                .filter(trade -> 
                        trade.getPsychologyData().getEntryPsychologyFactors().stream()
                            .noneMatch(factor -> "IMPULSIVE".equals(factor.getCode()) || 
                                              "FEAR_OF_MISSING_OUT".equals(factor.getCode())) &&
                        trade.getPsychologyData().getExitPsychologyFactors().stream()
                            .noneMatch(factor -> "PANIC".equals(factor.getCode())))
                .count();

        return new BigDecimal(disciplinedTrades)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(trades.size()), SCALE, ROUNDING_MODE);
    }

    @Override
    public String getMetricName() {
        return "Emotional Metrics";
    }
}
