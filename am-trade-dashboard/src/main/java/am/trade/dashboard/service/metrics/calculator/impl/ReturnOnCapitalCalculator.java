package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for Return on Capital (ROI) metric
 */
@Component
public class ReturnOnCapitalCalculator extends AbstractBigDecimalMetricCalculator {

    public ReturnOnCapitalCalculator() {
        super(2, RoundingMode.HALF_UP); // Use 2 decimal places for percentage
    }

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        BigDecimal totalInvestedAmount = BigDecimal.ZERO;
        
        for (TradeDetails trade : trades) {
            // Calculate profit/loss
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                totalProfitLoss = totalProfitLoss.add(trade.getMetrics().getProfitLoss());
            }
            
            // Calculate position size if available
            if (trade.getEntryInfo() != null && trade.getEntryInfo().getPrice() != null && 
                trade.getEntryInfo().getQuantity() != null) {
                BigDecimal positionSize = trade.getEntryInfo().getPrice()
                    .multiply(new BigDecimal(trade.getEntryInfo().getQuantity().toString()));
                totalInvestedAmount = totalInvestedAmount.add(positionSize);
            }
        }
        
        // Calculate ROI as percentage
        if (totalInvestedAmount.compareTo(BigDecimal.ZERO) > 0) {
            return totalProfitLoss.divide(totalInvestedAmount, scale, roundingMode)
                .multiply(new BigDecimal("100"));
        }
        
        return BigDecimal.ZERO;
    }

    @Override
    public String getMetricName() {
        return "returnOnCapital";
    }
}
