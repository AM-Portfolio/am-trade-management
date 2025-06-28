package am.trade.api.controller;

import am.trade.api.service.PortfolioSummaryService;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.AssetAllocation;

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
 * REST controller for portfolio summary operations
 */
@RestController
@RequestMapping("/api/v1/portfolio-summary")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Portfolio Summary", description = "Portfolio summary and analytics operations")
public class PortfolioSummaryController {

    private final PortfolioSummaryService portfolioSummaryService;

    @Operation(summary = "Get portfolio summary by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio summary retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid portfolio ID"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{portfolioId}")
    public ResponseEntity<PortfolioModel> getPortfolioSummary(
            @Parameter(description = "Portfolio ID") 
            @PathVariable String portfolioId) {
        
        try {
            log.info("Fetching portfolio summary for portfolioId: {}", portfolioId);
            PortfolioModel portfolioSummary = portfolioSummaryService.getPortfolioSummary(portfolioId);
            return ResponseEntity.ok(portfolioSummary);
        } catch (IllegalArgumentException e) {
            log.error("Invalid portfolio ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching portfolio summary", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get asset allocation for a portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Asset allocation retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid portfolio ID"),
        @ApiResponse(responseCode = "404", description = "Portfolio not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{portfolioId}/asset-allocation")
    public ResponseEntity<List<AssetAllocation>> getAssetAllocation(
            @Parameter(description = "Portfolio ID") 
            @PathVariable String portfolioId) {
        
        try {
            log.info("Fetching asset allocation for portfolioId: {}", portfolioId);
            List<AssetAllocation> assetAllocation = portfolioSummaryService.getAssetAllocation(portfolioId);
            return ResponseEntity.ok(assetAllocation);
        } catch (IllegalArgumentException e) {
            log.error("Invalid portfolio ID: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching asset allocation", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Get portfolio performance over time")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio performance retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{portfolioId}/performance")
    public ResponseEntity<Map<LocalDate, Double>> getPortfolioPerformance(
            @Parameter(description = "Portfolio ID") 
            @PathVariable String portfolioId,
            @Parameter(description = "Start date in ISO format (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date in ISO format (YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        try {
            log.info("Fetching portfolio performance for portfolioId: {} from {} to {}", 
                     portfolioId, startDate, endDate);
            Map<LocalDate, Double> performance = portfolioSummaryService.getPortfolioPerformance(
                portfolioId, startDate, endDate);
            return ResponseEntity.ok(performance);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching portfolio performance", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(summary = "Compare multiple portfolios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Portfolio comparison retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/compare")
    public ResponseEntity<Map<String, PortfolioModel>> comparePortfolios(
            @Parameter(description = "List of portfolio IDs to compare") 
            @RequestParam List<String> portfolioIds) {
        
        try {
            log.info("Comparing portfolios: {}", portfolioIds);
            Map<String, PortfolioModel> comparisonResult = 
                portfolioSummaryService.comparePortfolios(portfolioIds);
            return ResponseEntity.ok(comparisonResult);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error comparing portfolios", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
