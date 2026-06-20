package am.trade.common.models.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeJournalEntry {
    private String id;
    private String userId;
    private String tradeId;
    private String title;
    private String content;
    private List<BehaviorPatternSummary> behaviorPatternSummaries;
    private Map<String, Object> customFields;
    private String entryDate;
    private List<String> imageUrls;
    private List<JournalAttachment> attachments;
    private List<String> relatedTradeIds;
    private List<String> tagIds;
    private String createdAt;
    private String updatedAt;
}
