package am.trade.api.dto;

import am.trade.common.models.enums.TemplateFieldType;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for template field definitions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TemplateFieldRequest {

    @NotBlank(message = "Field ID is required")
    private String fieldId;

    @NotBlank(message = "Field label is required")
    private String fieldLabel;

    @NotNull(message = "Field type is required")
    private TemplateFieldType fieldType;

    private String placeholder;
    private String defaultValue;
    private Boolean required;
    private Integer order;

    private List<String> options;

    private Integer minLength;
    private Integer maxLength;
    private String validationPattern;

    private String helpText;
}
