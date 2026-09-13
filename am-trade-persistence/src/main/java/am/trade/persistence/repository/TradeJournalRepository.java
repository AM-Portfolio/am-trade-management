package am.trade.persistence.repository;

import am.trade.common.models.TradeJournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for trade journal entries
 */
@Repository
public interface TradeJournalRepository extends MongoRepository<TradeJournalEntry, String>, TradeJournalRepositoryCustom {

    /**
     * Find journal entries by user ID ordered by entry date descending
     */
    Page<TradeJournalEntry> findByUserIdOrderByEntryDateDesc(String userId, Pageable pageable);

    /**
     * Find journal entries by trade ID ordered by entry date descending
     */
    List<TradeJournalEntry> findByTradeIdOrderByEntryDateDesc(String tradeId);

    /**
     * Find journal entries by user ID and trade ID
     */
    List<TradeJournalEntry> findByUserIdAndTradeIdOrderByEntryDateDesc(String userId, String tradeId);

    /**
     * Find journal entries by user ID and date range ordered by entry date descending
     */
    Page<TradeJournalEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find all journal entries for a user in a date range (no pagination)
     */
    List<TradeJournalEntry> findByUserIdAndEntryDateBetween(
            String userId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find by user and journal status
     */
    List<TradeJournalEntry> findByUserIdAndJournalStatus(String userId, String journalStatus);

    /**
     * Find by user and entry type
     */
    List<TradeJournalEntry> findByUserIdAndEntryType(String userId, String entryType);

    /**
     * Find by user and symbol (case-insensitive)
     */
    List<TradeJournalEntry> findByUserIdAndSymbolIgnoreCase(String userId, String symbol);

    /**
     * Find by user and folder
     */
    List<TradeJournalEntry> findByUserIdAndFolderId(String userId, String folderId);

    /**
     * Find by user and playbook
     */
    List<TradeJournalEntry> findByUserIdAndPlaybookId(String userId, String playbookId);

    /**
     * Find by user and tag IDs containing any of the given tags
     */
    List<TradeJournalEntry> findByUserIdAndTagIdsIn(String userId, List<String> tagIds);

    /**
     * Find entries by IDs owned by user
     */
    List<TradeJournalEntry> findByUserIdAndIdIn(String userId, List<String> ids);

    /**
     * Count journal entries by user ID
     */
    long countByUserId(String userId);

    /**
     * Count journal entries by trade ID
     */
    long countByTradeId(String tradeId);

    /**
     * Count by user and status
     */
    long countByUserIdAndJournalStatus(String userId, String journalStatus);
}
