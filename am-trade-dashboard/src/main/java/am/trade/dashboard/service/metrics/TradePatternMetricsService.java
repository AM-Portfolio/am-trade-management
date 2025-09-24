package am.trade.dashboard.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradePatternMetrics;
import am.trade.common.models.enums.EntryPsychology;
import am.trade.common.models.enums.ExitPsychology;
import am.trade.common.models.enums.TradeBehaviorPattern;
import am.trade.dashboard.service.metrics.analyzer.TradeMetricsAnalyzer;
import am.trade.dashboard.service.metrics.analyzer.TradeMetricsAnalyzerFactory;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculatorFactory;
import am.trade.dashboard.service.metrics.grouping.TradeGroupingResult;
import am.trade.dashboard.service.metrics.grouping.TradeGroupingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Service for calculating trade pattern and psychology metrics from trade data
 * Refactored to use Strategy and Factory patterns for better extensibility
 */
@Service
@Slf4j
public class TradePatternMetricsService {

    private final TradeGroupingService groupingService;
    private final MetricsCalculatorFactory calculatorFactory;
    private final TradeMetricsAnalyzerFactory analyzerFactory;
    
    public TradePatternMetricsService(
            TradeGroupingService groupingService,
            MetricsCalculatorFactory calculatorFactory,
            TradeMetricsAnalyzerFactory analyzerFactory) {
        this.groupingService = groupingService;
        this.calculatorFactory = calculatorFactory;
        this.analyzerFactory = analyzerFactory;
    }
    
    /**
     * Calculate pattern metrics from a list of trades
     */
    public TradePatternMetrics calculateMetrics(List<TradeDetails> trades) {
        String processId = UUID.randomUUID().toString();
        log.info("[{}] Starting pattern metrics calculation for {} trades", processId, trades.size());
        if (trades == null || trades.isEmpty()) {
            return new TradePatternMetrics();
        }
        
        TradePatternMetrics metrics = new TradePatternMetrics();
        
        // Group trades and count psychology factors
        TradeGroupingResult groupingResult = groupingService.groupTradesByFactors(trades);
        
        // Get grouped trades
        Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern = groupingResult.getTradesByPattern();
        Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology = groupingResult.getTradesByEntryPsychology();
        Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology = groupingResult.getTradesByExitPsychology();
        
        // Calculate pattern metrics
        calculatePatternMetrics(metrics, tradesByPattern, trades.size());
        
        // Calculate entry psychology metrics
        calculateEntryPsychologyMetrics(metrics, tradesByEntryPsychology, trades.size());
        
        // Calculate exit psychology metrics
        calculateExitPsychologyMetrics(metrics, tradesByExitPsychology, trades.size());
        
        // Calculate overall metrics
        calculateOverallMetrics(metrics, trades);
        
        // Calculate advanced analytics
        calculateAdvancedAnalytics(metrics, trades, groupingResult);
        
        log.info("[{}] Completed pattern metrics calculation", processId);
        return metrics;
    }
    
