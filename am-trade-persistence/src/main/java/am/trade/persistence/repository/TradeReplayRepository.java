package am.trade.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import am.trade.persistence.entity.TradeReplay;

/**
 * Repository for TradeReplay entities
 */
@Repository
public interface TradeReplayRepository extends MongoRepository<TradeReplay, String> {
    
    /**
     * Find a trade replay by its replay ID
     * 
     * @param replayId The unique replay ID
     * @return Optional containing the trade replay if found
     */
    Optional<TradeReplay> findByReplayId(String replayId);
    
    /**
     * Find all trade replays for a specific symbol
     * 
     * @param symbol The trading symbol
     * @return List of trade replays
     */
    List<TradeReplay> findBySymbol(String symbol);
    
    /**
     * Find trade replays where either entry or exit date falls within a date range
     * 
     * @param startDate The start date of the range
     * @param endDate The end date of the range
     * @return List of trade replays
     */
    @Query("{'$or': [{'entry_date': {'$gte': ?0, '$lte': ?1}}, {'exit_date': {'$gte': ?0, '$lte': ?1}}]}")
    List<TradeReplay> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all trade replays for a specific portfolio
     * 
     * @param portfolioId The portfolio ID
     * @return List of trade replays
     */
    List<TradeReplay> findByPortfolioId(String portfolioId);
    
    /**
     * Find all trade replays for a specific strategy
     * 
     * @param strategyId The strategy ID
     * @return List of trade replays
     */
    List<TradeReplay> findByStrategyId(String strategyId);
    
    /**
     * Find all trade replays associated with an original trade
     * 
     * @param originalTradeId The original trade ID
     * @return List of trade replays
     */
    List<TradeReplay> findByOriginalTradeId(String originalTradeId);
    
    /**
     * Delete a trade replay by its replay ID
     * 
     * @param replayId The replay ID to delete
     * @return Number of documents deleted
     */
    long deleteByReplayId(String replayId);
}
