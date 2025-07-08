package am.trade.services.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;
import am.trade.services.service.TradeDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of TradeDetailsService that converts repository entities to domain models
 */
@Service
// Constructor injection is used instead of @RequiredArgsConstructor
@Slf4j
public class TradeDetailsServiceImpl implements TradeDetailsService {

    private final TradeDetailsRepository tradeDetailsRepository;
    private final TradeDetailsMapper tradeDetailsMapper;
    
    public TradeDetailsServiceImpl(TradeDetailsRepository tradeDetailsRepository, 
                                  TradeDetailsMapper tradeDetailsMapper) {
        this.tradeDetailsRepository = tradeDetailsRepository;
        this.tradeDetailsMapper = tradeDetailsMapper;
    }
    
    @Override
    public Optional<TradeDetails> findModelById(String id) {
        log.debug("Finding trade details by ID: {}", id);
        return tradeDetailsRepository.findById(id)
                .map(tradeDetailsMapper::toTradeDetails);
    }
    
    @Override
    public Optional<TradeDetails> findModelByTradeId(String tradeId) {
        log.debug("Finding trade details by trade ID: {}", tradeId);
        return tradeDetailsRepository.findByTradeId(tradeId)
                .map(tradeDetailsMapper::toTradeDetails);
    }
    
