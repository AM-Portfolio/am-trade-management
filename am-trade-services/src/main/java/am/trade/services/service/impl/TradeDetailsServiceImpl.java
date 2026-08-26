package am.trade.services.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.models.enums.TradeStatus;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.TradeDetailsRepository;
import am.trade.services.service.TradeDetailsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
    private final MongoTemplate mongoTemplate;
    
    public TradeDetailsServiceImpl(TradeDetailsRepository tradeDetailsRepository, 
                                  TradeDetailsMapper tradeDetailsMapper,
                                  MongoTemplate mongoTemplate) {
        this.tradeDetailsRepository = tradeDetailsRepository;
        this.tradeDetailsMapper = tradeDetailsMapper;
        this.mongoTemplate = mongoTemplate;
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
    @org.springframework.transaction.annotation.Transactional
    public void deleteByTradeId(String tradeId) {
        log.debug("Deleting trade details by trade ID: {}", tradeId);
        tradeDetailsRepository.findByTradeId(tradeId).ifPresent(entity -> {
            tradeDetailsRepository.deleteById(entity.getId());
            log.info("Deleted trade details with trade ID: {}", tradeId);
        });
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
    public List<TradeDetails> findModelsByUserIdAndEntryInfoTimestampBetween(String userId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by user ID: {} and entry date between {} and {}", userId, startDate, endDate);
        return tradeDetailsRepository.findByUserIdAndEntryInfoTimestampBetween(userId, startDate, endDate).stream()
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
    public List<TradeDetails> findModelsByPortfolioIdAndSymbolInIgnoreCase(String portfolioId, List<String> symbols) {
        log.debug("Finding trade details by portfolio ID: {} and symbols: {}", portfolioId, symbols);
        return tradeDetailsRepository.findByPortfolioIdAndSymbolInIgnoreCase(portfolioId, symbols).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeDetails> findModelsByUserIdAndPortfolioIdAndSymbolInIgnoreCase(String userId, String portfolioId, List<String> symbols) {
        log.debug("Finding trade details by user ID: {}, portfolio ID: {} and symbols: {}", userId, portfolioId, symbols);
        return tradeDetailsRepository.findByUserIdAndPortfolioIdAndSymbolInIgnoreCase(userId, portfolioId, symbols).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TradeDetails> findModelsByPortfolioIdAndSymbolInIgnoreCase(String portfolioId, List<String> symbols, Pageable pageable) {
        log.debug("Finding trade details by portfolio ID: {} and symbols: {} with pagination", portfolioId, symbols);
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findByPortfolioIdAndSymbolInIgnoreCase(portfolioId, symbols, pageable);
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
    }
    
    @Override
    public Page<TradeDetails> findModelsByUserIdAndPortfolioIdAndSymbolInIgnoreCase(String userId, String portfolioId, List<String> symbols, Pageable pageable) {
        log.debug("Finding trade details by user ID: {}, portfolio ID: {} and symbols: {} with pagination", userId, portfolioId, symbols);
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findByUserIdAndPortfolioIdAndSymbolInIgnoreCase(userId, portfolioId, symbols, pageable);
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
    }

    @Override
    public Page<TradeDetails> findModelsByUserIdAndPortfolioId(String userId, String portfolioId, Pageable pageable) {
        log.debug("Finding trade details by user ID: {}, portfolio ID: {} with pagination", userId, portfolioId);
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findByUserIdAndPortfolioId(userId, portfolioId, pageable);
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
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
        if (entity.getSymbol() != null) {
            entity.setSymbol(entity.getSymbol().toUpperCase());
        }
        
        if (tradeDetails.getTradeId() != null) {
            tradeDetailsRepository.findByTradeId(tradeDetails.getTradeId())
                    .ifPresent(existing -> entity.setId(existing.getId()));
        }
        
        return tradeDetailsMapper.toTradeDetails(tradeDetailsRepository.save(entity));
    }
    
    @Override
    @org.springframework.transaction.annotation.Transactional
    public List<TradeDetails> saveAllTradeDetails(List<TradeDetails> tradeDetailsList) {
        log.debug("Saving {} trade details records", tradeDetailsList.size());
        Map<String, String> existingIdByTradeId = resolveExistingIdsByTradeId(tradeDetailsList);
        List<TradeDetailsEntity> entities = toEntitiesReusingExistingIds(tradeDetailsList, existingIdByTradeId);
        List<TradeDetailsEntity> savedEntities = tradeDetailsRepository.saveAll(entities);
        return savedEntities.stream().map(tradeDetailsMapper::toTradeDetails).collect(Collectors.toList());
    }

    private Map<String, String> resolveExistingIdsByTradeId(List<TradeDetails> tradeDetailsList) {
        List<String> incomingTradeIds = tradeDetailsList.stream()
                .map(TradeDetails::getTradeId)
                .filter(id -> id != null)
                .collect(Collectors.toList());
        Map<String, String> existingIdByTradeId = new java.util.HashMap<>();
        if (!incomingTradeIds.isEmpty()) {
            tradeDetailsRepository.findByTradeIdIn(incomingTradeIds)
                    .forEach(existing -> existingIdByTradeId.put(existing.getTradeId(), existing.getId()));
        }
        return existingIdByTradeId;
    }

    private List<TradeDetailsEntity> toEntitiesReusingExistingIds(
            List<TradeDetails> tradeDetailsList, Map<String, String> existingIdByTradeId) {
        return tradeDetailsList.stream()
                .map(tradeDetails -> {
                    TradeDetailsEntity entity = tradeDetailsMapper.toTradeEntity(tradeDetails);
                    if (entity.getSymbol() != null) {
                        entity.setSymbol(entity.getSymbol().toUpperCase());
                    }
                    if (tradeDetails.getTradeId() != null) {
                        String existingId = existingIdByTradeId.get(tradeDetails.getTradeId());
                        if (existingId != null) {
                            entity.setId(existingId);
                        }
                    }
                    return entity;
                })
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
        
        List<TradeDetailsEntity> entities = tradeDetailsRepository.findByPortfolioIdInAndEntryInfoTimestampBetween(portfolioIds, startDate, endDate);
        
        List<TradeDetails> tradeDetails = entities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching portfolio IDs and date range criteria", tradeDetails.size());
        return tradeDetails;
    }

    @Override
    public Page<TradeDetails> findByFilters(
            String userId,
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            List<String> strategies,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable) {
        log.debug("Finding trade details by filters with pagination");
        Page<TradeDetailsEntity> entityPage = tradeDetailsRepository.findByFilters(
                userId, portfolioIds, symbols, statuses, strategies, startDate, endDate, pageable);
                
        List<TradeDetails> models = entityPage.getContent().stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        return new PageImpl<>(models, pageable, entityPage.getTotalElements());
    }

    @Override
    public List<TradeDetails> findModelsByUserIdAndPortfolioId(String userId, String portfolioId) {
        log.debug("Finding trade details by user ID: {} and portfolio ID: {}", userId, portfolioId);
        return tradeDetailsRepository.findByUserIdAndPortfolioId(userId, portfolioId).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
    }

    @Override
    public List<TradeDetails> findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(String userId, String portfolioId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by user ID: {}, portfolio ID: {} and entry date between {} and {}", userId, portfolioId, startDate, endDate);
        return tradeDetailsRepository.findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(userId, portfolioId, startDate, endDate).stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
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
        
        List<TradeDetailsEntity> entities = tradeDetailsRepository.findByUserIdAndEntryInfoTimestampBetween(userId, startDate, endDate);
        
        List<TradeDetails> userTradeDetails = entities.stream()
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
        
        List<TradeDetailsEntity> entities;
        if (startDate == null || endDate == null) {
            entities = tradeDetailsRepository.findByUserIdAndSymbol(userId, symbol);
        } else {
            entities = tradeDetailsRepository.findByUserIdAndSymbolAndEntryInfoTimestampBetween(userId, symbol, startDate, endDate);
        }
        
        List<TradeDetails> tradeDetails = entities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching user ID, symbol, and date range criteria", tradeDetails.size());
        return tradeDetails;
    }
    
    @Override
    public List<TradeDetails> findByUserIdAndSymbol(String userId, String symbol) {
        log.debug("Finding trade details by user ID: {} and symbol: {}", userId, symbol);
        
        List<TradeDetailsEntity> entities = tradeDetailsRepository.findByUserIdAndSymbol(userId, symbol);
        
        List<TradeDetails> tradeDetails = entities.stream()
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
        
        // Push all filtering to MongoDB — avoids fetching unfiltered user data into JVM memory
        org.springframework.data.mongodb.core.query.Query query =
                new org.springframework.data.mongodb.core.query.Query();
        
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("userId").is(userId));
        // NOTE: field name "streategy" is a persisted typo in the data model — must match the stored field name
        query.addCriteria(org.springframework.data.mongodb.core.query.Criteria.where("entryReasoning.streategy").is(strategy));
        
        if (startDate != null && endDate != null) {
            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria
                    .where("entryInfo.timestamp").gte(startDate).lte(endDate));
        } else if (startDate != null) {
            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria
                    .where("entryInfo.timestamp").gte(startDate));
        } else if (endDate != null) {
            query.addCriteria(org.springframework.data.mongodb.core.query.Criteria
                    .where("entryInfo.timestamp").lte(endDate));
        }
        
        List<TradeDetailsEntity> entities = mongoTemplate.find(query, TradeDetailsEntity.class);
        List<TradeDetails> tradeDetails = entities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching user ID, strategy, and date range criteria", tradeDetails.size());
        return tradeDetails;
    }
    
    @Override
    public List<TradeDetails> findByPortfolioIdAndEntryInfoTimestampBetween(String portfolioId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Finding trade details by portfolio ID: {} and entry date between {} and {}", portfolioId, startDate, endDate);
        
        List<TradeDetailsEntity> entities = tradeDetailsRepository.findByPortfolioIdAndEntryInfoTimestampBetween(portfolioId, startDate, endDate);
        
        List<TradeDetails> tradeDetails = entities.stream()
                .map(tradeDetailsMapper::toTradeDetails)
                .collect(Collectors.toList());
                
        log.info("Found {} trades matching portfolio ID and date range criteria", tradeDetails.size());
        return tradeDetails;
    }
}
