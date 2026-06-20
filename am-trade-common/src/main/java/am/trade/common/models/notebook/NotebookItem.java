package am.trade.common.models.notebook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookItem {
    private String id;
    private String userId;
    private NotebookItemType type;
    private String parentId;
    private String title;
    private String content;
    private List<String> tagIds;
    private Map<String, Object> metadata;
    private Map<String, Object> goalDetails;
    private String createdAt;
    private String updatedAt;
}
