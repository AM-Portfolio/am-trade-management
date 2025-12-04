package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Request DTO for using a template to create a journal entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UseTemplateRequest {

    @NotBlank(message = "User ID is required")
    private String userId;

    @NotBlank(message = "Template ID is required")
    private String templateId;

    // Field values filled by the user
    // Key: fieldId, Value: user's input for that field
    private Map<String, Object> fieldValues;

    // Optional: Associate with a specific trade
    private String tradeId;

    // Optional: Custom title for the journal entry (overrides template name)
    private String customTitle;
}
