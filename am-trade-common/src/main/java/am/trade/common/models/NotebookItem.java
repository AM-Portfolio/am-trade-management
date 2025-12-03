package am.trade.common.models;

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
 * Domain model for notebook items (Folders, Notes, Goals)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookItem {

    private String id;
    private String userId;
    private NotebookItemType type;
    private String parentId; // ID of the parent folder or goal

    private String title;
    private String content; // For notes or goal descriptions

    private List<String> tagIds;

    private Map<String, Object> metadata; // Flexible field for extra data

    // Goal specific details
    private Map<String, Object> goalDetails;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
