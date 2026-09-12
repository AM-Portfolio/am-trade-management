package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain model for Post-Trade Review in a journal entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PostTradeReview {
    private LocalDateTime exitDateTime;
    private Double actualExitPrice;
    private Double actualPnl;
    private Double actualRMultiple;
    private String followedStopLoss;
    private String followedTarget;
    private String tradeOutcome;
    private String whatWentWell;
    private String whatCouldBeImproved;
    private String lessonLearned;
    private String mistakeCategory;
    private Integer executionScore;
    private String emotionalState;
    private List<String> postChecklist;
    private List<String> completedChecklistItems;
}
