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
public interface TradeJournalRepository extends MongoRepository<TradeJournalEntry, String> {
    
    /**
     * Find journal entries by user ID ordered by entry date descending
     * 
     * @param userId User ID
     * @param pageable Pagination information
     * @return Page of journal entries
     */
    Page<TradeJournalEntry> findByUserIdOrderByEntryDateDesc(String userId, Pageable pageable);
    
    /**
     * Find journal entries by trade ID ordered by entry date descending
     * 
     * @param tradeId Trade ID
     * @return List of journal entries
     */
    List<TradeJournalEntry> findByTradeIdOrderByEntryDateDesc(String tradeId);
    
    /**
     * Find journal entries by user ID and date range ordered by entry date descending
     * 
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of journal entries
     */
    Page<TradeJournalEntry> findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
            String userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Find journal entries by user ID and tags
     * 
     * @param userId User ID
     * @param tags List of tags
     * @param pageable Pagination information
     * @return Page of journal entries
     */
    Page<TradeJournalEntry> findByUserIdAndTagsInOrderByEntryDateDesc(
            String userId, List<String> tags, Pageable pageable);
    
    /**
     * Count journal entries by user ID
     * 
     * @param userId User ID
     * @return Count of journal entries
     */
    long countByUserId(String userId);
    
    /**
     * Count journal entries by trade ID
     * 
     * @param tradeId Trade ID
     * @return Count of journal entries
     */
    long countByTradeId(String tradeId);
}
