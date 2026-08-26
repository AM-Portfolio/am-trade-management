package am.trade.api.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import am.trade.api.service.TradeApiService;
import am.trade.common.models.TradeDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v2/trades")
@Tag(name = "Trade API v2", description = "Version 2 API for trade operations (Paginated)")
@RequiredArgsConstructor
@Slf4j
@Validated
public class TradeControllerV2 {

    private final TradeApiService tradeApiService;

    @Operation(summary = "Get paginated trade details by portfolio ID and symbols")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trade details found"),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/details/portfolio/{portfolioId}")
    public ResponseEntity<Page<TradeDetails>> getTradeDetailsByPortfolioAndSymbolsPage(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Parameter(description = "Symbols to filter by (optional)") @RequestParam(required = false) List<String> symbols,
            Pageable pageable) {

        log.info("Fetching v2 paginated trade details for portfolio: {} with symbols: {}", portfolioId, symbols);
        Page<TradeDetails> tradeDetails = tradeApiService.getTradeDetailsByPortfolioAndSymbolsPage(portfolioId, symbols, pageable);
        return ResponseEntity.ok(tradeDetails);
    }
}
