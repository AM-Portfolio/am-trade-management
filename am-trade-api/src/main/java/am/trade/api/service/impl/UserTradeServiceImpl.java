package am.trade.api.service.impl;

import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.api.service.FavoriteFilterService;
import am.trade.api.service.TradeMetricsService;
import am.trade.api.service.UserTradeService;
import am.trade.common.models.FavoriteFilter;
import am.trade.common.models.MetricsFilterConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation of UserTradeService for user-specific trade operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserTradeServiceImpl implements UserTradeService {

    private final FavoriteFilterService favoriteFilterService;
    private final TradeMetricsService tradeMetricsService;

    @Override
    public MetricsResponse getMetricsWithFavoriteFilter(String userId, String filterId, List<String> portfolioIds) {
        log.info("Getting metrics with favorite filter: {} for user: {}", filterId, userId);
        
        // Get the favorite filter
        FavoriteFilter favoriteFilter = favoriteFilterService.getFilterById(userId, filterId);
        MetricsFilterRequest filterRequest;
        
        if (favoriteFilter == null) {
            log.info("Favorite filter not found, checking for user's default filter");
            // Try to get user's default filter
            FavoriteFilter defaultFilter = favoriteFilterService.getDefaultFilter(userId);
            
            if (defaultFilter != null) {
                log.info("Using user's default filter");
                filterRequest = convertToMetricsRequest(defaultFilter.getFilterConfig());
            } else if (portfolioIds != null && !portfolioIds.isEmpty()) {
                // If no default filter, create a basic filter with portfolioIds
                log.info("No default filter found, using basic filter with portfolioIds");
                filterRequest = MetricsFilterRequest.builder()
                        .portfolioIds(portfolioIds)
                        .build();
            } else {
                log.error("No filter found and no portfolioIds provided");
                throw new IllegalArgumentException("No filter found and no portfolioIds provided");
            }
        } else {
            // Convert filter config to metrics request
            filterRequest = convertToMetricsRequest(favoriteFilter.getFilterConfig());
        }
        
        // Get metrics with the filter
        return tradeMetricsService.getMetrics(filterRequest);
    }

    @Override
    public MetricsFilterRequest convertToMetricsRequest(MetricsFilterConfig filterConfig) {
        if (filterConfig == null) {
            return null;
        }
        
        MetricsFilterRequest.MetricsFilterRequestBuilder builder = MetricsFilterRequest.builder();
        
        // Set basic fields
        if (filterConfig.getPortfolioIds() != null) {
            builder.portfolioIds(filterConfig.getPortfolioIds());
        }
        
        if (filterConfig.getDateRange() != null) {
            Map<String, Object> dateRange = filterConfig.getDateRange();
            LocalDate startDate = dateRange.containsKey("startDate") ? 
                    LocalDate.parse(dateRange.get("startDate").toString()) : null;
            LocalDate endDate = dateRange.containsKey("endDate") ? 
                    LocalDate.parse(dateRange.get("endDate").toString()) : null;
                    
            if (startDate != null || endDate != null) {
                builder.dateRange(new MetricsFilterRequest.DateRangeFilter(startDate, endDate));
            }
        }
        
        if (filterConfig.getTimePeriod() != null) {
            builder.timePeriod(MetricsFilterRequest.TimePeriodFilter.valueOf(
                    filterConfig.getTimePeriod().toString()));
        }
        
        if (filterConfig.getMetricTypes() != null) {
            builder.metricTypes(new HashSet<>(filterConfig.getMetricTypes()));
        }
        
        if (filterConfig.getInstruments() != null) {
            builder.instruments(new HashSet<>(filterConfig.getInstruments()));
        }
        
        // Handle complex nested objects by converting from Map to appropriate objects
        // This would require more detailed conversion logic based on the specific structure
        // of each filter type
        
        if (filterConfig.getInstrumentFilters() != null) {
            // Convert instrument filters from Map to InstrumentFilterCriteria
            // This is a simplified example - actual implementation would depend on structure
            MetricsFilterRequest.InstrumentFilterCriteria instrumentFilters = 
                    convertInstrumentFilters(filterConfig.getInstrumentFilters());
            builder.instrumentFilters(instrumentFilters);
        }
        
        if (filterConfig.getTradeCharacteristics() != null) {
            // Convert trade characteristics from Map to TradeCharacteristicsFilter
            MetricsFilterRequest.TradeCharacteristicsFilter tradeCharacteristics = 
                    convertTradeCharacteristics(filterConfig.getTradeCharacteristics());
            builder.tradeCharacteristics(tradeCharacteristics);
        }
        
        if (filterConfig.getProfitLossFilters() != null) {
            // Convert profit/loss filters from Map to ProfitLossFilter
            MetricsFilterRequest.ProfitLossFilter profitLossFilters = 
                    convertProfitLossFilters(filterConfig.getProfitLossFilters());
            builder.profitLossFilters(profitLossFilters);
        }
        
        if (filterConfig.getGroupBy() != null) {
            builder.groupBy(filterConfig.getGroupBy());
        }
        
        if (filterConfig.getIncludeTradeDetails() != null) {
            builder.includeTradeDetails(filterConfig.getIncludeTradeDetails());
        }
        
        if (filterConfig.getCustomFilters() != null) {
            builder.customFilters(filterConfig.getCustomFilters());
        }
        
        return builder.build();
    }
    
    // Helper methods for converting complex nested objects
    
    private MetricsFilterRequest.InstrumentFilterCriteria convertInstrumentFilters(Map<String, Object> instrumentFilters) {
        // Implement conversion logic based on the structure of InstrumentFilterCriteria
        // This is a placeholder implementation
        return new MetricsFilterRequest.InstrumentFilterCriteria();
    }
    
    private MetricsFilterRequest.TradeCharacteristicsFilter convertTradeCharacteristics(Map<String, Object> tradeCharacteristics) {
        // Implement conversion logic based on the structure of TradeCharacteristicsFilter
        // This is a placeholder implementation
        return new MetricsFilterRequest.TradeCharacteristicsFilter();
    }
    
    private MetricsFilterRequest.ProfitLossFilter convertProfitLossFilters(Map<String, Object> profitLossFilters) {
        // Implement conversion logic based on the structure of ProfitLossFilter
        // This is a placeholder implementation
        return new MetricsFilterRequest.ProfitLossFilter();
    }
}
