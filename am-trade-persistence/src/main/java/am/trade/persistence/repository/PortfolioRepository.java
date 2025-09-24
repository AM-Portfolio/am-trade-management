package am.trade.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import am.trade.persistence.entity.PortfolioEntity;

/**
 * Repository interface for Portfolio entities
 * Provides CRUD operations and custom queries for portfolios
 */
@Repository
public interface PortfolioRepository extends MongoRepository<PortfolioEntity, String> {
    
    /**
     * Find a portfolio by its business ID
     * @param portfolioId The business ID of the portfolio
     * @return Optional containing the portfolio if found
     */
    Optional<PortfolioEntity> findByPortfolioId(String portfolioId);
    
    /**
     * Find all portfolios owned by a specific user
     * @param ownerId The ID of the portfolio owner
     * @return List of portfolios owned by the user
     */
    List<PortfolioEntity> findByOwnerId(String ownerId);
    
    /**
     * Find all active portfolios owned by a specific user
     * @param ownerId The ID of the portfolio owner
     * @param active Whether the portfolio is active
     * @return List of active portfolios owned by the user
     */
    List<PortfolioEntity> findByOwnerIdAndActive(String ownerId, boolean active);
    
    /**
     * Find portfolios with pagination
     * @param ownerId The ID of the portfolio owner
     * @param pageable Pagination information
     * @return Page of portfolios
     */
    Page<PortfolioEntity> findByOwnerId(String ownerId, Pageable pageable);
    
    /**
     * Find portfolios created after a specific date
     * @param createdDate The date after which portfolios were created
     * @return List of portfolios created after the specified date
     */
    List<PortfolioEntity> findByCreatedDateAfter(LocalDateTime createdDate);
    
    /**
     * Find portfolios updated after a specific date
     * @param lastUpdatedDate The date after which portfolios were updated
     * @return List of portfolios updated after the specified date
     */
    List<PortfolioEntity> findByLastUpdatedDateAfter(LocalDateTime lastUpdatedDate);
    
    /**
     * Find portfolios containing trades for a specific symbol
     * @param symbol The symbol to search for in trades
     * @return List of portfolios containing the specified symbol
     */
    @Query("{'trades.symbol': ?0}")
    List<PortfolioEntity> findByTradeSymbol(String symbol);
    
    /**
     * Check if a portfolio with the given business ID exists
     * @param portfolioId The business ID of the portfolio
     * @return true if the portfolio exists, false otherwise
     */
    boolean existsByPortfolioId(String portfolioId);
    
    /**
     * Delete a portfolio by its business ID
     * @param portfolioId The business ID of the portfolio to delete
     */
    void deleteByPortfolioId(String portfolioId);
}
