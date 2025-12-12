package am.trade.api.controller;

import am.trade.api.dto.summary.DailyPerformance;
import am.trade.api.dto.summary.TimingAnalysis;
import am.trade.api.dto.summary.TradePerformanceSummary;
import am.trade.api.service.TradePerformanceService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/performance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Trade Performance Analysis", description = "API for analyzing trade performance, identifying best days, and timing analysis")
public class TradePerformanceController {

    private final TradePerformanceService tradePerformanceService;

    @Operation(summary = "Get general performance summary")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Performance summary retrieved successfully", content = @Content(schema = @Schema(implementation = TradePerformanceSummary.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/summary")
    public ResponseEntity<TradePerformanceSummary> getPerformanceSummary(
            @Parameter(description = "Portfolio ID") @RequestParam String portfolioId,
            @Parameter(description = "Start date (optional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (optional)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Fetching performance summary for portfolio: {}", portfolioId);
        return ResponseEntity.ok(tradePerformanceService.getPerformanceSummary(portfolioId, startDate, endDate));
    }

    @Operation(summary = "Get daily performance ranking (best/worst days)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily performance retrieved successfully", content = @Content(schema = @Schema(implementation = DailyPerformance.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/daily")
    public ResponseEntity<List<DailyPerformance>> getDailyPerformance(
            @Parameter(description = "Portfolio ID") @RequestParam String portfolioId,
            @Parameter(description = "Limit number of days to return (default 10)") @RequestParam(required = false, defaultValue = "10") int limit) {

        log.info("Fetching daily performance for portfolio: {}, limit: {}", portfolioId, limit);
        return ResponseEntity.ok(tradePerformanceService.getDailyPerformance(portfolioId, limit));
    }

    @Operation(summary = "Get timing analysis (performance by hour of day)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Timing analysis retrieved successfully", content = @Content(schema = @Schema(implementation = TimingAnalysis.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/timing")
    public ResponseEntity<TimingAnalysis> getTimingAnalysis(
            @Parameter(description = "Portfolio ID") @RequestParam String portfolioId) {

        log.info("Fetching timing analysis for portfolio: {}", portfolioId);
        return ResponseEntity.ok(tradePerformanceService.getTimingAnalysis(portfolioId));
    }
}
