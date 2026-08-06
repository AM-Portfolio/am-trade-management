package am.trade.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import am.trade.api.dto.PortfolioCreateRequest;
import am.trade.api.dto.PortfolioUpdateRequest;
import am.trade.api.service.PortfolioApiService;
import am.trade.common.models.PortfolioModel;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/v1/portfolios")
@Tag(name = "Portfolio API", description = "API for portfolio operations")
@RequiredArgsConstructor
@Slf4j
@Validated
public class PortfolioController {

    private final PortfolioApiService portfolioApiService;

    @Operation(summary = "Create a new portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Portfolio created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<PortfolioModel> createPortfolio(
            @Parameter(description = "Portfolio creation request") @RequestBody @Valid PortfolioCreateRequest request) {
        log.info("Creating a new portfolio");
        PortfolioModel created = portfolioApiService.createPortfolio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Update an existing portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Portfolio updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{portfolioId}")
    public ResponseEntity<PortfolioModel> updatePortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Parameter(description = "Portfolio update request") @RequestBody @Valid PortfolioUpdateRequest request) {
        log.info("Updating portfolio with ID: {}", portfolioId);
        PortfolioModel updated = portfolioApiService.updatePortfolio(portfolioId, request);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Delete a portfolio")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Portfolio deleted successfully"),
            @ApiResponse(responseCode = "403", description = "Unauthorized access"),
            @ApiResponse(responseCode = "404", description = "Portfolio not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{portfolioId}")
    public ResponseEntity<Void> deletePortfolio(
            @Parameter(description = "Portfolio ID") @PathVariable String portfolioId,
            @Parameter(description = "Delete all associated trades") @RequestParam(defaultValue = "false") boolean deleteTrades) {
        log.info("Deleting portfolio with ID: {}, deleteTrades: {}", portfolioId, deleteTrades);
        portfolioApiService.deletePortfolio(portfolioId, deleteTrades);
        return ResponseEntity.noContent().build();
    }
}
