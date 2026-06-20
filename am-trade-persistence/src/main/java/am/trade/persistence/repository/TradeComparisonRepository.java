package am.trade.persistence.repository;

import am.trade.common.models.TradeDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for trade comparison operations
 */
@Repository
public interface TradeComparisonRepository extends MongoRepository<TradeDetails, String> {
    
    /**
     * Find trades by user ID and entry date between start and end dates
     * 
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of trades
     */
    @Query("{'userId': ?0, 'entryInfo.timestamp': {$gte: ?1, $lte: ?2}}")
    List<TradeDetails> findByUserIdAndEntryDateBetween(
            String userId, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by user ID and strategy with optional date range
     * 
     * @param userId User ID
     * @param strategy Strategy name
     * @param startDate Optional start date (can be null)
     * @param endDate Optional end date (can be null)
     * @return List of trades
     */
    @Query("{'userId': ?0, 'entryReasoning.strategy': ?1, " +
           "$and: [" +
           "  {$or: [" +
           "    {$and: [{$expr: {$eq: [?2, null]}}, {$expr: {$eq: [?3, null]}}]}, " +
           "    {'entryInfo.timestamp': {$gte: ?2, $lte: ?3}}" +
           "  ]}" +
           "]}")
    List<TradeDetails> findByUserIdAndStrategyAndDateRange(
            String userId, String strategy, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by user ID and symbol with optional date range
     * 
     * @param userId User ID
     * @param symbol Symbol/instrument
     * @param startDate Optional start date (can be null)
     * @param endDate Optional end date (can be null)
     * @return List of trades
     */
    @Query("{'userId': ?0, 'symbol': ?1, " +
           "$and: [" +
           "  {$or: [" +
           "    {$and: [{$expr: {$eq: [?2, null]}}, {$expr: {$eq: [?3, null]}}]}, " +
           "    {'entryInfo.timestamp': {$gte: ?2, $lte: ?3}}" +
           "  ]}" +
           "]}")
    List<TradeDetails> findByUserIdAndSymbolAndDateRange(
            String userId, String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by user ID and symbol
     * 
     * @param userId User ID
     * @param symbol Symbol/instrument
     * @return List of trades
     */
    @Query("{'userId': ?0, 'symbol': ?1}")
    List<TradeDetails> findByUserIdAndSymbol(String userId, String symbol);
    
    /**
     * Find trades by user ID and tags
     * 
     * @param userId User ID
     * @param tags List of tags
     * @return List of trades
     */
    @Query("{'userId': ?0, 'tags': {$in: ?1}}")
    List<TradeDetails> findByUserIdAndTagsIn(String userId, List<String> tags);
    
    /**
     * Count trades by user ID and strategy
     * 
     * @param userId User ID
     * @param strategy Strategy name
     * @return Count of trades
     */
    @Query(value = "{'userId': ?0, 'entryReasoning.strategy': ?1}", count = true)
    long countByUserIdAndStrategy(String userId, String strategy);
    
    /**
     * Count trades by user ID and symbol
     * 
     * @param userId User ID
     * @param symbol Symbol/instrument
     * @return Count of trades
     */
    @Query(value = "{'userId': ?0, 'symbol': ?1}", count = true)
    long countByUserIdAndSymbol(String userId, String symbol);
}
