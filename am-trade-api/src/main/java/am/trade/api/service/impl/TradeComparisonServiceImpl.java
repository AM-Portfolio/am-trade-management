package am.trade.api.service.impl;

import am.trade.api.dto.ComparisonRequest;
import am.trade.api.dto.ComparisonResponse;
import am.trade.api.service.TradeComparisonService;
import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.PerformanceMetricsService;
import am.trade.persistence.repository.TradeComparisonRepository;
import am.trade.services.service.TradeDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TradeComparisonService for comparing trade performance across different dimensions
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeComparisonServiceImpl implements TradeComparisonService {

    private final TradeDetailsService tradeDetailsService;
    private final PerformanceMetricsService performanceMetricsService;
    private final TradeComparisonRepository tradeComparisonRepository;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final List<String> DEFAULT_METRICS = Arrays.asList(
            "profitLoss", "winRate", "averageWin", "averageLoss", "maxDrawdown", "sharpeRatio");

    @Override
    public ComparisonResponse compareTradePerformance(ComparisonRequest request) {
        log.info("Comparing trade performance for user: {}, type: {}", 
                request.getUserId(), request.getComparisonType());
        
        validateComparisonRequest(request);
        
        switch (request.getComparisonType().toUpperCase()) {
            case "PORTFOLIO":
                return comparePortfolios(request);
            case "TIME_PERIOD":
                return compareTimePeriods(request);
            case "STRATEGY":
                return compareStrategies(request);
            case "INSTRUMENT":
                return compareInstruments(request);
            default:
                throw new IllegalArgumentException("Unsupported comparison type: " + request.getComparisonType());
        }
    }

    @Override
    public ComparisonResponse comparePortfolios(
            String userId, String portfolioId1, String portfolioId2, String startDate, String endDate) {
        
        log.info("Comparing portfolios {} and {} for user: {}", portfolioId1, portfolioId2, userId);
        
        // Create a comparison request
        ComparisonRequest request = ComparisonRequest.builder()
                .userId(userId)
                .comparisonType("PORTFOLIO")
                .portfolioIds(Arrays.asList(portfolioId1, portfolioId2))
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        return comparePortfolios(request);
    }

    @Override
    public ComparisonResponse compareTimePeriods(
            String userId, String period1Start, String period1End, 
            String period2Start, String period2End, String portfolioId) {
        
        log.info("Comparing time periods for user: {}", userId);
        
        // Create time periods
        List<ComparisonRequest.TimePeriod> timePeriods = new ArrayList<>();
        
        ComparisonRequest.TimePeriod period1 = new ComparisonRequest.TimePeriod();
        period1.setName("Period 1");
        period1.setStartDate(period1Start);
        period1.setEndDate(period1End);
        timePeriods.add(period1);
        
        ComparisonRequest.TimePeriod period2 = new ComparisonRequest.TimePeriod();
        period2.setName("Period 2");
        period2.setStartDate(period2Start);
        period2.setEndDate(period2End);
        timePeriods.add(period2);
        
        // Create a comparison request
        ComparisonRequest request = ComparisonRequest.builder()
                .userId(userId)
                .comparisonType("TIME_PERIOD")
                .timePeriods(timePeriods)
                .build();
        
        // Add portfolio ID if provided
        if (portfolioId != null && !portfolioId.trim().isEmpty()) {
            request.setPortfolioIds(Collections.singletonList(portfolioId));
        }
        
        return compareTimePeriods(request);
    }

    @Override
    public ComparisonResponse compareStrategies(
            String userId, String strategy1, String strategy2, String startDate, String endDate) {
        
        log.info("Comparing strategies {} and {} for user: {}", strategy1, strategy2, userId);
        
        // Create a comparison request
        ComparisonRequest request = ComparisonRequest.builder()
                .userId(userId)
                .comparisonType("STRATEGY")
                .strategies(Arrays.asList(strategy1, strategy2))
                .startDate(startDate)
                .endDate(endDate)
                .build();
        
        return compareStrategies(request);
    }
    
    /**
     * Compare portfolios based on the provided request
     */
    private ComparisonResponse comparePortfolios(ComparisonRequest request) {
        log.debug("Comparing portfolios: {}", request.getPortfolioIds());
        
        // Parse date range if provided
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
            startDateTime = LocalDate.parse(request.getStartDate(), DATE_FORMATTER).atStartOfDay();
        }
        
        if (request.getEndDate() != null && !request.getEndDate().trim().isEmpty()) {
            endDateTime = LocalDate.parse(request.getEndDate(), DATE_FORMATTER).atTime(LocalTime.MAX);
        }
        
        // Create dimensions for each portfolio
        List<ComparisonResponse.ComparisonDimension> dimensions = new ArrayList<>();
        Map<String, List<TradeDetails>> tradesByDimension = new HashMap<>();
        
        for (String portfolioId : request.getPortfolioIds()) {
            // Fetch trades for this portfolio
            List<TradeDetails> trades;
            
            if (startDateTime != null && endDateTime != null) {
                trades = tradeDetailsService.findByPortfolioIdInAndEntryInfoTimestampBetween(
                        Collections.singletonList(portfolioId), startDateTime, endDateTime);
            } else {
                trades = tradeDetailsService.findByPortfolioIdIn(Collections.singletonList(portfolioId));
            }
            
            // Create dimension
            ComparisonResponse.ComparisonDimension dimension = createDimension(
                    portfolioId, "Portfolio: " + portfolioId, trades, startDateTime, endDateTime);
            
            dimensions.add(dimension);
            tradesByDimension.put(dimension.getId(), trades);
        }
        
        // Calculate metrics for each dimension
        List<ComparisonResponse.ComparisonMetric> metrics = calculateMetrics(tradesByDimension);
        
        // Create and return response
        return createComparisonResponse(request, dimensions, metrics);
    }
    
    /**
     * Compare time periods based on the provided request
     */
    private ComparisonResponse compareTimePeriods(ComparisonRequest request) {
        log.debug("Comparing time periods: {}", request.getTimePeriods().size());
        
        // Create dimensions for each time period
        List<ComparisonResponse.ComparisonDimension> dimensions = new ArrayList<>();
        Map<String, List<TradeDetails>> tradesByDimension = new HashMap<>();
        
        for (ComparisonRequest.TimePeriod period : request.getTimePeriods()) {
            // Parse dates
            LocalDateTime startDateTime = LocalDate.parse(period.getStartDate(), DATE_FORMATTER).atStartOfDay();
            LocalDateTime endDateTime = LocalDate.parse(period.getEndDate(), DATE_FORMATTER).atTime(LocalTime.MAX);
            
            // Fetch trades for this period
            List<TradeDetails> trades;
            
            if (request.getPortfolioIds() != null && !request.getPortfolioIds().isEmpty()) {
                trades = tradeDetailsService.findByPortfolioIdInAndEntryInfoTimestampBetween(
                        request.getPortfolioIds(), startDateTime, endDateTime);
            } else {
                // If no portfolio IDs provided, fetch all trades for the user in this period
                // This would require a custom repository method that we'll assume exists
                trades = tradeComparisonRepository.findByUserIdAndEntryDateBetween(
                        request.getUserId(), startDateTime, endDateTime);
            }
            
            // Create dimension
            ComparisonResponse.ComparisonDimension dimension = createDimension(
                    UUID.randomUUID().toString(), period.getName(), trades, startDateTime, endDateTime);
            
            dimensions.add(dimension);
            tradesByDimension.put(dimension.getId(), trades);
        }
        
        // Calculate metrics for each dimension
        List<ComparisonResponse.ComparisonMetric> metrics = calculateMetrics(tradesByDimension);
        
        // Create and return response
        return createComparisonResponse(request, dimensions, metrics);
    }
    
    /**
     * Compare strategies based on the provided request
     */
    private ComparisonResponse compareStrategies(ComparisonRequest request) {
        log.debug("Comparing strategies: {}", request.getStrategies());
        
        // Parse date range if provided
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
            startDateTime = LocalDate.parse(request.getStartDate(), DATE_FORMATTER).atStartOfDay();
        }
        
        if (request.getEndDate() != null && !request.getEndDate().trim().isEmpty()) {
            endDateTime = LocalDate.parse(request.getEndDate(), DATE_FORMATTER).atTime(LocalTime.MAX);
        }
        
        // Create dimensions for each strategy
        List<ComparisonResponse.ComparisonDimension> dimensions = new ArrayList<>();
        Map<String, List<TradeDetails>> tradesByDimension = new HashMap<>();
        
        for (String strategy : request.getStrategies()) {
            // Fetch trades for this strategy
            List<TradeDetails> trades = tradeComparisonRepository.findByUserIdAndStrategyAndDateRange(
                    request.getUserId(), strategy, startDateTime, endDateTime);
            
            // Create dimension
            ComparisonResponse.ComparisonDimension dimension = createDimension(
                    UUID.randomUUID().toString(), "Strategy: " + strategy, trades, startDateTime, endDateTime);
            
            dimensions.add(dimension);
            tradesByDimension.put(dimension.getId(), trades);
        }
        
        // Calculate metrics for each dimension
        List<ComparisonResponse.ComparisonMetric> metrics = calculateMetrics(tradesByDimension);
        
        // Create and return response
        return createComparisonResponse(request, dimensions, metrics);
    }
    
    /**
     * Compare instruments based on the provided request
     */
    private ComparisonResponse compareInstruments(ComparisonRequest request) {
        log.debug("Comparing instruments: {}", request.getInstruments());
        
        // Parse date range if provided
        LocalDateTime startDateTime = null;
        LocalDateTime endDateTime = null;
        
        if (request.getStartDate() != null && !request.getStartDate().trim().isEmpty()) {
            startDateTime = LocalDate.parse(request.getStartDate(), DATE_FORMATTER).atStartOfDay();
        }
        
        if (request.getEndDate() != null && !request.getEndDate().trim().isEmpty()) {
            endDateTime = LocalDate.parse(request.getEndDate(), DATE_FORMATTER).atTime(LocalTime.MAX);
        }
        
        // Create dimensions for each instrument
        List<ComparisonResponse.ComparisonDimension> dimensions = new ArrayList<>();
        Map<String, List<TradeDetails>> tradesByDimension = new HashMap<>();
        
        for (String instrument : request.getInstruments()) {
            // Fetch trades for this instrument
            List<TradeDetails> trades;
            
            if (startDateTime != null && endDateTime != null) {
                trades = tradeComparisonRepository.findByUserIdAndSymbolAndDateRange(
                        request.getUserId(), instrument, startDateTime, endDateTime);
            } else {
                trades = tradeComparisonRepository.findByUserIdAndSymbol(request.getUserId(), instrument);
            }
            
            // Create dimension
            ComparisonResponse.ComparisonDimension dimension = createDimension(
                    UUID.randomUUID().toString(), "Instrument: " + instrument, trades, startDateTime, endDateTime);
            
            dimensions.add(dimension);
            tradesByDimension.put(dimension.getId(), trades);
        }
        
        // Calculate metrics for each dimension
        List<ComparisonResponse.ComparisonMetric> metrics = calculateMetrics(tradesByDimension);
        
        // Create and return response
        return createComparisonResponse(request, dimensions, metrics);
    }
    
    /**
     * Create a comparison dimension
     */
    private ComparisonResponse.ComparisonDimension createDimension(
            String id, String name, List<TradeDetails> trades, 
            LocalDateTime startDateTime, LocalDateTime endDateTime) {
        
        return ComparisonResponse.ComparisonDimension.builder()
                .id(id)
                .name(name)
                .description("Contains " + trades.size() + " trades")
                .startDate(startDateTime != null ? startDateTime.toLocalDate().format(DATE_FORMATTER) : null)
                .endDate(endDateTime != null ? endDateTime.toLocalDate().format(DATE_FORMATTER) : null)
                .tradeCount(trades.size())
                .metadata(new HashMap<>())
                .build();
    }
    
    /**
     * Calculate metrics for each dimension
     */
    private List<ComparisonResponse.ComparisonMetric> calculateMetrics(Map<String, List<TradeDetails>> tradesByDimension) {
        List<ComparisonResponse.ComparisonMetric> metrics = new ArrayList<>();
        
        // Define the metrics we want to calculate
        Map<String, String> metricDisplayNames = new HashMap<>();
        metricDisplayNames.put("profitLoss", "Profit/Loss");
        metricDisplayNames.put("winRate", "Win Rate");
        metricDisplayNames.put("averageWin", "Average Win");
        metricDisplayNames.put("averageLoss", "Average Loss");
        metricDisplayNames.put("maxDrawdown", "Max Drawdown");
        metricDisplayNames.put("sharpeRatio", "Sharpe Ratio");
        
        Map<String, String> metricUnits = new HashMap<>();
        metricUnits.put("profitLoss", "$");
        metricUnits.put("winRate", "%");
        metricUnits.put("averageWin", "$");
        metricUnits.put("averageLoss", "$");
        metricUnits.put("maxDrawdown", "$");
        metricUnits.put("sharpeRatio", "");
        
        // Calculate performance metrics for each dimension
        Map<String, PerformanceMetrics> performanceMetricsByDimension = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByDimension.entrySet()) {
            String dimensionId = entry.getKey();
            List<TradeDetails> trades = entry.getValue();
            
            PerformanceMetrics performanceMetrics = performanceMetricsService.calculateMetrics(trades);
            performanceMetricsByDimension.put(dimensionId, performanceMetrics);
        }
        
        // Create comparison metrics
        for (String metricName : DEFAULT_METRICS) {
            Map<String, Double> values = new HashMap<>();
            Map<String, Double> percentChanges = new HashMap<>();
            
            // Extract metric values for each dimension
            for (Map.Entry<String, PerformanceMetrics> entry : performanceMetricsByDimension.entrySet()) {
                String dimensionId = entry.getKey();
                PerformanceMetrics performanceMetrics = entry.getValue();
                
                // Extract the metric value using reflection
                Double value = extractMetricValue(performanceMetrics, metricName);
                values.put(dimensionId, value);
            }
            
            // Calculate percent changes if there are at least 2 dimensions
            if (values.size() >= 2) {
                // Use the first dimension as baseline
                String baselineDimensionId = tradesByDimension.keySet().iterator().next();
                Double baselineValue = values.get(baselineDimensionId);
                
                if (baselineValue != null && baselineValue != 0) {
                    for (Map.Entry<String, Double> entry : values.entrySet()) {
                        String dimensionId = entry.getKey();
                        Double value = entry.getValue();
                        
                        if (!dimensionId.equals(baselineDimensionId) && value != null) {
                            double percentChange = ((value - baselineValue) / Math.abs(baselineValue)) * 100;
                            percentChanges.put(dimensionId, percentChange);
                        }
                    }
                }
            }
            
            // Find best and worst dimensions
            String bestDimensionId = null;
            String worstDimensionId = null;
            Double bestValue = null;
            Double worstValue = null;
            
            for (Map.Entry<String, Double> entry : values.entrySet()) {
                String dimensionId = entry.getKey();
                Double value = entry.getValue();
                
                if (value != null) {
                    // For most metrics, higher is better
                    boolean isHigherBetter = !metricName.equals("maxDrawdown");
                    
                    if (bestValue == null || (isHigherBetter ? value > bestValue : value < bestValue)) {
                        bestValue = value;
                        bestDimensionId = dimensionId;
                    }
                    
                    if (worstValue == null || (isHigherBetter ? value < worstValue : value > worstValue)) {
                        worstValue = value;
                        worstDimensionId = dimensionId;
                    }
                }
            }
            
            // Calculate average and median
            List<Double> valuesList = values.values().stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            Double average = valuesList.stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0);
            
            Double median = calculateMedian(valuesList);
            
            // Create metric
            ComparisonResponse.ComparisonMetric metric = ComparisonResponse.ComparisonMetric.builder()
                    .name(metricName)
                    .displayName(metricDisplayNames.getOrDefault(metricName, metricName))
                    .unit(metricUnits.getOrDefault(metricName, ""))
                    .values(values)
                    .percentChanges(percentChanges)
                    .bestDimensionId(bestDimensionId)
                    .worstDimensionId(worstDimensionId)
                    .average(average)
                    .median(median)
                    .build();
            
            metrics.add(metric);
        }
        
        return metrics;
    }
    
    /**
     * Extract a metric value from performance metrics using reflection
     */
    private Double extractMetricValue(PerformanceMetrics performanceMetrics, String metricName) {
        try {
            // Convert metricName to getter method name (e.g., "profitLoss" -> "getProfitLoss")
            String getterName = "get" + metricName.substring(0, 1).toUpperCase() + metricName.substring(1);
            
            // Call the getter method using reflection
            return (Double) performanceMetrics.getClass().getMethod(getterName).invoke(performanceMetrics);
        } catch (Exception e) {
            log.warn("Failed to extract metric value for {}: {}", metricName, e.getMessage());
            return null;
        }
    }
    
    /**
     * Calculate median of a list of values
     */
    private Double calculateMedian(List<Double> values) {
        if (values.isEmpty()) {
            return 0.0;
        }
        
        Collections.sort(values);
        int middle = values.size() / 2;
        
        if (values.size() % 2 == 1) {
            // Odd number of elements
            return values.get(middle);
        } else {
            // Even number of elements
            return (values.get(middle - 1) + values.get(middle)) / 2.0;
        }
    }
    
    /**
     * Create comparison response
     */
    private ComparisonResponse createComparisonResponse(
            ComparisonRequest request, 
            List<ComparisonResponse.ComparisonDimension> dimensions,
            List<ComparisonResponse.ComparisonMetric> metrics) {
        
        return ComparisonResponse.builder()
                .comparisonId(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .comparisonType(request.getComparisonType())
                .dimensions(dimensions)
                .metrics(metrics)
                .additionalData(new HashMap<>())
                .generatedAt(LocalDateTime.now().toString())
                .build();
    }
    
    /**
     * Validate comparison request
     */
    private void validateComparisonRequest(ComparisonRequest request) {
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (request.getComparisonType() == null || request.getComparisonType().trim().isEmpty()) {
            throw new IllegalArgumentException("Comparison type is required");
        }
        
        switch (request.getComparisonType().toUpperCase()) {
            case "PORTFOLIO":
                if (request.getPortfolioIds() == null || request.getPortfolioIds().size() < 2) {
                    throw new IllegalArgumentException("At least two portfolio IDs are required for portfolio comparison");
                }
                break;
            case "TIME_PERIOD":
                if (request.getTimePeriods() == null || request.getTimePeriods().size() < 2) {
                    throw new IllegalArgumentException("At least two time periods are required for time period comparison");
                }
                
                // Validate each time period
                for (ComparisonRequest.TimePeriod period : request.getTimePeriods()) {
                    if (period.getStartDate() == null || period.getEndDate() == null) {
                        throw new IllegalArgumentException("Start date and end date are required for each time period");
                    }
                    
                    try {
                        LocalDate startDate = LocalDate.parse(period.getStartDate(), DATE_FORMATTER);
                        LocalDate endDate = LocalDate.parse(period.getEndDate(), DATE_FORMATTER);
                        
                        if (endDate.isBefore(startDate)) {
                            throw new IllegalArgumentException("End date cannot be before start date");
                        }
                    } catch (DateTimeParseException e) {
                        throw new IllegalArgumentException("Invalid date format. Use ISO date format (YYYY-MM-DD)");
                    }
                }
                break;
            case "STRATEGY":
                if (request.getStrategies() == null || request.getStrategies().size() < 2) {
                    throw new IllegalArgumentException("At least two strategies are required for strategy comparison");
                }
                break;
            case "INSTRUMENT":
                if (request.getInstruments() == null || request.getInstruments().size() < 2) {
                    throw new IllegalArgumentException("At least two instruments are required for instrument comparison");
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported comparison type: " + request.getComparisonType());
        }
        
        // Validate date range if provided
        if (request.getStartDate() != null && request.getEndDate() != null) {
            try {
                LocalDate startDate = LocalDate.parse(request.getStartDate(), DATE_FORMATTER);
                LocalDate endDate = LocalDate.parse(request.getEndDate(), DATE_FORMATTER);
                
                if (endDate.isBefore(startDate)) {
                    throw new IllegalArgumentException("End date cannot be before start date");
                }
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date format. Use ISO date format (YYYY-MM-DD)");
            }
        }
    }
}
