package am.trade.persistence.entity.notebook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import am.trade.common.models.notebook.NotebookItemType;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notebook_items")
public class NotebookItemEntity {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private NotebookItemType type;
    
    @Indexed
    private String parentId;
    
    private String title;
    private String content;
    
    private List<String> tagIds;
    private Map<String, Object> metadata;
    private Map<String, Object> goalDetails;
    
    private String createdAt;
    private String updatedAt;
}
