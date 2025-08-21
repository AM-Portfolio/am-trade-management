package am.trade.dashboard.service.metrics.calculator;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.impl.AverageTradeCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.BestWorstDayCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.ConsistencyMetricsCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.LargestTradeCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.TradeFrequencyCalculator;
import am.trade.dashboard.service.metrics.calculator.impl.TimeBasedReturnCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Registry for metric calculators that manages the collection of all available
 * metric calculators and provides methods to calculate and apply metrics.
 * 
 * This class follows the Composite and Strategy patterns to allow for flexible
 * calculation of various trading metrics.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsRegistry {

    private final List<MetricsCalculator> calculators;
    
    /**
     * Calculate all metrics and apply them to the provided PerformanceMetrics object
     * 
     * @param trades List of trade details to analyze
     * @param metrics PerformanceMetrics object to populate with calculated values
     */
    public void calculateAndApplyMetrics(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            log.warn("Cannot calculate metrics: trades list is empty or metrics object is null");
            return;
        }
        
        log.debug("Calculating metrics using {} calculators", calculators.size());
        
        // Get mapping of metric names to setters
        Map<String, BiConsumer<PerformanceMetrics, BigDecimal>> metricSetters = getMetricSetters();
        
        // Process special calculators first (those that calculate multiple metrics)
        processSpecialCalculators(trades, metrics);
        
        // Calculate and apply each standard metric
        for (MetricsCalculator calculator : calculators) {
            // Skip special calculators as they've already been processed
            if (isSpecialCalculator(calculator)) {
                continue;
            }
            
            try {
                String metricName = calculator.getMetricName();
                BigDecimal value = calculator.calculate(trades);
                
                BiConsumer<PerformanceMetrics, BigDecimal> setter = metricSetters.get(metricName);
                if (setter != null) {
                    setter.accept(metrics, value);
                    log.debug("Applied metric {}: {}", metricName, value);
                } else {
                    log.warn("No setter found for metric: {}", metricName);
                }
            } catch (Exception e) {
                log.error("Error calculating metric with calculator: {}", calculator.getClass().getSimpleName(), e);
            }
        }
    }
    
    /**
     * Process special calculators that calculate multiple metrics at once
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    private void processSpecialCalculators(List<TradeDetails> trades, PerformanceMetrics metrics) {
        // Process TimeBasedReturnCalculator
        calculators.stream()
                .filter(c -> c instanceof TimeBasedReturnCalculator)
                .findFirst()
                .ifPresent(calculator -> {
                    try {
                        TimeBasedReturnCalculator timeCalculator = (TimeBasedReturnCalculator) calculator;
                        timeCalculator.calculateTimeBasedReturns(trades, metrics);
                        log.debug("Applied time-based return metrics");
                    } catch (Exception e) {
                        log.error("Error calculating time-based metrics", e);
                    }
                });
        
        // Process LargestTradeCalculator
        calculators.stream()
                .filter(c -> c instanceof LargestTradeCalculator)
                .findFirst()
                .ifPresent(calculator -> {
                    try {
                        LargestTradeCalculator largestTradeCalculator = (LargestTradeCalculator) calculator;
                        largestTradeCalculator.calculateLargestTrades(trades, metrics);
                        log.debug("Applied largest trade metrics");
                    } catch (Exception e) {
                        log.error("Error calculating largest trade metrics", e);
                    }
                });
        
        // Process AverageTradeCalculator
        calculators.stream()
                .filter(c -> c instanceof AverageTradeCalculator)
                .findFirst()
                .ifPresent(calculator -> {
                    try {
                        AverageTradeCalculator averageTradeCalculator = (AverageTradeCalculator) calculator;
                        averageTradeCalculator.calculateAverageTrades(trades, metrics);
                        log.debug("Applied average trade metrics");
                    } catch (Exception e) {
                        log.error("Error calculating average trade metrics", e);
                    }
                });
        
        // Process BestWorstDayCalculator
        calculators.stream()
                .filter(c -> c instanceof BestWorstDayCalculator)
                .findFirst()
                .ifPresent(calculator -> {
                    try {
                        BestWorstDayCalculator bestWorstDayCalculator = (BestWorstDayCalculator) calculator;
                        bestWorstDayCalculator.calculateBestWorstDays(trades, metrics);
                        log.debug("Applied best/worst day metrics");
                    } catch (Exception e) {
                        log.error("Error calculating best/worst day metrics", e);
                    }
                });
        
        // Process ConsistencyMetricsCalculator
        calculators.stream()
                .filter(c -> c instanceof ConsistencyMetricsCalculator)
                .findFirst()
                .ifPresent(calculator -> {
                    try {
                        ConsistencyMetricsCalculator consistencyCalculator = (ConsistencyMetricsCalculator) calculator;
                        consistencyCalculator.calculateConsistencyMetrics(trades, metrics);
                        log.debug("Applied consistency metrics");
                    } catch (Exception e) {
                        log.error("Error calculating consistency metrics", e);
                    }
                });
    }
    
    /**
     * Check if a calculator is a special calculator that handles multiple metrics
     * 
     * @param calculator The calculator to check
     * @return True if it's a special calculator
     */
    private boolean isSpecialCalculator(MetricsCalculator calculator) {
        return calculator instanceof TimeBasedReturnCalculator ||
               calculator instanceof LargestTradeCalculator ||
               calculator instanceof AverageTradeCalculator ||
               calculator instanceof BestWorstDayCalculator ||
               calculator instanceof ConsistencyMetricsCalculator;
    }
    
    /**
     * Get all available metric calculators, sorted by priority
     * 
     * @return List of metric calculators
     */
    public List<MetricsCalculator> getCalculators() {
        // Sort calculators by priority if needed
        return calculators;
    }
    
    /**
     * Get a map of metric names to their corresponding setters in the PerformanceMetrics class
     * 
     * @return Map of metric name to setter function
     */
    private Map<String, BiConsumer<PerformanceMetrics, BigDecimal>> getMetricSetters() {
        // Using HashMap instead of Map.ofEntries for better maintainability with many entries
        Map<String, BiConsumer<PerformanceMetrics, BigDecimal>> setters = new HashMap<>();
        
        // Core performance metrics
        setters.put("totalProfitLoss", PerformanceMetrics::setTotalProfitLoss);
        setters.put("Win Rate", PerformanceMetrics::setWinRate);
        setters.put("profitFactor", PerformanceMetrics::setProfitFactor);
        setters.put("expectancy", PerformanceMetrics::setExpectancy);
        setters.put("Trade Expectancy", PerformanceMetrics::setExpectancy);
        
        // Return metrics
        setters.put("annualizedReturn", PerformanceMetrics::setAnnualizedReturn);
        setters.put("monthlyReturn", PerformanceMetrics::setMonthlyReturn);
        setters.put("quarterlyReturn", PerformanceMetrics::setQuarterlyReturn);
        setters.put("yearToDateReturn", PerformanceMetrics::setYearToDateReturn);
        
        // Consistency metrics
        setters.put("averageWinningTrade", PerformanceMetrics::setAverageWinningTrade);
        setters.put("averageLosingTrade", PerformanceMetrics::setAverageLosingTrade);
        setters.put("largestWinningTrade", PerformanceMetrics::setLargestWinningTrade);
        setters.put("largestLosingTrade", PerformanceMetrics::setLargestLosingTrade);
        setters.put("winLossRatio", PerformanceMetrics::setWinLossRatio);
        
        // Streak information
        setters.put("longestWinningStreak", (metrics, value) -> metrics.setLongestWinningStreak(value.intValue()));
        setters.put("longestLosingStreak", (metrics, value) -> metrics.setLongestLosingStreak(value.intValue()));
        setters.put("currentStreak", (metrics, value) -> metrics.setCurrentStreak(value.intValue()));
        
        // Time metrics
        setters.put("averageHoldingTimeWinning", PerformanceMetrics::setAverageHoldingTimeWinning);
        setters.put("averageHoldingTimeLosing", PerformanceMetrics::setAverageHoldingTimeLosing);
        setters.put("averageHoldingTimeOverall", PerformanceMetrics::setAverageHoldingTimeOverall);
        
        // Efficiency metrics
        setters.put("returnOnCapital", PerformanceMetrics::setReturnOnCapital);
        setters.put("returnPerUnit", PerformanceMetrics::setReturnPerUnit);
        setters.put("tradesPerDay", (metrics, value) -> {
            // Custom handling for metrics that don't have direct setters
            // This is just an example of how to handle custom metrics
            log.debug("Trades per day: {}", value);
        });
        
        return setters;
    }
}
