package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Aggregated journal summary metrics for a date range
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class JournalSummaryResponse {

    private long totalTrades;
    private Double totalPnl;
    private Double winRate;
    private Double avgRR;
    private long plannedCount;
    private long openCount;
    private long completedCount;
    private long archivedCount;
}
