package am.trade.api.controller;

import am.trade.api.service.TradeSummaryService;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;
import am.trade.dashboard.service.TradeMetricsCalculationService;
import am.trade.dashboard.service.MetricsCalculationService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for trade summary operations
 */
@RestController
@RequestMapping("/api/v1/trade-summary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade Summary", description = "Trade summary operations by calendar periods")
public class TradeSummaryController {

    private final TradeSummaryService tradeSummaryService;
    private final TradeMetricsCalculationService tradeMetricsCalculationService;
    private final MetricsCalculationService metricsCalculationService;

    @Operation(summary = "Get trade details by time period")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/trades")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByTimePeriod(
            @Parameter(description = "Period type (DAY, MONTH, QUARTER, FINANCIAL_YEAR, CUSTOM)") 
            @RequestParam String periodType,
            @Parameter(description = "Start date in ISO format (YYYY-MM-DD), required for DAY and CUSTOM") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date in ISO format (YYYY-MM-DD), required for CUSTOM") 
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Year, required for MONTH, QUARTER, FINANCIAL_YEAR") 
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12), required for MONTH") 
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Quarter (1-4), required for QUARTER") 
            @RequestParam(required = false) Integer quarter,
            @Parameter(description = "Portfolio ID (optional)") 
            @RequestParam(required = false) String portfolioId) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Fetching trade details for period: {}, portfolioId: {}", 
                    processId, periodType, portfolioId);
            
            Map<String, List<TradeDetails>> tradeDetails = getTradeDetailsByTimePeriodInternal(
                processId, periodType, startDate, endDate, year, month, quarter, portfolioId);
                
            log.info("[{}] Successfully retrieved trade details for period: {}, portfolioId: {}", 
                    processId, periodType, portfolioId);
            return ResponseEntity.ok(tradeDetails);
        } catch (IllegalArgumentException e) {
            log.error("[{}] Invalid request parameters: {}", processId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("[{}] Unexpected error while fetching trade details: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Operation(summary = "Get basic trade summary by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Basic trade summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trade summary not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/basic/{id}")
    public ResponseEntity<TradeSummaryBasic> getBasicTradeSummary(
            @Parameter(description = "Trade summary ID") 
            @PathVariable String id) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Fetching basic trade summary with ID: {}", processId, id);
            Optional<TradeSummaryBasic> tradeSummary = tradeSummaryService.findBasicById(id);
            
            if (tradeSummary.isPresent()) {
                log.info("[{}] Successfully retrieved basic trade summary with ID: {}", processId, id);
                return ResponseEntity.ok(tradeSummary.get());
            } else {
                log.warn("[{}] Basic trade summary not found with ID: {}", processId, id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("[{}] Error retrieving basic trade summary: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get detailed trade summary by basic ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed trade summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trade summary not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/detailed/by-basic/{basicId}")
    public ResponseEntity<TradeSummaryDetailed> getDetailedTradeSummaryByBasicId(
            @Parameter(description = "Basic trade summary ID") 
            @PathVariable String basicId) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Fetching detailed trade summary by basic ID: {}", processId, basicId);
            Optional<TradeSummaryDetailed> tradeSummary = tradeSummaryService.findDetailedByBasicId(basicId);
            
            if (tradeSummary.isPresent()) {
                log.info("[{}] Successfully retrieved detailed trade summary by basic ID: {}", processId, basicId);
                return ResponseEntity.ok(tradeSummary.get());
            } else {
                log.warn("[{}] Detailed trade summary not found for basic ID: {}", processId, basicId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("[{}] Error retrieving detailed trade summary by basic ID: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get detailed trade summary by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed trade summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trade summary not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/detailed/{id}")
    public ResponseEntity<TradeSummaryDetailed> getDetailedTradeSummary(
            @Parameter(description = "Trade summary ID") 
            @PathVariable String id) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Fetching detailed trade summary with ID: {}", processId, id);
            Optional<TradeSummaryDetailed> tradeSummary = tradeSummaryService.findDetailedById(id);
            
            if (tradeSummary.isPresent()) {
                log.info("[{}] Successfully retrieved detailed trade summary with ID: {}", processId, id);
                return ResponseEntity.ok(tradeSummary.get());
            } else {
                log.warn("[{}] Detailed trade summary not found with ID: {}", processId, id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("[{}] Error retrieving detailed trade summary: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Get composite trade summary with cached metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Composite trade summary retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Trade summary not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/composite/{id}")
    public ResponseEntity<TradeSummary> getCompositeTradeSummary(
            @Parameter(description = "Basic trade summary ID") 
            @PathVariable String id,
            @Parameter(description = "Force recalculation of metrics") 
            @RequestParam(required = false, defaultValue = "false") boolean forceRecalculate) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Fetching composite trade summary with ID: {}, forceRecalculate: {}", processId, id, forceRecalculate);
            
            if (forceRecalculate) {
                // Get the basic summary
                Optional<TradeSummaryBasic> basicOpt = tradeSummaryService.findBasicById(id);
                if (basicOpt.isPresent()) {
                    // Force recalculation of metrics
                    TradeSummaryDetailed detailed = metricsCalculationService.calculateDetailedMetrics(basicOpt.get());
                    TradeSummary composite = TradeSummary.fromBasicAndDetailed(basicOpt.get(), detailed);
                    return ResponseEntity.ok(composite);
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else {
                // Use cached metrics if available
                TradeSummary tradeSummary = metricsCalculationService.getTradeSummaryWithMetrics(id);
                if (tradeSummary != null) {
                    return ResponseEntity.ok(tradeSummary);
                } else {
                    return ResponseEntity.notFound().build();
                }
            }
        } catch (Exception e) {
            log.error("[{}] Error fetching composite trade summary: {}", processId, e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get all active basic trade summaries for an owner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade summaries retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/basic/by-owner/{ownerId}")
    public ResponseEntity<List<TradeSummaryBasic>> getAllActiveBasicByOwnerId(
            @Parameter(description = "Owner ID") 
            @PathVariable String ownerId) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Fetching all active basic trade summaries for owner: {}", processId, ownerId);
            List<TradeSummaryBasic> tradeSummaries = tradeSummaryService.findAllActiveBasicByOwnerId(ownerId);
            
            log.info("[{}] Successfully retrieved {} active basic trade summaries for owner: {}", 
                    processId, tradeSummaries.size(), ownerId);
            return ResponseEntity.ok(tradeSummaries);
        } catch (Exception e) {
            log.error("[{}] Error fetching trade summaries for owner: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Create a new trade summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trade summary created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade summary data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<TradeSummary> createTradeSummary(
            @Parameter(description = "Trade summary to create") 
            @RequestBody TradeSummary tradeSummary) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Creating new trade summary", processId);
            TradeSummary createdTradeSummary = tradeSummaryService.saveTradeSummary(tradeSummary);
            
            log.info("[{}] Successfully created trade summary with ID: {}", 
                    processId, createdTradeSummary.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTradeSummary);
        } catch (IllegalArgumentException e) {
            log.error("[{}] Invalid trade summary data: {}", processId, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("[{}] Error creating trade summary: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @Operation(summary = "Delete a trade summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Trade summary deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trade summary not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTradeSummary(
            @Parameter(description = "Trade summary ID") 
            @PathVariable String id) {
        
        String processId = UUID.randomUUID().toString();
        try {
            log.info("[{}] Processing request - Deleting trade summary with ID: {}", processId, id);
            tradeSummaryService.deleteTradeSummary(id);
            log.info("[{}] Successfully deleted trade summary with ID: {}", processId, id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("[{}] Error deleting trade summary: {}", processId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Internal method to get trade details by time period
     * Extracted for better logging and error handling with process ID
     * 
     * @param processId UUID for request tracing
     * @param periodType Type of time period (DAY, MONTH, QUARTER, FINANCIAL_YEAR, CUSTOM)
     * @param startDate Start date for DAY and CUSTOM periods
     * @param endDate End date for CUSTOM period
     * @param year Year for MONTH, QUARTER, FINANCIAL_YEAR periods
     * @param month Month number (1-12) for MONTH period
     * @param quarter Quarter number (1-4) for QUARTER period
     * @param portfolioId Optional portfolio ID filter
     * @return Map of trade details grouped by date/period
     */
    private Map<String, List<TradeDetails>> getTradeDetailsByTimePeriodInternal(
            String processId,
            String periodType, 
            LocalDate startDate, 
            LocalDate endDate, 
            Integer year, 
            Integer month, 
            Integer quarter, 
            String portfolioId) {
        
        log.debug("[{}] Delegating to tradeSummaryService.getTradeDetailsByTimePeriod", processId);
        return tradeSummaryService.getTradeDetailsByTimePeriod(
            periodType, startDate, endDate, year, month, quarter, portfolioId);
    }
}
