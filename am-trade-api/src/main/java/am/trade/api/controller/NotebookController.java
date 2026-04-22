package am.trade.api.controller;

import am.trade.api.dto.ErrorResponse;
import am.trade.api.dto.NotebookItemRequest;
import am.trade.api.dto.NotebookItemResponse;
import am.trade.api.dto.NotebookTagRequest;
import am.trade.api.dto.NotebookTagResponse;
import am.trade.api.service.NotebookService;
import am.trade.common.models.enums.NotebookItemType;
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
 * Controller for managing notebook items and tags
 */
@Slf4j
@RestController
@RequestMapping("/v1/notebook")
@RequiredArgsConstructor
@Tag(name = "Notebook", description = "API for managing notebook items (folders, notes, goals) and tags")
public class NotebookController {

    private final NotebookService notebookService;

    // --- Notebook Item Endpoints ---

    @Operation(summary = "Create a new notebook item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notebook item created successfully", content = @Content(schema = @Schema(implementation = NotebookItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping("/items")
    public ResponseEntity<NotebookItemResponse> createNotebookItem(@Valid @RequestBody NotebookItemRequest request) {
        log.info("Creating notebook item for user: {}", request.getUserId());
        try {
            NotebookItemResponse response = notebookService.createNotebookItem(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid notebook item data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get notebook item by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notebook item retrieved successfully", content = @Content(schema = @Schema(implementation = NotebookItemResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notebook item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/items/{itemId}")
    public ResponseEntity<NotebookItemResponse> getNotebookItem(@PathVariable String itemId) {
        log.info("Fetching notebook item with ID: {}", itemId);
        try {
            NotebookItemResponse response = notebookService.getNotebookItem(itemId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Notebook item not found: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Update a notebook item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notebook item updated successfully", content = @Content(schema = @Schema(implementation = NotebookItemResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request parameters", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Notebook item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/items/{itemId}")
    public ResponseEntity<NotebookItemResponse> updateNotebookItem(
            @PathVariable String itemId,
            @Valid @RequestBody NotebookItemRequest request) {
        log.info("Updating notebook item with ID: {}", itemId);
        try {
            NotebookItemResponse response = notebookService.updateNotebookItem(itemId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating notebook item: {}", e.getMessage());
            if (e.getMessage().contains("not found")) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } else {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
            }
        }
    }

    @Operation(summary = "Delete a notebook item")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notebook item deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notebook item not found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteNotebookItem(@PathVariable String itemId) {
        log.info("Deleting notebook item with ID: {}", itemId);
        try {
            notebookService.deleteNotebookItem(itemId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Notebook item not found: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @Operation(summary = "Get notebook items for a user")
    @GetMapping("/items")
    public ResponseEntity<List<NotebookItemResponse>> getNotebookItems(
            @RequestParam String userId,
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) NotebookItemType type) {

        log.info("Fetching notebook items for user: {}", userId);

        List<NotebookItemResponse> items;
        if (parentId != null) {
            items = notebookService.getNotebookItemsByUserAndParent(userId, parentId);
        } else if (type != null) {
            items = notebookService.getNotebookItemsByUserAndType(userId, type);
        } else {
            items = notebookService.getNotebookItemsByUser(userId);
        }

        return ResponseEntity.ok(items);
    }

    // --- Notebook Tag Endpoints ---

    @Operation(summary = "Create a new notebook tag")
    @PostMapping("/tags")
    public ResponseEntity<NotebookTagResponse> createNotebookTag(@Valid @RequestBody NotebookTagRequest request) {
        log.info("Creating notebook tag for user: {}", request.getUserId());
        try {
            NotebookTagResponse response = notebookService.createNotebookTag(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.error("Invalid notebook tag data: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Get notebook tags for a user")
    @GetMapping("/tags")
    public ResponseEntity<List<NotebookTagResponse>> getNotebookTags(@RequestParam String userId) {
        log.info("Fetching notebook tags for user: {}", userId);
        List<NotebookTagResponse> tags = notebookService.getNotebookTagsByUser(userId);
        return ResponseEntity.ok(tags);
    }

    @Operation(summary = "Update a notebook tag")
    @PutMapping("/tags/{tagId}")
    public ResponseEntity<NotebookTagResponse> updateNotebookTag(
            @PathVariable String tagId,
            @Valid @RequestBody NotebookTagRequest request) {
        log.info("Updating notebook tag with ID: {}", tagId);
        try {
            NotebookTagResponse response = notebookService.updateNotebookTag(tagId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("Error updating notebook tag: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Operation(summary = "Delete a notebook tag")
    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<Void> deleteNotebookTag(@PathVariable String tagId) {
        log.info("Deleting notebook tag with ID: {}", tagId);
        try {
            notebookService.deleteNotebookTag(tagId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.error("Notebook tag not found: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
