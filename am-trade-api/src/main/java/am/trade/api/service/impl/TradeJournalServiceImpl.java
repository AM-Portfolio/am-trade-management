package am.trade.api.service.impl;

import am.trade.api.dto.TradeJournalEntryRequest;
import am.trade.api.dto.TradeJournalEntryResponse;
import am.trade.api.service.TradeJournalService;
import am.trade.common.models.TradeJournalEntry;
import am.trade.persistence.repository.TradeJournalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of TradeJournalService for managing trade journal entries
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeJournalServiceImpl implements TradeJournalService {

    private final TradeJournalRepository tradeJournalRepository;

    @Override
    public TradeJournalEntryResponse createJournalEntry(TradeJournalEntryRequest request) {
        log.debug("Creating journal entry for user: {}", request.getUserId());
        
        validateRequest(request);
        
        TradeJournalEntry entry = convertToEntity(request);
        entry.setId(UUID.randomUUID().toString());
        
        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);
        
        TradeJournalEntry savedEntry = tradeJournalRepository.save(entry);
        log.info("Journal entry created with ID: {}", savedEntry.getId());
        
        return convertToResponse(savedEntry);
    }

    @Override
    public TradeJournalEntryResponse getJournalEntry(String entryId) {
        log.debug("Getting journal entry with ID: {}", entryId);
        
        TradeJournalEntry entry = findEntryById(entryId);
        return convertToResponse(entry);
    }

    @Override
    public Page<TradeJournalEntryResponse> getJournalEntriesByUser(String userId, Pageable pageable) {
        log.debug("Getting journal entries for user: {}", userId);
        
        Page<TradeJournalEntry> entries = tradeJournalRepository.findByUserIdOrderByEntryDateDesc(userId, pageable);
        return entries.map(this::convertToResponse);
    }

    @Override
    public List<TradeJournalEntryResponse> getJournalEntriesByTrade(String tradeId) {
        log.debug("Getting journal entries for trade: {}", tradeId);
        
        List<TradeJournalEntry> entries = tradeJournalRepository.findByTradeIdOrderByEntryDateDesc(tradeId);
        return entries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TradeJournalEntryResponse> getJournalEntriesByDateRange(
            String userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        
        log.debug("Getting journal entries for user: {} between {} and {}", userId, startDate, endDate);
        
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
        
        Page<TradeJournalEntry> entries = tradeJournalRepository.findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(
                userId, startDateTime, endDateTime, pageable);
        
        return entries.map(this::convertToResponse);
    }

    @Override
    public TradeJournalEntryResponse updateJournalEntry(String entryId, TradeJournalEntryRequest request) {
        log.debug("Updating journal entry with ID: {}", entryId);
        
        validateRequest(request);
        
        TradeJournalEntry existingEntry = findEntryById(entryId);
        
        // Check if user ID matches
        if (!existingEntry.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Cannot update journal entry that belongs to another user");
        }
        
        // Update fields
        existingEntry.setTitle(request.getTitle());
        existingEntry.setContent(request.getContent());
        existingEntry.setMood(request.getMood());
        existingEntry.setMarketSentiment(request.getMarketSentiment());
        existingEntry.setTags(request.getTags());
        existingEntry.setCustomFields(request.getCustomFields());
        existingEntry.setEntryDate(request.getEntryDate());
        existingEntry.setImageUrls(request.getImageUrls());
        existingEntry.setRelatedTradeIds(request.getRelatedTradeIds());
        existingEntry.setUpdatedAt(LocalDateTime.now());
        
        // Don't update tradeId as it's a key relationship
        // Don't update userId as it's the owner
        
        TradeJournalEntry updatedEntry = tradeJournalRepository.save(existingEntry);
        log.info("Journal entry updated with ID: {}", updatedEntry.getId());
        
        return convertToResponse(updatedEntry);
    }

    @Override
    public void deleteJournalEntry(String entryId) {
        log.debug("Deleting journal entry with ID: {}", entryId);
        
        // Check if entry exists
        findEntryById(entryId);
        
        tradeJournalRepository.deleteById(entryId);
        log.info("Journal entry deleted with ID: {}", entryId);
    }
    
    /**
     * Find a journal entry by ID
     * 
     * @param entryId Journal entry ID
     * @return Journal entry
     * @throws IllegalArgumentException if entry not found
     */
    private TradeJournalEntry findEntryById(String entryId) {
        Optional<TradeJournalEntry> entryOpt = tradeJournalRepository.findById(entryId);
        if (entryOpt.isEmpty()) {
            throw new IllegalArgumentException("Journal entry not found with ID: " + entryId);
        }
        return entryOpt.get();
    }
    
    /**
     * Validate journal entry request
     * 
     * @param request Journal entry request
     * @throws IllegalArgumentException if request is invalid
     */
    private void validateRequest(TradeJournalEntryRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Content is required");
        }
        
        if (request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        if (request.getEntryDate() == null) {
            throw new IllegalArgumentException("Entry date is required");
        }
        
        if (request.getMarketSentiment() != null && 
                (request.getMarketSentiment() < 1 || request.getMarketSentiment() > 10)) {
            throw new IllegalArgumentException("Market sentiment must be between 1 and 10");
        }
    }
    
    /**
     * Convert request DTO to entity
     * 
     * @param request Journal entry request
     * @return Journal entry entity
     */
    private TradeJournalEntry convertToEntity(TradeJournalEntryRequest request) {
        return TradeJournalEntry.builder()
                .userId(request.getUserId())
                .tradeId(request.getTradeId())
                .title(request.getTitle())
                .content(request.getContent())
                .mood(request.getMood())
                .marketSentiment(request.getMarketSentiment())
                .tags(request.getTags())
                .customFields(request.getCustomFields())
                .entryDate(request.getEntryDate())
                .imageUrls(request.getImageUrls())
                .relatedTradeIds(request.getRelatedTradeIds())
                .build();
    }
    
    /**
     * Convert entity to response DTO
     * 
     * @param entry Journal entry entity
     * @return Journal entry response
     */
    private TradeJournalEntryResponse convertToResponse(TradeJournalEntry entry) {
        return TradeJournalEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .tradeId(entry.getTradeId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .mood(entry.getMood())
                .marketSentiment(entry.getMarketSentiment())
                .tags(entry.getTags())
                .customFields(entry.getCustomFields())
                .entryDate(entry.getEntryDate())
                .imageUrls(entry.getImageUrls())
                .relatedTradeIds(entry.getRelatedTradeIds())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}