    /**
     * Calculate metrics for each behavior pattern
     */
    private void calculatePatternMetrics(
            TradePatternMetrics metrics,
            Map<TradeBehaviorPattern, List<TradeDetails>> tradesByPattern,
            int totalTrades) {
        
        Map<TradeBehaviorPattern, Integer> patternFrequency = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternProfitLoss = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternWinRate = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternExpectancy = new HashMap<>();
        Map<TradeBehaviorPattern, BigDecimal> patternRiskRewardRatio = new HashMap<>();
        
        // Process pattern metrics
        for (Map.Entry<TradeBehaviorPattern, List<TradeDetails>> entry : tradesByPattern.entrySet()) {
            TradeBehaviorPattern pattern = entry.getKey();
            List<TradeDetails> patternTrades = entry.getValue();
            
            // Calculate frequency
            patternFrequency.put(pattern, patternTrades.size());
            
            // Calculate profit/loss
            MetricsCalculator profitabilityCalculator = calculatorFactory.getCalculator("Profitability");
            patternProfitLoss.put(pattern, profitabilityCalculator.calculate(patternTrades));
            
            // Calculate win rate
            MetricsCalculator winRateCalculator = calculatorFactory.getCalculator("Win Rate");
            patternWinRate.put(pattern, winRateCalculator.calculate(patternTrades));
            
            // Calculate expectancy
            MetricsCalculator expectancyCalculator = calculatorFactory.getCalculator("Trade Expectancy");
            patternExpectancy.put(pattern, expectancyCalculator.calculate(patternTrades));
            
            // Calculate risk/reward ratio
            MetricsCalculator riskRewardCalculator = calculatorFactory.getCalculator("Risk-Reward Ratio");
            patternRiskRewardRatio.put(pattern, riskRewardCalculator.calculate(patternTrades));
        }
        
        // Set metrics in result object
        metrics.setPatternFrequency(patternFrequency);
        metrics.setPatternProfitLoss(patternProfitLoss);
        metrics.setPatternWinRate(patternWinRate);
        metrics.setPatternExpectancy(patternExpectancy);
        metrics.setPatternRiskRewardRatio(patternRiskRewardRatio);
    }
    
    /**
     * Calculate metrics for each entry psychology factor
     */
    private void calculateEntryPsychologyMetrics(
            TradePatternMetrics metrics,
            Map<EntryPsychology, List<TradeDetails>> tradesByEntryPsychology,
            int totalTrades) {
        
        Map<EntryPsychology, Integer> entryPsychologyFrequency = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyProfitLoss = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyWinRate = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyExpectancy = new HashMap<>();
        Map<EntryPsychology, BigDecimal> entryPsychologyRiskRewardRatio = new HashMap<>();
        
        // Process entry psychology metrics
        for (Map.Entry<EntryPsychology, List<TradeDetails>> entry : tradesByEntryPsychology.entrySet()) {
            EntryPsychology factor = entry.getKey();
            List<TradeDetails> factorTrades = entry.getValue();
            
            // Calculate frequency
            entryPsychologyFrequency.put(factor, factorTrades.size());
            
            // Calculate profit/loss
            MetricsCalculator profitabilityCalculator = calculatorFactory.getCalculator("Profitability");
            entryPsychologyProfitLoss.put(factor, profitabilityCalculator.calculate(factorTrades));
            
            // Calculate win rate
            MetricsCalculator winRateCalculator = calculatorFactory.getCalculator("Win Rate");
            entryPsychologyWinRate.put(factor, winRateCalculator.calculate(factorTrades));
            
            // Calculate expectancy
            MetricsCalculator expectancyCalculator = calculatorFactory.getCalculator("Trade Expectancy");
            entryPsychologyExpectancy.put(factor, expectancyCalculator.calculate(factorTrades));
            
            // Calculate risk/reward ratio
            MetricsCalculator riskRewardCalculator = calculatorFactory.getCalculator("Risk-Reward Ratio");
            entryPsychologyRiskRewardRatio.put(factor, riskRewardCalculator.calculate(factorTrades));
        }
        
        // Set metrics in result object
        metrics.setEntryPsychologyFrequency(entryPsychologyFrequency);
        metrics.setEntryPsychologyProfitLoss(entryPsychologyProfitLoss);
        metrics.setEntryPsychologyWinRate(entryPsychologyWinRate);
        metrics.setEntryPsychologyExpectancy(entryPsychologyExpectancy);
        metrics.setEntryPsychologyRiskRewardRatio(entryPsychologyRiskRewardRatio);
    }
    
