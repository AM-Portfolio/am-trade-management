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
        return serviceLayerTradeSummaryService.findBasicById(id);
    }
    
    /**
     * Find a detailed trade summary by ID
     * 
     * @param id The ID of the detailed trade summary
     * @return Optional containing the detailed trade summary if found, empty otherwise
     */
    @Override
    public Optional<TradeSummaryDetailed> findDetailedById(String id) {
        return serviceLayerTradeSummaryService.findDetailedById(id);
    }
    
    /**
     * Find a detailed trade summary by basic summary ID
     * 
     * @param basicId The ID of the basic trade summary
     * @return Optional containing the detailed trade summary if found, empty otherwise
     */
    @Override
    public Optional<TradeSummaryDetailed> findDetailedByBasicId(String basicId) {
        return serviceLayerTradeSummaryService.findDetailedByBasicId(basicId);
    }
    
    /**
     * Find all active basic trade summaries for an owner
     * 
     * @param ownerId The owner ID
     * @return List of active basic trade summaries
     */
    @Override
    public List<TradeSummaryBasic> findAllActiveBasicByOwnerId(String ownerId) {
        return serviceLayerTradeSummaryService.findAllActiveBasicByOwnerId(ownerId);
    }
    
    /**
     * Save a composite trade summary
     * 
     * @param tradeSummary The composite trade summary to save
     * @return The saved composite trade summary
     */
    @Override
    public TradeSummary saveTradeSummary(TradeSummary tradeSummary) {
        return serviceLayerTradeSummaryService.saveTradeSummary(tradeSummary);
    }
    
    /**
     * Update a composite trade summary
     * 
     * @param tradeSummary The composite trade summary to update
     * @return The updated composite trade summary
     */
    @Override
    public TradeSummary updateTradeSummary(TradeSummary tradeSummary) {
        return serviceLayerTradeSummaryService.updateTradeSummary(tradeSummary);
    }
    
    /**
     * Delete a trade summary and its associated detailed metrics
     * 
     * @param id The ID of the basic trade summary to delete
     */
    @Override
    public void deleteTradeSummary(String id) {
        serviceLayerTradeSummaryService.deleteTradeSummary(id);
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
        
        // Validate and process based on period type
        switch (periodType.toUpperCase()) {
            case "DAY":
                if (startDate == null) {
                    throw new IllegalArgumentException("Start date is required for DAY period type");
                }
                return tradeManagementService.getTradeDetailsByDay(startDate, portfolioId);
                
            case "MONTH":
                if (year == null || month == null) {
                    throw new IllegalArgumentException("Year and month are required for MONTH period type");
                }
                if (month < 1 || month > 12) {
                    throw new IllegalArgumentException("Month must be between 1 and 12");
                }
                return tradeManagementService.getTradeDetailsByMonth(year, month, portfolioId);
                
            case "QUARTER":
                if (year == null || quarter == null) {
                    throw new IllegalArgumentException("Year and quarter are required for QUARTER period type");
                }
                if (quarter < 1 || quarter > 4) {
                    throw new IllegalArgumentException("Quarter must be between 1 and 4");
                }
                return tradeManagementService.getTradeDetailsByQuarter(year, quarter, portfolioId);
                
            case "FINANCIAL_YEAR":
                if (year == null) {
                    throw new IllegalArgumentException("Year is required for FINANCIAL_YEAR period type");
                }
                return tradeManagementService.getTradeDetailsByFinancialYear(year, portfolioId);
                
            case "CUSTOM":
                if (startDate == null || endDate == null) {
                    throw new IllegalArgumentException("Start date and end date are required for CUSTOM period type");
                }
                if (startDate.isAfter(endDate)) {
                    throw new IllegalArgumentException("Start date cannot be after end date");
                }
                return tradeManagementService.getTradeDetailsByDateRange(startDate, endDate, portfolioId);
                
            default:
                throw new IllegalArgumentException("Invalid period type: " + periodType);
        }
    }
}
