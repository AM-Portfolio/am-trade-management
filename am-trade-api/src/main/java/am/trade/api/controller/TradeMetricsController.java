package am.trade.api.controller;

import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.api.service.TradeMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * REST controller for trade metrics operations with flexible filtering
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade Metrics", description = "Flexible trade metrics API with customizable filters")
public class TradeMetricsController {

    private final TradeMetricsService tradeMetricsService;

    @Operation(summary = "Get trade metrics with flexible filtering options")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully",
                content = @Content(schema = @Schema(implementation = MetricsResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<MetricsResponse> getTradeMetrics(
            @RequestBody MetricsFilterRequest filterRequest) {
        
        log.info("Fetching trade metrics with filters: {}", filterRequest);
        try {
            MetricsResponse metricsResponse = tradeMetricsService.getMetrics(filterRequest);
            return ResponseEntity.ok(metricsResponse);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get available metric types")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Available metric types retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/types")
    public ResponseEntity<List<String>> getAvailableMetricTypes() {
        log.info("Fetching available metric types");
        return ResponseEntity.ok(tradeMetricsService.getAvailableMetricTypes());
    }


    @Operation(summary = "Get metrics comparison between two time periods")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics comparison retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/compare")
    public ResponseEntity<List<MetricsResponse>> compareMetrics(
            @Parameter(description = "Portfolio IDs") 
            @RequestParam List<String> portfolioIds,
            @Parameter(description = "Metric types to include") 
            @RequestParam Set<String> metricTypes,
            @Parameter(description = "First period start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstPeriodStart,
            @Parameter(description = "First period end date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate firstPeriodEnd,
            @Parameter(description = "Second period start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate secondPeriodStart,
            @Parameter(description = "Second period end date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate secondPeriodEnd) {
        
        log.info("Comparing metrics for portfolios: {} between periods: [{} to {}] and [{} to {}]", 
                portfolioIds, firstPeriodStart, firstPeriodEnd, secondPeriodStart, secondPeriodEnd);
        
        try {
            List<MetricsResponse> comparisonResults = tradeMetricsService.compareMetrics(
                    portfolioIds, metricTypes, firstPeriodStart, firstPeriodEnd, secondPeriodStart, secondPeriodEnd);
            return ResponseEntity.ok(comparisonResults);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters for metrics comparison: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get metrics trends over time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics trends retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/trends")
    public ResponseEntity<Map<String, List<MetricsResponse>>> getMetricsTrends(
            @Parameter(description = "Portfolio IDs") 
            @RequestParam List<String> portfolioIds,
            @Parameter(description = "Metric types to include") 
            @RequestParam Set<String> metricTypes,
            @Parameter(description = "Start date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Time interval (DAY, WEEK, MONTH, QUARTER, YEAR)") 
            @RequestParam String interval) {
        
        log.info("Fetching metrics trends for portfolios: {} from {} to {} with interval: {}", 
                portfolioIds, startDate, endDate, interval);
        
        try {
            Map<String, List<MetricsResponse>> trends = tradeMetricsService.getMetricsTrends(
                    portfolioIds, metricTypes, startDate, endDate, interval);
            return ResponseEntity.ok(trends);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters for metrics trends: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
