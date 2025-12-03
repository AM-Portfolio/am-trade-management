package am.trade.api.service;

import am.trade.api.dto.NotebookItemRequest;
import am.trade.api.dto.NotebookItemResponse;
import am.trade.api.dto.NotebookTagRequest;
import am.trade.api.dto.NotebookTagResponse;
import am.trade.common.models.enums.NotebookItemType;

import java.util.List;

/**
 * Service for managing notebook items and tags
 */
public interface NotebookService {

    // Notebook Item Operations
    NotebookItemResponse createNotebookItem(NotebookItemRequest request);

    NotebookItemResponse getNotebookItem(String itemId);

    NotebookItemResponse updateNotebookItem(String itemId, NotebookItemRequest request);

    void deleteNotebookItem(String itemId);

    List<NotebookItemResponse> getNotebookItemsByUser(String userId);

    List<NotebookItemResponse> getNotebookItemsByUserAndParent(String userId, String parentId);

    List<NotebookItemResponse> getNotebookItemsByUserAndType(String userId, NotebookItemType type);

    // Notebook Tag Operations
    NotebookTagResponse createNotebookTag(NotebookTagRequest request);

    NotebookTagResponse getNotebookTag(String tagId);

    NotebookTagResponse updateNotebookTag(String tagId, NotebookTagRequest request);

    void deleteNotebookTag(String tagId);

    List<NotebookTagResponse> getNotebookTagsByUser(String userId);
}
