package am.trade.api.service;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Service interface for trade summary operations in the API layer
 */
public interface TradeSummaryService {
    
    /**
     * Find a basic trade summary by ID
     * 
     * @param id The ID of the basic trade summary
     * @return Optional containing the basic trade summary if found, empty otherwise
     */
    Optional<TradeSummaryBasic> findBasicById(String id);
    
    /**
     * Find a detailed trade summary by ID
     * 
     * @param id The ID of the detailed trade summary
     * @return Optional containing the detailed trade summary if found, empty otherwise
     */
    Optional<TradeSummaryDetailed> findDetailedById(String id);
    
    /**
     * Find a detailed trade summary by basic summary ID
     * 
     * @param basicId The ID of the basic trade summary
     * @return Optional containing the detailed trade summary if found, empty otherwise
     */
    Optional<TradeSummaryDetailed> findDetailedByBasicId(String basicId);
    
    /**
     * Find all active basic trade summaries for an owner
     * 
     * @param ownerId The owner ID
     * @return List of active basic trade summaries
     */
    List<TradeSummaryBasic> findAllActiveBasicByOwnerId(String ownerId);
    
    /**
     * Save a composite trade summary
     * 
     * @param tradeSummary The composite trade summary to save
     * @return The saved composite trade summary
     */
    TradeSummary saveTradeSummary(TradeSummary tradeSummary);
    
    /**
     * Update a composite trade summary
     * 
     * @param tradeSummary The composite trade summary to update
     * @return The updated composite trade summary
     */
    TradeSummary updateTradeSummary(TradeSummary tradeSummary);
    
    /**
     * Delete a trade summary and its associated detailed metrics
     * 
     * @param id The ID of the basic trade summary to delete
     */
    void deleteTradeSummary(String id);
    
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
}
