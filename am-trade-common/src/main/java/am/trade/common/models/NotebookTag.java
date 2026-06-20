package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain model for notebook tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookTag {

    private String id;
    private String userId;
    private String name;
    private String color;
    private String description;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
