package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain model for behavior pattern summary representing a day's trading activities
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Behavior pattern summary for a trading day")
public class BehaviorPatternSummary {
    
    @Schema(description = "Brief summary of the day's trading activities and observations", 
            example = "Active trading day with 3 positions. Strong bullish momentum in IT sector.")
    private String summary;
    
    @Schema(description = "Overall mood for the day (e.g., CONFIDENT, ANXIOUS, NEUTRAL, OPTIMISTIC, CAUTIOUS)", 
            example = "CONFIDENT")
    private String mood;
    
    @Min(value = 1, message = "Market sentiment must be at least 1")
    @Max(value = 10, message = "Market sentiment must be at most 10")
    @Schema(description = "Market sentiment for the day (1-10: 1=bearish, 10=bullish)", 
            example = "7", minimum = "1", maximum = "10")
    private Integer marketSentiment;
    
    @Schema(description = "Tags categorizing the day's activities or themes",
            example = "[\"high-volatility\", \"options-trading\", \"profit-day\"]")
    private List<String> tags;
}
