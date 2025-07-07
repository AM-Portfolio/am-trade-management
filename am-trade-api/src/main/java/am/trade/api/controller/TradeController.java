package am.trade.api.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import am.trade.api.service.TradeApiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/v1/trades")
@Tag(name = "Trade API", description = "API for trade operations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TradeController {
    
    private final TradeApiService tradeApiService;
    
    @Operation(summary = "Get trade details by portfolio ID and symbols")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details found"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/details/portfolio/{portfolioId}")
    public ResponseEntity<List<TradeDetails>> getTradeDetailsByPortfolioAndSymbols(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Parameter(description = "Symbols to filter by (optional)") @RequestParam(required = false) List<String> symbols) {
        
        log.info("Fetching trade details for portfolio: {} with symbols: {}", portfolioId, symbols);
        
        // Let the global exception handler manage exceptions
        List<TradeDetails> tradeDetails = tradeApiService.getTradeDetailsByPortfolioAndSymbols(portfolioId, symbols);
        return ResponseEntity.ok(tradeDetails);
    }
    
    @Operation(summary = "Add a new trade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trade created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/details")
    public ResponseEntity<TradeDetails> addTrade(
            @Parameter(description = "Trade details to add") @RequestBody @Validated TradeDetails tradeDetails) {
        
        log.info("Adding new trade for portfolio: {} and symbol: {}", 
                tradeDetails.getPortfolioId(), tradeDetails.getSymbol());
        
        // Let the global exception handler manage exceptions
        TradeDetails savedTrade = tradeApiService.addTrade(tradeDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTrade);
    }
    
    @Operation(summary = "Update an existing trade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data"),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/details/{tradeId}")
    public ResponseEntity<TradeDetails> updateTrade(
            @Parameter(description = "Trade ID") @PathVariable String tradeId,
            @Parameter(description = "Updated trade details") @RequestBody @Validated TradeDetails tradeDetails) {
        
        log.info("Updating trade with ID: {}", tradeId);
        
        // Ensure the trade ID in the path matches the trade ID in the request body
        if (!tradeId.equals(tradeDetails.getTradeId())) {
            log.error("Trade ID in path ({}) does not match trade ID in request body ({})", tradeId, tradeDetails.getTradeId());
            return ResponseEntity.badRequest().build();
        }
        
        // Validate required fields
        if (tradeDetails.getUserId() == null || tradeDetails.getUserId().isEmpty()) {
            log.error("User ID is required");
            return ResponseEntity.badRequest().build();
        }
        
        TradeDetails updatedTrade = tradeApiService.updateTrade(tradeId, tradeDetails);
        return ResponseEntity.ok(updatedTrade);
    }
    
    @Operation(summary = "Filter trades by multiple criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades found successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/filter")
    public ResponseEntity<Page<TradeDetails>> getTradesByFilters(
            @Parameter(description = "Portfolio IDs to filter by") @RequestParam(required = false) List<String> portfolioIds,
            @Parameter(description = "Symbols to filter by") @RequestParam(required = false) List<String> symbols,
            @Parameter(description = "Trade statuses to filter by") @RequestParam(required = false) List<TradeStatus> statuses,
            @Parameter(description = "Start date for filtering trades (format: yyyy-MM-dd)") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date for filtering trades (format: yyyy-MM-dd)") 
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Strategies to filter by") @RequestParam(required = false) List<String> strategies,
            Pageable pageable) {
        
        log.info("Filtering trades with criteria - portfolioIds: {}, symbols: {}, statuses: {}, startDate: {}, endDate: {}, strategies: {}", 
                portfolioIds, symbols, statuses, startDate, endDate, strategies);
        
        // Let the global exception handler manage exceptions
        Page<TradeDetails> filteredTrades = tradeApiService.getTradesByFilters(
                portfolioIds, symbols, statuses, startDate, endDate, strategies, pageable);
        return ResponseEntity.ok(filteredTrades);
    }
    
    @Operation(summary = "Add or update multiple trades")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/details/batch")
    public ResponseEntity<List<TradeDetails>> addOrUpdateTrades(
            @Parameter(description = "List of trade details to add or update") @RequestBody @Validated List<TradeDetails> tradeDetailsList) {
        
        log.info("Processing batch of {} trades", tradeDetailsList.size());
        
        // Let the global exception handler manage exceptions
        List<TradeDetails> savedTrades = tradeApiService.addOrUpdateTrades(tradeDetailsList);
        return ResponseEntity.ok(savedTrades);
    }
    
    @Operation(summary = "Get trade details by trade IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade details retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/details/by-ids")
    public ResponseEntity<List<TradeDetails>> getTradeDetailsByTradeIds(
            @Parameter(description = "List of trade IDs to fetch") @RequestBody List<String> tradeIds) {
        
        log.info("Fetching trade details for {} trade IDs", tradeIds.size());
        
        // Let the global exception handler manage exceptions
        List<TradeDetails> tradeDetails = tradeApiService.getTradeDetailsByTradeIds(tradeIds);
        return ResponseEntity.ok(tradeDetails);
    }
}
