package am.trade.api.service.impl;

import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.api.service.TradeMetricsService;
import am.trade.common.models.*;
import am.trade.dashboard.service.TradeMetricsCalculationService;
import am.trade.dashboard.service.metrics.*;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.services.service.TradeDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
    private final TradeDetailsMapper tradeDetailsMapper;
    private final PerformanceMetricsService performanceMetricsService;
    private final RiskMetricsService riskMetricsService;
    private final TradeDistributionMetricsService distributionMetricsService;
    private final TradeTimingMetricsService timingMetricsService;
    private final TradePatternMetricsService patternMetricsService;
    private final TradeMetricsCalculationService metricsCalculationService;

    private static final List<String> AVAILABLE_METRIC_TYPES = Arrays.asList(
            "PERFORMANCE", "RISK", "DISTRIBUTION", "TIMING", "PATTERN", "STRATEGY");

    @Override
    public MetricsResponse getMetrics(MetricsFilterRequest filterRequest) {
        validateFilterRequest(filterRequest);
        
        // Convert dates to LocalDateTime for repository queries
        LocalDateTime startDateTime = filterRequest.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = filterRequest.getEndDate().atTime(LocalTime.MAX);
        
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
                .startDate(firstPeriodStart)
                .endDate(firstPeriodEnd)
                .metricTypes(metricTypes)
                .build();
        
        MetricsFilterRequest secondPeriodRequest = MetricsFilterRequest.builder()
                .portfolioIds(portfolioIds)
                .startDate(secondPeriodStart)
                .endDate(secondPeriodEnd)
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
                    .startDate(intervalStart)
                    .endDate(intervalEnd)
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
    
    private void validateFilterRequest(MetricsFilterRequest filterRequest) {
        if (filterRequest.getPortfolioIds() == null || filterRequest.getPortfolioIds().isEmpty()) {
            throw new IllegalArgumentException("At least one portfolio ID must be provided");
        }
        
        if (filterRequest.getStartDate() == null) {
            throw new IllegalArgumentException("Start date must be provided");
        }
        
        if (filterRequest.getEndDate() == null) {
            throw new IllegalArgumentException("End date must be provided");
        }
        
        if (filterRequest.getStartDate().isAfter(filterRequest.getEndDate())) {
            throw new IllegalArgumentException("Start date cannot be after end date");
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
        // Apply in-memory filters based on the filter request
        
        if (filterRequest.getStrategies() != null && !filterRequest.getStrategies().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getStrategy() != null && 
                            filterRequest.getStrategies().contains(trade.getStrategy()))
                    .collect(Collectors.toList());
        }
        
        if (filterRequest.getInstruments() != null && !filterRequest.getInstruments().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getSymbol() != null && 
                            filterRequest.getInstruments().contains(trade.getSymbol()))
                    .collect(Collectors.toList());
        }
        
        if (filterRequest.getTags() != null && !filterRequest.getTags().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getTags() != null && 
                            !Collections.disjoint(trade.getTags(), filterRequest.getTags()))
                    .collect(Collectors.toList());
        }
        
        if (filterRequest.getDirections() != null && !filterRequest.getDirections().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getTradePositionType() != null && 
                            filterRequest.getDirections().contains(trade.getTradePositionType().name()))
                    .collect(Collectors.toList());
        }
        
        if (filterRequest.getStatuses() != null && !filterRequest.getStatuses().isEmpty()) {
            trades = trades.stream()
                    .filter(trade -> trade.getStatus() != null && 
                            filterRequest.getStatuses().contains(trade.getStatus().name()))
                    .collect(Collectors.toList());
        }
        
        // Apply numeric filters
        if (filterRequest.getMinProfitLoss() != null) {
            trades = trades.stream()
                    .filter(trade -> trade.getMetrics() != null && 
                            trade.getMetrics().getProfitLoss() != null && 
                            trade.getMetrics().getProfitLoss().doubleValue() >= filterRequest.getMinProfitLoss())
                    .collect(Collectors.toList());
        }
        
        if (filterRequest.getMaxProfitLoss() != null) {
            trades = trades.stream()
                    .filter(trade -> trade.getMetrics() != null && 
                            trade.getMetrics().getProfitLoss() != null && 
                            trade.getMetrics().getProfitLoss().doubleValue() <= filterRequest.getMaxProfitLoss())
                    .collect(Collectors.toList());
        }
        
        // Apply position size filters
        if (filterRequest.getMinPositionSize() != null || filterRequest.getMaxPositionSize() != null) {
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
                        
                        boolean meetsMin = filterRequest.getMinPositionSize() == null || 
                                positionSize >= filterRequest.getMinPositionSize();
                        boolean meetsMax = filterRequest.getMaxPositionSize() == null || 
                                positionSize <= filterRequest.getMaxPositionSize();
                        
                        return meetsMin && meetsMax;
                    })
                    .collect(Collectors.toList());
        }
        
        // Apply holding time filters
        if (filterRequest.getMinHoldingTimeHours() != null || filterRequest.getMaxHoldingTimeHours() != null) {
            trades = trades.stream()
                    .filter(trade -> {
                        if (trade.getEntryInfo() == null || trade.getExitInfo() == null || 
                                trade.getEntryInfo().getTimestamp() == null || 
                                trade.getExitInfo().getTimestamp() == null) {
                            return false;
                        }
                        
                        long holdingHours = ChronoUnit.HOURS.between(
                                trade.getEntryInfo().getTimestamp(), 
                                trade.getExitInfo().getTimestamp());
                        
                        boolean meetsMin = filterRequest.getMinHoldingTimeHours() == null || 
                                holdingHours >= filterRequest.getMinHoldingTimeHours();
                        boolean meetsMax = filterRequest.getMaxHoldingTimeHours() == null || 
                                holdingHours <= filterRequest.getMaxHoldingTimeHours();
                        
                        return meetsMin && meetsMax;
                    })
                    .collect(Collectors.toList());
        }
        
        return trades;
    }
    
    private MetricsResponse buildMetricsResponse(List<TradeDetails> trades, MetricsFilterRequest filterRequest) {
        MetricsResponse response = new MetricsResponse();
        response.setPortfolioIds(filterRequest.getPortfolioIds());
        response.setStartDate(filterRequest.getStartDate());
        response.setEndDate(filterRequest.getEndDate());
        response.setTotalTradesCount(trades.size());
        
        // Initialize metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("generatedAt", LocalDateTime.now());
        metadata.put("appliedFilters", filterRequest);
        response.setMetadata(metadata);
        
        // Calculate requested metrics
        Set<String> metricTypes = filterRequest.getMetricTypes();
        if (metricTypes.isEmpty() || metricTypes.contains("PERFORMANCE")) {
            response.setPerformanceMetrics(performanceMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.isEmpty() || metricTypes.contains("RISK")) {
            response.setRiskMetrics(riskMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.isEmpty() || metricTypes.contains("DISTRIBUTION")) {
            response.setDistributionMetrics(distributionMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.isEmpty() || metricTypes.contains("TIMING")) {
            response.setTimingMetrics(timingMetricsService.calculateMetrics(trades));
        }
        
        if (metricTypes.isEmpty() || metricTypes.contains("PATTERN")) {
            response.setPatternMetrics(patternMetricsService.calculateMetrics(trades));
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
