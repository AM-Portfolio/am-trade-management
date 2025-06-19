package am.trade.dashboard.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import am.trade.dashboard.model.TradeMetrics;

/**
 * Service interface for trade metrics operations
 */
public interface TradeMetricsService {
    
    /**
     * Get trade metrics for a specific time range
     */
    TradeMetrics getTradeMetrics(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get trade metrics for a specific portfolio
     */
    TradeMetrics getPortfolioMetrics(String portfolioId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get trade metrics for a specific trader
     */
    TradeMetrics getTraderMetrics(String traderId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get trade metrics for a specific symbol
     */
    TradeMetrics getSymbolMetrics(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Get trade volume trend over time
     */
    Map<LocalDateTime, Long> getTradeVolumeTrend(LocalDateTime startDate, LocalDateTime endDate, String interval);
    
    /**
     * Get trade value trend over time
     */
    Map<LocalDateTime, Double> getTradeValueTrend(LocalDateTime startDate, LocalDateTime endDate, String interval);
    
    /**
     * Get top traded symbols by volume
     */
    List<Map.Entry<String, Long>> getTopTradedSymbolsByVolume(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    /**
     * Get top traded symbols by value
     */
    List<Map.Entry<String, Double>> getTopTradedSymbolsByValue(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    /**
     * Get top performing traders
     */
    List<Map.Entry<String, Double>> getTopPerformingTraders(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    /**
     * Get top performing portfolios
     */
    List<Map.Entry<String, Double>> getTopPerformingPortfolios(LocalDateTime startDate, LocalDateTime endDate, int limit);
    
    /**
     * Generate daily trade summary report
     */
    byte[] generateDailyTradeSummaryReport(LocalDateTime date);
    
    /**
     * Generate monthly trade summary report
     */
    byte[] generateMonthlyTradeSummaryReport(int year, int month);
}
