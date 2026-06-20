package am.trade.services.journal;

import am.trade.common.models.journal.TradeJournalEntry;
import am.trade.persistence.entity.journal.JournalEntryEntity;
import am.trade.persistence.repository.journal.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JournalService {

    private final JournalEntryRepository repository;

    public TradeJournalEntry createJournalEntry(TradeJournalEntry dto, String userId) {
        log.info("Creating journal entry for user: {}", userId);
        
        JournalEntryEntity entity = mapToEntity(dto);
        entity.setUserId(userId);
        entity.setCreatedAt(Instant.now().toString());
        entity.setUpdatedAt(Instant.now().toString());
        
        JournalEntryEntity saved = repository.save(entity);
        return mapToDto(saved);
    }

    public TradeJournalEntry getJournalEntry(String id) {
        log.info("Fetching journal entry: {}", id);
        return repository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Journal entry not found: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
    }

    public TradeJournalEntry updateJournalEntry(String id, TradeJournalEntry dto, String userId) {
        log.info("Updating journal entry: {}", id);
        
        JournalEntryEntity existing = repository.findById(id)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Journal entry not found: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!existing.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized to update this journal entry", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        
        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setTradeId(dto.getTradeId());
        existing.setBehaviorPatternSummaries(dto.getBehaviorPatternSummaries());
        existing.setCustomFields(dto.getCustomFields());
        existing.setEntryDate(dto.getEntryDate());
        existing.setImageUrls(dto.getImageUrls());
        existing.setAttachments(dto.getAttachments());
        existing.setRelatedTradeIds(dto.getRelatedTradeIds());
        existing.setTagIds(dto.getTagIds());
        existing.setUpdatedAt(Instant.now().toString());
        
        JournalEntryEntity saved = repository.save(existing);
        return mapToDto(saved);
    }

    public void deleteJournalEntry(String id, String userId) {
        log.info("Deleting journal entry: {}", id);
        JournalEntryEntity existing = repository.findById(id)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Journal entry not found: " + id, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!existing.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized to delete this journal entry", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        
        repository.deleteById(id);
    }

    public List<TradeJournalEntry> getJournalEntriesByUser(String userId) {
        log.info("Fetching journal entries for user: {}", userId);
        return repository.findByUserId(userId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TradeJournalEntry> getJournalEntriesByTrade(String tradeId) {
        log.info("Fetching journal entries for trade: {}", tradeId);
        return repository.findByTradeId(tradeId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<TradeJournalEntry> getJournalEntriesByDateRange(String userId, String startDate, String endDate) {
        log.info("Fetching journal entries for user: {} between {} and {}", userId, startDate, endDate);
        return repository.findByUserIdAndEntryDateBetween(userId, startDate, endDate).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private JournalEntryEntity mapToEntity(TradeJournalEntry dto) {
        return JournalEntryEntity.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .tradeId(dto.getTradeId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .behaviorPatternSummaries(dto.getBehaviorPatternSummaries())
                .customFields(dto.getCustomFields())
                .entryDate(dto.getEntryDate())
                .imageUrls(dto.getImageUrls())
                .attachments(dto.getAttachments())
                .relatedTradeIds(dto.getRelatedTradeIds())
                .tagIds(dto.getTagIds())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private TradeJournalEntry mapToDto(JournalEntryEntity entity) {
        return TradeJournalEntry.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .tradeId(entity.getTradeId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .behaviorPatternSummaries(entity.getBehaviorPatternSummaries())
                .customFields(entity.getCustomFields())
                .entryDate(entity.getEntryDate())
                .imageUrls(entity.getImageUrls())
                .attachments(entity.getAttachments())
                .relatedTradeIds(entity.getRelatedTradeIds())
                .tagIds(entity.getTagIds())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
