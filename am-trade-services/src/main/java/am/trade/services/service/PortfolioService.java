package am.trade.services.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.persistence.entity.PortfolioEntity;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.PortfolioMapper;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.PortfolioRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for persisting and retrieving portfolio data
 * Handles the mapping between domain models and persistence entities
 */
@Service
@Slf4j
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioMapper portfolioMapper;
    private final TradeDetailsMapper tradeDetailsMapper;

    @Autowired
    public PortfolioService(PortfolioRepository portfolioRepository, PortfolioMapper portfolioMapper, TradeDetailsMapper tradeDetailsMapper) {
        this.portfolioRepository = portfolioRepository;
        this.portfolioMapper = portfolioMapper;
        this.tradeDetailsMapper = tradeDetailsMapper;
    }

    /**
     * Save a portfolio model to the database
     * @param portfolioModel The portfolio model to save
     * @return The saved portfolio model with updated information
     */
    public PortfolioModel savePortfolio(PortfolioModel portfolioModel) {
        log.info("Saving portfolio with ID: {}", portfolioModel.getPortfolioId());
        
        // Convert model to entity
        PortfolioEntity entity = portfolioMapper.toEntity(portfolioModel);
        
        // Save entity to repository
        PortfolioEntity savedEntity = portfolioRepository.save(entity);
        
        // Convert saved entity back to model
        return portfolioMapper.toModel(savedEntity);
    }

    /**
     * Find a portfolio by its business ID
     * @param portfolioId The business ID of the portfolio
     * @return Optional containing the portfolio if found
     */
    public Optional<PortfolioModel> findByPortfolioId(String portfolioId) {
        log.info("Finding portfolio with ID: {}", portfolioId);
        
        return portfolioRepository.findByPortfolioId(portfolioId)
                .map(portfolioMapper::toModel);
    }

    /**
     * Find all portfolios owned by a specific user
     * @param ownerId The ID of the portfolio owner
     * @return List of portfolios owned by the user
     */
    public List<PortfolioModel> findByOwnerId(String ownerId) {
        log.info("Finding portfolios for owner: {}", ownerId);
        
        return portfolioRepository.findByOwnerId(ownerId).stream()
                .map(portfolioMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Find all active portfolios owned by a specific user
     * @param ownerId The ID of the portfolio owner
     * @return List of active portfolios owned by the user
     */
    public List<PortfolioModel> findActivePortfoliosByOwnerId(String ownerId) {
        log.info("Finding active portfolios for owner: {}", ownerId);
        
        return portfolioRepository.findByOwnerIdAndActive(ownerId, true).stream()
                .map(portfolioMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Find portfolios with pagination
     * @param ownerId The ID of the portfolio owner
     * @param pageable Pagination information
     * @return Page of portfolios
     */
    public Page<PortfolioModel> findByOwnerIdPaginated(String ownerId, Pageable pageable) {
        log.info("Finding paginated portfolios for owner: {}", ownerId);
        
        return portfolioRepository.findByOwnerId(ownerId, pageable)
                .map(portfolioMapper::toModel);
    }

    /**
     * Find portfolios containing trades for a specific symbol
     * @param symbol The symbol to search for in trades
     * @return List of portfolios containing the specified symbol
     */
    public List<PortfolioModel> findByTradeSymbol(String symbol) {
        log.info("Finding portfolios with trades for symbol: {}", symbol);
        
        return portfolioRepository.findByTradeSymbol(symbol).stream()
                .map(portfolioMapper::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Delete a portfolio by its business ID
     * @param portfolioId The business ID of the portfolio to delete
     */
    public void deleteByPortfolioId(String portfolioId) {
        log.info("Deleting portfolio with ID: {}", portfolioId);
        
        portfolioRepository.deleteByPortfolioId(portfolioId);
    }

    /**
     * Update trades for a specific portfolio
     * @param portfolioId The business ID of the portfolio
     * @param tradeDetails The list of trade details to update
     * @return The updated portfolio model
     */
    public PortfolioModel updatePortfolioTrades(String portfolioId, List<TradeDetails> tradeDetails) {
        log.info("Updating trades for portfolio with ID: {}", portfolioId);
        
        Optional<PortfolioEntity> portfolioOpt = portfolioRepository.findByPortfolioId(portfolioId);
        
        if (portfolioOpt.isPresent()) {
            PortfolioEntity portfolio = portfolioOpt.get();
            
            // Convert trade details to entities
            List<String> tradeEntities = tradeDetails.stream()
            .map(TradeDetails::getTradeId)
            .collect(Collectors.toList());
            
            // Update trades
            portfolio.setTrades(tradeEntities);
            
            // Save updated portfolio
            PortfolioEntity savedEntity = portfolioRepository.save(portfolio);
            
            // Return updated model
            return portfolioMapper.toModel(savedEntity);
        } else {
            log.warn("Portfolio with ID {} not found for trade update", portfolioId);
            return null;
        }
    }
}
