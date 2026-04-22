package am.trade.api.dto;

import am.trade.common.models.enums.JournalTemplateCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for creating/updating journal templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JournalTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    private String description;

    @NotNull(message = "Category is required")
    private JournalTemplateCategory category;

    @Valid
    private List<TemplateFieldRequest> fields;

    private Boolean isSystemTemplate;
    private Boolean isRecommended;

    @NotBlank(message = "Creator user ID is required")
    private String createdBy;

    private List<String> tags;
    private String thumbnailUrl;
}
