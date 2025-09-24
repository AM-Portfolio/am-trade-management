package am.trade.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import am.trade.models.document.Trade;
import am.trade.models.enums.OrderStatus;

/**
 * Repository interface for Trade document
 */
@Repository
public interface TradeRepository extends MongoRepository<Trade, String> {
    
    Optional<Trade> findByTradeId(String tradeId);
    
    Optional<Trade> findByOrderId(String orderId);
    
    List<Trade> findBySymbol(String symbol);
    
    @Query("{'trade_date': {$gte: ?0, $lte: ?1}}")
    List<Trade> findByTradeDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by portfolio ID with pagination
     */
    Page<Trade> findByPortfolioId(String portfolioId, Pageable pageable);
    
    @Query("{'settlement_date': {$gte: ?0, $lte: ?1}}")
    List<Trade> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Trade> findByStatus(OrderStatus status);
    
    List<Trade> findByPortfolioId(String portfolioId);
    
    List<Trade> findByTraderId(String traderId);
    
    List<Trade> findByCounterpartyId(String counterpartyId);
    
    List<Trade> findByStrategyId(String strategyId);
    
    @Query("{'symbol': ?0, 'trade_date': {$gte: ?1, $lte: ?2}}")
    List<Trade> findBySymbolAndTradeDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    Page<Trade> findBySymbol(String symbol, Pageable pageable);
    
    Page<Trade> findByStatus(OrderStatus status, Pageable pageable);
    
    @Query("{'portfolio_id': ?0, 'trade_date': {$gte: ?1, $lte: ?2}}")
    Page<Trade> findByPortfolioIdAndTradeDateBetween(String portfolioId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
