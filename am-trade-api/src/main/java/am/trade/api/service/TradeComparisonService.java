package am.trade.api.service;

import am.trade.api.dto.ComparisonRequest;
import am.trade.api.dto.ComparisonResponse;

/**
 * Service for comparing trade performance across different dimensions
 */
public interface TradeComparisonService {

    /**
     * Compare trade performance based on the provided comparison request
     * 
     * @param request Comparison request
     * @return Comparison response
     * @throws IllegalArgumentException if request is invalid
     */
    ComparisonResponse compareTradePerformance(ComparisonRequest request);
    
    /**
     * Compare performance between two portfolios
     * 
     * @param userId User ID
     * @param portfolioId1 First portfolio ID
     * @param portfolioId2 Second portfolio ID
     * @param startDate Optional start date (ISO format)
     * @param endDate Optional end date (ISO format)
     * @return Comparison response
     * @throws IllegalArgumentException if request is invalid
     */
    ComparisonResponse comparePortfolios(
            String userId, String portfolioId1, String portfolioId2, String startDate, String endDate);
    
    /**
     * Compare performance between two time periods
     * 
     * @param userId User ID
     * @param period1Start First period start date (ISO format)
     * @param period1End First period end date (ISO format)
     * @param period2Start Second period start date (ISO format)
     * @param period2End Second period end date (ISO format)
     * @param portfolioId Optional portfolio ID to filter trades
     * @return Comparison response
     * @throws IllegalArgumentException if request is invalid
     */
    ComparisonResponse compareTimePeriods(
            String userId, String period1Start, String period1End, 
            String period2Start, String period2End, String portfolioId);
    
    /**
     * Compare performance between two strategies
     * 
     * @param userId User ID
     * @param strategy1 First strategy
     * @param strategy2 Second strategy
     * @param startDate Optional start date (ISO format)
     * @param endDate Optional end date (ISO format)
     * @return Comparison response
     * @throws IllegalArgumentException if request is invalid
     */
    ComparisonResponse compareStrategies(
            String userId, String strategy1, String strategy2, String startDate, String endDate);
}
