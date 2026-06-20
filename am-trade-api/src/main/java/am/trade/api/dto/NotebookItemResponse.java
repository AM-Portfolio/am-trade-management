package am.trade.api.dto;

import am.trade.common.models.enums.NotebookItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for notebook items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookItemResponse {

    private String id;
    private String userId;
    private NotebookItemType type;
    private String parentId;
    private String title;
    private String content;
    private List<String> tagIds;
    private Map<String, Object> metadata;
    private Map<String, Object> goalDetails;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
