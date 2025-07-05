package am.trade.dashboard.service;

import am.trade.common.models.*;
import am.trade.dashboard.service.metrics.*;
// Removed unused import
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating various trade metrics including pattern and psychology metrics
 * This service delegates the actual metric calculations to specialized service classes
 */
@Service
@Slf4j
public class TradeMetricsCalculationService {

    private final TradeDetailsRepository tradeDetailsRepository;
    private final TradeDetailsMapper tradeDetailsMapper;
    private final PerformanceMetricsService performanceMetricsService;
    private final RiskMetricsService riskMetricsService;
    private final TradeDistributionMetricsService distributionMetricsService;
    private final TradeTimingMetricsService timingMetricsService;
    private final TradePatternMetricsService patternMetricsService;
    private final TradingFeedbackService tradingFeedbackService;
    
    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = HALF_UP;

    public TradeMetricsCalculationService(TradeDetailsRepository tradeDetailsRepository, 
                                         TradeDetailsMapper tradeDetailsMapper,
                                         PerformanceMetricsService performanceMetricsService,
                                         RiskMetricsService riskMetricsService,
                                         TradeDistributionMetricsService distributionMetricsService,
                                         TradeTimingMetricsService timingMetricsService,
                                         TradePatternMetricsService patternMetricsService,
                                         TradingFeedbackService tradingFeedbackService) {
        this.tradeDetailsRepository = tradeDetailsRepository;
        this.tradeDetailsMapper = tradeDetailsMapper;
        this.performanceMetricsService = performanceMetricsService;
        this.riskMetricsService = riskMetricsService;
        this.distributionMetricsService = distributionMetricsService;
        this.timingMetricsService = timingMetricsService;
        this.patternMetricsService = patternMetricsService;
        this.tradingFeedbackService = tradingFeedbackService;
    }
    
    /**
     * Calculate all metrics for a list of portfolio IDs
     * 
     * @param portfolioIds List of portfolio IDs to calculate metrics for
     * @return A complete trade summary with all metrics calculated
     */
    public TradeSummary calculateAllMetrics(List<String> portfolioIds) {
        List<TradeDetailsEntity> tradeEntities = tradeDetailsRepository.findByPortfolioIdIn(portfolioIds);
        List<TradeDetails> trades = tradeEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        
        if (trades.isEmpty()) {
            return createEmptyTradeSummary(portfolioIds);
        }
        
        TradeSummary summary = new TradeSummary();
        summary.setPortfolioIds(portfolioIds);
        summary.setTradeDetails(trades);
        
        // Calculate and set all metrics using specialized services
        summary.setPerformanceMetrics(performanceMetricsService.calculateMetrics(trades));
        summary.setRiskMetrics(riskMetricsService.calculateMetrics(trades));
        summary.setDistributionMetrics(distributionMetricsService.calculateMetrics(trades));
        summary.setTimingMetrics(timingMetricsService.calculateMetrics(trades));
        summary.setPatternMetrics(patternMetricsService.calculateMetrics(trades));
        
        // Generate personalized trading feedback based on trade details
        summary.setTradingFeedback(tradingFeedbackService.generateFeedback(trades));
        
        // Set legacy metrics for backward compatibility
        setLegacyMetrics(summary, trades);
        
        return summary;
    }
    
    /**
     * Set legacy metrics for backward compatibility
     * These metrics are kept in the TradeSummary object directly for backward compatibility
     * with existing code that may rely on them
     * 
     * @param summary The trade summary to update with legacy metrics
     * @param trades The list of trades to analyze
     */
    private void setLegacyMetrics(TradeSummary summary, List<TradeDetails> trades) {
        // Calculate basic metrics
        BigDecimal totalProfitLoss = trades.stream()
                .map(t -> t.getMetrics() != null ? t.getMetrics().getProfitLoss() : null)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long winCount = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        
        BigDecimal winRate = trades.isEmpty() ? BigDecimal.ZERO : 
                BigDecimal.valueOf(winCount * 100.0 / trades.size()).setScale(2, ROUNDING_MODE);
        
        // Set legacy metrics
        summary.setTotalProfitLoss(totalProfitLoss);
        summary.setWinRate(winRate);
        
        // Categorize winning and losing trades
        List<TradeDetails> winningTrades = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .sorted((t1, t2) -> t2.getMetrics().getProfitLoss().compareTo(t1.getMetrics().getProfitLoss()))
                .collect(Collectors.toList());
        
        List<TradeDetails> losingTrades = trades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0)
                .sorted((t1, t2) -> t1.getMetrics().getProfitLoss().compareTo(t2.getMetrics().getProfitLoss()))
                .collect(Collectors.toList());
        
        summary.setWinningTrades(winningTrades);
        summary.setLosingTrades(losingTrades);
    }
    
    /**
     * Create an empty trade summary when no trades are found
     * 
     * @param portfolioIds List of portfolio IDs to include in the empty summary
     * @return A trade summary with empty metrics
     */
    private TradeSummary createEmptyTradeSummary(List<String> portfolioIds) {
        TradeSummary summary = new TradeSummary();
        summary.setPortfolioIds(portfolioIds);
        summary.setTradeDetails(Collections.emptyList());
        summary.setWinningTrades(Collections.emptyList());
        summary.setLosingTrades(Collections.emptyList());
        summary.setTotalProfitLoss(BigDecimal.ZERO);
        summary.setWinRate(BigDecimal.ZERO);
        
        // Set empty metrics objects
        summary.setPerformanceMetrics(new PerformanceMetrics());
        summary.setRiskMetrics(new RiskMetrics());
        summary.setDistributionMetrics(new TradeDistributionMetrics());
        summary.setTimingMetrics(new TradeTimingMetrics());
        summary.setPatternMetrics(new TradePatternMetrics());
        summary.setTradingFeedback(new TradingFeedback());
        
        return summary;
    }
}
