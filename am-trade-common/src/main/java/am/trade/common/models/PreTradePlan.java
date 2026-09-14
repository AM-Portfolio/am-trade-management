package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Domain model for Pre-Trade Plan in a journal entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class PreTradePlan {
    private String setupDescription;
    private String entryRationale;
    private String marketContext;
    private String strategy;
    private String setup;
    private Double plannedEntryPrice;
    private Double plannedStopLoss;
    private Double plannedTarget;
    private Double plannedQuantity;
    private Double plannedRiskAmount;
    private Double plannedRiskPercent;
    private Double plannedRRRatio;
    private List<String> setupChecklist;
    private List<String> confirmedChecklistItems;
}
