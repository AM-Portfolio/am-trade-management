package am.trade.dashboard.service.metrics.analyzer;

import am.trade.dashboard.service.metrics.analyzer.impl.AdaptabilityAnalyzer;
import am.trade.dashboard.service.metrics.analyzer.impl.OverconfidenceAnalyzer;
import am.trade.dashboard.service.metrics.analyzer.impl.PatternConsistencyAnalyzer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating and managing trade metrics analyzer instances
 * Uses Spring dependency injection to get analyzer beans
 */
@Component
public class TradeMetricsAnalyzerFactory {

    private final Map<String, TradeMetricsAnalyzer> analyzers = new HashMap<>();
    
    public TradeMetricsAnalyzerFactory(
            AdaptabilityAnalyzer adaptabilityAnalyzer,
            OverconfidenceAnalyzer overconfidenceAnalyzer,
            PatternConsistencyAnalyzer patternConsistencyAnalyzer) {
        
        // Register all available analyzers
        registerAnalyzer("Adaptability Score", adaptabilityAnalyzer);
        registerAnalyzer("Overconfidence Index", overconfidenceAnalyzer);
        registerAnalyzer("Pattern Consistency", patternConsistencyAnalyzer);
    }
    
    /**
     * Register an analyzer with the factory
     * 
     * @param name Name to register the analyzer under
     * @param analyzer Analyzer instance
     */
    public void registerAnalyzer(String name, TradeMetricsAnalyzer analyzer) {
        analyzers.put(name, analyzer);
    }
    
    /**
     * Get an analyzer by name
     * 
     * @param name Name of the analyzer to retrieve
     * @return The analyzer instance
     * @throws IllegalArgumentException if analyzer not found
     */
    public TradeMetricsAnalyzer getAnalyzer(String name) {
        TradeMetricsAnalyzer analyzer = analyzers.get(name);
        if (analyzer == null) {
            throw new IllegalArgumentException("No analyzer registered with name: " + name);
        }
        return analyzer;
    }
    
    /**
     * Check if an analyzer exists
     * 
     * @param name Name of the analyzer to check
     * @return true if analyzer exists, false otherwise
     */
    public boolean hasAnalyzer(String name) {
        return analyzers.containsKey(name);
    }
}
