package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calculator for win-loss ratio metric (average win / average loss)
 */
@Component
public class WinLossRatioCalculator extends AbstractBigDecimalMetricCalculator {

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        BigDecimal totalWinAmount = BigDecimal.ZERO;
        BigDecimal totalLossAmount = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null) {
                continue;
            }
            
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            int comparison = profitLoss.compareTo(BigDecimal.ZERO);
            
            if (comparison > 0) {
                // Win
                totalWinAmount = totalWinAmount.add(profitLoss);
                winCount++;
            } else if (comparison < 0) {
                // Loss
                totalLossAmount = totalLossAmount.add(profitLoss.abs());
                lossCount++;
            }
        }
        
        // Calculate average win and loss
        BigDecimal averageWin = winCount > 0 ? 
                safeDivide(totalWinAmount, BigDecimal.valueOf(winCount)) : 
                BigDecimal.ZERO;
        
        BigDecimal averageLoss = lossCount > 0 ? 
                safeDivide(totalLossAmount, BigDecimal.valueOf(lossCount)) : 
                BigDecimal.ZERO;
        
        // Calculate win-loss ratio
        return averageLoss.compareTo(BigDecimal.ZERO) > 0 ? 
                safeDivide(averageWin, averageLoss) : 
                BigDecimal.ZERO;
    }

    @Override
    public String getMetricName() {
        return "winLossRatio";
    }
}
