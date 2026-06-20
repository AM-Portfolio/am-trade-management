package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for trade management metrics including stop loss adherence,
 * target adherence, and position adjustment effectiveness.
 */
@Component
public class TradeManagementCalculator implements MetricsCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the overall trade management quality score
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal stopLossAdherence = calculateStopLossAdherenceRate(trades);
        BigDecimal targetAdherence = calculateTargetAdherenceRate(trades);
        BigDecimal adjustmentEffectiveness = calculateAdjustmentEffectivenessScore(trades);
        BigDecimal scaleQuality = calculateAverageScaleQuality(trades);
        
        // Overall management quality is the weighted average of components
        return stopLossAdherence.multiply(new BigDecimal("0.3"))
                .add(targetAdherence.multiply(new BigDecimal("0.3")))
                .add(adjustmentEffectiveness.multiply(new BigDecimal("0.2")))
                .add(scaleQuality.multiply(new BigDecimal("0.2")));
    }

    /**
     * Calculate rate of adhering to stop losses
     */
    public BigDecimal calculateStopLossAdherenceRate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Count trades with stop loss set (using risk amount as indicator of stop loss)
        long tradesWithStopLoss = trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null 
                        && trade.getMetrics().getRiskAmount().compareTo(BigDecimal.ZERO) > 0)
                .count();
                
        if (tradesWithStopLoss == 0) {
            return BigDecimal.ZERO;
        }
        
        // Count trades where stop loss was honored (profit/loss > -risk amount)
        long tradesHonoringStopLoss = trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getRiskAmount() != null 
                        && trade.getMetrics().getProfitLoss() != null)
                .filter(trade -> {
                    // A trade honored stop loss if profit/loss is greater than negative risk amount
                    BigDecimal stopLossThreshold = trade.getMetrics().getRiskAmount().negate();
                    return trade.getMetrics().getProfitLoss().compareTo(stopLossThreshold) >= 0;
                })
                .count();
        
        return new BigDecimal(tradesHonoringStopLoss)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(tradesWithStopLoss), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate rate of adhering to profit targets
     */
    public BigDecimal calculateTargetAdherenceRate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Count trades with profit target set (using reward amount as indicator of profit target)
        long tradesWithTarget = trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getRewardAmount() != null 
                        && trade.getMetrics().getRewardAmount().compareTo(BigDecimal.ZERO) > 0)
                .count();
                
        if (tradesWithTarget == 0) {
            return BigDecimal.ZERO;
        }
        
        // Count trades where profit target was hit (profit/loss >= reward amount)
        long tradesHittingTarget = trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getRewardAmount() != null 
                        && trade.getMetrics().getProfitLoss() != null)
                .filter(trade -> {
                    // A trade hit profit target if profit/loss is greater than or equal to reward amount
                    return trade.getMetrics().getProfitLoss().compareTo(trade.getMetrics().getRewardAmount()) >= 0;
                })
                .count();
        
        return new BigDecimal(tradesHittingTarget)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(tradesWithTarget), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate effectiveness of position adjustments
     */
    public BigDecimal calculateAdjustmentEffectivenessScore(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Count trades with position adjustments (using multiple trade executions as indicator)
        long tradesWithAdjustments = trades.stream()
                .filter(trade -> trade.getTradeExecutions() != null && trade.getTradeExecutions().size() > 1)
                .count();
                
        if (tradesWithAdjustments == 0) {
            return BigDecimal.ZERO;
        }
        
        // Count trades where adjustments improved outcome
        long effectiveAdjustments = trades.stream()
                .filter(trade -> trade.getTradeExecutions() != null && trade.getTradeExecutions().size() > 1)
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null 
                        && trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        return new BigDecimal(effectiveAdjustments)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(tradesWithAdjustments), SCALE, ROUNDING_MODE);
    }

    /**
     * Calculate average quality of scaling in and out of positions
     */
    public BigDecimal calculateAverageScaleQuality(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal scaleInQuality = calculateAverageScaleInQuality(trades);
        BigDecimal scaleOutQuality = calculateAverageScaleOutQuality(trades);
        
        return scaleInQuality.add(scaleOutQuality).divide(new BigDecimal("2"), SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate quality of scaling into positions
     */
    public BigDecimal calculateAverageScaleInQuality(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // Count trades with multiple trade executions (indicating scale-in entries)
        long tradesWithScaleIn = trades.stream()
                .filter(trade -> trade.getTradeExecutions() != null && trade.getTradeExecutions().size() > 1)
                .count();
                
        if (tradesWithScaleIn == 0) {
            return BigDecimal.ZERO;
        }
        
        // Count profitable trades with scale-in entries
        long profitableScaleIns = trades.stream()
                .filter(trade -> trade.getTradeExecutions() != null && trade.getTradeExecutions().size() > 1)
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null 
                        && trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        return new BigDecimal(profitableScaleIns)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(tradesWithScaleIn), SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate quality of scaling out of positions
     */
    public BigDecimal calculateAverageScaleOutQuality(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // For scale-out exits, we'll look at trades with multiple executions and completed status
        long tradesWithScaleOut = trades.stream()
                .filter(trade -> trade.getTradeExecutions() != null && trade.getTradeExecutions().size() > 1)
                .filter(trade -> trade.getStatus() != null && trade.getStatus() != TradeStatus.OPEN)
                .count();
                
        if (tradesWithScaleOut == 0) {
            return BigDecimal.ZERO;
        }
        
        // Count profitable trades with scale-out exits
        long profitableScaleOuts = trades.stream()
                .filter(trade -> trade.getTradeExecutions() != null && trade.getTradeExecutions().size() > 1)
                .filter(trade -> trade.getStatus() != null && trade.getStatus() != TradeStatus.OPEN)
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null 
                        && trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        return new BigDecimal(profitableScaleOuts)
                .multiply(new BigDecimal("100"))
                .divide(new BigDecimal(tradesWithScaleOut), SCALE, ROUNDING_MODE);
    }

    @Override
    public String getMetricName() {
        return "Trade Management";
    }
}
