package am.trade.api.controller;

import am.trade.api.dto.*;
import am.trade.api.service.TradeJournalService;
import am.trade.common.models.Attachment;
import am.trade.common.models.PostTradeReview;
import am.trade.common.models.PreTradePlan;
import am.trade.common.models.TradeExecution;
import com.am.security.context.UserContext;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing trade journal entries
 */
@Slf4j
@RestController
@RequestMapping("/v1/journal")
@RequiredArgsConstructor
@Tag(name = "Trade Journal", description = "API for managing trade journal entries")
public class TradeJournalController {

    private final TradeJournalService tradeJournalService;

    @Operation(summary = "Create a new journal entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Journal entry created successfully", content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<Object> createJournalEntry(
            @Valid @RequestBody TradeJournalEntryRequest request) {

        log.info("Creating journal entry for user: {}", UserContext.getUserIdOrThrow());
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

    @Operation(summary = "Get journal entries for a user with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Journal entries retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/user")
    public ResponseEntity<Page<TradeJournalEntryResponse>> getJournalEntriesByUser(
            @RequestParam(required = false) String journalStatus,
            @RequestParam(required = false) String entryType,
            @RequestParam(required = false) String symbol,
            @RequestParam(required = false) String setup,
            @RequestParam(required = false) String folderId,
            @RequestParam(required = false) String playbookId,
            @RequestParam(required = false) List<String> tagIds,
            @RequestParam(required = false) String tradeId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String q,
            Pageable pageable) {

        String userId = UserContext.getUserIdOrThrow();
        log.info("Fetching journal entries for user: {} with filters", userId);

        boolean hasFilters = journalStatus != null || entryType != null || symbol != null || setup != null
                || folderId != null || playbookId != null || (tagIds != null && !tagIds.isEmpty())
                || tradeId != null || startDate != null || endDate != null || q != null;

        Page<TradeJournalEntryResponse> entries = hasFilters
                ? tradeJournalService.searchJournalEntries(userId, journalStatus, entryType, symbol, setup,
                        folderId, playbookId, tagIds, tradeId, startDate, endDate, q, pageable)
                : tradeJournalService.getJournalEntriesByUser(userId, pageable);

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
            @ApiResponse(responseCode = "400", description = "Invalid date range", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/date-range")
    public ResponseEntity<Object> getJournalEntriesByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable) {
        String userId = UserContext.getUserIdOrThrow();
        log.info("Fetching journal entries for user: {} between {} and {}", userId, startDate, endDate);

        if (endDate.isBefore(startDate)) {
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    "End date cannot be before start date",
                    "/api/v1/journal/date-range");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            Page<TradeJournalEntryResponse> entries = tradeJournalService.getJournalEntriesByDateRange(userId,
                    startDate, endDate, pageable);
            return ResponseEntity.ok(entries);
        } catch (IllegalArgumentException e) {
            log.error("Error fetching journal entries: {}", e.getMessage());
            ErrorResponse errorResponse = ErrorResponse.badRequest(
                    e.getMessage(),
                    "/api/v1/journal/date-range");
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Operation(summary = "Get journal summary metrics")
    @GetMapping("/summary")
    public ResponseEntity<Object> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userId = UserContext.getUserIdOrThrow();
        try {
            return ResponseEntity.ok(tradeJournalService.getSummary(userId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/summary"));
        }
    }

    @Operation(summary = "Get mistake analysis summary")
    @GetMapping("/mistakes/summary")
    public ResponseEntity<Object> getMistakesSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userId = UserContext.getUserIdOrThrow();
        try {
            return ResponseEntity.ok(tradeJournalService.getMistakesSummary(userId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/mistakes/summary"));
        }
    }

    @Operation(summary = "Get lessons learned")
    @GetMapping("/lessons")
    public ResponseEntity<Object> getLessons(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "10") int limit) {
        String userId = UserContext.getUserIdOrThrow();
        try {
            return ResponseEntity.ok(tradeJournalService.getLessons(userId, startDate, endDate, limit));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/lessons"));
        }
    }

    @Operation(summary = "Get plan adherence metrics")
    @GetMapping("/adherence")
    public ResponseEntity<Object> getAdherence(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userId = UserContext.getUserIdOrThrow();
        try {
            return ResponseEntity.ok(tradeJournalService.getAdherence(userId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/adherence"));
        }
    }

    @Operation(summary = "Get journal report card")
    @GetMapping("/report-card")
    public ResponseEntity<Object> getReportCard(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userId = UserContext.getUserIdOrThrow();
        try {
            return ResponseEntity.ok(tradeJournalService.getReportCard(userId, startDate, endDate));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/report-card"));
        }
    }

    @Operation(summary = "Export journal entries as CSV")
    @GetMapping("/export")
    public ResponseEntity<Object> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        String userId = UserContext.getUserIdOrThrow();
        try {
            String csv = tradeJournalService.exportCsv(userId, startDate, endDate);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"journal-export.csv\"")
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(csv);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/export"));
        }
    }

    @Operation(summary = "Create journal entries from existing trades")
    @PostMapping("/from-trades")
    public ResponseEntity<Object> createFromTrades(@Valid @RequestBody FromTradesRequest request) {
        try {
            List<TradeJournalEntryResponse> created = tradeJournalService.createFromTrades(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/from-trades"));
        }
    }

    @Operation(summary = "Import journal stubs from CSV (JSON body with csv text)")
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> importCsvJson(@RequestBody JournalImportRequest request) {
        try {
            String csv = request != null ? request.getCsv() : null;
            Boolean createStubs = request != null ? request.getCreateJournalStubs() : Boolean.TRUE;
            return ResponseEntity.ok(tradeJournalService.importCsv(csv, createStubs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/import"));
        }
    }

    @Operation(summary = "Import journal stubs from CSV file upload")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Object> importCsvFile(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            @RequestParam(required = false, defaultValue = "true") Boolean createJournalStubs) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("file is required");
            }
            String csv = new String(file.getBytes(), java.nio.charset.StandardCharsets.UTF_8);
            return ResponseEntity.ok(tradeJournalService.importCsv(csv, createJournalStubs));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/import"));
        } catch (java.io.IOException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.badRequest("Failed to read uploaded file", "/api/v1/journal/import"));
        }
    }

    @Operation(summary = "Bulk archive journal entries")
    @PostMapping("/bulk/archive")
    public ResponseEntity<Object> bulkArchive(@Valid @RequestBody JournalBulkRequest request) {
        String userId = UserContext.getUserIdOrThrow();
        int count = tradeJournalService.bulkArchive(userId, request.getEntryIds());
        return ResponseEntity.ok(java.util.Map.of("archived", count));
    }

    @Operation(summary = "Bulk delete journal entries")
    @PostMapping("/bulk/delete")
    public ResponseEntity<Object> bulkDelete(@Valid @RequestBody JournalBulkRequest request) {
        String userId = UserContext.getUserIdOrThrow();
        int count = tradeJournalService.bulkDelete(userId, request.getEntryIds());
        return ResponseEntity.ok(java.util.Map.of("deleted", count));
    }

    @Operation(summary = "Bulk add tags to journal entries")
    @PostMapping("/bulk/tags")
    public ResponseEntity<Object> bulkAddTags(@Valid @RequestBody JournalBulkRequest request) {
        String userId = UserContext.getUserIdOrThrow();
        int count = tradeJournalService.bulkAddTags(userId, request.getEntryIds(), request.getTagIds());
        return ResponseEntity.ok(java.util.Map.of("updated", count));
    }

    @Operation(summary = "Get journal entry by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Journal entry retrieved successfully", content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
            @ApiResponse(responseCode = "404", description = "Journal entry not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
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
                    .addDetail(e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @Operation(summary = "Update a journal entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Journal entry updated successfully", content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Journal entry not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
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

            if (e.getMessage().contains("not found") || e.getMessage().contains("another user")) {
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

    @Operation(summary = "Update pre-trade plan")
    @PutMapping("/{entryId}/pre-plan")
    public ResponseEntity<Object> updatePrePlan(
            @PathVariable String entryId,
            @RequestBody PreTradePlan preTradePlan) {
        try {
            return ResponseEntity.ok(tradeJournalService.updatePrePlan(entryId, preTradePlan));
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "Update trade execution")
    @PutMapping("/{entryId}/execution")
    public ResponseEntity<Object> updateExecution(
            @PathVariable String entryId,
            @RequestBody TradeExecution tradeExecution) {
        try {
            return ResponseEntity.ok(tradeJournalService.updateExecution(entryId, tradeExecution));
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "Update post-trade review")
    @PutMapping("/{entryId}/post-review")
    public ResponseEntity<Object> updatePostReview(
            @PathVariable String entryId,
            @RequestBody PostTradeReview postTradeReview,
            @RequestParam(required = false, defaultValue = "false") Boolean markCompleted) {
        try {
            return ResponseEntity.ok(tradeJournalService.updatePostReview(entryId, postTradeReview, markCompleted));
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "Link journal entry to a trade")
    @PostMapping("/{entryId}/link-trade")
    public ResponseEntity<Object> linkTrade(
            @PathVariable String entryId,
            @Valid @RequestBody LinkTradeRequest request) {
        try {
            return ResponseEntity.ok(tradeJournalService.linkTrade(entryId, request.getTradeId()));
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "List attachments for a journal entry")
    @GetMapping("/{entryId}/attachments")
    public ResponseEntity<Object> listAttachments(@PathVariable String entryId) {
        try {
            List<Attachment> attachments = tradeJournalService.listAttachments(entryId);
            return ResponseEntity.ok(attachments);
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "Add attachment to a journal entry")
    @PostMapping("/{entryId}/attachments")
    public ResponseEntity<Object> addAttachment(
            @PathVariable String entryId,
            @RequestBody JournalAttachmentRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(tradeJournalService.addAttachment(entryId, request));
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "Remove attachment from a journal entry")
    @DeleteMapping("/{entryId}/attachments")
    public ResponseEntity<Object> removeAttachment(
            @PathVariable String entryId,
            @RequestParam String fileUrl) {
        try {
            return ResponseEntity.ok(tradeJournalService.removeAttachment(entryId, fileUrl));
        } catch (IllegalArgumentException e) {
            return toEntryError(entryId, e);
        }
    }

    @Operation(summary = "Delete a journal entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Journal entry deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Journal entry not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
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

    private ResponseEntity<Object> toEntryError(String entryId, IllegalArgumentException e) {
        if (e.getMessage() != null
                && (e.getMessage().contains("not found") || e.getMessage().contains("another user"))) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.notFound(e.getMessage(), "/api/v1/journal/" + entryId));
        }
        return ResponseEntity.badRequest()
                .body(ErrorResponse.badRequest(e.getMessage(), "/api/v1/journal/" + entryId));
    }
}
