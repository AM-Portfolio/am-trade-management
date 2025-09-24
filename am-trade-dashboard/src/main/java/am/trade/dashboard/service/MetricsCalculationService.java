package am.trade.dashboard.service;

import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;

/**
 * Service interface for calculating and caching trade metrics
 * Provides methods to calculate detailed metrics, check if recalculation is needed,
 * and retrieve composite trade summaries with up-to-date metrics
 */
public interface MetricsCalculationService {

    /**
     * Calculate detailed metrics for a basic trade summary
     * This method performs the actual calculation and updates the lastCalculatedTimestamp
     *
     * @param basicSummary The basic trade summary to calculate metrics for
     * @return The detailed trade metrics
     */
    TradeSummaryDetailed calculateDetailedMetrics(TradeSummaryBasic basicSummary);

    /**
     * Check if metrics need to be recalculated based on cache expiry or data changes
     *
     * @param basicSummary The basic trade summary
     * @param detailedSummary The detailed trade metrics
     * @return true if metrics need recalculation, false otherwise
     */
    boolean needsRecalculation(TradeSummaryBasic basicSummary, TradeSummaryDetailed detailedSummary);

    /**
     * Get a composite trade summary with up-to-date metrics
     * This method will check if recalculation is needed and perform it if necessary
     *
     * @param basicSummaryId The ID of the basic trade summary
     * @return The composite trade summary with up-to-date metrics, or null if not found
     */
    TradeSummary getTradeSummaryWithMetrics(String basicSummaryId);
}
