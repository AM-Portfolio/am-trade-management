package am.trade.dashboard.service.metrics.calculator;

import am.trade.dashboard.service.metrics.calculator.impl.*;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Factory for creating and managing metrics calculator instances
 * Uses Spring dependency injection to get calculator beans
 */
@Component
public class MetricsCalculatorFactory {

    private final Map<String, MetricsCalculator> calculators = new HashMap<>();
    
    public MetricsCalculatorFactory(
            RiskRewardCalculator riskRewardCalculator,
            ExpectancyCalculator expectancyCalculator,
            WinRateCalculator winRateCalculator,
            ProfitabilityCalculator profitabilityCalculator,
            EmotionalMetricsCalculator emotionalMetricsCalculator,
            DecisionQualityCalculator decisionQualityCalculator,
            TradeManagementCalculator tradeManagementCalculator,
            List<MetricsCalculator> additionalCalculators) {
        
        // Register all available calculators
        registerCalculator("Risk-Reward Ratio", riskRewardCalculator);
        registerCalculator("Trade Expectancy", expectancyCalculator);
        registerCalculator("Win Rate", winRateCalculator);
        registerCalculator("Profitability", profitabilityCalculator);
        registerCalculator("Emotional Metrics", emotionalMetricsCalculator);
        registerCalculator("Decision Quality", decisionQualityCalculator);
        registerCalculator("Trade Management", tradeManagementCalculator);
        
        // Register any additional calculators provided through dependency injection
        additionalCalculators.forEach(calculator -> registerCalculator(calculator.getMetricName(), calculator));
    }
    
    /**
     * Register a calculator with the factory
     * 
     * @param name Name to register the calculator under
     * @param calculator Calculator instance
     */
    public void registerCalculator(String name, MetricsCalculator calculator) {
        calculators.put(name, calculator);
    }
    
    /**
     * Get a calculator by name
     * 
     * @param name Name of the calculator to retrieve
     * @return The calculator instance
     * @throws IllegalArgumentException if calculator not found
     */
    public MetricsCalculator getCalculator(String name) {
        MetricsCalculator calculator = calculators.get(name);
        if (calculator == null) {
            throw new IllegalArgumentException("No calculator registered with name: " + name);
        }
        return calculator;
    }
    
    /**
     * Check if a calculator exists
     * 
     * @param name Name of the calculator to check
     * @return true if calculator exists, false otherwise
     */
    public boolean hasCalculator(String name) {
        return calculators.containsKey(name);
    }
    
    /**
     * Get all registered calculator names
     * 
     * @return Set of calculator names
     */
    public Iterable<String> getCalculatorNames() {
        return calculators.keySet();
    }
    
    /**
     * Get all available calculators
     * 
     * @return Map of all registered calculators
     */
    public Map<String, MetricsCalculator> getAllCalculators() {
        return new HashMap<>(calculators);
    }
}
