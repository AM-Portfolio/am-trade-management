package am.trade.api.controller.journal;

import am.trade.common.models.journal.TradeJournalEntry;
import am.trade.services.journal.JournalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/api/v1/journal", "/v1/journal"})
@RequiredArgsConstructor
@Slf4j
@Validated
public class JournalController {

    private final JournalService journalService;

    private String getCurrentUserId(TradeJournalEntry entry) {
        String userId = com.am.security.context.UserContext.getUserId();
        if (userId != null && !userId.isEmpty()) {
            return userId;
        }
        if (entry != null && entry.getUserId() != null && !entry.getUserId().isEmpty()) {
            return entry.getUserId();
        }
        return "default-dev-user-id";
    }
    
    private String getCurrentUserId() {
        return getCurrentUserId(null);
    }

    @PostMapping
    public ResponseEntity<TradeJournalEntry> createJournalEntry(@RequestBody @Validated TradeJournalEntry request) {
        log.info("Creating journal entry");
        String userId = getCurrentUserId(request);
        TradeJournalEntry created = journalService.createJournalEntry(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{entryId}")
    public ResponseEntity<TradeJournalEntry> getJournalEntry(@PathVariable String entryId) {
        log.info("Getting journal entry: {}", entryId);
        TradeJournalEntry entry = journalService.getJournalEntry(entryId);
        return ResponseEntity.ok(entry);
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<TradeJournalEntry> updateJournalEntry(
            @PathVariable String entryId,
            @RequestBody @Validated TradeJournalEntry request) {
        log.info("Updating journal entry: {}", entryId);
        String userId = getCurrentUserId(request);
        TradeJournalEntry updated = journalService.updateJournalEntry(entryId, request, userId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteJournalEntry(@PathVariable String entryId) {
        log.info("Deleting journal entry: {}", entryId);
        String userId = getCurrentUserId();
        journalService.deleteJournalEntry(entryId, userId);
        return ResponseEntity.noContent().build();
    }

    // Returns wrapped in "content" to match frontend parsing of Page objects
    @GetMapping("/user")
    public ResponseEntity<Map<String, Object>> getJournalEntriesByUser() {
        String userId = getCurrentUserId();
        log.info("Getting journal entries for user: {}", userId);
        List<TradeJournalEntry> entries = journalService.getJournalEntriesByUser(userId);
        return ResponseEntity.ok(Collections.singletonMap("content", entries));
    }

    @GetMapping("/trade/{tradeId}")
    public ResponseEntity<List<TradeJournalEntry>> getJournalEntriesByTrade(@PathVariable String tradeId) {
        log.info("Getting journal entries for trade: {}", tradeId);
        List<TradeJournalEntry> entries = journalService.getJournalEntriesByTrade(tradeId);
        return ResponseEntity.ok(entries);
    }

    // Returns wrapped in "content" to match frontend parsing
    @GetMapping("/date-range")
    public ResponseEntity<Map<String, Object>> getJournalEntriesByDateRange(
            @RequestParam String startDate,
            @RequestParam String endDate) {
        String userId = getCurrentUserId();
        log.info("Getting journal entries by date range for user: {}", userId);
        List<TradeJournalEntry> entries = journalService.getJournalEntriesByDateRange(userId, startDate, endDate);
        return ResponseEntity.ok(Collections.singletonMap("content", entries));
    }
}
