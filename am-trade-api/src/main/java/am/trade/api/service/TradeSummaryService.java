package am.trade.api.service;

import am.trade.common.models.TradeDetails;
import am.trade.services.model.TradeSummary;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for trade summary operations in the API layer
 */
public interface TradeSummaryService {
    
    /**
     * Get trade details based on time period type and parameters
     * 
     * @param periodType The type of time period (DAY, MONTH, QUARTER, FINANCIAL_YEAR, CUSTOM)
     * @param startDate Start date for the period (required for DAY and CUSTOM)
     * @param endDate End date for the period (required for CUSTOM)
     * @param year Year value (required for MONTH, QUARTER, FINANCIAL_YEAR)
     * @param month Month value (required for MONTH)
     * @param quarter Quarter value (required for QUARTER)
     * @param portfolioId Optional portfolio ID to filter by
     * @return Map of portfolio IDs to their trade details for the specified period
     */
    Map<String, List<TradeDetails>> getTradeDetailsByTimePeriod(
            String periodType,
            LocalDate startDate,
            LocalDate endDate,
            Integer year,
            Integer month,
            Integer quarter,
            String portfolioId);
    
    /**
     * Get trade summary for a specific portfolio and date range
     * 
     * @param portfolioId The portfolio ID
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Summary statistics for the specified portfolio and date range
     */
    TradeSummary getTradeSummary(String portfolioId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get daily trade summaries for a specific portfolio and month
     * 
     * @param portfolioId The portfolio ID
     * @param year The year
     * @param month The month (1-12)
     * @return Map of dates to their trade summaries for the specified month
     */
    Map<LocalDate, TradeSummary> getDailyTradeSummaries(String portfolioId, int year, int month);
}
