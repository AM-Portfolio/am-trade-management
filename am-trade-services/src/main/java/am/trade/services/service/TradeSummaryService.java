package am.trade.services.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import am.trade.common.models.TradeSummary;
import am.trade.common.models.TradeSummaryBasic;
import am.trade.common.models.TradeSummaryDetailed;

/**
 * Service interface for handling trade summary operations
 * Provides methods to store and retrieve TradeSummaryBasic and TradeSummaryDetailed documents
 */
public interface TradeSummaryService {
    
    /**
     * Save a TradeSummaryBasic document to MongoDB
     * 
     * @param basic The basic trade summary to save
     * @return The saved TradeSummaryBasic with generated ID
     */
    TradeSummaryBasic saveTradeSummaryBasic(TradeSummaryBasic basic);
    
    /**
     * Save a TradeSummaryDetailed document to MongoDB
     * 
     * @param detailed The detailed trade metrics to save
     * @return The saved TradeSummaryDetailed with generated ID
     */
    TradeSummaryDetailed saveTradeSummaryDetailed(TradeSummaryDetailed detailed);
    
    /**
     * Save both basic and detailed trade summary documents in a single transaction
     * 
     * @param basic The basic trade summary
     * @param detailed The detailed trade metrics
     * @return The composite TradeSummary object
     */
    TradeSummary saveTradeSummary(TradeSummaryBasic basic, TradeSummaryDetailed detailed);
    
    /**
     * Save a composite TradeSummary by splitting it into basic and detailed components
     * 
     * @param tradeSummary The composite trade summary
     * @return The saved composite TradeSummary
     */
    TradeSummary saveTradeSummary(TradeSummary tradeSummary);
    
    /**
     * Find a TradeSummaryBasic by its ID
     * 
     * @param id The ID of the basic trade summary
     * @return Optional containing the TradeSummaryBasic if found
     */
    Optional<TradeSummaryBasic> findBasicById(String id);
    
    /**
     * Find a TradeSummaryDetailed by its ID
     * 
     * @param id The ID of the detailed trade metrics
     * @return Optional containing the TradeSummaryDetailed if found
     */
    Optional<TradeSummaryDetailed> findDetailedById(String id);
    
    /**
     * Find a TradeSummaryDetailed by its associated TradeSummaryBasic ID
     * 
     * @param basicId The ID of the associated basic trade summary
     * @return Optional containing the TradeSummaryDetailed if found
     */
    Optional<TradeSummaryDetailed> findDetailedByBasicId(String basicId);
    
    /**
     * Find a composite TradeSummary by the basic ID
     * Combines both basic and detailed information
     * 
     * @param id The ID of the basic trade summary
     * @return Optional containing the composite TradeSummary if found
     */
    Optional<TradeSummary> findTradeSummaryById(String id);
    
    /**
     * Find all TradeSummaryBasic documents by owner ID
     * 
     * @param ownerId The owner ID
     * @return List of TradeSummaryBasic documents
     */
    List<TradeSummaryBasic> findBasicByOwnerId(String ownerId);
    
    /**
     * Find all TradeSummaryBasic documents by owner ID with pagination
     * 
     * @param ownerId The owner ID
     * @param pageable Pagination information
     * @return Page of TradeSummaryBasic documents
     */
    Page<TradeSummaryBasic> findBasicByOwnerId(String ownerId, Pageable pageable);
    
    /**
     * Find all TradeSummaryBasic documents by date range
     * 
     * @param startDate The start date
     * @param endDate The end date
     * @return List of TradeSummaryBasic documents
     */
    List<TradeSummaryBasic> findBasicByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    /**
     * Find all TradeSummaryBasic documents by portfolio ID
     * 
     * @param portfolioId The portfolio ID
     * @return List of TradeSummaryBasic documents
     */
    List<TradeSummaryBasic> findBasicByPortfolioId(String portfolioId);
    
    /**
     * Find all active TradeSummaryBasic documents
     * 
     * @return List of active TradeSummaryBasic documents
     */
    List<TradeSummaryBasic> findAllActiveBasic();
    
    /**
     * Find all active TradeSummaryBasic documents by owner ID
     * 
     * @param ownerId The owner ID
     * @return List of active TradeSummaryBasic documents
     */
    List<TradeSummaryBasic> findAllActiveBasicByOwnerId(String ownerId);
    
    /**
     * Delete a TradeSummaryBasic and its associated TradeSummaryDetailed
     * 
     * @param id The ID of the basic trade summary
     */
    void deleteTradeSummary(String id);
    
    /**
     * Update a TradeSummaryBasic document
     * 
     * @param basic The updated basic trade summary
     * @return The updated TradeSummaryBasic
     */
    TradeSummaryBasic updateTradeSummaryBasic(TradeSummaryBasic basic);
    
    /**
     * Update a TradeSummaryDetailed document
     * 
     * @param detailed The updated detailed trade metrics
     * @return The updated TradeSummaryDetailed
     */
    TradeSummaryDetailed updateTradeSummaryDetailed(TradeSummaryDetailed detailed);
    
    /**
     * Update a composite TradeSummary by updating both basic and detailed components
     * 
     * @param tradeSummary The updated composite trade summary
     * @return The updated composite TradeSummary
     */
    TradeSummary updateTradeSummary(TradeSummary tradeSummary);
}
