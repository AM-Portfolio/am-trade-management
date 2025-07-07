package am.trade.api.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeSummary;
import am.trade.common.models.enums.TradeStatus;
import am.trade.services.service.TradeDetailsService;
import am.trade.api.service.TradeManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TradeManagementService that provides calendar-based trade analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeManagementServiceImpl implements TradeManagementService {

    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final TradeDetailsService tradeDetailsService;

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByDay(LocalDate date, String portfolioId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        
        return getTradeDetailsByDateTimeRange(startOfDay, endOfDay, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByMonth(int year, int month, String portfolioId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByQuarter(int year, int quarter, String portfolioId) {
        // Calculate the start month of the quarter (1->1, 2->4, 3->7, 4->10)
        int startMonth = (quarter - 1) * 3 + 1;
        
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);
        
        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByFinancialYear(int financialYear, String portfolioId) {
        // Financial year is from April 1 to March 31
        // For FY 2024-2025, financialYear parameter would be 2025
        int startYear = financialYear - 1;
        
        LocalDate startDate = LocalDate.of(startYear, Month.APRIL, 1);
        LocalDate endDate = LocalDate.of(financialYear, Month.MARCH, 31);
        
        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByDateRange(LocalDate startDate, LocalDate endDate, String portfolioId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        return getTradeDetailsByDateTimeRange(startDateTime, endDateTime, portfolioId);
    }
    
    private Map<String, List<TradeDetails>> getTradeDetailsByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime, String portfolioId) {
        List<TradeDetails> trades;
        
        // If portfolio ID is provided, filter by it
        if (portfolioId != null && !portfolioId.isEmpty()) {
            // Get all trades for the portfolio
            trades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
            
            // Filter by date range
            trades = trades.stream()
                .filter(trade -> {
                    LocalDateTime tradeDate = trade.getEntryInfo() != null ? 
                        trade.getEntryInfo().getTimestamp() : null;
                    
                    return tradeDate != null && 
                        !tradeDate.isBefore(startDateTime) && 
                        !tradeDate.isAfter(endDateTime);
                })
                .collect(Collectors.toList());
        } else {
            // Get all trades in the date range
            trades = tradeDetailsService.findModelsByEntryDateBetween(startDateTime, endDateTime);
        }
        
        // Group trades by portfolio ID
        return trades.stream()
            .collect(Collectors.groupingBy(
                TradeDetails::getPortfolioId,
                Collectors.toList()
            ));
    }

    @Override
    public Page<TradeDetails> getTradeDetailsByPortfolio(String portfolioId, Pageable pageable) {
        return tradeDetailsService.findModelsByPortfolioId(portfolioId, pageable);
    }
    
    @Override
    public List<TradeDetails> getAllTradesByTradePortfolioId(String portfolioId) {
        return tradeDetailsService.findModelsByPortfolioId(portfolioId);
    }
    
    @Override
    public List<TradeDetails> getTradesByDateRange(String portfolioId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        List<TradeDetails> allTrades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        
        return allTrades.stream()
            .filter(trade -> {
                LocalDateTime tradeDate = trade.getEntryInfo() != null ? 
                    trade.getEntryInfo().getTimestamp() : null;
                
                return tradeDate != null && 
                    !tradeDate.isBefore(startDateTime) && 
                    !tradeDate.isAfter(endDateTime);
            })
            .collect(Collectors.toList());
    }

    @Override
    public List<TradeDetails> getTradesBySymbols(String portfolioId, List<String> symbols) {
        log.info("Fetching trades for portfolio: {} filtered by symbols: {}", portfolioId, symbols);
        
        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        if (symbols == null || symbols.isEmpty()) {
            // If no symbols provided, return all trades for the portfolio
            return getAllTradesByTradePortfolioId(portfolioId);
        }
        
        // Get all trades for the portfolio
        List<TradeDetails> allTrades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        
        // Filter by symbols (case-insensitive)
        return allTrades.stream()
                .filter(trade -> trade.getSymbol() != null && 
                        symbols.stream()
                                .anyMatch(symbol -> trade.getSymbol().equalsIgnoreCase(symbol)))
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<TradeDetails> getTradesByFilters(
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            List<String> strategies,
            Pageable pageable) {
        
        log.info("Fetching trades with filters - portfolioIds: {}, symbols: {}, statuses: {}, startDate: {}, endDate: {}, strategies: {}", 
                portfolioIds, symbols, statuses, startDate, endDate, strategies);
        
        // Collect all trades from the specified portfolios
        List<TradeDetails> allTrades;
        
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            // If no portfolio IDs provided, get all trades from all portfolios
            // This would require a method to get all trades, which might not be efficient
            // Consider implementing a repository method for this or limiting the scope
            throw new IllegalArgumentException("At least one portfolio ID must be provided");
        } else {
            // Get trades from all specified portfolios
            allTrades = portfolioIds.stream()
                    .flatMap(portfolioId -> tradeDetailsService.findModelsByPortfolioId(portfolioId).stream())
                    .collect(Collectors.toList());
        }
        
        // Apply filters
        List<TradeDetails> filteredTrades = allTrades.stream()
                // Filter by symbols if provided
                .filter(trade -> symbols == null || symbols.isEmpty() || 
                        (trade.getSymbol() != null && 
                         symbols.stream().anyMatch(symbol -> trade.getSymbol().equalsIgnoreCase(symbol))))
                
                // Filter by statuses if provided
                .filter(trade -> statuses == null || statuses.isEmpty() ||
                        (trade.getStatus() != null && statuses.contains(trade.getStatus())))
                
                // Filter by strategies if provided
                .filter(trade -> strategies == null || strategies.isEmpty() ||
                        (trade.getStrategy() != null && 
                         strategies.stream().anyMatch(strategy -> trade.getStrategy().equalsIgnoreCase(strategy))))
                
                // Filter by date range if provided
                .filter(trade -> {
                    // If no date range provided, include all trades
                    if (startDate == null && endDate == null) {
                        return true;
                    }
                    
                    LocalDate tradeDate = trade.getTradeDate();
                    if (tradeDate == null) {
                        return false; // Skip trades without a date
                    }
                    
                    // Check if the trade date is within the specified range
                    boolean afterStartDate = startDate == null || !tradeDate.isBefore(startDate);
                    boolean beforeEndDate = endDate == null || !tradeDate.isAfter(endDate);
                    
                    return afterStartDate && beforeEndDate;
                })
                .collect(Collectors.toList());
        
        // Apply pagination
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredTrades.size());
        
        // Handle case where start might be beyond the list size
        if (start > filteredTrades.size()) {
            return new PageImpl<>(List.of(), pageable, filteredTrades.size());
        }
        
        List<TradeDetails> pagedTrades = filteredTrades.subList(start, end);
        
        return new PageImpl<>(pagedTrades, pageable, filteredTrades.size());
    }

}
