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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        
        try {
            log.info("Fetching trade details for period: {}, portfolioId: {}", periodType, portfolioId);
            Map<String, List<TradeDetails>> tradeDetails = tradeSummaryService.getTradeDetailsByTimePeriod(
                periodType, startDate, endDate, year, month, quarter, portfolioId);
            return ResponseEntity.ok(tradeDetails);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
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
        
        try {
            log.info("Fetching basic trade summary with ID: {}", id);
            return tradeSummaryService.findBasicById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching basic trade summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get detailed trade metrics by basic summary ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed trade metrics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Detailed metrics not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/detailed/by-basic-id/{basicId}")
    public ResponseEntity<TradeSummaryDetailed> getDetailedTradeSummaryByBasicId(
            @Parameter(description = "Basic trade summary ID") 
            @PathVariable String basicId) {
        
        try {
            log.info("Fetching detailed trade metrics for basic summary ID: {}", basicId);
            return tradeSummaryService.findDetailedByBasicId(basicId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching detailed trade metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get detailed trade metrics by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detailed trade metrics retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Detailed metrics not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/detailed/{id}")
    public ResponseEntity<TradeSummaryDetailed> getDetailedTradeSummary(
            @Parameter(description = "Detailed trade metrics ID") 
            @PathVariable String id) {
        
        try {
            log.info("Fetching detailed trade metrics with ID: {}", id);
            return tradeSummaryService.findDetailedById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error fetching detailed trade metrics: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
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
        
        try {
            log.info("Fetching composite trade summary with ID: {}, forceRecalculate: {}", id, forceRecalculate);
            
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
            log.error("Error fetching composite trade summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Get all active basic trade summaries for an owner")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade summaries retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/basic/by-owner/{ownerId}")
    public ResponseEntity<List<TradeSummaryBasic>> getActiveBasicTradeSummariesByOwner(
            @Parameter(description = "Owner ID") 
            @PathVariable String ownerId) {
        
        try {
            log.info("Fetching active basic trade summaries for owner: {}", ownerId);
            List<TradeSummaryBasic> summaries = tradeSummaryService.findAllActiveBasicByOwnerId(ownerId);
            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            log.error("Error fetching basic trade summaries: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Save or update a composite trade summary")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade summary saved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade summary data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/composite")
    public ResponseEntity<TradeSummary> saveCompositeTradeSummary(
            @RequestBody TradeSummary tradeSummary) {
        
        try {
            log.info("Saving composite trade summary: {}", tradeSummary.getId());
            TradeSummary saved = tradeSummary.getId() == null ? 
                tradeSummaryService.saveTradeSummary(tradeSummary) : 
                tradeSummaryService.updateTradeSummary(tradeSummary);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            log.error("Invalid trade summary data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error saving trade summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
    
    @Operation(summary = "Delete a trade summary and its associated detailed metrics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Trade summary deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trade summary not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTradeSummary(
            @Parameter(description = "Trade summary ID") 
            @PathVariable String id) {
        
        try {
            log.info("Deleting trade summary with ID: {}", id);
            if (tradeSummaryService.findBasicById(id).isPresent()) {
                tradeSummaryService.deleteTradeSummary(id);
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error deleting trade summary: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }


}
