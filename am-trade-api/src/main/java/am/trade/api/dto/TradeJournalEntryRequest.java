package am.trade.api.dto;

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
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String tradeId; // Optional, can be null for general journal entries
    
    @NotBlank(message = "Title is required")
    private String title;
    
    @NotBlank(message = "Content is required")
    private String content;
    
    private String mood; // Trading mood/psychology (e.g., CONFIDENT, ANXIOUS, NEUTRAL)
    
    private Integer marketSentiment; // Scale of 1-10, 1 being bearish, 10 being bullish
    
    private List<String> tags;
    
    private Map<String, Object> customFields;
    
    @NotNull(message = "Entry date is required")
    private LocalDateTime entryDate;
    
    private List<String> imageUrls; // URLs to attached images
    
    private List<String> relatedTradeIds; // Other related trades
}
