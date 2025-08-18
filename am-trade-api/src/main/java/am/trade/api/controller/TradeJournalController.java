package am.trade.api.controller;

import am.trade.api.dto.ErrorResponse;
import am.trade.api.dto.TradeJournalEntryRequest;
import am.trade.api.dto.TradeJournalEntryResponse;
import am.trade.api.service.TradeJournalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing trade journal entries
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/journal")
@RequiredArgsConstructor
@Tag(name = "Trade Journal", description = "API for managing trade journal entries")
public class TradeJournalController {

    private final TradeJournalService tradeJournalService;

    @Operation(summary = "Create a new journal entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Journal entry created successfully",
                content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Object> createJournalEntry(
            @Valid @RequestBody TradeJournalEntryRequest request) {
        
        log.info("Creating journal entry for user: {}", request.getUserId());
        try {
            TradeJournalEntryResponse response = tradeJournalService.createJournalEntry(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid journal entry data: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/journal");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Get journal entry by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Journal entry retrieved successfully",
                content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
        @ApiResponse(responseCode = "404", description = "Journal entry not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{entryId}")
    public ResponseEntity<Object> getJournalEntry(@PathVariable String entryId) {
        log.info("Fetching journal entry with ID: {}", entryId);
        try {
            TradeJournalEntryResponse response = tradeJournalService.getJournalEntry(entryId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Journal entry not found: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.notFound(
                    "Journal entry not found", 
                    "/api/v1/journal/" + entryId)
                    .addDetail("No journal entry found with ID: " + entryId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Get journal entries for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Journal entries retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<TradeJournalEntryResponse>> getJournalEntriesByUser(
            @PathVariable String userId,
            Pageable pageable) {
        
        log.info("Fetching journal entries for user: {}", userId);
        Page<TradeJournalEntryResponse> entries = tradeJournalService.getJournalEntriesByUser(userId, pageable);
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Get journal entries for a specific trade")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Journal entries retrieved successfully"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<TradeJournalEntryResponse>> getJournalEntriesByTrade(@PathVariable String tradeId) {
        log.info("Fetching journal entries for trade: {}", tradeId);
        List<TradeJournalEntryResponse> entries = tradeJournalService.getJournalEntriesByTrade(tradeId);
        return ResponseEntity.ok(entries);
    }

    @Operation(summary = "Get journal entries by date range")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Journal entries retrieved successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid date range",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/date-range")
    public ResponseEntity<Object> getJournalEntriesByDateRange(
            @RequestParam String userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        
        log.info("Fetching journal entries for user: {} between {} and {}", userId, startDate, endDate);
        
        if (endDate.isBefore(startDate)) {
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    "End date cannot be before start date", 
                    "/api/v1/journal/date-range");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            Page<TradeJournalEntryResponse> entries = 
                    tradeJournalService.getJournalEntriesByDateRange(userId, startDate, endDate, pageable);
            return ResponseEntity.ok(entries);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching journal entries: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(), 
                    "/api/v1/journal/date-range");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Update a journal entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Journal entry updated successfully",
                content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404", description = "Journal entry not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{entryId}")
    public ResponseEntity<Object> updateJournalEntry(
            @PathVariable String entryId,
            @Valid @RequestBody TradeJournalEntryRequest request) {
        
        log.info("Updating journal entry with ID: {}", entryId);
        try {
            TradeJournalEntryResponse response = tradeJournalService.updateJournalEntry(entryId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating journal entry: {}", e.getMessage());
            
            if (e.getMessage().contains("not found")) {
                ErrorResponse errorResponse = ErrorResponse.notFound(
                        e.getMessage(), 
                        "/api/v1/journal/" + entryId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            } else {
                ErrorResponse errorResponse = ErrorResponse.badRequest(
                        e.getMessage(), 
                        "/api/v1/journal/" + entryId);
                return ResponseEntity.badRequest().body(errorResponse);
            }
        }
    }

    @Operation(summary = "Delete a journal entry")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Journal entry deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Journal entry not found",
                content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{entryId}")
    public ResponseEntity<Object> deleteJournalEntry(@PathVariable String entryId) {
        log.info("Deleting journal entry with ID: {}", entryId);
        try {
            tradeJournalService.deleteJournalEntry(entryId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Journal entry not found: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.notFound(
                    e.getMessage(), 
                    "/api/v1/journal/" + entryId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }
}
