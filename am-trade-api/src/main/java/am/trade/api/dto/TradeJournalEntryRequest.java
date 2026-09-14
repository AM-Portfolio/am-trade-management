package am.trade.api.dto;

import am.trade.common.models.Attachment;
import am.trade.common.models.BehaviorPatternSummary;
import am.trade.common.models.PreTradePlan;
import am.trade.common.models.PostTradeReview;
import am.trade.common.models.TradeExecution;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Request DTO for trade journal entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TradeJournalEntryRequest {

    private String tradeId; // Optional, can be null for general journal entries

    @NotBlank(message = "Title is required")
    private String title;

    private String content;

    private String entryType;
    private String journalStatus;
    private String symbol;
    private String setup;
    private String tradeDirection;
    private String folderId;
    private String playbookId;

    private PreTradePlan preTradePlan;
    private TradeExecution tradeExecution;
    private PostTradeReview postTradeReview;
    private Double planAdherenceScore;
    private Double checklistCompletionPct;

    private Map<String, Object> customFields;

    @NotNull(message = "Entry date is required")
    private LocalDateTime entryDate;

    // Legacy field - maintained for backward compatibility
    @Deprecated
    private List<String> imageUrls; // URLs to attached images

    // Enhanced attachment support with metadata (similar to TradeDetails)
    private List<Attachment> attachments;

    // Additional URL fields for various resources
    private List<String> chartUrls; // Chart analysis images
    private List<String> documentUrls; // PDF documents, notes, etc.
    private List<String> videoUrls; // Video analysis or recordings
    private List<String> externalUrls; // External references (news, articles, etc.)

    private List<String> relatedTradeIds; // Other related trades

    private List<String> tagIds; // Notebook Tag IDs

    private List<BehaviorPatternSummary> behaviorPatternSummaries; // Daily behavior pattern summaries
}
