package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Composite report card for a journal date range
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalReportCardResponse {

    private LocalDate from;
    private LocalDate to;
    private JournalSummaryResponse summary;
    private List<MistakeAnalysisResponse> mistakes;
    private JournalAdherenceResponse adherence;
    private List<LessonLearnedResponse> topLessons;
}
