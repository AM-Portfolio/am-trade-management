package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Calculator for average holding time metric in hours
 */
@Component
public class AverageHoldingTimeCalculator extends AbstractBigDecimalMetricCalculator {

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        BigDecimal totalHoldingHours = BigDecimal.ZERO;
        int tradesWithHoldingTime = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getEntryInfo() != null && trade.getExitInfo() != null &&
                trade.getEntryInfo().getTimestamp() != null && trade.getExitInfo().getTimestamp() != null) {
                
                Duration holdingTime = Duration.between(
                    trade.getEntryInfo().getTimestamp(), 
                    trade.getExitInfo().getTimestamp()
                );
                
                // Skip negative durations (data error)
                if (holdingTime.isNegative()) {
                    continue;
                }
                
                totalHoldingHours = totalHoldingHours.add(
                    BigDecimal.valueOf(holdingTime.toHours())
                );
                tradesWithHoldingTime++;
            }
        }
        
        return tradesWithHoldingTime > 0 ?
                safeDivide(totalHoldingHours, BigDecimal.valueOf(tradesWithHoldingTime)) :
                BigDecimal.ZERO;
    }

    @Override
    public String getMetricName() {
        return "averageHoldingTimeOverall";
    }
}
