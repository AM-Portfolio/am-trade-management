package am.trade.api.controller;

import java.util.List;

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

import am.trade.api.dto.ErrorResponse;
import am.trade.api.dto.FavoriteFilterRequest;
import am.trade.api.dto.FavoriteFilterResponse;
import am.trade.api.dto.MetricsFilterRequest;
import am.trade.api.dto.MetricsResponse;
import am.trade.api.service.FavoriteFilterService;
import am.trade.api.service.UserTradeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/filters")
@RequiredArgsConstructor
@Tag(name = "Favorite Filters", description = "API for managing user's favorite metric filters")
public class FavoriteFilterController {

    private final FavoriteFilterService favoriteFilterService;
    private final UserTradeService userTradeService;

    @Operation(summary = "Create a new favorite filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Filter created successfully",
                content = @Content(schema = @Schema(implementation = FavoriteFilterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<FavoriteFilterResponse> createFilter(
            @RequestParam String userId,
            @Valid @RequestBody FavoriteFilterRequest request) {
        
        log.info("Creating favorite filter for user: {}", userId);
        try {
            FavoriteFilterResponse response = favoriteFilterService.createFilter(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(summary = "Update an existing favorite filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filter updated successfully",
                content = @Content(schema = @Schema(implementation = FavoriteFilterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "404", description = "Filter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{filterId}")
    public ResponseEntity<FavoriteFilterResponse> updateFilter(
            @RequestParam String userId,
            @PathVariable String filterId,
            @Valid @RequestBody FavoriteFilterRequest request) {
        
        log.info("Updating favorite filter: {} for user: {}", filterId, userId);
        try {
            FavoriteFilterResponse response = favoriteFilterService.updateFilter(userId, filterId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters or filter not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get all favorite filters for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filters retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<FavoriteFilterResponse>> getUserFilters(
            @RequestParam String userId) {
        
        log.info("Getting all favorite filters for user: {}", userId);
        List<FavoriteFilterResponse> filters = favoriteFilterService.getUserFilters(userId);
        return ResponseEntity.ok(filters);
    }

    @Operation(summary = "Get a specific favorite filter by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filter retrieved successfully",
                content = @Content(schema = @Schema(implementation = FavoriteFilterResponse.class))),
        @ApiResponse(responseCode = "404", description = "Filter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{filterId}")
    public ResponseEntity<FavoriteFilterResponse> getFilterById(
            @RequestParam String userId,
            @PathVariable String filterId) {
        
        log.info("Getting favorite filter: {} for user: {}", filterId, userId);
        try {
            FavoriteFilterResponse filter = favoriteFilterService.getFilterById(userId, filterId);
            return ResponseEntity.ok(filter);
        } catch (IllegalArgumentException e) {
            log.error("Filter not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a favorite filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Filter deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Filter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{filterId}")
    public ResponseEntity<Void> deleteFilter(
            @RequestParam String userId,
            @PathVariable String filterId) {
        
        log.info("Deleting favorite filter: {} for user: {}", filterId, userId);
        try {
            boolean deleted = favoriteFilterService.deleteFilter(userId, filterId);
            return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Filter not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get the user's default filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Default filter retrieved successfully",
                content = @Content(schema = @Schema(implementation = FavoriteFilterResponse.class))),
        @ApiResponse(responseCode = "404", description = "No default filter found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/default")
    public ResponseEntity<FavoriteFilterResponse> getDefaultFilter(
            @RequestParam String userId) {
        
        log.info("Getting default filter for user: {}", userId);
        FavoriteFilterResponse filter = favoriteFilterService.getDefaultFilter(userId);
        return filter != null ? ResponseEntity.ok(filter) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Set a filter as the default")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Filter set as default successfully",
                content = @Content(schema = @Schema(implementation = FavoriteFilterResponse.class))),
        @ApiResponse(responseCode = "404", description = "Filter not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{filterId}/default")
    public ResponseEntity<FavoriteFilterResponse> setDefaultFilter(
            @RequestParam String userId,
            @PathVariable String filterId) {
        
        log.info("Setting filter: {} as default for user: {}", filterId, userId);
        try {
            FavoriteFilterResponse response = favoriteFilterService.setDefaultFilter(userId, filterId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Filter not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @Operation(summary = "Save current filter as favorite")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Filter saved successfully",
                content = @Content(schema = @Schema(implementation = FavoriteFilterResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/save-current")
    public ResponseEntity<FavoriteFilterResponse> saveCurrentFilter(
            @RequestParam String userId,
            @RequestParam String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "false") boolean isDefault,
            @Valid @RequestBody MetricsFilterRequest currentFilter) {
        
        log.info("Saving current filter as favorite for user: {}", userId);
        try {
            // Convert MetricsFilterRequest to FavoriteFilterRequest
            FavoriteFilterRequest request = FavoriteFilterRequest.builder()
                    .name(name)
                    .description(description)
                    .isDefault(isDefault)
                    .filterConfig(convertToFilterConfig(currentFilter))
                    .build();
            
            FavoriteFilterResponse response = favoriteFilterService.createFilter(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @Operation(summary = "Get trade metrics using a saved favorite filter")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Metrics retrieved successfully",
                content = @Content(schema = @Schema(implementation = MetricsResponse.class))),
        @ApiResponse(responseCode = "404", description = "Filter not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/apply/{filterId}")
    public ResponseEntity<Object> getMetricsWithFavoriteFilter(
            @RequestParam String userId,
            @PathVariable String filterId,
            @RequestParam(required = false) List<String> portfolioIds) {
        
        log.info("Fetching trade metrics with favorite filter: {} for user: {}", filterId, userId);
        try {
            // Delegate to the UserTradeService
            MetricsResponse metricsResponse = userTradeService.getMetricsWithFavoriteFilter(userId, filterId, portfolioIds);
            return ResponseEntity.ok(metricsResponse);
        } catch (IllegalArgumentException e) {
            log.error("Invalid request parameters or filter not found: {}", e.getMessage());
            
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/filters/apply/" + filterId)
                    .addDetail("The requested filter ID '" + filterId + "' was not found")
                    .addDetail("No default filter exists for user '" + userId + "'")
                    .addDetail("No portfolioIds were provided as fallback")
                    .addDetail("Please provide portfolioIds or create a filter first");
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * Convert a MetricsFilterRequest to MetricsFilterConfig
     */
    private am.trade.common.models.MetricsFilterConfig convertToFilterConfig(MetricsFilterRequest request) {
        am.trade.common.models.MetricsFilterConfig.MetricsFilterConfigBuilder builder = 
                am.trade.common.models.MetricsFilterConfig.builder();
        
        if (request.getPortfolioIds() != null) {
            builder.portfolioIds(new java.util.ArrayList<>(request.getPortfolioIds()));
        }
        
        if (request.getMetricTypes() != null) {
            builder.metricTypes(new java.util.ArrayList<>(request.getMetricTypes()));
        }
        
        if (request.getInstruments() != null) {
            builder.instruments(new java.util.ArrayList<>(request.getInstruments()));
        }
        
        // Convert specific filter types to Map<String, Object>
        // In a real implementation, you would use Jackson or similar to convert these objects
        // For now, we'll just set them to null
        
        return builder.build();
    }
}
