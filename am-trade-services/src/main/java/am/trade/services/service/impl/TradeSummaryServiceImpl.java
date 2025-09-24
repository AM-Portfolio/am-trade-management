package am.trade.services.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;
import am.trade.persistence.repository.TradeSummaryBasicRepository;
import am.trade.persistence.repository.TradeSummaryDetailedRepository;
import am.trade.services.service.TradeSummaryService;

/**
 * Implementation of the TradeSummaryService interface
 * Handles storage and retrieval of TradeSummaryBasic and TradeSummaryDetailed documents in MongoDB
 */
@Service("serviceLayerTradeSummaryService")
public class TradeSummaryServiceImpl implements TradeSummaryService {

    private final TradeSummaryBasicRepository basicRepository;
    private final TradeSummaryDetailedRepository detailedRepository;

    @Autowired
    public TradeSummaryServiceImpl(TradeSummaryBasicRepository basicRepository, 
                                  TradeSummaryDetailedRepository detailedRepository) {
        this.basicRepository = basicRepository;
        this.detailedRepository = detailedRepository;
    }

    @Override
    public TradeSummaryBasic saveTradeSummaryBasic(TradeSummaryBasic basic) {
        if (basic.getId() == null) {
            basic.setId(UUID.randomUUID().toString());
        }
        return basicRepository.save(basic);
    }

    @Override
    public TradeSummaryDetailed saveTradeSummaryDetailed(TradeSummaryDetailed detailed) {
        if (detailed.getId() == null) {
            detailed.setId(UUID.randomUUID().toString());
        }
        return detailedRepository.save(detailed);
    }

    @Override
    @Transactional
    public TradeSummary saveTradeSummary(TradeSummaryBasic basic, TradeSummaryDetailed detailed) {
        // Save basic summary first
        TradeSummaryBasic savedBasic = saveTradeSummaryBasic(basic);
        
        // Set the reference to the basic summary in detailed summary
        detailed.setTradeSummaryBasicId(savedBasic.getId());
        
        // Save detailed summary
        TradeSummaryDetailed savedDetailed = saveTradeSummaryDetailed(detailed);
        
        // Update the reference to detailed summary in basic summary
        savedBasic.setDetailedMetricsId(savedDetailed.getId());
        basicRepository.save(savedBasic);
        
        // Create and return the composite object
        return TradeSummary.fromBasicAndDetailed(savedBasic, savedDetailed);
    }

    @Override
    @Transactional
    public TradeSummary saveTradeSummary(TradeSummary tradeSummary) {
        // Split the composite object into basic and detailed components
        TradeSummaryBasic basic = tradeSummary.toBasicSummary();
        TradeSummaryDetailed detailed = tradeSummary.toDetailedSummary();
        
        // Save both components
        return saveTradeSummary(basic, detailed);
    }

    @Override
    public Optional<TradeSummaryBasic> findBasicById(String id) {
        return basicRepository.findById(id);
    }

    @Override
    public Optional<TradeSummaryDetailed> findDetailedById(String id) {
        return detailedRepository.findById(id);
    }

    @Override
    public Optional<TradeSummaryDetailed> findDetailedByBasicId(String basicId) {
        return detailedRepository.findByTradeSummaryBasicId(basicId);
    }

    @Override
    public Optional<TradeSummary> findTradeSummaryById(String id) {
        // Find the basic summary
        Optional<TradeSummaryBasic> basicOpt = findBasicById(id);
        
        if (basicOpt.isPresent()) {
            TradeSummaryBasic basic = basicOpt.get();
            
            // Find the detailed summary using the reference
            Optional<TradeSummaryDetailed> detailedOpt = findDetailedById(basic.getDetailedMetricsId());
            
            // Create and return the composite object
            TradeSummaryDetailed detailed = detailedOpt.orElse(null);
            return Optional.of(TradeSummary.fromBasicAndDetailed(basic, detailed));
        }
        
        return Optional.empty();
    }

    @Override
    public List<TradeSummaryBasic> findBasicByOwnerId(String ownerId) {
        return basicRepository.findByOwnerId(ownerId);
    }

    @Override
    public Page<TradeSummaryBasic> findBasicByOwnerId(String ownerId, Pageable pageable) {
        return basicRepository.findByOwnerId(ownerId, pageable);
    }

    @Override
    public List<TradeSummaryBasic> findBasicByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return basicRepository.findByDateRange(startDate, endDate);
    }

    @Override
    public List<TradeSummaryBasic> findBasicByPortfolioId(String portfolioId) {
        return basicRepository.findByPortfolioIdsContaining(portfolioId);
    }

    @Override
    public List<TradeSummaryBasic> findAllActiveBasic() {
        return basicRepository.findAllActive();
    }

    @Override
    public List<TradeSummaryBasic> findAllActiveBasicByOwnerId(String ownerId) {
        return basicRepository.findAllActiveByOwnerId(ownerId);
    }

    @Override
    @Transactional
    public void deleteTradeSummary(String id) {
        // Find the basic summary to get the reference to detailed summary
        Optional<TradeSummaryBasic> basicOpt = findBasicById(id);
        
        if (basicOpt.isPresent()) {
            TradeSummaryBasic basic = basicOpt.get();
            
            // Delete the detailed summary first
            if (basic.getDetailedMetricsId() != null) {
                detailedRepository.deleteById(basic.getDetailedMetricsId());
            }
            
            // Delete the basic summary
            basicRepository.deleteById(id);
        }
    }

    @Override
    public TradeSummaryBasic updateTradeSummaryBasic(TradeSummaryBasic basic) {
        // Ensure the document exists before updating
        if (basic.getId() != null && basicRepository.existsById(basic.getId())) {
            return basicRepository.save(basic);
        }
        throw new IllegalArgumentException("Cannot update non-existent TradeSummaryBasic");
    }

    @Override
    public TradeSummaryDetailed updateTradeSummaryDetailed(TradeSummaryDetailed detailed) {
        // Ensure the document exists before updating
        if (detailed.getId() != null && detailedRepository.existsById(detailed.getId())) {
            return detailedRepository.save(detailed);
        }
        throw new IllegalArgumentException("Cannot update non-existent TradeSummaryDetailed");
    }

    @Override
    @Transactional
    public TradeSummary updateTradeSummary(TradeSummary tradeSummary) {
        // Split the composite object into basic and detailed components
        TradeSummaryBasic basic = tradeSummary.toBasicSummary();
        TradeSummaryDetailed detailed = tradeSummary.toDetailedSummary();
        
        // Update basic summary
        TradeSummaryBasic updatedBasic = updateTradeSummaryBasic(basic);
        
        // Update detailed summary
        TradeSummaryDetailed updatedDetailed = updateTradeSummaryDetailed(detailed);
          
        // Create and return the updated composite object
        return TradeSummary.fromBasicAndDetailed(updatedBasic, updatedDetailed);
    }
}