    /**
     * Calculate metrics for each exit psychology factor
     */
    private void calculateExitPsychologyMetrics(
            TradePatternMetrics metrics,
            Map<ExitPsychology, List<TradeDetails>> tradesByExitPsychology,
            int totalTrades) {
        
        Map<ExitPsychology, Integer> exitPsychologyFrequency = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyProfitLoss = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyWinRate = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyExpectancy = new HashMap<>();
        Map<ExitPsychology, BigDecimal> exitPsychologyRiskRewardRatio = new HashMap<>();
        
        // Process exit psychology metrics
        for (Map.Entry<ExitPsychology, List<TradeDetails>> entry : tradesByExitPsychology.entrySet()) {
            ExitPsychology factor = entry.getKey();
            List<TradeDetails> factorTrades = entry.getValue();
            
            // Calculate frequency
            exitPsychologyFrequency.put(factor, factorTrades.size());
            
            // Calculate profit/loss
            MetricsCalculator profitabilityCalculator = calculatorFactory.getCalculator("Profitability");
            exitPsychologyProfitLoss.put(factor, profitabilityCalculator.calculate(factorTrades));
            
            // Calculate win rate
            MetricsCalculator winRateCalculator = calculatorFactory.getCalculator("Win Rate");
            exitPsychologyWinRate.put(factor, winRateCalculator.calculate(factorTrades));
            
            // Calculate expectancy
            MetricsCalculator expectancyCalculator = calculatorFactory.getCalculator("Trade Expectancy");
            exitPsychologyExpectancy.put(factor, expectancyCalculator.calculate(factorTrades));
            
            // Calculate risk/reward ratio
            MetricsCalculator riskRewardCalculator = calculatorFactory.getCalculator("Risk-Reward Ratio");
            exitPsychologyRiskRewardRatio.put(factor, riskRewardCalculator.calculate(factorTrades));
        }
        
        // Set metrics in result object
        metrics.setExitPsychologyFrequency(exitPsychologyFrequency);
        metrics.setExitPsychologyProfitLoss(exitPsychologyProfitLoss);
        metrics.setExitPsychologyWinRate(exitPsychologyWinRate);
        metrics.setExitPsychologyExpectancy(exitPsychologyExpectancy);
        metrics.setExitPsychologyRiskRewardRatio(exitPsychologyRiskRewardRatio);
    }
    
    /**
     * Calculate overall metrics for all trades
     */
    private void calculateOverallMetrics(TradePatternMetrics metrics, List<TradeDetails> trades) {
        // Calculate overall win rate
        MetricsCalculator winRateCalculator = calculatorFactory.getCalculator("Win Rate");
        metrics.setOverallWinRate(winRateCalculator.calculate(trades));
        
        // Calculate overall expectancy
        MetricsCalculator expectancyCalculator = calculatorFactory.getCalculator("Trade Expectancy");
        metrics.setOverallExpectancy(expectancyCalculator.calculate(trades));
        
        // Calculate overall risk/reward ratio
        MetricsCalculator riskRewardCalculator = calculatorFactory.getCalculator("Risk-Reward Ratio");
        metrics.setOverallRiskRewardRatio(riskRewardCalculator.calculate(trades));
    }
    
    /**
     * Calculate advanced analytics metrics
     */
    private void calculateAdvancedAnalytics(
            TradePatternMetrics metrics,
            List<TradeDetails> trades,
            TradeGroupingResult groupingResult) {
        
        // Calculate adaptability score
        TradeMetricsAnalyzer adaptabilityAnalyzer = analyzerFactory.getAnalyzer("Adaptability Score");
        metrics.setAdaptabilityScore(adaptabilityAnalyzer.analyze(trades, groupingResult));
        
        // Calculate overconfidence index
        TradeMetricsAnalyzer overconfidenceAnalyzer = analyzerFactory.getAnalyzer("Overconfidence Index");
        metrics.setOverconfidenceIndex(overconfidenceAnalyzer.analyze(trades, groupingResult));
        
        // Calculate pattern consistency score
        TradeMetricsAnalyzer patternConsistencyAnalyzer = analyzerFactory.getAnalyzer("Pattern Consistency");
        metrics.setPatternConsistencyScore(patternConsistencyAnalyzer.analyze(trades, groupingResult));
    }
}