package am.trade.analytics.service;

import am.trade.analytics.model.TradeReplay;
import am.trade.analytics.model.dto.TradeReplayRequest;
import am.trade.analytics.model.dto.TradeReplayResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for trade replay functionality
 * Provides methods for creating and retrieving trade replays
 */
public interface TradeReplayService {

    /**
     * Create a new trade replay analysis based on entry and exit dates
     * 
     * @param request The trade replay request containing entry/exit details
     * @return The created trade replay response with analysis results
     */
    TradeReplayResponse createTradeReplay(TradeReplayRequest request);
    
    /**
     * Get a trade replay by its ID
     * 
     * @param replayId The ID of the trade replay
     * @return Optional containing the trade replay if found
     */
    Optional<TradeReplay> getTradeReplayById(String replayId);
    
    /**
     * Find trade replays by symbol
     * 
     * @param symbol The stock symbol
     * @return List of trade replays for the given symbol
     */
    List<TradeReplay> findTradeReplaysBySymbol(String symbol);
    
    /**
     * Find trade replays by date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of trade replays within the date range
     */
    List<TradeReplay> findTradeReplaysByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trade replays by portfolio ID
     * 
     * @param portfolioId The portfolio ID
     * @return List of trade replays for the given portfolio
     */
    List<TradeReplay> findTradeReplaysByPortfolioId(String portfolioId);
    
    /**
     * Find trade replays by strategy ID
     * 
     * @param strategyId The strategy ID
     * @return List of trade replays for the given strategy
     */
    List<TradeReplay> findTradeReplaysByStrategyId(String strategyId);
    
    /**
     * Find trade replays by original trade ID
     * 
     * @param tradeId The original trade ID
     * @return List of trade replays for the given trade
     */
    List<TradeReplay> findTradeReplaysByOriginalTradeId(String tradeId);
    
    /**
     * Delete a trade replay by its ID
     * 
     * @param replayId The ID of the trade replay to delete
     * @return true if deleted successfully, false otherwise
     */
    boolean deleteTradeReplay(String replayId);
}
