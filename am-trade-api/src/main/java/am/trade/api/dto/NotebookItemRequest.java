package am.trade.api.dto;

import am.trade.common.models.enums.NotebookItemType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Request DTO for notebook items
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookItemRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotNull(message = "Type is required")
    private NotebookItemType type;

    private String parentId;

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    private List<String> tagIds;

    private Map<String, Object> metadata;

    private Map<String, Object> goalDetails;
}
