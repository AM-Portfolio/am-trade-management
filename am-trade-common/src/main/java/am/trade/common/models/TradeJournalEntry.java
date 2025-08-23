package am.trade.common.models;

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
public class TradeJournalEntry {
    
    private String id;
    private String userId;
    private String tradeId;
    private String title;
    private String content;
    private String mood;
    private Integer marketSentiment;
    private List<String> tags;
    private Map<String, Object> customFields;
    private LocalDateTime entryDate;
    private List<String> imageUrls;
    private List<String> relatedTradeIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
