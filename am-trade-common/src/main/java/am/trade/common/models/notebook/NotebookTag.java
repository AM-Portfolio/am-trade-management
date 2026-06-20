package am.trade.common.models.notebook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotebookTag {
    private String id;
    private String userId;
    private String name;
    private String color;
    private String createdAt;
    private String updatedAt;
}
