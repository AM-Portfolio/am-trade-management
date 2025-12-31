package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Calculator for profit factor metric (total winning amount / total losing amount)
 */
@Component
public class ProfitFactorCalculator extends AbstractBigDecimalMetricCalculator {

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        BigDecimal totalWinAmount = BigDecimal.ZERO;
        BigDecimal totalLossAmount = BigDecimal.ZERO;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null) {
                continue;
            }
            
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            int comparison = profitLoss.compareTo(BigDecimal.ZERO);
            
            if (comparison > 0) {
                // Win
                totalWinAmount = totalWinAmount.add(profitLoss);
            } else if (comparison < 0) {
                // Loss
                totalLossAmount = totalLossAmount.add(profitLoss.abs());
            }
        }
        
        // Calculate profit factor (total wins / total losses)
        return totalLossAmount.compareTo(BigDecimal.ZERO) > 0 ? 
                safeDivide(totalWinAmount, totalLossAmount) : 
                BigDecimal.ZERO;
    }

    @Override
    public String getMetricName() {
        return "profitFactor";
    }
}
