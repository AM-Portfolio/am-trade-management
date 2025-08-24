package am.trade.api.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import am.trade.api.service.TradeApiService;
import am.trade.api.service.TradeManagementService;
import am.trade.api.validation.TradeValidator;
import am.trade.common.models.Attachment;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of TradeApiService that handles trade API operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeApiServiceImpl implements TradeApiService {
    
    private final TradeManagementService tradeManagementService;
    private final TradeProcessingService tradeProcessingService;
    private final TradeDetailsService tradeDetailsService;
    private final TradeValidator tradeValidator;
    
    @Override
    public List<TradeDetails> getTradeDetailsByPortfolioAndSymbols(String portfolioId, List<String> symbols) {
        log.info("Service: Fetching trade details for portfolio: {} with symbols: {}", portfolioId, symbols);
        return tradeManagementService.getTradesBySymbols(portfolioId, symbols);
    }
    
    @Override
    public TradeDetails addTrade(TradeDetails tradeDetails) {
        if (tradeDetails == null) {
            log.error("Cannot add null trade");
            return null;
        }
        
        log.info("Service: Adding new trade for portfolio: {} and symbol: {} by user: {}", 
                tradeDetails.getPortfolioId(), tradeDetails.getSymbol(), tradeDetails.getUserId());
        
        // Validate required fields and other trade data
        tradeValidator.validateTrade(tradeDetails);

        // Generate a new trade ID if not provided
        if (tradeDetails.getTradeId() == null || tradeDetails.getTradeId().isEmpty()) {
            tradeDetails.setTradeId(UUID.randomUUID().toString());
        }
        
        logTradeComponents(tradeDetails);
        
        // Save trade details and process for portfolio aggregation
        TradeDetails savedTrade = tradeDetailsService.saveTradeDetails(tradeDetails);
        if (savedTrade != null) {
            tradeProcessingService.processTradeDetails(
                List.of(savedTrade.getTradeId()), 
                savedTrade.getPortfolioId(), 
                savedTrade.getUserId());
        }
        
        return savedTrade;
    }
    
    /**
     * Log information about trade components for debugging
     * @param tradeDetails The trade details to log information about
     */
    private void logTradeComponents(TradeDetails tradeDetails) {
        // Log if psychology data is present
        if (tradeDetails.getPsychologyData() != null) {
            log.info("Trade psychology data provided for trade");
        }
        
        // Log if entry reasoning is present
        if (tradeDetails.getEntryReasoning() != null) {
            log.info("Trade entry reasoning provided for trade");
        }
        
        // Log if attachments are present
        if (tradeDetails.getAttachments() != null && !tradeDetails.getAttachments().isEmpty()) {
            log.info("Trade analysis images provided for trade: {} images", tradeDetails.getAttachments().size());
            // Validate attachments if needed
            // tradeValidator.validateAttachments(tradeDetails.getAttachments());
        }
    }
    
    @Override
    public TradeDetails updateTrade(String tradeId, TradeDetails tradeDetails) {
        if (tradeId == null || tradeId.isEmpty() || tradeDetails == null) {
            log.error("Cannot update trade: invalid trade ID or trade details");
            throw new IllegalArgumentException("Invalid trade ID or trade details");
        }
        
        log.info("Service: Updating trade with ID: {}", tradeId);
        
        // Validate that the trade exists
        Optional<TradeDetails> existingTradeOpt = tradeDetailsService.findModelByTradeId(tradeId);
        if (existingTradeOpt.isEmpty()) {
            log.error("Trade with ID {} not found", tradeId);
            throw new IllegalArgumentException("Trade not found with ID: " + tradeId);
        }
        
        // Get the original trade
        TradeDetails originalTrade = existingTradeOpt.get();
        
        
        // Ensure the trade ID in the path matches the one in the body
        tradeDetails.setTradeId(tradeId);
        
        // Preserve non-editable fields from the original trade
        preserveNonEditableFields(originalTrade, tradeDetails);
        
        // Handle attachments merging
        handleAttachmentsMerging(tradeId, tradeDetails, originalTrade);
        
        // Log trade components for debugging
        logTradeComponents(tradeDetails);
        
        // Save and process trade
        TradeDetails savedTrade = tradeDetailsService.saveTradeDetails(tradeDetails);
        if (savedTrade != null) {
            tradeProcessingService.processTradeDetails(
                List.of(savedTrade.getTradeId()), 
                savedTrade.getPortfolioId(), 
                savedTrade.getUserId());
        }
        
        return savedTrade;
    }
    
    
    /**
     * Preserves non-editable fields from the original trade in the updated trade
     * @param originalTrade The original trade details
     * @param updatedTrade The updated trade details
     */
    private void preserveNonEditableFields(TradeDetails originalTrade, TradeDetails updatedTrade) {
        // Preserve user ID
        updatedTrade.setUserId(originalTrade.getUserId());
        
        // Preserve portfolio ID
        updatedTrade.setPortfolioId(originalTrade.getPortfolioId());
        
        // Preserve symbol
        updatedTrade.setSymbol(originalTrade.getSymbol());
        
        // Preserve trade position type (long/short)
        updatedTrade.setTradePositionType(originalTrade.getTradePositionType());
        
        // Preserve instrument info
        updatedTrade.setInstrumentInfo(originalTrade.getInstrumentInfo());
        
        // Preserve entry info core details (price, size)
        if (originalTrade.getEntryInfo() != null) {
            if (updatedTrade.getEntryInfo() == null) {
                updatedTrade.setEntryInfo(originalTrade.getEntryInfo());
            } else {
                updatedTrade.getEntryInfo().setPrice(originalTrade.getEntryInfo().getPrice());
                updatedTrade.getEntryInfo().setQuantity(originalTrade.getEntryInfo().getQuantity());
            }
        }
        
        // Preserve trade executions list
        updatedTrade.setTradeExecutions(originalTrade.getTradeExecutions());
    }
    
    /**
     * Handle merging of attachments between original and updated trade
     * @param tradeId The ID of the trade being updated
     * @param updatedTrade The updated trade details
     * @param originalTrade The original trade details
     */
    private void handleAttachmentsMerging(String tradeId, TradeDetails updatedTrade, TradeDetails originalTrade) {
        // Handle trade analysis images if present in update
        if (updatedTrade.getAttachments() != null && !updatedTrade.getAttachments().isEmpty()) {
            log.info("New trade analysis images provided for trade update: {} images", updatedTrade.getAttachments().size());
            
            // If original trade had images, merge them with new ones
            if (originalTrade.getAttachments() != null && !originalTrade.getAttachments().isEmpty()) {
                // Create a new list with both existing and new attachments
                List<Attachment> mergedAttachments = new ArrayList<>(originalTrade.getAttachments());
                mergedAttachments.addAll(updatedTrade.getAttachments());
                updatedTrade.setAttachments(mergedAttachments);
                log.info("Merged {} existing and {} new attachments for trade {}", 
                        originalTrade.getAttachments().size(), updatedTrade.getAttachments().size(), tradeId);
            }
            // Otherwise keep the new attachments as is
        } else if (originalTrade.getAttachments() != null && !originalTrade.getAttachments().isEmpty()) {
            // Preserve existing images if not provided in update
            updatedTrade.setAttachments(new ArrayList<>(originalTrade.getAttachments()));
            log.info("Preserving existing {} attachments for trade {}", 
                    originalTrade.getAttachments().size(), tradeId);
        }
    }
    
    @Override
    public Page<TradeDetails> getTradesByFilters(
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            List<String> strategies,
            Pageable pageable) {
        
        log.info("Service: Filtering trades with criteria - portfolioIds: {}, symbols: {}, statuses: {}, startDate: {}, endDate: {}, strategies: {}", 
                portfolioIds, symbols, statuses, startDate, endDate, strategies);
        
        return tradeManagementService.getTradesByFilters(
                portfolioIds, symbols, statuses, startDate, endDate, strategies, pageable);
    }
    
    @Override
    public List<TradeDetails> addOrUpdateTrades(List<TradeDetails> tradeDetailsList) {
        if (tradeDetailsList == null || tradeDetailsList.isEmpty()) {
            log.warn("Empty or null trade details list provided");
            return Collections.emptyList();
        }
        
        log.info("Service: Processing batch of {} trades", tradeDetailsList.size());
        
        try {
            // Validate all trades in batch
            validateTradesBatch(tradeDetailsList);
            
            // Generate trade IDs for new trades and prepare trades for saving
            prepareTradesForSaving(tradeDetailsList);
            
            // Log batch statistics
            logBatchStatistics(tradeDetailsList);
            
            // Save all trades in a single operation
            return tradeDetailsService.saveAllTradeDetails(tradeDetailsList);
        } catch (Exception e) {
            log.error("Error processing batch of trades: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Validates all trades in a batch
     * @param trades List of trades to validate
     */
    private void validateTradesBatch(List<TradeDetails> trades) {
        for (TradeDetails trade : trades) {
            tradeValidator.validateTrade(trade);
        }
        log.info("All trades in batch passed validation");
    }
    
    /**
     * Prepares trades for saving by generating IDs for new trades
     * @param trades List of trades to prepare
     */
    private void prepareTradesForSaving(List<TradeDetails> trades) {
        trades.forEach(trade -> {
            if (trade.getTradeId() == null || trade.getTradeId().isEmpty()) {
                trade.setTradeId(UUID.randomUUID().toString());
            }
        });
    }
    
    /**
     * Logs statistics about the batch of trades
     * @param trades List of trades to analyze
     */
    private void logBatchStatistics(List<TradeDetails> trades) {
        // Log information about trade analysis images in the batch
        long tradesWithImages = trades.stream()
                .filter(trade -> trade.getAttachments() != null && !trade.getAttachments().isEmpty())
                .count();
        
        if (tradesWithImages > 0) {
            log.info("{} out of {} trades in batch contain attachments", tradesWithImages, trades.size());
        }
        
        // Log information about psychology data
        long tradesWithPsychology = trades.stream()
                .filter(trade -> trade.getPsychologyData() != null)
                .count();
        
        if (tradesWithPsychology > 0) {
            log.info("{} out of {} trades in batch contain psychology data", tradesWithPsychology, trades.size());
        }
    }
    
    @Override
    public List<TradeDetails> getTradeDetailsByTradeIds(List<String> tradeIds) {
        if (tradeIds == null || tradeIds.isEmpty()) {
            log.warn("Empty trade IDs list provided");
            return Collections.emptyList();
        }
        
        log.info("Service: Fetching trade details for {} trade IDs in a single database call", tradeIds.size());
        
        try {
            // Use the efficient method that makes a single database call
            List<TradeDetails> tradeDetails = tradeDetailsService.findModelsByTradeIds(tradeIds);
            
            // Log success metrics
            int foundCount = tradeDetails.size();
            int requestedCount = tradeIds.size();
            
            if (foundCount < requestedCount) {
                log.warn("Only found {} trades out of {} requested IDs", foundCount, requestedCount);
            } else {
                log.info("Successfully retrieved all {} requested trade details", requestedCount);
            }
            
            return tradeDetails;
        } catch (Exception e) {
            log.error("Error retrieving trade details for {} IDs: {}", tradeIds.size(), e.getMessage(), e);
            throw e;
        }
    }
}
