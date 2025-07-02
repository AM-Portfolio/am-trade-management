package am.trade.api.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import am.trade.persistence.service.TradeDetailsService;
import am.trade.services.service.TradeManagementService;
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
    
    private final TradeManagementService tradeManagementService;
    private final TradeDetailsService tradeDetailsService;
    
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
        
        try {
            List<TradeDetails> tradeDetails = tradeManagementService.getTradesBySymbols(portfolioId, symbols);
            return ResponseEntity.ok(tradeDetails);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error fetching trade details", e);
            return ResponseEntity.internalServerError().build();
        }
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
        
        log.info("Adding new trade for portfolio: {} and symbol: {} by user: {}", 
                tradeDetails.getPortfolioId(), tradeDetails.getSymbol(), tradeDetails.getUserId());
        
        // Validate required fields
        if (tradeDetails.getUserId() == null || tradeDetails.getUserId().isEmpty()) {
            log.error("User ID is required");
            return ResponseEntity.badRequest().build();
        }

        tradeDetails.setTradeId(UUID.randomUUID().toString());
        
        try {
            // Handle trade analysis images if present
            if (tradeDetails.getAttachments() != null && !tradeDetails.getAttachments().isEmpty()) {
                log.info("Trade analysis images provided for trade: {} images", tradeDetails.getAttachments().size());
                // You might want to validate the image format/size here
                // or process the images (resize, compress, etc.) before saving
            }
            
            TradeDetails savedTrade = tradeDetailsService.saveTradeDetails(tradeDetails);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedTrade);
        } catch (IllegalArgumentException e) {
            log.error("Invalid trade data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error adding trade", e);
            return ResponseEntity.internalServerError().build();
        }
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
        
        log.info("Updating trade with ID: {} for portfolio: {} by user: {}", 
                tradeId, tradeDetails.getPortfolioId(), tradeDetails.getUserId());
        
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
        
        try {
            // Check if the trade exists
            Optional<TradeDetails> existingTrade = tradeDetailsService.findModelByTradeId(tradeId);
            if (existingTrade.isEmpty()) {
                log.error("Trade with ID {} not found", tradeId);
                return ResponseEntity.notFound().build();
            }
            
            // Handle trade analysis images if present
            if (tradeDetails.getAttachments() != null && !tradeDetails.getAttachments().isEmpty()) {
                log.info("Updated trade analysis images provided for trade {}: {} images", 
                        tradeId, tradeDetails.getAttachments().size());
                // You might want to validate the image format/size here
                // or process the images (resize, compress, etc.) before saving
            } else if (existingTrade.get().getAttachments() != null && !existingTrade.get().getAttachments().isEmpty()) {
                // Preserve existing images if not provided in update
                tradeDetails.setAttachments(existingTrade.get().getAttachments());
                log.info("Preserving existing {} attachments for trade {}", 
                        existingTrade.get().getAttachments().size(), tradeId);
            }
            
            // Update the trade
            TradeDetails updatedTrade = tradeDetailsService.saveTradeDetails(tradeDetails);
            return ResponseEntity.ok(updatedTrade);
        } catch (IllegalArgumentException e) {
            log.error("Invalid trade data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating trade", e);
            return ResponseEntity.internalServerError().build();
        }
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
        
        try {
            Page<TradeDetails> filteredTrades = tradeManagementService.getTradesByFilters(
                    portfolioIds, symbols, statuses, startDate, endDate, strategies, pageable);
            return ResponseEntity.ok(filteredTrades);
        } catch (IllegalArgumentException e) {
            log.error("Invalid filter parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error filtering trades", e);
            return ResponseEntity.internalServerError().build();
        }
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
        
        // Validate required fields for all trades in batch
        boolean hasInvalidData = tradeDetailsList.stream()
                .anyMatch(trade -> trade.getUserId() == null || trade.getUserId().isEmpty());
        
        if (hasInvalidData) {
            log.error("User ID is required for all trades in batch");
            return ResponseEntity.badRequest().build();
        }
        
        try {
            // Log information about trade analysis images in the batch
            long tradesWithImages = tradeDetailsList.stream()
                    .filter(trade -> trade.getAttachments() != null && !trade.getAttachments().isEmpty())
                    .count();
            
            if (tradesWithImages > 0) {
                log.info("{} out of {} trades in batch contain attachments", tradesWithImages, tradeDetailsList.size());
            }
            
            List<TradeDetails> savedTrades = tradeDetailsService.saveAllTradeDetails(tradeDetailsList);
            return ResponseEntity.ok(savedTrades);
        } catch (IllegalArgumentException e) {
            log.error("Invalid trade data in batch: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error processing trade batch", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
