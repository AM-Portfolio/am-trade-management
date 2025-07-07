package am.trade.api.service;

import am.trade.common.models.ProfitLossHeatmapData;

/**
 * Service for generating profit/loss heatmap data
 */
public interface ProfitLossHeatmapService {
    
    /**
     * Get yearly profit/loss heatmap data
     * 
     * @param portfolioId Portfolio ID
     * @param includeTradeDetails Whether to include trade details in the response
     * @return ProfitLossHeatmapData with yearly granularity
     */
    ProfitLossHeatmapData getYearlyHeatmap(String portfolioId, boolean includeTradeDetails);
    
    /**
     * Get monthly profit/loss heatmap data for a specific financial year
     * 
     * @param portfolioId Portfolio ID
     * @param financialYear Financial year (e.g., 2025 for FY 2025-26)
     * @param includeTradeDetails Whether to include trade details in the response
     * @return ProfitLossHeatmapData with monthly granularity
     */
    ProfitLossHeatmapData getMonthlyHeatmap(String portfolioId, int financialYear, boolean includeTradeDetails);
    
    /**
     * Get daily profit/loss heatmap data for a specific month
     * 
     * @param portfolioId Portfolio ID
     * @param year Year
     * @param month Month (1-12)
     * @param includeTradeDetails Whether to include trade details in the response
     * @return ProfitLossHeatmapData with daily granularity
     */
    ProfitLossHeatmapData getDailyHeatmap(String portfolioId, int year, int month, boolean includeTradeDetails);
}
