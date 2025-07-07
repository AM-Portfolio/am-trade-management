package am.trade.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import am.trade.common.models.TradeSummaryBasic;

/**
 * Repository interface for TradeSummaryBasic document
 * Handles the storage and retrieval of basic trade summary information
 */
@Repository
public interface TradeSummaryBasicRepository extends MongoRepository<TradeSummaryBasic, String> {
    
    Optional<TradeSummaryBasic> findById(String id);
    
    Optional<TradeSummaryBasic> findByUserId(String userId);
    
    List<TradeSummaryBasic> findByOwnerId(String ownerId);
    
    @Query("{'start_date': {$gte: ?0}, 'end_date': {$lte: ?1}}")
    List<TradeSummaryBasic> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TradeSummaryBasic> findByPortfolioIdsContaining(String portfolioId);
    
    @Query("{'portfolio_ids': {$in: [?0]}, 'start_date': {$gte: ?1}, 'end_date': {$lte: ?2}}")
    List<TradeSummaryBasic> findByPortfolioIdAndDateRange(String portfolioId, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<TradeSummaryBasic> findByOwnerId(String ownerId, Pageable pageable);
    
    @Query("{'active': true}")
    List<TradeSummaryBasic> findAllActive();
    
    @Query("{'active': true, 'owner_id': ?0}")
    List<TradeSummaryBasic> findAllActiveByOwnerId(String ownerId);
}
