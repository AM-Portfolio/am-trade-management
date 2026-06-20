package am.trade.services.notebook;

import am.trade.common.models.notebook.NotebookItem;
import am.trade.common.models.notebook.NotebookItemType;
import am.trade.common.models.notebook.NotebookTag;
import am.trade.persistence.entity.notebook.NotebookItemEntity;
import am.trade.persistence.entity.notebook.NotebookTagEntity;
import am.trade.persistence.repository.notebook.NotebookItemRepository;
import am.trade.persistence.repository.notebook.NotebookTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotebookService {

    private final NotebookItemRepository itemRepository;
    private final NotebookTagRepository tagRepository;

    // --- Notebook Items ---

    public NotebookItem createNotebookItem(NotebookItem dto, String userId) {
        log.info("Creating notebook item for user: {}", userId);
        
        NotebookItemEntity entity = mapItemToEntity(dto);
        entity.setUserId(userId);
        entity.setCreatedAt(Instant.now().toString());
        entity.setUpdatedAt(Instant.now().toString());
        
        NotebookItemEntity saved = itemRepository.save(entity);
        return mapItemToDto(saved);
    }

    public List<NotebookItem> getNotebookItems(String userId, String parentId, NotebookItemType type) {
        log.info("Fetching notebook items for user: {}, parentId: {}, type: {}", userId, parentId, type);
        
        List<NotebookItemEntity> entities;
        if (parentId != null) {
            entities = itemRepository.findByUserIdAndParentId(userId, parentId);
        } else if (type != null) {
            entities = itemRepository.findByUserIdAndType(userId, type);
        } else {
            entities = itemRepository.findByUserId(userId);
        }
        
        return entities.stream().map(this::mapItemToDto).collect(Collectors.toList());
    }

    public NotebookItem getNotebookItem(String itemId, String userId) {
        log.info("Fetching notebook item: {}", itemId);
        NotebookItemEntity entity = itemRepository.findById(itemId)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Notebook item not found: " + itemId, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!entity.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        return mapItemToDto(entity);
    }

    public NotebookItem updateNotebookItem(String itemId, NotebookItem dto, String userId) {
        log.info("Updating notebook item: {}", itemId);
        
        NotebookItemEntity existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Notebook item not found: " + itemId, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!existing.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        
        existing.setTitle(dto.getTitle());
        existing.setContent(dto.getContent());
        existing.setParentId(dto.getParentId());
        existing.setTagIds(dto.getTagIds());
        existing.setMetadata(dto.getMetadata());
        existing.setGoalDetails(dto.getGoalDetails());
        existing.setUpdatedAt(Instant.now().toString());
        
        NotebookItemEntity saved = itemRepository.save(existing);
        return mapItemToDto(saved);
    }

    public void deleteNotebookItem(String itemId, String userId) {
        log.info("Deleting notebook item: {}", itemId);
        NotebookItemEntity existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Notebook item not found: " + itemId, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!existing.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        
        itemRepository.deleteById(itemId);
    }

    // --- Notebook Tags ---

    public NotebookTag createNotebookTag(NotebookTag dto, String userId) {
        log.info("Creating notebook tag for user: {}", userId);
        
        NotebookTagEntity entity = mapTagToEntity(dto);
        entity.setUserId(userId);
        entity.setCreatedAt(Instant.now().toString());
        entity.setUpdatedAt(Instant.now().toString());
        
        NotebookTagEntity saved = tagRepository.save(entity);
        return mapTagToDto(saved);
    }

    public List<NotebookTag> getNotebookTags(String userId) {
        log.info("Fetching notebook tags for user: {}", userId);
        return tagRepository.findByUserId(userId).stream()
                .map(this::mapTagToDto)
                .collect(Collectors.toList());
    }

    public NotebookTag updateNotebookTag(String tagId, NotebookTag dto, String userId) {
        log.info("Updating notebook tag: {}", tagId);
        
        NotebookTagEntity existing = tagRepository.findById(tagId)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Notebook tag not found: " + tagId, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!existing.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        
        existing.setName(dto.getName());
        existing.setColor(dto.getColor());
        existing.setUpdatedAt(Instant.now().toString());
        
        NotebookTagEntity saved = tagRepository.save(existing);
        return mapTagToDto(saved);
    }

    public void deleteNotebookTag(String tagId, String userId) {
        log.info("Deleting notebook tag: {}", tagId);
        NotebookTagEntity existing = tagRepository.findById(tagId)
                .orElseThrow(() -> new am.trade.exceptions.TradeException("Notebook tag not found: " + tagId, org.springframework.http.HttpStatus.NOT_FOUND));
                
        if (!existing.getUserId().equals(userId)) {
            throw new am.trade.exceptions.TradeException("Unauthorized", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        
        tagRepository.deleteById(tagId);
    }

    // --- Mappers ---

    private NotebookItemEntity mapItemToEntity(NotebookItem dto) {
        return NotebookItemEntity.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .type(dto.getType())
                .parentId(dto.getParentId())
                .title(dto.getTitle())
                .content(dto.getContent())
                .tagIds(dto.getTagIds())
                .metadata(dto.getMetadata())
                .goalDetails(dto.getGoalDetails())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private NotebookItem mapItemToDto(NotebookItemEntity entity) {
        return NotebookItem.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .type(entity.getType())
                .parentId(entity.getParentId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .tagIds(entity.getTagIds())
                .metadata(entity.getMetadata())
                .goalDetails(entity.getGoalDetails())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
    
    private NotebookTagEntity mapTagToEntity(NotebookTag dto) {
        return NotebookTagEntity.builder()
                .id(dto.getId())
                .userId(dto.getUserId())
                .name(dto.getName())
                .color(dto.getColor())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    private NotebookTag mapTagToDto(NotebookTagEntity entity) {
        return NotebookTag.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .name(entity.getName())
                .color(entity.getColor())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
