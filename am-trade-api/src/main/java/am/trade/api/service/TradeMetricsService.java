package am.trade.api.service;

import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Service interface for flexible trade metrics operations
 */
public interface TradeMetricsService {
    
    /**
     * Get metrics based on flexible filter criteria
     * 
     * @param filterRequest The filter criteria for metrics calculation
     * @return Response containing the requested metrics
     */
    MetricsResponse getMetrics(MetricsFilterRequest filterRequest);
    
    /**
     * Get a list of all available metric types that can be requested
     * 
     * @return List of available metric type names
     */
    List<String> getAvailableMetricTypes();
    
    /**
     * Compare metrics between two time periods
     * 
     * @param portfolioIds List of portfolio IDs to include
     * @param metricTypes Set of metric types to include in the comparison
     * @param firstPeriodStart Start date of the first period
     * @param firstPeriodEnd End date of the first period
     * @param secondPeriodStart Start date of the second period
     * @param secondPeriodEnd End date of the second period
     * @return List of metrics responses for comparison (first period, second period)
     */
    List<MetricsResponse> compareMetrics(
            List<String> portfolioIds,
            Set<String> metricTypes,
            LocalDate firstPeriodStart,
            LocalDate firstPeriodEnd,
            LocalDate secondPeriodStart,
            LocalDate secondPeriodEnd);
    
    /**
     * Get metrics trends over time with specified interval
     * 
     * @param portfolioIds List of portfolio IDs to include
     * @param metricTypes Set of metric types to include in the trends
     * @param startDate Start date of the overall period
     * @param endDate End date of the overall period
     * @param interval Time interval for breaking down the trends (DAY, WEEK, MONTH, QUARTER, YEAR)
     * @return Map of interval labels to metrics responses for each interval
     */
    Map<String, List<MetricsResponse>> getMetricsTrends(
            List<String> portfolioIds,
            Set<String> metricTypes,
            LocalDate startDate,
            LocalDate endDate,
            String interval);
}
