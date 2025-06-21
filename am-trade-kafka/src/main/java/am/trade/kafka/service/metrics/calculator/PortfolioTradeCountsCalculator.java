package am.trade.kafka.service.metrics.calculator;

import am.trade.models.document.Trade;
import am.trade.models.document.statistics.PortfolioTradeCounts;

import java.util.List;

/**
 * Calculator for portfolio-level trade counts
 */
public interface PortfolioTradeCountsCalculator {
    
    /**
     * Calculate portfolio trade counts for a specific portfolio
     * 
     * @param trades List of trades to analyze
     * @param portfolioId Portfolio ID to calculate metrics for
     * @return PortfolioTradeCounts object with calculated metrics
     */
    PortfolioTradeCounts calculate(List<Trade> trades, String portfolioId);
    
    /**
     * Calculate portfolio trade counts for all portfolios found in the trades list
     * 
     * @param trades List of trades to analyze
     * @return List of PortfolioTradeCounts objects, one for each portfolio
     */
    List<PortfolioTradeCounts> calculateForAllPortfolios(List<Trade> trades);
}
