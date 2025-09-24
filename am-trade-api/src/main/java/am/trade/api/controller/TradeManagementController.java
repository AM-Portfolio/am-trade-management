package am.trade.api.controller;

import am.trade.common.models.TradeDetails;
import am.trade.api.service.TradeManagementService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * REST controller for trade management operations
 * Provides endpoints for retrieving trade details by various time periods and portfolios
 */
@RestController
@RequestMapping("/api/v1/trades")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade Management", description = "API for trade management operations by time periods and portfolios")
public class TradeManagementController {

    private final TradeManagementService tradeManagementService;

    /**
     * Get trade details for a specific day
     * 
     * @param date The date to retrieve trade details for
     * @param portfolioId Optional portfolio ID to filter trades
     * @return Map of trade details grouped by portfolio ID
     */
    @Operation(summary = "Get trade details for a specific day")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/calendar/day")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByDay(
            @Parameter(description = "Date to retrieve trade details for (format: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Optional portfolio ID to filter trades") 
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Fetching trade details for date: {} and portfolio: {}", date, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByDay(date, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a specific month
     * 
     * @param year The year
     * @param month The month (1-12)
     * @param portfolioId Optional portfolio ID to filter trades
     * @return Map of trade details grouped by portfolio ID
     */
    @Operation(summary = "Get trade details for a specific month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/calendar/month")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByMonth(
            @Parameter(description = "Year") @RequestParam int year,
            @Parameter(description = "Month (1-12)") @RequestParam int month,
            @Parameter(description = "Optional portfolio ID to filter trades") 
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Fetching trade details for year: {}, month: {} and portfolio: {}", year, month, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByMonth(year, month, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a specific quarter
     * 
     * @param year The year
     * @param quarter The quarter (1-4)
     * @param portfolioId Optional portfolio ID to filter trades
     * @return Map of trade details grouped by portfolio ID
     */
    @Operation(summary = "Get trade details for a specific quarter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid quarter value (must be 1-4)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/calendar/quarter")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByQuarter(
            @Parameter(description = "Year") @RequestParam int year,
            @Parameter(description = "Quarter (1-4)") @RequestParam int quarter,
            @Parameter(description = "Optional portfolio ID to filter trades") 
            @RequestParam(required = false) String portfolioId) {
        
        if (quarter < 1 || quarter > 4) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Fetching trade details for year: {}, quarter: {} and portfolio: {}", year, quarter, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByQuarter(year, quarter, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a specific financial year
     * 
     * @param financialYear The financial year
     * @param portfolioId Optional portfolio ID to filter trades
     * @return Map of trade details grouped by portfolio ID
     */
    @Operation(summary = "Get trade details for a specific financial year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/calendar/financial-year")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByFinancialYear(
            @Parameter(description = "Financial year") @RequestParam int financialYear,
            @Parameter(description = "Optional portfolio ID to filter trades") 
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Fetching trade details for financial year: {} and portfolio: {}", financialYear, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByFinancialYear(financialYear, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get trade details for a custom date range
     * 
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @param portfolioId Optional portfolio ID to filter trades
     * @return Map of trade details grouped by portfolio ID
     */
    @Operation(summary = "Get trade details for a custom date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range (start date must be before or equal to end date)"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/calendar/custom")
    public ResponseEntity<Map<String, List<TradeDetails>>> getTradeDetailsByDateRange(
            @Parameter(description = "Start date of the range (format: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date of the range (format: yyyy-MM-dd)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Optional portfolio ID to filter trades") 
            @RequestParam(required = false) String portfolioId) {
        
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        
        log.info("Fetching trade details from {} to {} for portfolio: {}", startDate, endDate, portfolioId);
        Map<String, List<TradeDetails>> tradeDetails = tradeManagementService.getTradeDetailsByDateRange(startDate, endDate, portfolioId);
        return ResponseEntity.ok(tradeDetails);
    }

    /**
     * Get paginated trade details for a specific portfolio
     * 
     * @param portfolioId The portfolio ID to retrieve trade details for
     * @param pageable Pagination information
     * @return Page of trade details for the specified portfolio
     */
    @Operation(summary = "Get paginated trade details for a specific portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolio-details/{portfolioId}")
    public ResponseEntity<Page<TradeDetails>> getTradeDetailsByPortfolio(
            @Parameter(description = "Portfolio ID to retrieve trade details for") 
            @PathVariable String portfolioId,
            @Parameter(description = "Pagination information") 
            Pageable pageable) {
        
        log.info("Fetching paginated trade details for portfolio: {}", portfolioId);
        Page<TradeDetails> tradeDetails = tradeManagementService.getTradeDetailsByPortfolio(portfolioId, pageable);
        return ResponseEntity.ok(tradeDetails);
    }
}
