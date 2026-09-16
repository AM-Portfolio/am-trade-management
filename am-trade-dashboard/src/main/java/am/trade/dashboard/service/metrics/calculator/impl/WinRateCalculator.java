package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Calculator for win rate metrics.
 * Win % = wins (pnl &gt; 0) / eligible (non-null pnl). Returns null when eligible=0.
 */
@Component
public class WinRateCalculator implements MetricsCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return null;
        }

        long eligible = 0;
        long winCount = 0;
        for (TradeDetails t : trades) {
            if (t.getMetrics() == null || t.getMetrics().getProfitLoss() == null) {
                continue;
            }
            eligible++;
            if (t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
                winCount++;
            }
        }

        if (eligible == 0) {
            return null;
        }

        return BigDecimal.valueOf(winCount * 100.0 / eligible).setScale(SCALE, ROUNDING_MODE);
    }
    
    @Override
    public String getMetricName() {
        return "Win Rate";
    }
}
