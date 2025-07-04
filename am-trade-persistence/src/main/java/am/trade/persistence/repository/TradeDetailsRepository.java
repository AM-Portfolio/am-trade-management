package am.trade.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import am.trade.common.models.enums.TradeStatus;
import am.trade.models.enums.OrderStatus;
import am.trade.persistence.entity.TradeDetailsEntity;

/**
 * Repository interface for Trade document
 */
@Repository
public interface TradeDetailsRepository extends MongoRepository<TradeDetailsEntity, String> {

    Optional<TradeDetailsEntity> findById(String id);
    
    Optional<TradeDetailsEntity> findByTradeId(String tradeId);
    
    List<TradeDetailsEntity> findBySymbol(String symbol);
    
    @Query("{'entryInfo.timestamp': {$gte: ?0, $lte: ?1}}")
    List<TradeDetailsEntity> findByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by portfolio ID with pagination
     */
    Page<TradeDetailsEntity> findByPortfolioId(String portfolioId, Pageable pageable);
    
    @Query("{'exitInfo.timestamp': {$gte: ?0, $lte: ?1}}")
    List<TradeDetailsEntity> findByExitDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<TradeDetailsEntity> findByStatus(TradeStatus status);
    
    List<TradeDetailsEntity> findByPortfolioId(String portfolioId);
    
    @Query("{'symbol': ?0, 'entryInfo.timestamp': {$gte: ?1, $lte: ?2}}")
    List<TradeDetailsEntity> findBySymbolAndEntryDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("{'symbol': ?0, 'exitInfo.timestamp': {$gte: ?1, $lte: ?2}}")
    List<TradeDetailsEntity> findBySymbolAndExitDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<TradeDetailsEntity> findBySymbol(String symbol, Pageable pageable);
    
    Page<TradeDetailsEntity> findByStatus(TradeStatus status, Pageable pageable);
    
    /**
     * Find all trade details belonging to any of the provided portfolio IDs
     * 
     * @param portfolioIds List of portfolio IDs to search for
     * @return List of trade details belonging to any of the specified portfolios
     */
    @Query("{'portfolioId': {$in: ?0}}")
    List<TradeDetailsEntity> findByPortfolioIdIn(List<String> portfolioIds);
    
    /**
     * Find all trade details belonging to any of the provided portfolio IDs with pagination
     * 
     * @param portfolioIds List of portfolio IDs to search for
     * @param pageable Pagination information
     * @return Page of trade details belonging to any of the specified portfolios
     */
    @Query("{'portfolioId': {$in: ?0}}")
    Page<TradeDetailsEntity> findByPortfolioIdIn(List<String> portfolioIds, Pageable pageable);
    
    @Query("{'portfolio_id': ?0, 'trade_date': {$gte: ?1, $lte: ?2}}")
    Page<TradeDetailsEntity> findByPortfolioIdAndTradeDateBetween(String portfolioId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    List<TradeDetailsEntity> findByTradeIdIn(List<String> tradeIds);
}
