package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Domain model for trade journal entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TradeJournalEntry {

    private String id;
    private String userId;
    private String tradeId;
    private String title;
    private String content;
    private Map<String, Object> customFields;
    private LocalDateTime entryDate;

    // Legacy field - maintained for backward compatibility
    @Deprecated
    private List<String> imageUrls;

    // Enhanced attachment support with metadata (similar to TradeDetails)
    private List<Attachment> attachments;

    // Additional URL fields for various resources
    private List<String> chartUrls; // Chart analysis images
    private List<String> documentUrls; // PDF documents, notes, etc.
    private List<String> videoUrls; // Video analysis or recordings
    private List<String> externalUrls; // External references (news, articles, etc.)

    private List<String> relatedTradeIds;
    private List<String> tagIds; // IDs of NotebookTags associated with this entry
    private List<BehaviorPatternSummary> behaviorPatternSummaries; // Daily behavior pattern summaries
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
