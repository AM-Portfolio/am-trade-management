package am.trade.common.models;

import am.trade.common.models.enums.TemplateFieldType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents a field definition in a journal template
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TemplateField {

    private String fieldId;
    private String fieldLabel;
    private TemplateFieldType fieldType;
    private String placeholder;
    private String defaultValue;
    private Boolean required;
    private Integer order; // Display order in the template

    // For dropdown and checkbox list options
    private List<String> options;

    // Additional validation rules
    private Integer minLength;
    private Integer maxLength;
    private String validationPattern; // Regex pattern for validation

    // Help text or description for the field
    private String helpText;
}
