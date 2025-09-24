package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class TradeJournalEntryResponse {
    
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
