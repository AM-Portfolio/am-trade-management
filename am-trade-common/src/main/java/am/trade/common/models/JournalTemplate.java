package am.trade.common.models;

import am.trade.common.models.enums.JournalTemplateCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain model for journal templates
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class JournalTemplate {

    private String id;
    private String name;
    private String description;
    private JournalTemplateCategory category;

    // Template structure - defines the fields in this template
    private List<TemplateField> fields;

    // Metadata
    private Boolean isSystemTemplate; // System templates cannot be deleted by users
    private Boolean isRecommended; // Recommended templates appear in "Recommended" section
    private Integer usageCount; // Track how many times this template has been used

    // User tracking
    private String createdBy; // User ID who created this template
    private List<String> favoriteUserIds; // List of user IDs who favorited this template

    // Tags for categorization and search
    private List<String> tags;

    // Preview/thumbnail
    private String thumbnailUrl;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
