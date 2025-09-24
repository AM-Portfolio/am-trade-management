package am.trade.api.controller;

import am.trade.api.dto.DashboardPreferencesRequest;
import am.trade.api.dto.DashboardPreferencesResponse;
import am.trade.api.dto.ErrorResponse;
import am.trade.api.service.UserPreferencesService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for managing user preferences for dashboard and trade views
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
@Tag(name = "User Preferences", description = "API for managing user preferences for dashboard and trade views")
public class UserPreferencesController {

    private final UserPreferencesService userPreferencesService;

    @Operation(summary = "Get user dashboard preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences retrieved successfully",
                content = @Content(schema = @Schema(implementation = DashboardPreferencesResponse.class))),
        @ApiResponse(responseCode = "404", description = "Preferences not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/dashboard")
    public ResponseEntity<Object> getDashboardPreferences(@RequestParam String userId) {
        log.info("Fetching dashboard preferences for user: {}", userId);
        try {
            DashboardPreferencesResponse preferences = userPreferencesService.getDashboardPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (IllegalArgumentException e) {
            log.error("Preferences not found: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.notFound(
                    "Dashboard preferences not found", 
                    "/api/v1/preferences/dashboard")
                    .addDetail("No preferences found for user: " + userId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Save user dashboard preferences")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences saved successfully",
                content = @Content(schema = @Schema(implementation = DashboardPreferencesResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/dashboard")
    public ResponseEntity<Object> saveDashboardPreferences(
            @RequestParam String userId,
            @Valid @RequestBody DashboardPreferencesRequest request) {
        
        log.info("Saving dashboard preferences for user: {}", userId);
        try {
            DashboardPreferencesResponse preferences = userPreferencesService.saveDashboardPreferences(userId, request);
            return ResponseEntity.ok(preferences);
        } catch (IllegalArgumentException e) {
            log.error("Invalid preferences data: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/preferences/dashboard");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Reset user dashboard preferences to default")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Preferences reset successfully",
                content = @Content(schema = @Schema(implementation = DashboardPreferencesResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/dashboard/reset")
    public ResponseEntity<Object> resetDashboardPreferences(@RequestParam String userId) {
        log.info("Resetting dashboard preferences for user: {}", userId);
        try {
            DashboardPreferencesResponse preferences = userPreferencesService.resetDashboardPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (IllegalArgumentException e) {
            log.error("User not found: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.notFound(
                    e.getMessage(), 
                    "/api/v1/preferences/dashboard/reset");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
