package am.trade.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import am.trade.common.models.TradeDetails;
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
public class TradeController {
    
    private final TradeManagementService tradeManagementService;
    
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
}
