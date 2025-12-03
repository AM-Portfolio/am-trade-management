package am.trade.api.service.impl;

import am.trade.api.dto.NotebookItemRequest;
import am.trade.api.dto.NotebookItemResponse;
import am.trade.api.dto.NotebookTagRequest;
import am.trade.api.dto.NotebookTagResponse;
import am.trade.api.service.NotebookService;
import am.trade.common.models.NotebookItem;
import am.trade.common.models.NotebookTag;
import am.trade.common.models.enums.NotebookItemType;
import am.trade.persistence.repository.NotebookItemRepository;
import am.trade.persistence.repository.NotebookTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotebookServiceImpl implements NotebookService {

    private final NotebookItemRepository notebookItemRepository;
    private final NotebookTagRepository notebookTagRepository;

    // --- Notebook Item Operations ---

    @Override
    public NotebookItemResponse createNotebookItem(NotebookItemRequest request) {
        log.debug("Creating notebook item for user: {}", request.getUserId());

        NotebookItem item = NotebookItem.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .type(request.getType())
                .parentId(request.getParentId())
                .title(request.getTitle())
                .content(request.getContent())
                .tagIds(request.getTagIds())
                .metadata(request.getMetadata())
                .goalDetails(request.getGoalDetails())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        NotebookItem savedItem = notebookItemRepository.save(item);
        return convertToItemResponse(savedItem);
    }

    @Override
    public NotebookItemResponse getNotebookItem(String itemId) {
        NotebookItem item = notebookItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Notebook item not found with ID: " + itemId));
        return convertToItemResponse(item);
    }

    @Override
    public NotebookItemResponse updateNotebookItem(String itemId, NotebookItemRequest request) {
        NotebookItem item = notebookItemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Notebook item not found with ID: " + itemId));

        if (!item.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Cannot update notebook item belonging to another user");
        }

        item.setTitle(request.getTitle());
        item.setContent(request.getContent());
        item.setTagIds(request.getTagIds());
        item.setMetadata(request.getMetadata());
        item.setGoalDetails(request.getGoalDetails());
        item.setUpdatedAt(LocalDateTime.now());
        // Note: type and parentId are typically not changed, but could be if needed.

        NotebookItem updatedItem = notebookItemRepository.save(item);
        return convertToItemResponse(updatedItem);
    }

    @Override
    public void deleteNotebookItem(String itemId) {
        if (!notebookItemRepository.existsById(itemId)) {
            throw new IllegalArgumentException("Notebook item not found with ID: " + itemId);
        }

        // Find and delete all children (recursive)
        // This handles deleting a Folder or Goal and all its contents
        List<NotebookItem> children = notebookItemRepository.findByUserIdAndParentId(
                notebookItemRepository.findById(itemId).get().getUserId(),
                itemId);

        for (NotebookItem child : children) {
            deleteNotebookItem(child.getId());
        }

        notebookItemRepository.deleteById(itemId);
    }

    @Override
    public List<NotebookItemResponse> getNotebookItemsByUser(String userId) {
        return notebookItemRepository.findByUserId(userId).stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotebookItemResponse> getNotebookItemsByUserAndParent(String userId, String parentId) {
        return notebookItemRepository.findByUserIdAndParentId(userId, parentId).stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<NotebookItemResponse> getNotebookItemsByUserAndType(String userId, NotebookItemType type) {
        return notebookItemRepository.findByUserIdAndType(userId, type).stream()
                .map(this::convertToItemResponse)
                .collect(Collectors.toList());
    }

    // --- Notebook Tag Operations ---

    @Override
    public NotebookTagResponse createNotebookTag(NotebookTagRequest request) {
        log.debug("Creating notebook tag for user: {}", request.getUserId());

        NotebookTag tag = NotebookTag.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .name(request.getName())
                .color(request.getColor())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        NotebookTag savedTag = notebookTagRepository.save(tag);
        return convertToTagResponse(savedTag);
    }

    @Override
    public NotebookTagResponse getNotebookTag(String tagId) {
        NotebookTag tag = notebookTagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Notebook tag not found with ID: " + tagId));
        return convertToTagResponse(tag);
    }

    @Override
    public NotebookTagResponse updateNotebookTag(String tagId, NotebookTagRequest request) {
        NotebookTag tag = notebookTagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Notebook tag not found with ID: " + tagId));

        if (!tag.getUserId().equals(request.getUserId())) {
            throw new IllegalArgumentException("Cannot update notebook tag belonging to another user");
        }

        tag.setName(request.getName());
        tag.setColor(request.getColor());
        tag.setDescription(request.getDescription());
        tag.setUpdatedAt(LocalDateTime.now());

        NotebookTag updatedTag = notebookTagRepository.save(tag);
        return convertToTagResponse(updatedTag);
    }

    @Override
    public void deleteNotebookTag(String tagId) {
        if (!notebookTagRepository.existsById(tagId)) {
            throw new IllegalArgumentException("Notebook tag not found with ID: " + tagId);
        }
        notebookTagRepository.deleteById(tagId);
    }

    @Override
    public List<NotebookTagResponse> getNotebookTagsByUser(String userId) {
        return notebookTagRepository.findByUserId(userId).stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    // --- Helpers ---

    private NotebookItemResponse convertToItemResponse(NotebookItem item) {
        return NotebookItemResponse.builder()
                .id(item.getId())
                .userId(item.getUserId())
                .type(item.getType())
                .parentId(item.getParentId())
                .title(item.getTitle())
                .content(item.getContent())
                .tagIds(item.getTagIds())
                .metadata(item.getMetadata())
                .goalDetails(item.getGoalDetails())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private NotebookTagResponse convertToTagResponse(NotebookTag tag) {
        return NotebookTagResponse.builder()
                .id(tag.getId())
                .userId(tag.getUserId())
                .name(tag.getName())
                .color(tag.getColor())
                .description(tag.getDescription())
                .createdAt(tag.getCreatedAt())
                .updatedAt(tag.getUpdatedAt())
                .build();
    }
}
