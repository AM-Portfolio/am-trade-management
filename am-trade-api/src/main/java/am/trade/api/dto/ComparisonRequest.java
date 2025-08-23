package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO for trade performance comparisons
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ComparisonRequest {
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotBlank(message = "Comparison type is required")
    private String comparisonType; // TIME_PERIOD, PORTFOLIO, STRATEGY, INSTRUMENT
    
    // For portfolio comparison
    private List<String> portfolioIds;
    
    // For time period comparison
    private List<TimePeriod> timePeriods;
    
    // For strategy comparison
    private List<String> strategies;
    
    // For instrument comparison
    private List<String> instruments;
    
    // Optional filters
    private String startDate;
    private String endDate;
    private List<String> includedMetrics;
    private Boolean normalizeResults;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimePeriod {
        @NotBlank(message = "Period name is required")
        private String name;
        
        @NotBlank(message = "Start date is required")
        private String startDate;
        
        @NotBlank(message = "End date is required")
        private String endDate;
    }
}
