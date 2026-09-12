package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lesson learned extracted from a journal post-trade review
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LessonLearnedResponse {

    private String entryId;
    private String symbol;
    private String lessonLearned;
    private LocalDateTime entryDate;
    private String mistakeCategory;
    private Integer executionScore;
}
