package am.trade.api.service.impl;

import am.trade.api.service.TradeSummaryService;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;
import am.trade.api.service.TradeManagementService;
// Using fully qualified name for service layer TradeSummaryService to avoid collision

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of TradeSummaryService that handles time period logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSummaryServiceImpl implements TradeSummaryService {

    private final TradeManagementService tradeManagementService;
    private final am.trade.services.service.TradeSummaryService serviceLayerTradeSummaryService;

    /**
     * Find a basic trade summary by ID
     * 
     * @param id The ID of the basic trade summary
     * @return Optional containing the basic trade summary if found, empty otherwise
     */
    @Override
    public Optional<TradeSummaryBasic> findBasicById(String id) {
        String processId = UUID.randomUUID().toString();
        log.debug("[{}] Finding basic trade summary with ID: {}", processId, id);
        Optional<TradeSummaryBasic> result = serviceLayerTradeSummaryService.findBasicById(id);
        log.debug("[{}] Basic trade summary found: {}", processId, result.isPresent());
        return result;
    }
    
    /**
     * Find a detailed trade summary by ID
     * 
     * @param id The ID of the detailed trade summary
     * @return Optional containing the detailed trade summary if found, empty otherwise
     */
    @Override
    public Optional<TradeSummaryDetailed> findDetailedById(String id) {
        String processId = UUID.randomUUID().toString();
        log.debug("[{}] Finding detailed trade summary with ID: {}", processId, id);
        Optional<TradeSummaryDetailed> result = serviceLayerTradeSummaryService.findDetailedById(id);
        log.debug("[{}] Detailed trade summary found: {}", processId, result.isPresent());
        return result;
    }
    
    /**
     * Find a detailed trade summary by basic summary ID
     * 
     * @param basicId The ID of the basic trade summary
     * @return Optional containing the detailed trade summary if found, empty otherwise
     */
    @Override
    public Optional<TradeSummaryDetailed> findDetailedByBasicId(String basicId) {
        String processId = UUID.randomUUID().toString();
        log.debug("[{}] Finding detailed trade summary by basic ID: {}", processId, basicId);
        Optional<TradeSummaryDetailed> result = serviceLayerTradeSummaryService.findDetailedByBasicId(basicId);
        log.debug("[{}] Detailed trade summary found for basic ID: {}", processId, result.isPresent());
        return result;
    }
    
    /**
     * Find all active basic trade summaries for an owner
     * 
     * @param ownerId The owner ID
     * @return List of active basic trade summaries
     */
    @Override
    public List<TradeSummaryBasic> findAllActiveBasicByOwnerId(String ownerId) {
        String processId = UUID.randomUUID().toString();
        log.debug("[{}] Finding all active basic trade summaries for owner: {}", processId, ownerId);
        List<TradeSummaryBasic> results = serviceLayerTradeSummaryService.findAllActiveBasicByOwnerId(ownerId);
        log.debug("[{}] Found {} active basic trade summaries for owner: {}", processId, results.size(), ownerId);
        return results;
    }
    
    /**
     * Save a composite trade summary
     * 
     * @param tradeSummary The composite trade summary to save
     * @return The saved composite trade summary
     */
    @Override
    public TradeSummary saveTradeSummary(TradeSummary tradeSummary) {
        String processId = UUID.randomUUID().toString();
        log.info("[{}] Saving trade summary with ID: {}", processId, tradeSummary.getId());
        TradeSummary result = serviceLayerTradeSummaryService.saveTradeSummary(tradeSummary);
        log.info("[{}] Successfully saved trade summary with ID: {}", processId, result.getId());
        return result;
    }
    
    /**
     * Update a composite trade summary
     * 
     * @param tradeSummary The composite trade summary to update
     * @return The updated composite trade summary
     */
    @Override
    public TradeSummary updateTradeSummary(TradeSummary tradeSummary) {
        String processId = UUID.randomUUID().toString();
        log.info("[{}] Updating trade summary with ID: {}", processId, tradeSummary.getId());
        TradeSummary result = serviceLayerTradeSummaryService.updateTradeSummary(tradeSummary);
        log.info("[{}] Successfully updated trade summary with ID: {}", processId, result.getId());
        return result;
    }
    
    /**
     * Delete a trade summary and its associated detailed metrics
     * 
     * @param id The ID of the basic trade summary to delete
     */
    @Override
    public void deleteTradeSummary(String id) {
        String processId = UUID.randomUUID().toString();
        log.info("[{}] Deleting trade summary with ID: {}", processId, id);
        serviceLayerTradeSummaryService.deleteTradeSummary(id);
        log.info("[{}] Successfully deleted trade summary with ID: {}", processId, id);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByTimePeriod(
            String periodType,
            LocalDate startDate,
            LocalDate endDate,
            Integer year,
            Integer month,
            Integer quarter,
            String portfolioId) {
        String processId = UUID.randomUUID().toString();
        log.info("[{}] Getting trade details by time period: {}, portfolioId: {}", processId, periodType, portfolioId);
        Map<String, List<TradeDetails>> result = null;
        // Validate and process based on period type
        switch (periodType.toUpperCase()) {
            case "DAY":
                if (startDate == null) {
                    throw new IllegalArgumentException("Start date is required for DAY period type");
                }
                log.debug("[{}] Fetching trade details for day: {}", processId, startDate);
                result = tradeManagementService.getTradeDetailsByDay(startDate, portfolioId);
                log.debug("[{}] Found {} day entries for date: {}", processId, result.size(), startDate);
                return result;
                
            case "MONTH":
                if (year == null || month == null) {
                    throw new IllegalArgumentException("Year and month are required for MONTH period type");
                }
                if (month < 1 || month > 12) {
                    throw new IllegalArgumentException("Month must be between 1 and 12");
                }
                log.debug("[{}] Fetching trade details for month: {}/{}", processId, year, month);
                result = tradeManagementService.getTradeDetailsByMonth(year, month, portfolioId);
                log.debug("[{}] Found {} day entries for month: {}/{}", processId, result.size(), year, month);
                return result;
                
            case "QUARTER":
                if (year == null || quarter == null) {
                    throw new IllegalArgumentException("Year and quarter are required for QUARTER period type");
                }
                if (quarter < 1 || quarter > 4) {
                    throw new IllegalArgumentException("Quarter must be between 1 and 4");
                }
                log.debug("[{}] Fetching trade details for quarter: {} Q{}", processId, year, quarter);
                result = tradeManagementService.getTradeDetailsByQuarter(year, quarter, portfolioId);
                log.debug("[{}] Found {} day entries for quarter: {} Q{}", processId, result.size(), year, quarter);
                return result;
                
            case "FINANCIAL_YEAR":
                if (year == null) {
                    throw new IllegalArgumentException("Year is required for FINANCIAL_YEAR period type");
                }
                log.debug("[{}] Fetching trade details for financial year: {}", processId, year);
                result = tradeManagementService.getTradeDetailsByFinancialYear(year, portfolioId);
                log.debug("[{}] Found {} day entries for financial year: {}", processId, result.size(), year);
                return result;
                
            case "CUSTOM":
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Start date and end date are required for CUSTOM period type");
                }
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date");
                }
                log.debug("[{}] Fetching trade details for date range: {} to {}", processId, startDate, endDate);
                result = tradeManagementService.getTradeDetailsByDateRange(startDate, endDate, portfolioId);
                log.debug("[{}] Found {} day entries for date range: {} to {}", processId, result.size(), startDate, endDate);
                return result;
                
            default:
                log.error("[{}] Invalid period type: {}", processId, periodType);
                throw new IllegalArgumentException("Invalid period type: " + periodType);
        }
    }
}
