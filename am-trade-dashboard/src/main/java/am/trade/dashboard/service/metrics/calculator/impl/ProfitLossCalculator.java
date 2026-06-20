package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Calculator for total profit/loss metric
 */
@Component
public class ProfitLossCalculator extends AbstractBigDecimalMetricCalculator {

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        return trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null)
                .map(trade -> trade.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public String getMetricName() {
        return "totalProfitLoss";
    }
}
