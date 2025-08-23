package am.trade.api.controller;

import am.trade.api.dto.ComparisonRequest;
import am.trade.api.dto.ComparisonResponse;
import am.trade.api.dto.ErrorResponse;
import am.trade.api.service.TradeComparisonService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for comparing trade performance across different dimensions
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/comparison")
@RequiredArgsConstructor
@Tag(name = "Trade Comparison", description = "API for comparing trade performance across different dimensions")
public class TradeComparisonController {

    private final TradeComparisonService tradeComparisonService;

    @Operation(summary = "Compare trade performance across time periods, portfolios, or strategies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comparison completed successfully",
                content = @Content(schema = @Schema(implementation = ComparisonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Object> compareTradePerformance(@Valid @RequestBody ComparisonRequest request) {
        log.info("Comparing trade performance for user: {}", request.getUserId());
        try {
            ComparisonResponse response = tradeComparisonService.compareTradePerformance(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid comparison request: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/comparison");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Compare performance between two specific portfolios")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comparison completed successfully",
                content = @Content(schema = @Schema(implementation = ComparisonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolios")
    public ResponseEntity<Object> comparePortfolios(
            @RequestParam String userId,
            @RequestParam String portfolioId1,
            @RequestParam String portfolioId2,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Comparing portfolios {} and {} for user: {}", portfolioId1, portfolioId2, userId);
        try {
            ComparisonResponse response = tradeComparisonService.comparePortfolios(
                    userId, portfolioId1, portfolioId2, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid portfolio comparison request: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/comparison/portfolios");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Compare performance between two time periods")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comparison completed successfully",
                content = @Content(schema = @Schema(implementation = ComparisonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/time-periods")
    public ResponseEntity<Object> compareTimePeriods(
            @RequestParam String userId,
            @RequestParam String period1Start,
            @RequestParam String period1End,
            @RequestParam String period2Start,
            @RequestParam String period2End,
            @RequestParam(required = false) String portfolioId) {
        
        log.info("Comparing time periods for user: {}", userId);
        try {
            ComparisonResponse response = tradeComparisonService.compareTimePeriods(
                    userId, period1Start, period1End, period2Start, period2End, portfolioId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid time period comparison request: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/comparison/time-periods");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Compare performance between strategies")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Comparison completed successfully",
                content = @Content(schema = @Schema(implementation = ComparisonResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/strategies")
    public ResponseEntity<Object> compareStrategies(
            @RequestParam String userId,
            @RequestParam String strategy1,
            @RequestParam String strategy2,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        
        log.info("Comparing strategies {} and {} for user: {}", strategy1, strategy2, userId);
        try {
            ComparisonResponse response = tradeComparisonService.compareStrategies(
                    userId, strategy1, strategy2, startDate, endDate);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid strategy comparison request: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/comparison/strategies");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
