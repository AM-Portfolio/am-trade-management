package am.trade.api.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import am.trade.models.dto.TradeDTO;
import am.trade.services.service.TradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST controller for trade operations
 */
@RestController
@RequestMapping("/api/v1/trades")
@Tag(name = "Trade API", description = "API for trade operations")
@RequiredArgsConstructor
@Slf4j
public class TradeController {
    
    private final TradeService tradeService;
    
    @Operation(summary = "Create a new trade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Trade created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<TradeDTO> createTrade(@Valid @RequestBody TradeDTO tradeDTO) {
        log.info("Creating new trade: {}", tradeDTO);
        TradeDTO createdTrade = tradeService.createTrade(tradeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTrade);
    }
    
    @Operation(summary = "Get a trade by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade found", 
                content = @Content(schema = @Schema(implementation = TradeDTO.class))),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TradeDTO> getTradeById(
            @Parameter(description = "Trade ID") @PathVariable String id) {
        log.info("Fetching trade with id: {}", id);
        return tradeService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Update a trade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trade updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid trade data provided"),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TradeDTO> updateTrade(
            @Parameter(description = "Trade ID") @PathVariable String id,
            @Valid @RequestBody TradeDTO tradeDTO) {
        log.info("Updating trade with id: {}", id);
        if (!tradeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tradeDTO.setId(id);
        TradeDTO updatedTrade = tradeService.updateTrade(id, tradeDTO);
        return ResponseEntity.ok(updatedTrade);
    }
    
    @Operation(summary = "Delete a trade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Trade deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Trade not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrade(
            @Parameter(description = "Trade ID") @PathVariable String id) {
        log.info("Deleting trade with id: {}", id);
        if (!tradeService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tradeService.deleteTrade(id);
        return ResponseEntity.noContent().build();
    }
    
    @Operation(summary = "Get all trades with pagination")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<Page<TradeDTO>> getAllTrades(Pageable pageable) {
        log.info("Fetching all trades with pagination");
        Page<TradeDTO> trades = tradeService.findAllTrades(pageable);
        return ResponseEntity.ok(trades);
    }
    
    @Operation(summary = "Get trades by symbol")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<List<TradeDTO>> getTradesBySymbol(
            @Parameter(description = "Symbol") @PathVariable String symbol) {
        log.info("Fetching trades for symbol: {}", symbol);
        List<TradeDTO> trades = tradeService.findBySymbol(symbol);
        return ResponseEntity.ok(trades);
    }
    
    @Operation(summary = "Get trades by date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades found"),
        @ApiResponse(responseCode = "400", description = "Invalid date range provided"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/date-range")
    public ResponseEntity<List<TradeDTO>> getTradesByDateRange(
            @Parameter(description = "Start date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("Fetching trades between {} and {}", startDate, endDate);
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        return ResponseEntity.ok(trades);
    }
    
    @Operation(summary = "Get trades by portfolio")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/portfolio/{portfolioId}")
    public ResponseEntity<Page<TradeDTO>> getTradesByPortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            Pageable pageable) {
        log.info("Fetching trades for portfolio: {}", portfolioId);
        Page<TradeDTO> trades = tradeService.findByPortfolioId(portfolioId, pageable);
        return ResponseEntity.ok(trades);
    }
    
    @Operation(summary = "Get trades by trader")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Trades found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/trader/{traderId}")
    public ResponseEntity<List<TradeDTO>> getTradesByTrader(
            @Parameter(description = "Trader ID") @PathVariable String traderId) {
        log.info("Fetching trades for trader: {}", traderId);
        List<TradeDTO> trades = tradeService.findByTraderId(traderId);
        return ResponseEntity.ok(trades);
    }
}