    @Override
    public List<TradeDetails> findModelsBySymbol(String symbol) {
        log.debug("Finding trade details by symbol: {}", symbol);
        return tradeDetailsRepository.findBySymbol(symbol).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TradeDetails> findModelsByEntryDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by entry date between {} and {}", startDate, endDate);
        return tradeDetailsRepository.findByEntryDateBetween(startDate, endDate).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public Page<TradeDetails> findModelsByPortfolioId(String portfolioId, Pageable pageable) {
        log.debug("Finding trade details by portfolio ID: {} with pagination", portfolioId);
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findByPortfolioId(portfolioId, pageable);
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public List<TradeDetails> findModelsByExitDateBetween(LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by exit date between {} and {}", startDate, endDate);
        return tradeDetailsRepository.findByExitDateBetween(startDate, endDate).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TradeDetails> findModelsByStatus(TradeStatus status) {
        log.debug("Finding trade details by status: {}", status);
        // Convert TradeStatus to OrderStatus if needed, or handle the mapping appropriately
        // This depends on how your enums are structured
        return tradeDetailsRepository.findByStatus(status).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TradeDetails> findModelsByPortfolioId(String portfolioId) {
        log.debug("Finding trade details by portfolio ID: {}", portfolioId);
        return tradeDetailsRepository.findByPortfolioId(portfolioId).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TradeDetails> findModelsBySymbolAndEntryDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by symbol: {} and entry date between {} and {}", symbol, startDate, endDate);
        return tradeDetailsRepository.findBySymbolAndEntryDateBetween(symbol, startDate, endDate).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TradeDetails> findModelsBySymbolAndExitDateBetween(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by symbol: {} and exit date between {} and {}", symbol, startDate, endDate);
        return tradeDetailsRepository.findBySymbolAndExitDateBetween(symbol, startDate, endDate).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TradeDetails> findModelsBySymbol(String symbol, Pageable pageable) {
        log.debug("Finding trade details by symbol: {} with pagination", symbol);
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findBySymbol(symbol, pageable);
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
    }

    @Override
    public Page<TradeDetails> findModelsByStatus(TradeStatus status, Pageable pageable) {
        log.debug("Finding trade details by status: {} with pagination", status);
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findByStatus(status, pageable);
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public TradeDetails saveTradeDetails(TradeDetails tradeDetails) {
        log.debug("Saving trade details: {}", tradeDetails);
        TradeDetailsEntity entity = tradeDetailsMapper.toTradeEntity(tradeDetails);
         return tradeDetailsMapper.toTradeDetails(tradeDetailsRepository.save(entity));
    }
    
    @Override
    public List<TradeDetails> saveAllTradeDetails(List<TradeDetails> tradeDetailsList) {
        log.debug("Saving {} trade details records", tradeDetailsList.size());
        List<TradeDetailsEntity> entities = tradeDetailsList.stream()
                .map(tradeDetailsMapper::toTradeEntity)
                .collect(Collectors.toList());
        List<TradeDetailsEntity> savedEntities = tradeDetailsRepository.saveAll(entities);
        
        return savedEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TradeDetails> findModelsByTradeIds(List<String> tradeIds) {
        log.debug("Finding trade details by trade IDs: {}", tradeIds);
        List<TradeDetailsEntity> entities = tradeDetailsRepository.findByTradeIdIn(tradeIds);
        List<TradeDetails> tradeDetails = entities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        log.info("Found {} trades out of {} requested IDs", tradeDetails.size(), tradeIds.size());
        return tradeDetails;
    }
    
    @Override
    public List<TradeDetails> findByPortfolioIdInAndEntryInfoTimestampBetween(List<String> portfolioIds, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by portfolio IDs: {} and entry date between {} and {}", portfolioIds, startDate, endDate);
        
        // Use existing repository methods to implement this functionality
        List<TradeDetailsEntity> entities = tradeDetailsRepository.findByPortfolioIdIn(portfolioIds);
        
        // Filter by entry date in memory since we don't have a direct repository method
        List<TradeDetailsEntity> filteredEntities = entities.stream()
                .filter(entity -> {
                    // Access timestamp through entryInfo
                    if (entity.getEntryInfo() == null) {
                        return false;
                    }
                    LocalDateTime entryDate = entity.getEntryInfo().getTimestamp();
                    return entryDate != null && 
                           (entryDate.isEqual(startDate) || entryDate.isAfter(startDate)) && 
                           (entryDate.isEqual(endDate) || entryDate.isBefore(endDate));
                })
                .collect(Collectors.toList());
        
        List<TradeDetails> tradeDetails = filteredEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching portfolio IDs and date range criteria", tradeDetails.size());
        return tradeDetails;
    }

    @Override
    public List<TradeDetails> findByPortfolioIdIn(List<String> portfolioIds) {
        log.debug("Finding trade details by portfolio IDs: {}", portfolioIds);
        return tradeDetailsRepository.findByPortfolioIdIn(portfolioIds).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeDetails> findByUserIdAndEntryInfoTimestampBetween(String userId, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Finding trade details by user ID: {} and entry date between {} and {}", userId, startDate, endDate);
        
        // Use the repository method to get trades by user ID
        List<TradeDetailsEntity> userEntities = tradeDetailsRepository.findByUserId(userId);
        
        // Filter by entry date in memory
        List<TradeDetailsEntity> dateFilteredEntities = userEntities.stream()
                .filter(entity -> {
                    // Access timestamp through entryInfo
                    if (entity.getEntryInfo() == null) {
                        return false;
                    }
                    LocalDateTime entryDate = entity.getEntryInfo().getTimestamp();
                    return entryDate != null && 
                           (entryDate.isEqual(startDate) || entryDate.isAfter(startDate)) && 
                           (entryDate.isEqual(endDate) || entryDate.isBefore(endDate));
                })
                .collect(Collectors.toList());
        
        // Map entities to domain models
        List<TradeDetails> userTradeDetails = dateFilteredEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching user ID and date range criteria", userTradeDetails.size());
        return userTradeDetails;
    }
    
    @Override
    public List<TradeDetails> findByUserIdAndSymbolAndDateRange(String userId, String symbol, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Finding trade details by user ID: {} and symbol: {} with date range: {} to {}", 
                userId, symbol, startDate, endDate);
        
        // Get all trades by user ID
        List<TradeDetailsEntity> userEntities = tradeDetailsRepository.findByUserId(userId);
        
        // Filter by symbol and date range in memory
        List<TradeDetailsEntity> filteredEntities = userEntities.stream()
                .filter(entity -> entity.getSymbol().equals(symbol))
                .filter(entity -> {
                    // If no date range specified, include all trades
                    if (startDate == null || endDate == null) {
                        return true;
                    }
                    
                    // Otherwise filter by entry date
                    if (entity.getEntryInfo() == null) {
                        return false;
                    }
                    LocalDateTime entryDate = entity.getEntryInfo().getTimestamp();
                    return entryDate != null && 
                           (entryDate.isEqual(startDate) || entryDate.isAfter(startDate)) && 
                           (entryDate.isEqual(endDate) || entryDate.isBefore(endDate));
                })
                .collect(Collectors.toList());
        
        // Map entities to domain models
        List<TradeDetails> tradeDetails = filteredEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching user ID, symbol, and date range criteria", tradeDetails.size());
        return tradeDetails;
    }
    
    @Override
    public List<TradeDetails> findByUserIdAndSymbol(String userId, String symbol) {
        log.debug("Finding trade details by user ID: {} and symbol: {}", userId, symbol);
        
        // Get all trades by user ID
        List<TradeDetailsEntity> userEntities = tradeDetailsRepository.findByUserId(userId);
        
        // Filter by symbol in memory
        List<TradeDetailsEntity> filteredEntities = userEntities.stream()
                .filter(entity -> entity.getSymbol().equals(symbol))
                .collect(Collectors.toList());
        
        // Map entities to domain models
        List<TradeDetails> tradeDetails = filteredEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching user ID and symbol criteria", tradeDetails.size());
        return tradeDetails;
    }
    
    @Override
    public List<TradeDetails> findByUserIdAndStrategyAndDateRange(String userId, String strategy, LocalDateTime startDate,
            LocalDateTime endDate) {
        log.debug("Finding trade details by user ID: {} and strategy: {} with date range: {} to {}", 
                userId, strategy, startDate, endDate);
        
        // Get all trades by user ID
        List<TradeDetailsEntity> userEntities = tradeDetailsRepository.findByUserId(userId);
        
        // Filter by strategy and date range in memory
        List<TradeDetailsEntity> filteredEntities = userEntities.stream()
                .filter(entity -> {
                    // Check if the entity has entry reasoning with the specified strategy
                    if (entity.getEntryReasoning() == null) {
                        return false;
                    }
                    return strategy.equals(entity.getEntryReasoning().getStrategy());
                })
                .filter(entity -> {
                    // If no date range specified, include all trades
                    if (startDate == null || endDate == null) {
                        return true;
                    }
                    
                    // Otherwise filter by entry date
                    if (entity.getEntryInfo() == null) {
                        return false;
                    }
                    LocalDateTime entryDate = entity.getEntryInfo().getTimestamp();
                    return entryDate != null && 
                           (entryDate.isEqual(startDate) || entryDate.isAfter(startDate)) && 
                           (entryDate.isEqual(endDate) || entryDate.isBefore(endDate));
                })
                .collect(Collectors.toList());
        
        // Map entities to domain models
        List<TradeDetails> tradeDetails = filteredEntities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching user ID, strategy, and date range criteria", tradeDetails.size());
        return tradeDetails;
    }
}
