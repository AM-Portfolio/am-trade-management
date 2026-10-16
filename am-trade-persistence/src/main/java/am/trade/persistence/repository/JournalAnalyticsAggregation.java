package am.trade.persistence.repository;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * Mongo aggregation results for journal analytics (mapped in service layer).
 */
public final class JournalAnalyticsAggregation {

    private JournalAnalyticsAggregation() {
    }

    @Data
    @Builder
    public static class Summary {
        private long plannedCount;
        private long openCount;
        private long completedCount;
        private long archivedCount;
        private long totalTrades;
        private double totalPnl;
        private int pnlCount;
        private int wins;
        private double rrSum;
        private int rrCount;
    }

    @Data
    @Builder
    public static class MistakeGroup {
        private String mistakeCategory;
        private long occurrenceCount;
        private double totalPnlImpact;
        private long lossOccurrences;
    }

    @Data
    @Builder
    public static class MistakesResult {
        @Builder.Default
        private List<MistakeGroup> groups = new ArrayList<>();
        private long totalLosses;
    }

    @Data
    @Builder
    public static class Adherence {
        private Double avgPlanAdherenceScore;
        private Double avgChecklistCompletionPct;
        private Double avgExecutionScore;
        private int adherenceCount;
        private int checklistCount;
        private int execCount;
    }
}
