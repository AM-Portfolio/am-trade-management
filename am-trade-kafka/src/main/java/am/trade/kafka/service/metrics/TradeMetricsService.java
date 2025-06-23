package am.trade.kafka.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.models.document.TradeStatistics;

import java.util.List;

/**
 * Service interface for calculating trade metrics from a list of trades
 */
public interface TradeMetricsService {
    
    /**
     * Calculate or update trade statistics based on a list of trades
     * 
     * @param trades List of trades to analyze
     * @return Updated TradeStatistics object with calculated metrics
     */
    TradeStatistics calculateMetrics(List<TradeDetails> trades);
    
    /**
     * Calculate or update trade statistics based on a list of trades for a specific portfolio
     * 
     * @param trades List of trades to analyze
     * @param portfolioId The portfolio ID to filter trades for
     * @return Updated TradeStatistics object with calculated metrics
     */
    TradeStatistics calculateMetricsForPortfolio(List<TradeDetails> trades, String portfolioId);
}
