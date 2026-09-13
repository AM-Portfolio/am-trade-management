package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mistake category aggregation for journal analytics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MistakeAnalysisResponse {

    private String mistakeCategory;
    private long occurrenceCount;
    private Double totalPnlImpact;
    private Double avgPnlPerOccurrence;
    private Double percentageOfLosses;
}
