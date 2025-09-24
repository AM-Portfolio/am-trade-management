package am.trade.api.service;

import am.trade.api.dto.TradeJournalEntryRequest;
import am.trade.api.dto.TradeJournalEntryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing trade journal entries
 */
public interface TradeJournalService {

    /**
     * Create a new journal entry
     * 
     * @param request Journal entry request
     * @return Created journal entry
     * @throws IllegalArgumentException if request is invalid
     */
    TradeJournalEntryResponse createJournalEntry(TradeJournalEntryRequest request);
    
    /**
     * Get a journal entry by ID
     * 
     * @param entryId Journal entry ID
     * @return Journal entry
     * @throws IllegalArgumentException if entry not found
     */
    TradeJournalEntryResponse getJournalEntry(String entryId);
    
    /**
     * Get journal entries for a user with pagination
     * 
     * @param userId User ID
     * @param pageable Pagination information
     * @return Page of journal entries
     */
    Page<TradeJournalEntryResponse> getJournalEntriesByUser(String userId, Pageable pageable);
    
    /**
     * Get journal entries for a specific trade
     * 
     * @param tradeId Trade ID
     * @return List of journal entries
     */
    List<TradeJournalEntryResponse> getJournalEntriesByTrade(String tradeId);
    
    /**
     * Get journal entries by date range
     * 
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @param pageable Pagination information
     * @return Page of journal entries
     * @throws IllegalArgumentException if date range is invalid
     */
    Page<TradeJournalEntryResponse> getJournalEntriesByDateRange(String userId, LocalDate startDate, LocalDate endDate, Pageable pageable);
    
    /**
     * Update a journal entry
     * 
     * @param entryId Journal entry ID
     * @param request Journal entry request
     * @return Updated journal entry
     * @throws IllegalArgumentException if entry not found or request is invalid
     */
    TradeJournalEntryResponse updateJournalEntry(String entryId, TradeJournalEntryRequest request);
    
    /**
     * Delete a journal entry
     * 
     * @param entryId Journal entry ID
     * @throws IllegalArgumentException if entry not found
     */
    void deleteJournalEntry(String entryId);
}
