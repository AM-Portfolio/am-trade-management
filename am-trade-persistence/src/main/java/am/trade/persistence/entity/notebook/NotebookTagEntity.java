package am.trade.persistence.entity.notebook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notebook_tags")
public class NotebookTagEntity {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    private String name;
    private String color;
    
    private String createdAt;
    private String updatedAt;
}
