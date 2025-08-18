package am.trade.api.service.impl;

import am.trade.api.dto.*;
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
        FavoriteFilterResponse favoriteFilter = favoriteFilterService.getFilterById(userId, filterId);
        MetricsFilterRequest filterRequest;
        
        if (favoriteFilter == null) {
            log.info("Favorite filter not found, checking for user's default filter");
            // Try to get user's default filter
            FavoriteFilterResponse defaultFilter = favoriteFilterService.getDefaultFilter(userId);
            
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
                builder.dateRange(new DateRangeFilter(startDate, endDate));
            }
        }
        
        if (filterConfig.getTimePeriod() != null) {
            builder.timePeriod(TimePeriodFilter.valueOf(
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
            InstrumentFilterCriteria instrumentFilters = 
                    convertInstrumentFilters(filterConfig.getInstrumentFilters());
            builder.instrumentFilters(instrumentFilters);
        }
        
        if (filterConfig.getTradeCharacteristics() != null) {
            // Convert trade characteristics from Map to TradeCharacteristicsFilter
            TradeCharacteristicsFilter tradeCharacteristics = 
                    convertTradeCharacteristics(filterConfig.getTradeCharacteristics());
            builder.tradeCharacteristics(tradeCharacteristics);
        }
        
        if (filterConfig.getProfitLossFilters() != null) {
            // Convert profit/loss filters from Map to ProfitLossFilter
            ProfitLossFilter profitLossFilters = 
                    convertProfitLossFilters(filterConfig.getProfitLossFilters());
            builder.profitLossFilters(profitLossFilters);
        }
        
        if (filterConfig.getGroupBy() != null) {
            builder.groupBy(filterConfig.getGroupBy());
        }
        
        return builder.build();
    }
    
    // Helper methods for converting complex nested objects
    
    private InstrumentFilterCriteria convertInstrumentFilters(Map<String, Object> instrumentFilters) {
        if (instrumentFilters == null || instrumentFilters.isEmpty()) {
            return null;
        }
        
        InstrumentFilterCriteria.InstrumentFilterCriteriaBuilder builder = InstrumentFilterCriteria.builder();
        
        if (instrumentFilters.containsKey("marketSegments")) {
            Object segments = instrumentFilters.get("marketSegments");
            if (segments instanceof List<?>) {
                Set<String> marketSegments = new HashSet<>();
                ((List<?>) segments).forEach(item -> {
                    if (item instanceof String) {
                        marketSegments.add((String) item);
                    }
                });
                builder.marketSegments(marketSegments);
            }
        }
        
        if (instrumentFilters.containsKey("baseSymbols")) {
            Object symbols = instrumentFilters.get("baseSymbols");
            if (symbols instanceof List<?>) {
                Set<String> baseSymbols = new HashSet<>();
                ((List<?>) symbols).forEach(item -> {
                    if (item instanceof String) {
                        baseSymbols.add((String) item);
                    }
                });
                builder.baseSymbols(baseSymbols);
            }
        }
        
        if (instrumentFilters.containsKey("indexTypes")) {
            Object types = instrumentFilters.get("indexTypes");
            if (types instanceof List<?>) {
                Set<String> indexTypes = new HashSet<>();
                ((List<?>) types).forEach(item -> {
                    if (item instanceof String) {
                        indexTypes.add((String) item);
                    }
                });
                builder.indexTypes(indexTypes);
            }
        }
        
        if (instrumentFilters.containsKey("derivativeTypes")) {
            Object types = instrumentFilters.get("derivativeTypes");
            if (types instanceof List<?>) {
                Set<String> derivativeTypes = new HashSet<>();
                ((List<?>) types).forEach(item -> {
                    if (item instanceof String) {
                        derivativeTypes.add((String) item);
                    }
                });
                builder.derivativeTypes(derivativeTypes);
            }
        }
        
        return builder.build();
    }
    
    private TradeCharacteristicsFilter convertTradeCharacteristics(Map<String, Object> tradeCharacteristics) {
        if (tradeCharacteristics == null || tradeCharacteristics.isEmpty()) {
            return null;
        }
        
        TradeCharacteristicsFilter.TradeCharacteristicsFilterBuilder builder = TradeCharacteristicsFilter.builder();
        
        if (tradeCharacteristics.containsKey("strategies")) {
            Object strategies = tradeCharacteristics.get("strategies");
            if (strategies instanceof List<?>) {
                Set<String> strategiesSet = new HashSet<>();
                ((List<?>) strategies).forEach(item -> {
                    if (item instanceof String) {
                        strategiesSet.add((String) item);
                    }
                });
                builder.strategies(strategiesSet);
            }
        }
        
        if (tradeCharacteristics.containsKey("tags")) {
            Object tags = tradeCharacteristics.get("tags");
            if (tags instanceof List<?>) {
                Set<String> tagsSet = new HashSet<>();
                ((List<?>) tags).forEach(item -> {
                    if (item instanceof String) {
                        tagsSet.add((String) item);
                    }
                });
                builder.tags(tagsSet);
            }
        }
        
        if (tradeCharacteristics.containsKey("directions")) {
            Object directions = tradeCharacteristics.get("directions");
            if (directions instanceof List<?>) {
                Set<String> directionsSet = new HashSet<>();
                ((List<?>) directions).forEach(item -> {
                    if (item instanceof String) {
                        directionsSet.add((String) item);
                    }
                });
                builder.directions(directionsSet);
            }
        }
        
        if (tradeCharacteristics.containsKey("statuses")) {
            Object statuses = tradeCharacteristics.get("statuses");
            if (statuses instanceof List<?>) {
                Set<String> statusesSet = new HashSet<>();
                ((List<?>) statuses).forEach(item -> {
                    if (item instanceof String) {
                        statusesSet.add((String) item);
                    }
                });
                builder.statuses(statusesSet);
            }
        }
        
        if (tradeCharacteristics.containsKey("minHoldingTimeHours")) {
            Object minHours = tradeCharacteristics.get("minHoldingTimeHours");
            if (minHours != null) {
                builder.minHoldingTimeHours(Integer.valueOf(minHours.toString()));
            }
        }
        
        if (tradeCharacteristics.containsKey("maxHoldingTimeHours")) {
            Object maxHours = tradeCharacteristics.get("maxHoldingTimeHours");
            if (maxHours != null) {
                builder.maxHoldingTimeHours(Integer.valueOf(maxHours.toString()));
            }
        }
        
        return builder.build();
    }
    
    private ProfitLossFilter convertProfitLossFilters(Map<String, Object> profitLossFilters) {
        if (profitLossFilters == null || profitLossFilters.isEmpty()) {
            return null;
        }
        
        ProfitLossFilter.ProfitLossFilterBuilder builder = ProfitLossFilter.builder();
        
        if (profitLossFilters.containsKey("minProfitLoss")) {
            Object minPL = profitLossFilters.get("minProfitLoss");
            if (minPL != null) {
                builder.minProfitLoss(Double.valueOf(minPL.toString()));
            }
        }
        
        if (profitLossFilters.containsKey("maxProfitLoss")) {
            Object maxPL = profitLossFilters.get("maxProfitLoss");
            if (maxPL != null) {
                builder.maxProfitLoss(Double.valueOf(maxPL.toString()));
            }
        }
        
        if (profitLossFilters.containsKey("minPositionSize")) {
            Object minSize = profitLossFilters.get("minPositionSize");
            if (minSize != null) {
                builder.minPositionSize(Double.valueOf(minSize.toString()));
            }
        }
        
        if (profitLossFilters.containsKey("maxPositionSize")) {
            Object maxSize = profitLossFilters.get("maxPositionSize");
            if (maxSize != null) {
                builder.maxPositionSize(Double.valueOf(maxSize.toString()));
            }
        }
        
        return builder.build();
    }
}
