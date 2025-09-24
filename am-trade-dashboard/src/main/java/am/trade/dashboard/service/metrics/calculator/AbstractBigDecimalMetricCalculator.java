package am.trade.dashboard.service.metrics.calculator;

import am.trade.common.models.TradeDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Abstract base class for metric calculators that return BigDecimal values.
 * Provides common functionality for BigDecimal-based metrics.
 */
public abstract class AbstractBigDecimalMetricCalculator implements MetricCalculator<BigDecimal> {

    protected static final int DEFAULT_SCALE = 4;
    protected static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    protected final int scale;
    protected final RoundingMode roundingMode;
    
    /**
     * Constructor with default scale and rounding mode
     */
    protected AbstractBigDecimalMetricCalculator() {
        this(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
    
    /**
     * Constructor with custom scale and rounding mode
     * 
     * @param scale The scale to use for BigDecimal operations
     * @param roundingMode The rounding mode to use for BigDecimal operations
     */
    protected AbstractBigDecimalMetricCalculator(int scale, RoundingMode roundingMode) {
        this.scale = scale;
        this.roundingMode = roundingMode;
    }
    
    /**
     * Calculate the metric from trade data
     * 
     * @param trades List of trade details to analyze
     * @return The calculated metric value as a BigDecimal
     */
    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return doCalculate(trades);
    }
    
    /**
     * Template method to be implemented by concrete calculators
     * 
     * @param trades List of trade details to analyze
     * @return The calculated metric value
     */
    protected abstract BigDecimal doCalculate(List<TradeDetails> trades);
    
    /**
     * Safely divide two BigDecimals, handling division by zero
     * 
     * @param numerator The numerator
     * @param denominator The denominator
     * @return The result of the division, or BigDecimal.ZERO if denominator is zero
     */
    protected BigDecimal safeDivide(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return numerator.divide(denominator, scale, roundingMode);
    }
    
    /**
     * Safely divide two BigDecimals and convert to percentage, handling division by zero
     * 
     * @param numerator The numerator
     * @param denominator The denominator
     * @return The result of the division as a percentage, or BigDecimal.ZERO if denominator is zero
     */
    protected BigDecimal safePercentage(BigDecimal numerator, BigDecimal denominator) {
        return safeDivide(numerator, denominator).multiply(new BigDecimal("100")).setScale(2, roundingMode);
    }
}
