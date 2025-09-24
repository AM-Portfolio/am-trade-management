package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for win rate metrics
 */
@Component
public class WinRateCalculator implements MetricsCalculator {

    private static final int SCALE = 2; // Win rate typically shown as percentage with 2 decimal places
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        long winCount = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                       t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        // Calculate as percentage
        return BigDecimal.valueOf(winCount * 100.0 / trades.size()).setScale(SCALE, ROUNDING_MODE);
    }
    
    @Override
    public String getMetricName() {
        return "Win Rate";
    }
}
