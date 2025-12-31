package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for risk-reward ratio metrics
 */
@Component
public class RiskRewardCalculator implements MetricsCalculator {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
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
    
    @Override
    public String getMetricName() {
        return "Risk-Reward Ratio";
    }
}
