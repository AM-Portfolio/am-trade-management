package am.trade.api.dto;

import am.trade.common.models.Attachment;
import am.trade.common.models.BehaviorPatternSummary;
import am.trade.common.models.PreTradePlan;
import am.trade.common.models.PostTradeReview;
import am.trade.common.models.TradeExecution;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for trade journal entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Trade journal entry response with enhanced attachment support")
public class TradeJournalEntryResponse {

    @Schema(description = "Unique identifier for the journal entry")
    private String id;

    @Schema(description = "User ID who created the journal entry")
    private String userId;

    @Schema(description = "Associated trade ID (optional)")
    private String tradeId;

    @Schema(description = "Journal entry title")
    private String title;

    @Schema(description = "Journal entry content/body")
    private String content;

    @Schema(description = "Entry type")
    private String entryType;

    @Schema(description = "Journal status")
    private String journalStatus;

    @Schema(description = "Symbol for the trade setup")
    private String symbol;

    @Schema(description = "Setup identifier")
    private String setup;

    @Schema(description = "Trade direction (LONG/SHORT)")
    private String tradeDirection;

    @Schema(description = "Folder ID for organizing journal entries")
    private String folderId;

    @Schema(description = "Playbook/template ID associated with this entry")
    private String playbookId;

    @Schema(description = "Pre-trade plan details")
    private PreTradePlan preTradePlan;

    @Schema(description = "Actual trade execution details")
    private TradeExecution tradeExecution;

    @Schema(description = "Post-trade review details")
    private PostTradeReview postTradeReview;

    @Schema(description = "Plan adherence score (0-100)")
    private Double planAdherenceScore;

    @Schema(description = "Checklist completion percentage (0-100)")
    private Double checklistCompletionPct;

    @Schema(description = "Custom fields for extensibility")
    private Map<String, Object> customFields;

    @Schema(description = "Date of the journal entry")
    private LocalDateTime entryDate;

    // Legacy field - maintained for backward compatibility
    @Deprecated
    @Schema(description = "Legacy image URLs (deprecated - use attachments instead)")
    private List<String> imageUrls;

    // Enhanced attachment support with metadata
    @Schema(description = "List of attachments with metadata (fileName, fileUrl, fileType, uploadedAt, description)")
    private List<Attachment> attachments;

    // Additional URL fields for various resources
    @Schema(description = "URLs to chart analysis images")
    private List<String> chartUrls;

    @Schema(description = "URLs to PDF documents, notes, etc.")
    private List<String> documentUrls;

    @Schema(description = "URLs to video analysis or recordings")
    private List<String> videoUrls;

    @Schema(description = "External reference URLs (news articles, etc.)")
    private List<String> externalUrls;

    @Schema(description = "IDs of related trades")
    private List<String> relatedTradeIds;

    @Schema(description = "Notebook Tag IDs associated with this entry")
    private List<String> tagIds;

    @Schema(description = "List of daily behavior pattern summaries")
    private List<BehaviorPatternSummary> behaviorPatternSummaries;

    @Schema(description = "Timestamp when the entry was created")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when the entry was last updated")
    private LocalDateTime updatedAt;
}
