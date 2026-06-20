package am.trade.api.service.impl;

import am.trade.api.dto.DateRangeFilter;
import am.trade.api.dto.InstrumentFilterCriteria;
import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.api.dto.ProfitLossFilter;
import am.trade.api.dto.TimePeriodFilter;
import am.trade.api.dto.TradeCharacteristicsFilter;
import am.trade.api.service.TradeMetricsService;
import am.trade.common.models.*;
import am.trade.dashboard.service.metrics.*;
import am.trade.services.service.TradeDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of the TradeMetricsService for flexible metrics calculation
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeMetricsServiceImpl implements TradeMetricsService {

    private final TradeDetailsService tradeDetailsService;
    private final PerformanceMetricsService performanceMetricsService;
    private final RiskMetricsService riskMetricsService;
    private final TradeDistributionMetricsService distributionMetricsService;
    private final TradeTimingMetricsService timingMetricsService;
    private final TradePatternMetricsService patternMetricsService;

    private static final List<String> AVAILABLE_METRIC_TYPES = Arrays.asList(
        "PERFORMANCE", "RISK", "DISTRIBUTION", "TIMING", "PATTERN", "STRATEGY", 
        "FREQUENCY", "CONSISTENCY", "PSYCHOLOGY", "FEEDBACK");

    @Override
    public MetricsResponse getMetrics(MetricsFilterRequest filterRequest) {
        validateFilterRequest(filterRequest);
        
        // Set date range based on timePeriod or dateRange
        LocalDateTime startDateTime;
        LocalDateTime endDateTime;
        
        // If timePeriod is provided, it takes precedence over dateRange
        if (filterRequest.getTimePeriod() != null) {
            // Calculate date range based on time period
            DateRangeFilter calculatedDateRange = calculateDateRangeFromTimePeriod(filterRequest.getTimePeriod());
            startDateTime = calculatedDateRange.getStartDate().atStartOfDay();
            endDateTime = calculatedDateRange.getEndDate().atTime(LocalTime.MAX);
            
            // Update the dateRange in the request to match the calculated period
            filterRequest.setDateRange(calculatedDateRange);
        } else if (filterRequest.getDateRange() != null) {
            startDateTime = filterRequest.getDateRange().getStartDate().atStartOfDay();
            endDateTime = filterRequest.getDateRange().getEndDate().atTime(LocalTime.MAX);
        } else {
            // Default to last 30 days if no date range or time period provided
            LocalDate today = LocalDate.now();
            startDateTime = today.minusDays(30).atStartOfDay();
            endDateTime = today.atTime(LocalTime.MAX);
            
            // Create and set a date range for the response
            DateRangeFilter defaultDateRange = new DateRangeFilter();
            defaultDateRange.setStartDate(today.minusDays(30));
            defaultDateRange.setEndDate(today);
            filterRequest.setDateRange(defaultDateRange);
        }
        
        // Fetch trades based on filter criteria
        List<TradeDetails> trades = fetchTradesByFilters(filterRequest, startDateTime, endDateTime);
        
        // Apply additional filters that can't be done at the database level
        trades = applyAdditionalFilters(trades, filterRequest);
        
        // Build the response with requested metrics
        return buildMetricsResponse(trades, filterRequest);
    }

    @Override
    public List<String> getAvailableMetricTypes() {
        return new ArrayList<>(AVAILABLE_METRIC_TYPES);
    }

    @Override
    public List<MetricsResponse> compareMetrics(
            List<String> portfolioIds,
            Set<String> metricTypes,
            LocalDate firstPeriodStart,
            LocalDate firstPeriodEnd,
            LocalDate secondPeriodStart,
            LocalDate secondPeriodEnd) {
        
        // Validate input
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            throw new IllegalArgumentException("Portfolio IDs must be provided");
        }
        
        if (metricTypes == null || metricTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one metric type must be specified");
        }
        
        // Create filter requests for both periods
        MetricsFilterRequest firstPeriodRequest = MetricsFilterRequest.builder()
                .portfolioIds(portfolioIds)
                .dateRange(DateRangeFilter.builder().startDate(firstPeriodStart).endDate(firstPeriodEnd).build())
                .metricTypes(metricTypes)
                .build();
        
        MetricsFilterRequest secondPeriodRequest = MetricsFilterRequest.builder()
                .portfolioIds(portfolioIds)
                .dateRange(DateRangeFilter.builder().startDate(secondPeriodStart).endDate(secondPeriodEnd).build())
                .metricTypes(metricTypes)
                .build();
        
        // Get metrics for both periods
        MetricsResponse firstPeriodMetrics = getMetrics(firstPeriodRequest);
        MetricsResponse secondPeriodMetrics = getMetrics(secondPeriodRequest);
        
        // Add comparison metadata
        firstPeriodMetrics.getMetadata().put("periodLabel", "First Period");
        secondPeriodMetrics.getMetadata().put("periodLabel", "Second Period");
        
        return Arrays.asList(firstPeriodMetrics, secondPeriodMetrics);
    }

    @Override
    public Map<String, List<MetricsResponse>> getMetricsTrends(
            List<String> portfolioIds,
            Set<String> metricTypes,
            LocalDate startDate,
            LocalDate endDate,
            String interval) {
        
        // Validate input
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            throw new IllegalArgumentException("Portfolio IDs must be provided");
        }
        
        if (metricTypes == null || metricTypes.isEmpty()) {
            throw new IllegalArgumentException("At least one metric type must be specified");
        }
        
        if (interval == null || interval.isEmpty()) {
            throw new IllegalArgumentException("Interval must be specified");
        }
        
        // Generate time intervals based on the specified interval type
        List<Map.Entry<LocalDate, LocalDate>> intervals = generateTimeIntervals(startDate, endDate, interval);
        
        // Calculate metrics for each interval
        Map<String, List<MetricsResponse>> trends = new LinkedHashMap<>();
        
        for (Map.Entry<LocalDate, LocalDate> periodInterval : intervals) {
            LocalDate intervalStart = periodInterval.getKey();
            LocalDate intervalEnd = periodInterval.getValue();
            
            MetricsFilterRequest filterRequest = MetricsFilterRequest.builder()
                    .portfolioIds(portfolioIds)
                    .dateRange(DateRangeFilter.builder().startDate(intervalStart).endDate(intervalEnd).build())
                    .metricTypes(metricTypes)
                    .build();
            
            MetricsResponse intervalMetrics = getMetrics(filterRequest);
            
            // Format the interval label based on interval type
            String intervalLabel = formatIntervalLabel(intervalStart, intervalEnd, interval);
            
            // Add to trends map
            trends.computeIfAbsent(intervalLabel, k -> new ArrayList<>()).add(intervalMetrics);
        }
        
        return trends;
    }

    // Helper methods
    
    /**
     * Calculate date range based on predefined time period
     * 
     * @param timePeriod The time period to calculate date range for
     * @return DateRangeFilter with calculated start and end dates
     */
    private DateRangeFilter calculateDateRangeFromTimePeriod(TimePeriodFilter timePeriod) {
        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate = today; // Default end date is today for most periods
        
        switch (timePeriod) {
            case TODAY:
                startDate = today;
                break;
                
            case YESTERDAY:
                startDate = today.minusDays(1);
                endDate = today.minusDays(1);
                break;
                
            case LAST_7_DAYS:
                startDate = today.minusDays(6); // 6 days back + today = 7 days
                break;
                
            case LAST_14_DAYS:
                startDate = today.minusDays(13); // 13 days back + today = 14 days
                break;
                
            case LAST_30_DAYS:
                startDate = today.minusDays(29); // 29 days back + today = 30 days
                break;
                
            case THIS_WEEK:
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                break;
                
            case LAST_WEEK:
                startDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1);
                endDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusDays(1);
                break;
                
            case THIS_MONTH:
                startDate = today.withDayOfMonth(1);
                break;
                
            case LAST_MONTH:
                startDate = today.minusMonths(1).withDayOfMonth(1);
                endDate = today.withDayOfMonth(1).minusDays(1);
                break;
                
            case THIS_QUARTER:
                startDate = today.withDayOfMonth(1).withMonth((today.getMonthValue() - 1) / 3 * 3 + 1);
                break;
                
            case LAST_QUARTER:
                LocalDate firstDayOfQuarter = today.withDayOfMonth(1).withMonth((today.getMonthValue() - 1) / 3 * 3 + 1);
                startDate = firstDayOfQuarter.minusMonths(3);
                endDate = firstDayOfQuarter.minusDays(1);
                break;
                
            case THIS_YEAR:
                startDate = today.withDayOfYear(1);
                break;
                
            case LAST_YEAR:
                startDate = today.minusYears(1).withDayOfYear(1);
                endDate = today.withDayOfYear(1).minusDays(1);
                break;
                
            case LAST_3_MONTHS:
                startDate = today.minusMonths(3);
                break;
                
            case LAST_6_MONTHS:
                startDate = today.minusMonths(6);
                break;
                
            case LAST_12_MONTHS:
                startDate = today.minusYears(1);
                break;
                
            case LAST_2_YEARS:
                startDate = today.minusYears(2);
                break;
                
            case LAST_3_YEARS:
                startDate = today.minusYears(3);
                break;
                
            case LAST_5_YEARS:
                startDate = today.minusYears(5);
                break;
                
            case CUSTOM:
            default:
                // For CUSTOM, we expect dateRange to be provided separately
                // Default to last 30 days if no specific period is recognized
                startDate = today.minusDays(29);
                break;
        }
        
        DateRangeFilter dateRange = new DateRangeFilter();
        dateRange.setStartDate(startDate);
        dateRange.setEndDate(endDate);
        return dateRange;
    }
    
    private void validateFilterRequest(MetricsFilterRequest filterRequest) {
        if (filterRequest == null) {
            throw new IllegalArgumentException("Filter request cannot be null");
        }
        
        if (filterRequest.getPortfolioIds() == null || filterRequest.getPortfolioIds().isEmpty()) {
            throw new IllegalArgumentException("At least one portfolio ID is required");
        }
        
        // Date range is now optional
        if (filterRequest.getDateRange() != null) {
            // Only validate start and end dates if date range is provided
            if (filterRequest.getDateRange().getStartDate() == null) {
                throw new IllegalArgumentException("Start date is required when date range is provided");
            }
            
            if (filterRequest.getDateRange().getEndDate() == null) {
                throw new IllegalArgumentException("End date is required when date range is provided");
            }
            
            if (filterRequest.getDateRange().getStartDate().isAfter(filterRequest.getDateRange().getEndDate())) {
                throw new IllegalArgumentException("Start date cannot be after end date");
            }
        }
        
        // Initialize empty collections if they are null
        if (filterRequest.getMetricTypes() == null) {
            filterRequest.setMetricTypes(new HashSet<>());
        }
    }
    
    private List<TradeDetails> fetchTradesByFilters(MetricsFilterRequest filterRequest, LocalDateTime startDateTime,
            LocalDateTime endDateTime) {
        
        // Basic query by portfolio IDs and date range - already returns domain models
        List<TradeDetails> trades = tradeDetailsService.findByPortfolioIdInAndEntryInfoTimestampBetween(
                filterRequest.getPortfolioIds(), startDateTime, endDateTime);
        
        // Apply additional filters if needed
        
        // Return the trades directly since they're already domain models
        return trades;
    }
    
    private List<TradeDetails> applyAdditionalFilters(List<TradeDetails> trades, MetricsFilterRequest filterRequest) {
        if (trades.isEmpty()) {
            return trades;
        }
        
        // Initialize filter objects if they are null to prevent NullPointerExceptions
        if (filterRequest.getTradeCharacteristics() == null) {
            filterRequest.setTradeCharacteristics(new TradeCharacteristicsFilter());
        }
        
        if (filterRequest.getInstrumentFilters() == null) {
            filterRequest.setInstrumentFilters(new InstrumentFilterCriteria());
        }
        
        if (filterRequest.getProfitLossFilters() == null) {
            filterRequest.setProfitLossFilters(new ProfitLossFilter());
        }
        
        // Apply in-memory filters based on the filter request
        
        if (filterRequest.getTradeCharacteristics().getStrategies() != null && !filterRequest.getTradeCharacteristics().getStrategies().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getStrategy() != null && 
                            filterRequest.getTradeCharacteristics().getStrategies().contains(trade.getStrategy()))
                    .collect(Collectors.toList());
        }
        
        // Filter by specific instruments
        if (filterRequest.getInstruments() != null && !filterRequest.getInstruments().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getInstrumentInfo() != null && 
                            filterRequest.getInstruments().contains(trade.getInstrumentInfo().getRawSymbol()))
                    .collect(Collectors.toList());
        }
        
        // Apply instrument filters if present
        if (filterRequest.getInstrumentFilters() != null) {
            // Apply market segment filter
            if (filterRequest.getInstrumentFilters().getMarketSegments() != null && 
                !filterRequest.getInstrumentFilters().getMarketSegments().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getInstrumentInfo() != null && 
                                trade.getInstrumentInfo().getSegment() != null && 
                                filterRequest.getInstrumentFilters().getMarketSegments()
                                    .contains(trade.getInstrumentInfo().getSegment().name()))
                        .collect(Collectors.toList());
            }
            
            // Apply base symbol filter
            if (filterRequest.getInstrumentFilters().getBaseSymbols() != null && 
                !filterRequest.getInstrumentFilters().getBaseSymbols().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getInstrumentInfo() != null && 
                                trade.getInstrumentInfo().getBaseSymbol() != null && 
                                filterRequest.getInstrumentFilters().getBaseSymbols()
                                    .contains(trade.getInstrumentInfo().getBaseSymbol()))
                        .collect(Collectors.toList());
            }
            
            // Apply index type filter
            if (filterRequest.getInstrumentFilters().getIndexTypes() != null && 
                !filterRequest.getInstrumentFilters().getIndexTypes().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getInstrumentInfo() != null && 
                                trade.getInstrumentInfo().getIndexType() != null && 
                                filterRequest.getInstrumentFilters().getIndexTypes()
                                    .contains(trade.getInstrumentInfo().getIndexType().name()))
                        .collect(Collectors.toList());
            }
            
            // Apply derivative type filter
            if (filterRequest.getInstrumentFilters().getDerivativeTypes() != null && 
                !filterRequest.getInstrumentFilters().getDerivativeTypes().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> {
                            if (trade.getInstrumentInfo() == null || 
                                trade.getInstrumentInfo().getDerivativeInfo() == null) {
                                return false;
                            }
                            
                            // Check if it's a futures contract
                            boolean isFutures = trade.getInstrumentInfo().getSegment() != null && 
                                (trade.getInstrumentInfo().getSegment().name().contains("FUTURES"));
                            
                            // Check if it's an options contract
                            boolean isOptions = trade.getInstrumentInfo().getSegment() != null && 
                                (trade.getInstrumentInfo().getSegment().name().contains("OPTIONS"));
                            
                            // Match with requested derivative types
                            return (isFutures && filterRequest.getInstrumentFilters().getDerivativeTypes().contains("FUTURES")) || 
                                   (isOptions && filterRequest.getInstrumentFilters().getDerivativeTypes().contains("OPTIONS"));
                        })
                        .collect(Collectors.toList());
            }
        }
        
        // Apply trade characteristics filters
        if (filterRequest.getTradeCharacteristics() != null) {
            // Filter by trade directions
            if (filterRequest.getTradeCharacteristics().getDirections() != null && 
                !filterRequest.getTradeCharacteristics().getDirections().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getTradePositionType() != null && 
                                filterRequest.getTradeCharacteristics().getDirections().contains(trade.getTradePositionType().name()))
                        .collect(Collectors.toList());
            }
            
            // Filter by trade statuses
            if (filterRequest.getTradeCharacteristics().getStatuses() != null && 
                !filterRequest.getTradeCharacteristics().getStatuses().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getStatus() != null && 
                                filterRequest.getTradeCharacteristics().getStatuses().contains(trade.getStatus().name()))
                        .collect(Collectors.toList());
            }
            
            // Filter by strategies
            if (filterRequest.getTradeCharacteristics().getStrategies() != null && 
                !filterRequest.getTradeCharacteristics().getStrategies().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getStrategy() != null && 
                                filterRequest.getTradeCharacteristics().getStrategies().contains(trade.getStrategy()))
                        .collect(Collectors.toList());
            }
            
            // Filter by tags
            if (filterRequest.getTradeCharacteristics().getTags() != null && 
                !filterRequest.getTradeCharacteristics().getTags().isEmpty()) {
                trades = trades.stream()
                        .filter(trade -> trade.getTags() != null && 
                                !Collections.disjoint(trade.getTags(), filterRequest.getTradeCharacteristics().getTags()))
                        .collect(Collectors.toList());
            }
            
            // Filter by holding time
            if (filterRequest.getTradeCharacteristics().getMinHoldingTimeHours() != null || 
                filterRequest.getTradeCharacteristics().getMaxHoldingTimeHours() != null) {
                trades = trades.stream()
                        .filter(trade -> {
                            if (trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null) {
                                return false;
                            }
                            
                            LocalDateTime exitTime = (trade.getExitInfo() != null && trade.getExitInfo().getTimestamp() != null) ? 
                                    trade.getExitInfo().getTimestamp() : LocalDateTime.now();
                            
                            long holdingHours = ChronoUnit.HOURS.between(trade.getEntryInfo().getTimestamp(), exitTime);
                            
                            boolean meetsMinHours = filterRequest.getTradeCharacteristics().getMinHoldingTimeHours() == null || 
                                    holdingHours >= filterRequest.getTradeCharacteristics().getMinHoldingTimeHours();
                            boolean meetsMaxHours = filterRequest.getTradeCharacteristics().getMaxHoldingTimeHours() == null || 
                                    holdingHours <= filterRequest.getTradeCharacteristics().getMaxHoldingTimeHours();
                            
                            return meetsMinHours && meetsMaxHours;
                        })
                        .collect(Collectors.toList());
            }
        }
        
        // Apply profit/loss filters
        if (filterRequest.getProfitLossFilters() != null) {
            // Filter by min profit/loss
            if (filterRequest.getProfitLossFilters().getMinProfitLoss() != null) {
                trades = trades.stream()
                        .filter(trade -> trade.getMetrics() != null && 
                                trade.getMetrics().getProfitLoss() != null && 
                                trade.getMetrics().getProfitLoss().doubleValue() >= filterRequest.getProfitLossFilters().getMinProfitLoss())
                        .collect(Collectors.toList());
            }
            
            // Filter by max profit/loss
            if (filterRequest.getProfitLossFilters().getMaxProfitLoss() != null) {
                trades = trades.stream()
                        .filter(trade -> trade.getMetrics() != null && 
                                trade.getMetrics().getProfitLoss() != null && 
                                trade.getMetrics().getProfitLoss().doubleValue() <= filterRequest.getProfitLossFilters().getMaxProfitLoss())
                        .collect(Collectors.toList());
            }
            
            // Filter by position size
            if (filterRequest.getProfitLossFilters().getMinPositionSize() != null || 
                filterRequest.getProfitLossFilters().getMaxPositionSize() != null) {
                trades = trades.stream()
                        .filter(trade -> {
                            if (trade.getEntryInfo() == null || 
                                    trade.getEntryInfo().getPrice() == null || 
                                    trade.getEntryInfo().getQuantity() == null) {
                                return false;
                            }
                            
                            double positionSize = trade.getEntryInfo().getPrice()
                                    .multiply(new java.math.BigDecimal(trade.getEntryInfo().getQuantity().toString()))
                                    .doubleValue();
                            
                            boolean meetsMinSize = filterRequest.getProfitLossFilters().getMinPositionSize() == null || 
                                    positionSize >= filterRequest.getProfitLossFilters().getMinPositionSize();
                            boolean meetsMaxSize = filterRequest.getProfitLossFilters().getMaxPositionSize() == null || 
                                    positionSize <= filterRequest.getProfitLossFilters().getMaxPositionSize();
                            
                            return meetsMinSize && meetsMaxSize;
                        })
                        .collect(Collectors.toList());
            }
        }
        
        return trades;
    }

    
    private MetricsResponse buildMetricsResponse(List<TradeDetails> trades, MetricsFilterRequest filterRequest) {
        MetricsResponse response = new MetricsResponse();
        response.setPortfolioIds(filterRequest.getPortfolioIds());
        
        // Date range should be set by now (either provided or default)
        if (filterRequest.getDateRange() != null) {
            response.setStartDate(filterRequest.getDateRange().getStartDate());
            response.setEndDate(filterRequest.getDateRange().getEndDate());
        }
        
        response.setTotalTradesCount(trades.size());
        
        // Include trade details in the response if requested
        if (filterRequest.getIncludeTradeDetails() != null && filterRequest.getIncludeTradeDetails()) {
            response.setTradeDetails(trades);
        }
        
        // Initialize metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generatedAt", LocalDateTime.now());
        metadata.put("appliedFilters", filterRequest);
        response.setMetadata(metadata);
        
        // Calculate requested metrics
        Set<String> metricTypes = filterRequest.getMetricTypes();
        
        // If no metric types specified, only include performance metrics by default
        if (metricTypes.isEmpty()) {
            response.setPerformanceMetrics(performanceMetricsService.calculateMetrics(trades));
            return response;
        }
        
        // Otherwise, only calculate metrics that were explicitly requested
        if (metricTypes.contains("PERFORMANCE")) {
            response.setPerformanceMetrics(performanceMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("RISK")) {
            response.setRiskMetrics(riskMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("DISTRIBUTION")) {
            response.setDistributionMetrics(distributionMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("TIMING")) {
            response.setTimingMetrics(timingMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("PATTERN")) {
            response.setPatternMetrics(patternMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("STRATEGY")) {
            // Assuming there's a method to calculate strategy metrics
            // response.setStrategyMetrics(strategyMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("FREQUENCY")) {
            // Add implementation when frequency metrics service is available
            // response.setFrequencyMetrics(frequencyMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("CONSISTENCY")) {
            // Add implementation when consistency metrics service is available
            // response.setConsistencyMetrics(consistencyMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("PSYCHOLOGY")) {
            // Add implementation when psychology metrics service is available
            // response.setPsychologyMetrics(psychologyMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.contains("FEEDBACK")) {
            // Add implementation when feedback service is available
            // response.setTradingFeedback(tradingFeedbackService.generateFeedback(trades));
        }
        
        // Handle grouping if requested
        if (filterRequest.getGroupBy() != null && !filterRequest.getGroupBy().isEmpty()) {
            response.setGroupedMetrics(calculateGroupedMetrics(trades, filterRequest));
        }
        
        return response;
    }
    
    private Map<String, Map<String, Object>> calculateGroupedMetrics(
            List<TradeDetails> trades, MetricsFilterRequest filterRequest) {
        
        Map<String, Map<String, Object>> groupedMetrics = new HashMap<>();
        
        for (String groupDimension : filterRequest.getGroupBy()) {
            Map<String, List<TradeDetails>> groupedTrades = new HashMap<>();
            
            // Group trades by the specified dimension
            switch (groupDimension.toUpperCase()) {
                case "STRATEGY":
                    // Group by instrument
                    Map<String, List<TradeDetails>> instrumentGroups = trades.stream()
                            .collect(Collectors.groupingBy(t -> t.getInstrumentInfo() != null ? 
                                    t.getInstrumentInfo().getSymbol() : t.getSymbol()));
                    
                    // For each instrument, further group by direction
                    for (Map.Entry<String, List<TradeDetails>> entry : instrumentGroups.entrySet()) {
                        String instrument = entry.getKey();
                        List<TradeDetails> instrumentTrades = entry.getValue();
                        
                        Map<String, List<TradeDetails>> directionGroups = instrumentTrades.stream()
                                .collect(Collectors.groupingBy(t -> t.getTradePositionType() != null ? 
                                        t.getTradePositionType().toString() : "Unknown"));
                        
                        for (Map.Entry<String, List<TradeDetails>> dirEntry : directionGroups.entrySet()) {
                            String direction = dirEntry.getKey();
                            String groupKey = instrument + "-" + direction;
                            groupedTrades.put(groupKey, dirEntry.getValue());
                        }
                    }
                    break;
                    
                case "INSTRUMENT":
                    trades.forEach(trade -> {
                        String instrument = trade.getInstrumentInfo() != null ? trade.getInstrumentInfo().getSymbol() : trade.getSymbol();
                        groupedTrades.computeIfAbsent(instrument != null ? instrument : "Unknown", k -> new ArrayList<>()).add(trade);
                    });
                    break;
                    
                case "DIRECTION":
                    trades.forEach(trade -> {
                        String direction = trade.getTradePositionType() != null ? trade.getTradePositionType().toString() : "Unknown";
                        groupedTrades.computeIfAbsent(direction, k -> new ArrayList<>()).add(trade);
                    });
                    break;
                    
                case "TAG":
                    // For tags, we need to flatten since a trade can have multiple tags
                    trades.stream()
                            .filter(t -> t.getTags() != null && !t.getTags().isEmpty())
                            .forEach(trade -> {
                                for (String tag : trade.getTags()) {
                                    groupedTrades.computeIfAbsent(tag, k -> new ArrayList<>()).add(trade);
                                }
                            });
                    break;
                    
                default:
                    log.warn("Unsupported grouping dimension: {}", groupDimension);
                    continue;
            }
            
            // Calculate metrics for each group
            Map<String, Object> dimensionMetrics = new HashMap<>();
            for (Map.Entry<String, List<TradeDetails>> entry : groupedTrades.entrySet()) {
                String groupValue = entry.getKey();
                List<TradeDetails> groupTrades = entry.getValue();
                
                // Calculate basic metrics for this group
                Map<String, Object> groupMetrics = new HashMap<>();
                groupMetrics.put("tradeCount", groupTrades.size());
                
                // Calculate performance metrics for this group
                Set<String> metricTypes = filterRequest.getMetricTypes();
                if (metricTypes.isEmpty() || metricTypes.contains("PERFORMANCE")) {
                    groupMetrics.put("performance", performanceMetricsService.calculateMetrics(groupTrades));
                }
                
                if (metricTypes.isEmpty() || metricTypes.contains("RISK")) {
                    groupMetrics.put("risk", riskMetricsService.calculateMetrics(groupTrades));
                }
                
                dimensionMetrics.put(groupValue, groupMetrics);
            }
            
            groupedMetrics.put(groupDimension.toUpperCase(), dimensionMetrics);
        }
        
        return groupedMetrics;
    }
    
    private List<Map.Entry<LocalDate, LocalDate>> generateTimeIntervals(
            LocalDate startDate, LocalDate endDate, String intervalType) {
        
        List<Map.Entry<LocalDate, LocalDate>> intervals = new ArrayList<>();
        
        switch (intervalType.toUpperCase()) {
            case "DAY":
                LocalDate currentDay = startDate;
                while (!currentDay.isAfter(endDate)) {
                    intervals.add(new AbstractMap.SimpleEntry<>(currentDay, currentDay));
                    currentDay = currentDay.plusDays(1);
                }
                break;
                
            case "WEEK":
                LocalDate currentWeekStart = startDate;
                while (!currentWeekStart.isAfter(endDate)) {
                    LocalDate weekEnd = currentWeekStart.plusDays(6);
                    if (weekEnd.isAfter(endDate)) {
                        weekEnd = endDate;
                    }
                    intervals.add(new AbstractMap.SimpleEntry<>(currentWeekStart, weekEnd));
                    currentWeekStart = currentWeekStart.plusWeeks(1);
                }
                break;
                
            case "MONTH":
                LocalDate currentMonthStart = startDate.withDayOfMonth(1);
                while (!currentMonthStart.isAfter(endDate)) {
                    LocalDate monthEnd = currentMonthStart.with(TemporalAdjusters.lastDayOfMonth());
                    if (monthEnd.isAfter(endDate)) {
                        monthEnd = endDate;
                    }
                    intervals.add(new AbstractMap.SimpleEntry<>(currentMonthStart, monthEnd));
                    currentMonthStart = currentMonthStart.plusMonths(1).withDayOfMonth(1);
                }
                break;
                
            case "QUARTER":
                LocalDate currentQuarterStart = startDate.withDayOfMonth(1);
                int startMonth = currentQuarterStart.getMonthValue();
                currentQuarterStart = currentQuarterStart.withMonth(startMonth - ((startMonth - 1) % 3));
                
                while (!currentQuarterStart.isAfter(endDate)) {
                    LocalDate quarterEnd = currentQuarterStart.plusMonths(2).with(TemporalAdjusters.lastDayOfMonth());
                    if (quarterEnd.isAfter(endDate)) {
                        quarterEnd = endDate;
                    }
                    intervals.add(new AbstractMap.SimpleEntry<>(currentQuarterStart, quarterEnd));
                    currentQuarterStart = currentQuarterStart.plusMonths(3).withDayOfMonth(1);
                }
                break;
                
            case "YEAR":
                LocalDate currentYearStart = startDate.withDayOfYear(1);
                while (!currentYearStart.isAfter(endDate)) {
                    LocalDate yearEnd = currentYearStart.with(TemporalAdjusters.lastDayOfYear());
                    if (yearEnd.isAfter(endDate)) {
                        yearEnd = endDate;
                    }
                    intervals.add(new AbstractMap.SimpleEntry<>(currentYearStart, yearEnd));
                    currentYearStart = currentYearStart.plusYears(1).withDayOfYear(1);
                }
                break;
                
            default:
                throw new IllegalArgumentException("Unsupported interval type: " + intervalType);
        }
        
        return intervals;
    }
    
    private String formatIntervalLabel(LocalDate start, LocalDate end, String intervalType) {
        switch (intervalType.toUpperCase()) {
            case "DAY":
                return start.toString();
                
            case "WEEK":
                return start.toString() + " to " + end.toString();
                
            case "MONTH":
                return start.getMonth() + " " + start.getYear();
                
            case "QUARTER":
                int quarter = ((start.getMonthValue() - 1) / 3) + 1;
                return "Q" + quarter + " " + start.getYear();
                
            case "YEAR":
                return String.valueOf(start.getYear());
                
            default:
                return start.toString() + " to " + end.toString();
        }
    }
}
