package am.trade.services.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import am.trade.models.enums.OrderStatus;
import am.trade.models.dto.TradeDTO;

/**
 * Service interface for trade operations
 */
public interface TradeService {
    
    /**
     * Create a new trade
     */
    TradeDTO createTrade(TradeDTO tradeDTO);
    
    /**
     * Update an existing trade
     */
    TradeDTO updateTrade(String id, TradeDTO tradeDTO);
    
    /**
     * Check if a trade exists by ID
     */
    boolean existsById(String id);
    
    /**
     * Find a trade by ID
     */
    Optional<TradeDTO> findById(String id);
    
    /**
     * Find a trade by trade ID
     */
    Optional<TradeDTO> findByTradeId(String tradeId);
    
    /**
     * Find all trades with pagination
     */
    Page<TradeDTO> findAllTrades(Pageable pageable);
    
    /**
     * Find trades by portfolio ID with pagination
     */
    Page<TradeDTO> findByPortfolioId(String portfolioId, Pageable pageable);
    
    /**
     * Find a trade by order ID
     */
    Optional<TradeDTO> findByOrderId(String orderId);
    
    /**
     * Find all trades for a symbol
     */
    List<TradeDTO> findBySymbol(String symbol);
    
    /**
     * Find trades by date range
     */
    List<TradeDTO> findByTradeDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by settlement date range
     */
    List<TradeDTO> findBySettlementDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by status
     */
    List<TradeDTO> findByStatus(OrderStatus status);
    
    /**
     * Find trades by portfolio ID
     */
    List<TradeDTO> findByPortfolioId(String portfolioId);
    
    /**
     * Find trades by trader ID
     */
    List<TradeDTO> findByTraderId(String traderId);
    
    /**
     * Find trades by symbol and date range
     */
    List<TradeDTO> findBySymbolAndTradeDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find trades by symbol with pagination
     */
    Page<TradeDTO> findBySymbol(String symbol, Pageable pageable);
    
    /**
     * Find trades by status with pagination
     */
    Page<TradeDTO> findByStatus(OrderStatus status, Pageable pageable);
    
    /**
     * Find trades by portfolio ID and date range with pagination
     */
    Page<TradeDTO> findByPortfolioIdAndTradeDateBetween(String portfolioId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    
    /**
     * Delete a trade by ID
     */
    void deleteTrade(String id);
    
    /**
     * Update trade status
     */
    TradeDTO updateTradeStatus(String id, OrderStatus status);
}
