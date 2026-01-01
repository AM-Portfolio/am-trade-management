package am.trade.api.controller;

import am.trade.api.dto.*;
import am.trade.api.service.JournalTemplateService;
import am.trade.common.models.enums.JournalTemplateCategory;
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
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/**
 * Controller for managing journal templates
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/journal-templates")
@RequiredArgsConstructor
@Tag(name = "Journal Templates", description = "API for managing journal templates with favorites and recommendations")
public class JournalTemplateController {

    private final JournalTemplateService templateService;

    @Operation(summary = "Create a new journal template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Template created successfully", content = @Content(schema = @Schema(implementation = JournalTemplateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public ResponseEntity<JournalTemplateResponse> createTemplate(@Valid @RequestBody JournalTemplateRequest request) {
        log.info("Creating journal template: {}", request.getName());
        try {
            JournalTemplateResponse response = templateService.createTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid template data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get template by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template retrieved successfully", content = @Content(schema = @Schema(implementation = JournalTemplateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/{templateId}")
    public ResponseEntity<JournalTemplateResponse> getTemplate(
            @PathVariable String templateId,
            @RequestParam String userId) {
        log.info("Fetching template with ID: {}", templateId);
        try {
            JournalTemplateResponse response = templateService.getTemplate(templateId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Template not found: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Update a journal template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Template updated successfully", content = @Content(schema = @Schema(implementation = JournalTemplateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{templateId}")
    public ResponseEntity<JournalTemplateResponse> updateTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody JournalTemplateRequest request) {
        log.info("Updating template with ID: {}", templateId);
        try {
            JournalTemplateResponse response = templateService.updateTemplate(templateId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating template: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }

    @Operation(summary = "Delete a journal template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Template deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Cannot delete system template", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/{templateId}")
    public ResponseEntity<Void> deleteTemplate(
            @PathVariable String templateId,
            @RequestParam String userId) {
        log.info("Deleting template with ID: {}", templateId);
        try {
            templateService.deleteTemplate(templateId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Error deleting template: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }

    @Operation(summary = "Get all templates with optional filters")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Templates retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping
    public ResponseEntity<List<JournalTemplateResponse>> getAllTemplates(
            @RequestParam String userId,
            @RequestParam(required = false) JournalTemplateCategory category,
            @RequestParam(required = false) String search) {
        log.info("Fetching all templates for user: {}, category: {}, search: {}", userId, category, search);
        List<JournalTemplateResponse> templates = templateService.getAllTemplates(userId, category, search);
        return ResponseEntity.ok(templates);
    }

    @Operation(summary = "Get favorite templates for a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite templates retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/favorites")
    public ResponseEntity<List<JournalTemplateResponse>> getFavoriteTemplates(@RequestParam String userId) {
        log.info("Fetching favorite templates for user: {}", userId);
        List<JournalTemplateResponse> templates = templateService.getFavoriteTemplates(userId);
        return ResponseEntity.ok(templates);
    }

    @Operation(summary = "Get recommended templates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Recommended templates retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/recommended")
    public ResponseEntity<List<JournalTemplateResponse>> getRecommendedTemplates(@RequestParam String userId) {
        log.info("Fetching recommended templates for user: {}", userId);
        List<JournalTemplateResponse> templates = templateService.getRecommendedTemplates(userId);
        return ResponseEntity.ok(templates);
    }

    @Operation(summary = "Get user's custom templates")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Custom templates retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/my-templates")
    public ResponseEntity<List<JournalTemplateResponse>> getUserCustomTemplates(@RequestParam String userId) {
        log.info("Fetching custom templates for user: {}", userId);
        List<JournalTemplateResponse> templates = templateService.getUserCustomTemplates(userId);
        return ResponseEntity.ok(templates);
    }

    @Operation(summary = "Toggle favorite status for a template")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Favorite status toggled successfully", content = @Content(schema = @Schema(implementation = JournalTemplateResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{templateId}/favorite")
    public ResponseEntity<JournalTemplateResponse> toggleFavorite(
            @PathVariable String templateId,
            @RequestParam String userId) {
        log.info("Toggling favorite for template: {} and user: {}", templateId, userId);
        try {
            JournalTemplateResponse response = templateService.toggleFavorite(templateId, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Template not found: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Use template to create a journal entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Journal entry created from template successfully", content = @Content(schema = @Schema(implementation = TradeJournalEntryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Template not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/{templateId}/use")
    public ResponseEntity<TradeJournalEntryResponse> useTemplate(
            @PathVariable String templateId,
            @Valid @RequestBody UseTemplateRequest request) {
        log.info("Using template {} for user {}", templateId, request.getUserId());

        // Ensure templateId in path matches request
        if (!templateId.equals(request.getTemplateId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Template ID mismatch");
        }

        try {
            TradeJournalEntryResponse response = templateService.useTemplate(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Error using template: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }
}
