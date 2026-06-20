package am.trade.api.controller.notebook;

import am.trade.common.models.notebook.NotebookItem;
import am.trade.common.models.notebook.NotebookItemType;
import am.trade.common.models.notebook.NotebookTag;
import am.trade.services.notebook.NotebookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/notebook", "/v1/notebook"})
@RequiredArgsConstructor
@Slf4j
@Validated
public class NotebookController {

    private final NotebookService notebookService;

    private String getCurrentUserId(String providedId) {
        String userId = com.am.security.context.UserContext.getUserId();
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }
        if (providedId != null && !providedId.isEmpty()) {
            return providedId;
        }
        return "default-dev-user-id";
    }

    private String getCurrentUserId() {
        return getCurrentUserId(null);
    }

    // --- Notebook Items ---

    @PostMapping("/items")
    public ResponseEntity<NotebookItem> createNotebookItem(@RequestBody @Validated NotebookItem request) {
        log.info("Creating notebook item");
        String userId = getCurrentUserId(request.getUserId());
        NotebookItem created = notebookService.createNotebookItem(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/items")
    public ResponseEntity<List<NotebookItem>> getNotebookItems(
            @RequestParam(required = false) String parentId,
            @RequestParam(required = false) NotebookItemType type) {
        String userId = getCurrentUserId();
        log.info("Getting notebook items for user: {}", userId);
        List<NotebookItem> items = notebookService.getNotebookItems(userId, parentId, type);
        return ResponseEntity.ok(items);
    }

    @GetMapping("/items/{itemId}")
    public ResponseEntity<NotebookItem> getNotebookItem(@PathVariable String itemId) {
        String userId = getCurrentUserId();
        log.info("Getting notebook item: {}", itemId);
        NotebookItem item = notebookService.getNotebookItem(itemId, userId);
        return ResponseEntity.ok(item);
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<NotebookItem> updateNotebookItem(
            @PathVariable String itemId,
            @RequestBody @Validated NotebookItem request) {
        String userId = getCurrentUserId(request.getUserId());
        log.info("Updating notebook item: {}", itemId);
        NotebookItem updated = notebookService.updateNotebookItem(itemId, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> deleteNotebookItem(@PathVariable String itemId) {
        String userId = getCurrentUserId();
        log.info("Deleting notebook item: {}", itemId);
        notebookService.deleteNotebookItem(itemId, userId);
        return ResponseEntity.noContent().build();
    }

    // --- Notebook Tags ---

    @PostMapping("/tags")
    public ResponseEntity<NotebookTag> createNotebookTag(@RequestBody @Validated NotebookTag request) {
        log.info("Creating notebook tag");
        String userId = getCurrentUserId(request.getUserId());
        NotebookTag created = notebookService.createNotebookTag(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<NotebookTag>> getNotebookTags() {
        String userId = getCurrentUserId();
        log.info("Getting notebook tags for user: {}", userId);
        List<NotebookTag> tags = notebookService.getNotebookTags(userId);
        return ResponseEntity.ok(tags);
    }

    @PutMapping("/tags/{tagId}")
    public ResponseEntity<NotebookTag> updateNotebookTag(
            @PathVariable String tagId,
            @RequestBody @Validated NotebookTag request) {
        String userId = getCurrentUserId(request.getUserId());
        log.info("Updating notebook tag: {}", tagId);
        NotebookTag updated = notebookService.updateNotebookTag(tagId, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<Void> deleteNotebookTag(@PathVariable String tagId) {
        String userId = getCurrentUserId();
        log.info("Deleting notebook tag: {}", tagId);
        notebookService.deleteNotebookTag(tagId, userId);
        return ResponseEntity.noContent().build();
    }
}
