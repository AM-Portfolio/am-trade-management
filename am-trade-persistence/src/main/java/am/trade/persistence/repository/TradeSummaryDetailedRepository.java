package am.trade.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import am.trade.common.models.TradeSummaryDetailed;

/**
 * Repository interface for TradeSummaryDetailed document
 * Handles the storage and retrieval of detailed trade metrics and analysis
 */
@Repository
public interface TradeSummaryDetailedRepository extends MongoRepository<TradeSummaryDetailed, String> {
    
    Optional<TradeSummaryDetailed> findById(String id);
    
    Optional<TradeSummaryDetailed> findByTradeSummaryBasicId(String tradeSummaryBasicId);
    
    @Query("{'last_calculated_timestamp': {$gte: ?0}}")
    List<TradeSummaryDetailed> findByLastCalculatedTimestampAfter(long timestamp);
    
    @Query("{'trade_summary_basic_id': ?0, 'last_calculated_timestamp': {$gte: ?1}}")
    Optional<TradeSummaryDetailed> findLatestByTradeSummaryBasicId(String tradeSummaryBasicId, long timestamp);
    
    void deleteByTradeSummaryBasicId(String tradeSummaryBasicId);
}
