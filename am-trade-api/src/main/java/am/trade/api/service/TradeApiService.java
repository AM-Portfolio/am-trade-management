package am.trade.api.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;

/**
 * Service interface for handling trade API operations
 */
public interface TradeApiService {
    
    /**
     * Get trade details by portfolio ID and optional symbols
     * 
     * @param portfolioId The portfolio ID
     * @param symbols Optional list of symbols to filter by
     * @return List of trade details
     */
    List<TradeDetails> getTradeDetailsByPortfolioAndSymbols(String portfolioId, List<String> symbols);
    
    /**
     * Add a new trade
     * 
     * @param tradeDetails The trade details to add
     * @return The saved trade details
     */
    TradeDetails addTrade(TradeDetails tradeDetails);
    
    /**
     * Update an existing trade
     * 
     * @param tradeId The ID of the trade to update
     * @param tradeDetails The updated trade details
     * @return The updated trade details
     */
    TradeDetails updateTrade(String tradeId, TradeDetails tradeDetails);
    
    /**
     * Get trades by filters
     * 
     * @param portfolioIds Portfolio IDs to filter by
     * @param symbols Symbols to filter by
     * @param statuses Trade statuses to filter by
     * @param startDate Start date for filtering trades
     * @param endDate End date for filtering trades
     * @param strategies Strategies to filter by
     * @param pageable Pagination information
     * @return Page of filtered trade details
     */
    Page<TradeDetails> getTradesByFilters(
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            List<String> strategies,
            Pageable pageable);
    
    /**
     * Add or update multiple trades
     * 
     * @param tradeDetailsList List of trade details to add or update
     * @return List of saved trade details
     */
    List<TradeDetails> addOrUpdateTrades(List<TradeDetails> tradeDetailsList);
    
    /**
     * Get trade details by list of trade IDs
     * 
     * @param tradeIds List of trade IDs to fetch
     * @return List of trade details matching the provided IDs
     */
    List<TradeDetails> getTradeDetailsByTradeIds(List<String> tradeIds);
}
