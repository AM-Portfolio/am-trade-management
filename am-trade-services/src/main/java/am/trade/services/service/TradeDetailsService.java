package am.trade.services.service;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Custom repository interface for returning TradeDetails domain models directly
 */
public interface TradeDetailsService {
    
    Optional<TradeDetails> findModelById(String id);
    
    Optional<TradeDetails> findModelByTradeId(String tradeId);
    
    List<TradeDetails> findModelsBySymbol(String symbol);
    
    List<TradeDetails> findModelsByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    Page<TradeDetails> findModelsByPortfolioId(String portfolioId, Pageable pageable);
    
    List<TradeDetails> findModelsByExitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TradeDetails> findModelsByStatus(TradeStatus status);
    
    List<TradeDetails> findModelsByPortfolioId(String portfolioId);
    
    List<TradeDetails> findModelsBySymbolAndEntryDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    List<TradeDetails> findModelsBySymbolAndExitDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<TradeDetails> findModelsBySymbol(String symbol, Pageable pageable);
    
    Page<TradeDetails> findModelsByStatus(TradeStatus status, Pageable pageable);
    
    /**
     * Save a single TradeDetails model to the database
     * @param tradeDetails The trade details model to save
     * @return The saved trade details model with any generated IDs
     */
    TradeDetails saveTradeDetails(TradeDetails tradeDetails);
    
    /**
     * Save a list of TradeDetails models to the database
     * @param tradeDetailsList The list of trade details models to save
     * @return The list of saved trade details models with any generated IDs
     */
    List<TradeDetails> saveAllTradeDetails(List<TradeDetails> tradeDetailsList);
    
    /**
     * Find trade details by multiple trade IDs in a single database call
     * @param tradeIds List of trade IDs to search for
     * @return List of trade details matching the provided IDs
     */
    List<TradeDetails> findModelsByTradeIds(List<String> tradeIds);
    
    /**
     * Find trade details by portfolio IDs and entry timestamp between given dates
     * @param portfolioIds List of portfolio IDs to search for
     * @param startDate Start date for entry timestamp range
     * @param endDate End date for entry timestamp range
     * @return List of trade details matching the criteria
     */
    List<TradeDetails> findByPortfolioIdInAndEntryInfoTimestampBetween(List<String> portfolioIds, LocalDateTime startDate, LocalDateTime endDate);
}
