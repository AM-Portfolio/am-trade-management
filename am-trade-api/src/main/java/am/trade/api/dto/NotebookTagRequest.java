package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for notebook tags
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class NotebookTagRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Name is required")
    private String name;

    private String color;

    private String description;
}
