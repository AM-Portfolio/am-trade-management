package am.trade.api.dto;

import am.trade.common.models.enums.JournalTemplateCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for journal templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JournalTemplateResponse {

    private String id;
    private String name;
    private String description;
    private JournalTemplateCategory category;

    private List<TemplateFieldResponse> fields;

    private Boolean isSystemTemplate;
    private Boolean isRecommended;
    private Integer usageCount;

    private String createdBy;

    // Computed field - indicates if current user has favorited this template
    private Boolean isFavorite;

    private List<String> tags;
    private String thumbnailUrl;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
