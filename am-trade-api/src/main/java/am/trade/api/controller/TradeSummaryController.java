package am.trade.api.controller;

import am.trade.api.service.TradeSummaryService;
import am.trade.common.models.TradeDetails;
import am.trade.services.model.TradeSummary;

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

    @Operation(summary = "Get trade summary for a specific portfolio and date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade summary retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range or missing portfolio ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    public ResponseEntity<TradeSummary> getTradeSummary(
            @Parameter(description = "Portfolio ID") 
            @RequestParam String portfolioId,
            @Parameter(description = "Start date in ISO format (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date in ISO format (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Fetching trade summary for portfolio: {} from {} to {}", portfolioId, startDate, endDate);
            TradeSummary tradeSummary = tradeSummaryService.getTradeSummary(portfolioId, startDate, endDate);
            return ResponseEntity.ok(tradeSummary);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get daily trade summary for a specific portfolio and month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Daily trade summaries retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid month value or missing portfolio ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/daily-summary")
    public ResponseEntity<Map<LocalDate, TradeSummary>> getDailyTradeSummaries(
            @Parameter(description = "Portfolio ID") 
            @RequestParam String portfolioId,
            @Parameter(description = "Year") 
            @RequestParam int year,
            @Parameter(description = "Month (1-12)") 
            @RequestParam int month) {
        
        try {
            log.info("Fetching daily trade summaries for portfolio: {} for {}-{}", portfolioId, year, month);
            Map<LocalDate, TradeSummary> dailySummaries = tradeSummaryService.getDailyTradeSummaries(portfolioId, year, month);
            return ResponseEntity.ok(dailySummaries);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
