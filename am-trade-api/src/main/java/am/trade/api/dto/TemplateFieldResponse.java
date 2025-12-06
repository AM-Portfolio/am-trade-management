package am.trade.api.dto;

import am.trade.common.models.enums.TemplateFieldType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO for template field definitions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TemplateFieldResponse {

    private String fieldId;
    private String fieldLabel;
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
