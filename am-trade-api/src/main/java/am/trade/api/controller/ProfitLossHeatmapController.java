package am.trade.api.controller;

import am.trade.common.models.ProfitLossHeatmapData;
import am.trade.services.service.ProfitLossHeatmapService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for profit/loss heatmap operations
 */
@RestController
@RequestMapping("/api/v1/heatmap")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Profit/Loss Heatmap", description = "Profit/Loss heatmap operations with various granularities")
public class ProfitLossHeatmapController {

    private final ProfitLossHeatmapService profitLossHeatmapService;

    @Operation(summary = "Get yearly profit/loss heatmap data")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Heatmap data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid portfolio ID"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/yearly")
    public ResponseEntity<ProfitLossHeatmapData> getYearlyHeatmap(
            @Parameter(description = "Portfolio ID") 
            @RequestParam String portfolioId,
            @Parameter(description = "Whether to include trade details in the response") 
            @RequestParam(defaultValue = "false") boolean includeTradeDetails) {
        
        try {
            log.info("Fetching yearly profit/loss heatmap for portfolio: {}", portfolioId);
            ProfitLossHeatmapData heatmapData = profitLossHeatmapService.getYearlyHeatmap(portfolioId, includeTradeDetails);
            return ResponseEntity.ok(heatmapData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get monthly profit/loss heatmap data for a specific financial year")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Heatmap data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid portfolio ID or financial year"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/monthly")
    public ResponseEntity<ProfitLossHeatmapData> getMonthlyHeatmap(
            @Parameter(description = "Portfolio ID") 
            @RequestParam String portfolioId,
            @Parameter(description = "Financial year (e.g., 2025 for FY 2025-26)") 
            @RequestParam int financialYear,
            @Parameter(description = "Whether to include trade details in the response") 
            @RequestParam(defaultValue = "false") boolean includeTradeDetails) {
        
        try {
            log.info("Fetching monthly profit/loss heatmap for portfolio: {} for financial year: {}", portfolioId, financialYear);
            ProfitLossHeatmapData heatmapData = profitLossHeatmapService.getMonthlyHeatmap(portfolioId, financialYear, includeTradeDetails);
            return ResponseEntity.ok(heatmapData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Get daily profit/loss heatmap data for a specific month")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Heatmap data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid portfolio ID, year, or month"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/daily")
    public ResponseEntity<ProfitLossHeatmapData> getDailyHeatmap(
            @Parameter(description = "Portfolio ID") 
            @RequestParam String portfolioId,
            @Parameter(description = "Year") 
            @RequestParam int year,
            @Parameter(description = "Month (1-12)") 
            @RequestParam int month,
            @Parameter(description = "Whether to include trade details in the response") 
            @RequestParam(defaultValue = "false") boolean includeTradeDetails) {
        
        try {
            log.info("Fetching daily profit/loss heatmap for portfolio: {} for {}-{}", portfolioId, year, month);
            ProfitLossHeatmapData heatmapData = profitLossHeatmapService.getDailyHeatmap(portfolioId, year, month, includeTradeDetails);
            return ResponseEntity.ok(heatmapData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching daily heatmap data", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get unified profit/loss heatmap data with specified granularity")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Heatmap data retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<ProfitLossHeatmapData> getHeatmap(
            @Parameter(description = "Portfolio ID") 
            @RequestParam String portfolioId,
            @Parameter(description = "Granularity (YEARLY, MONTHLY, DAILY)") 
            @RequestParam String granularity,
            @Parameter(description = "Financial year (required for MONTHLY granularity)") 
            @RequestParam(required = false) Integer financialYear,
            @Parameter(description = "Year (required for DAILY granularity)") 
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Month (1-12, required for DAILY granularity)") 
            @RequestParam(required = false) Integer month,
            @Parameter(description = "Whether to include trade details in the response") 
            @RequestParam(defaultValue = "false") boolean includeTradeDetails) {
        
        try {
            log.info("Fetching profit/loss heatmap for portfolio: {} with granularity: {}", portfolioId, granularity);
            
            ProfitLossHeatmapData heatmapData;
            
            switch (granularity.toUpperCase()) {
                case "YEARLY":
                    heatmapData = profitLossHeatmapService.getYearlyHeatmap(portfolioId, includeTradeDetails);
                    break;
                case "MONTHLY":
                    if (financialYear == null) {
                        return ResponseEntity.badRequest().build();
                    }
                    heatmapData = profitLossHeatmapService.getMonthlyHeatmap(portfolioId, financialYear, includeTradeDetails);
                    break;
                case "DAILY":
                    if (year == null || month == null) {
                        return ResponseEntity.badRequest().build();
                    }
                    heatmapData = profitLossHeatmapService.getDailyHeatmap(portfolioId, year, month, includeTradeDetails);
                    break;
                default:
                    log.error("Invalid granularity: {}", granularity);
                    return ResponseEntity.badRequest().build();
            }
            
            return ResponseEntity.ok(heatmapData);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching heatmap data", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
