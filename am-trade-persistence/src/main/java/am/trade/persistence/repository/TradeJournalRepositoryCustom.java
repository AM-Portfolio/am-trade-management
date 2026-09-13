package am.trade.persistence.repository;

import am.trade.common.models.TradeJournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Custom MongoTemplate-backed queries for trade journal entries
 */
public interface TradeJournalRepositoryCustom {

    /**
     * Filtered search for journal entries.
     * userId is required; all other filters are optional.
     */
    Page<TradeJournalEntry> searchEntries(
            String userId,
            String journalStatus,
            String entryType,
            String symbol,
            String setup,
            String folderId,
            String playbookId,
            List<String> tagIds,
            String tradeId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            String q,
            Pageable pageable);
}
