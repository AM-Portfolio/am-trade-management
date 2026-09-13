package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Plan / checklist / execution adherence averages
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalAdherenceResponse {

    private Double avgPlanAdherenceScore;
    private Double avgChecklistCompletionPct;
    private Double avgExecutionScore;
    private long sampleSize;
}
