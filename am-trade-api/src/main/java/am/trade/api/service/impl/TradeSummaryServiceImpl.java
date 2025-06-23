package am.trade.api.service.impl;

import am.trade.api.service.TradeSummaryService;
import am.trade.common.models.TradeDetails;
import am.trade.services.model.TradeSummary;
import am.trade.services.service.TradeManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TradeSummaryService that handles time period logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSummaryServiceImpl implements TradeSummaryService {

    private final TradeManagementService tradeManagementService;

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

    @Override
    public TradeSummary getTradeSummary(String portfolioId, LocalDate startDate, LocalDate endDate) {
        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID is required");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        return tradeManagementService.getTradeSummary(portfolioId, startDate, endDate);
    }

    @Override
    public Map<LocalDate, TradeSummary> getDailyTradeSummaries(String portfolioId, int year, int month) {
        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID is required");
        }
        
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        
        // Calculate start and end dates for the month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // Generate daily summaries
        return startDate.datesUntil(endDate.plusDays(1))
            .collect(Collectors.toMap(
                date -> date,
                date -> tradeManagementService.getTradeSummary(portfolioId, date, date)
            ));
    }
}
