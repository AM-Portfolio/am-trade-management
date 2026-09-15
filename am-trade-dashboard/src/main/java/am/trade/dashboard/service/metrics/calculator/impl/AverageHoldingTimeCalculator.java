package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.AbstractBigDecimalMetricCalculator;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

/**
 * Calculator for average holding time.
 * Primary metric ({@code averageHoldingTimeOverall}) is fractional hours.
 * Also populates {@code averageHoldingTimeMinutes} when applied via
 * {@link #applyHoldingTimes(List, PerformanceMetrics)}.
 */
@Component
public class AverageHoldingTimeCalculator extends AbstractBigDecimalMetricCalculator {

    @Override
    protected BigDecimal doCalculate(List<TradeDetails> trades) {
        BigDecimal totalHoldingMinutes = BigDecimal.ZERO;
        int tradesWithHoldingTime = 0;

        for (TradeDetails trade : trades) {
            Duration holdingTime = holdDurationOrNull(trade);
            if (holdingTime == null || holdingTime.isNegative()) {
                continue;
            }
            totalHoldingMinutes = totalHoldingMinutes.add(minutesOf(holdingTime));
            tradesWithHoldingTime++;
        }

        if (tradesWithHoldingTime == 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal avgMinutes = safeDivide(
                totalHoldingMinutes, BigDecimal.valueOf(tradesWithHoldingTime));
        // Legacy field: fractional hours
        return safeDivide(avgMinutes, BigDecimal.valueOf(60));
    }

    /**
     * Sets fractional-hour overall hold and precise minutes on {@code metrics}.
     */
    public void applyHoldingTimes(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || metrics == null) {
            return;
        }
        BigDecimal totalHoldingMinutes = BigDecimal.ZERO;
        int sample = 0;
        for (TradeDetails trade : trades) {
            Duration holdingTime = holdDurationOrNull(trade);
            if (holdingTime == null || holdingTime.isNegative()) {
                continue;
            }
            totalHoldingMinutes = totalHoldingMinutes.add(minutesOf(holdingTime));
            sample++;
        }
        if (sample == 0) {
            metrics.setAverageHoldingTimeOverall(BigDecimal.ZERO);
            metrics.setAverageHoldingTimeMinutes(BigDecimal.ZERO);
            return;
        }
        BigDecimal avgMinutes = safeDivide(totalHoldingMinutes, BigDecimal.valueOf(sample));
        metrics.setAverageHoldingTimeMinutes(avgMinutes);
        metrics.setAverageHoldingTimeOverall(safeDivide(avgMinutes, BigDecimal.valueOf(60)));
    }

    private static Duration holdDurationOrNull(TradeDetails trade) {
        if (trade.getEntryInfo() == null || trade.getExitInfo() == null
                || trade.getEntryInfo().getTimestamp() == null
                || trade.getExitInfo().getTimestamp() == null) {
            return null;
        }
        return Duration.between(
                trade.getEntryInfo().getTimestamp(),
                trade.getExitInfo().getTimestamp());
    }

    private static BigDecimal minutesOf(Duration holdingTime) {
        return BigDecimal.valueOf(holdingTime.toMillis())
                .divide(BigDecimal.valueOf(60_000L), 4, java.math.RoundingMode.HALF_UP);
    }

    @Override
    public String getMetricName() {
        return "averageHoldingTimeOverall";
    }
}
