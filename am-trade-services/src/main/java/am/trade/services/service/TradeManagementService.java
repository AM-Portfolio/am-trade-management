package am.trade.services.service;

import am.trade.common.models.TradeDetails;
import am.trade.services.model.TradeSummary;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Service interface for trade management operations including calendar-based analytics
 */
public interface TradeManagementService {
    
    /**
     * Get trade details for a specific day grouped by portfolio
     * 
     * @param date The date to retrieve trades for
     * @param portfolioId Optional portfolio ID to filter by (can be null for all portfolios)
     * @return Map of portfolio IDs to their trade details for the specified day
     */
    Map<String, List<TradeDetails>> getTradeDetailsByDay(LocalDate date, String portfolioId);
    
    /**
     * Get trade details for a specific month grouped by portfolio
     * 
     * @param year The year
     * @param month The month (1-12)
     * @param portfolioId Optional portfolio ID to filter by (can be null for all portfolios)
     * @return Map of portfolio IDs to their trade details for the specified month
     */
    Map<String, List<TradeDetails>> getTradeDetailsByMonth(int year, int month, String portfolioId);
    
    /**
     * Get trade details for a specific quarter grouped by portfolio
     * 
     * @param year The year
     * @param quarter The quarter (1-4)
     * @param portfolioId Optional portfolio ID to filter by (can be null for all portfolios)
     * @return Map of portfolio IDs to their trade details for the specified quarter
     */
    Map<String, List<TradeDetails>> getTradeDetailsByQuarter(int year, int quarter, String portfolioId);
    
    /**
     * Get trade details for a specific financial year grouped by portfolio
     * Financial year is considered from April 1 to March 31
     * 
     * @param financialYear The financial year (e.g., 2025 for FY 2024-2025)
     * @param portfolioId Optional portfolio ID to filter by (can be null for all portfolios)
     * @return Map of portfolio IDs to their trade details for the specified financial year
     */
    Map<String, List<TradeDetails>> getTradeDetailsByFinancialYear(int financialYear, String portfolioId);
    
    /**
     * Get trade details for a custom date range grouped by portfolio
     * 
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @param portfolioId Optional portfolio ID to filter by (can be null for all portfolios)
     * @return Map of portfolio IDs to their trade details for the specified date range
     */
    Map<String, List<TradeDetails>> getTradeDetailsByDateRange(LocalDate startDate, LocalDate endDate, String portfolioId);
    
    /**
     * Get paginated trade details for a specific portfolio
     * 
     * @param portfolioId The portfolio ID
     * @param pageable Pagination information
     * @return Page of trade details for the specified portfolio
     */
    Page<TradeDetails> getTradeDetailsByPortfolio(String portfolioId, Pageable pageable);
    
    /**
     * Get trade summary statistics for a specific portfolio and date range
     * 
     * @param portfolioId The portfolio ID
     * @param startDate The start date (inclusive)
     * @param endDate The end date (inclusive)
     * @return Summary statistics for the specified portfolio and date range
     */
    TradeSummary getTradeSummary(String portfolioId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get all trades for a specific portfolio
     * 
     * @param portfolioId The portfolio ID
     * @return List of all trade details for the specified portfolio
     */
    List<TradeDetails> getAllTradesByTradePortfolioId(String portfolioId);    
    /**
     * Get trades for a specific portfolio within a date range
     * If startDate and endDate are null, returns all trades for the portfolio
     * 
     * @param portfolioId The portfolio ID
     * @param startDate The start date (inclusive), can be null for all trades
     * @param endDate The end date (inclusive), can be null for all trades
     * @return List of trade details for the specified portfolio and date range
     */
    List<TradeDetails> getTradesByDateRange(String portfolioId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Get trades for a specific portfolio filtered by symbols
     * 
     * @param portfolioId The portfolio ID
     * @param symbols List of symbols to filter by
     * @return List of trade details for the specified portfolio and symbols
     */
    List<TradeDetails> getTradesBySymbols(String portfolioId, List<String> symbols);
}
