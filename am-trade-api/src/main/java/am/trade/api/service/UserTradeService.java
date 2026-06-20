package am.trade.api.service;

import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.common.models.MetricsFilterConfig;

import java.util.List;

/**
 * Service for user-specific trade operations and features
 */
public interface UserTradeService {

    /**
     * Get metrics using a favorite filter with fallback options
     * 
     * @param userId User ID
     * @param filterId Favorite filter ID
     * @param portfolioIds Optional portfolio IDs for fallback
     * @return MetricsResponse containing the metrics data
     * @throws IllegalArgumentException if no filter found and no portfolioIds provided
     */
    MetricsResponse getMetricsWithFavoriteFilter(String userId, String filterId, List<String> portfolioIds);
    
    /**
     * Convert a metrics filter configuration to a metrics filter request
     * 
     * @param filterConfig The filter configuration to convert
     * @return MetricsFilterRequest object
     */
    MetricsFilterRequest convertToMetricsRequest(MetricsFilterConfig filterConfig);
}
