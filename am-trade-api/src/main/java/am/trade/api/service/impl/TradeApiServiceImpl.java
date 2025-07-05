package am.trade.api.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
        log.info("Service: Adding new trade for portfolio: {} and symbol: {} by user: {}", 
                tradeDetails.getPortfolioId(), tradeDetails.getSymbol(), tradeDetails.getUserId());
        
        // Validate required fields and other trade data
        tradeValidator.validateTrade(tradeDetails);

        // Generate a new trade ID if not provided
        if (tradeDetails.getTradeId() == null || tradeDetails.getTradeId().isEmpty()) {
            tradeDetails.setTradeId(UUID.randomUUID().toString());
        }
        
        // Log if psychology data is present
        if (tradeDetails.getPsychologyData() != null) {
            log.info("Trade psychology data provided for trade");
        }
        
        // Log if entry reasoning is present
        if (tradeDetails.getEntryReasoning() != null) {
            log.info("Trade entry reasoning provided for trade");
        }
        
        // Handle trade analysis images if present
        if (tradeDetails.getAttachments() != null && !tradeDetails.getAttachments().isEmpty()) {
            log.info("Trade analysis images provided for trade: {} images", tradeDetails.getAttachments().size());
            // Validate attachments
            // tradeValidator.validateAttachments(tradeDetails.getAttachments());
        }
        tradeProcessingService.processTradeDetails(List.of(tradeDetails.getTradeId()), tradeDetails.getPortfolioId(), tradeDetails.getUserId());
        return tradeDetailsService.saveTradeDetails(tradeDetails);
    }
    
    @Override
    public TradeDetails updateTrade(String tradeId, TradeDetails tradeDetails) {
        log.info("Service: Updating trade with ID: {}", tradeId);
        
        // Validate that the trade exists
        Optional<TradeDetails> existingTrade = tradeDetailsService.findModelByTradeId(tradeDetails.getTradeId());
        if (existingTrade.isEmpty()) {
            log.error("Trade with ID {} not found", tradeDetails.getTradeId());
            throw new IllegalArgumentException("Trade not found with ID: " + tradeDetails.getTradeId());
        }
        
        // Ensure the trade ID in the path matches the one in the body
        tradeDetails.setTradeId(tradeDetails.getTradeId());
        
        // Preserve the original user ID and portfolio ID
        TradeDetails originalTrade = existingTrade.get();
        tradeDetails.setUserId(originalTrade.getUserId());
        
        // If portfolio ID is not provided, use the original one
        if (tradeDetails.getPortfolioId() == null || tradeDetails.getPortfolioId().isEmpty()) {
            tradeDetails.setPortfolioId(originalTrade.getPortfolioId());
        }
        
        // Handle trade psychology data if present
        if (tradeDetails.getPsychologyData() != null) {
            log.info("Updated trade psychology data provided for trade {}", tradeId);
            // Validate psychology data
            tradeValidator.validatePsychologyData(tradeDetails.getPsychologyData());
        } else if (originalTrade.getPsychologyData() != null) {
            // Preserve existing psychology data if not provided in update
            tradeDetails.setPsychologyData(originalTrade.getPsychologyData());
            log.info("Preserving existing psychology data for trade {}", tradeId);
        }
        
        // Handle trade entry reasoning if present
        if (tradeDetails.getEntryReasoning() != null) {
            log.info("Updated trade entry reasoning provided for trade {}", tradeId);
            // Validate entry reasoning
            tradeValidator.validateEntryReasoning(tradeDetails.getEntryReasoning());
        } else if (originalTrade.getEntryReasoning() != null) {
            // Preserve existing entry reasoning if not provided in update
            tradeDetails.setEntryReasoning(originalTrade.getEntryReasoning());
            log.info("Preserving existing entry reasoning for trade {}", tradeId);
        }
        
        // Handle trade analysis images if present
        if (tradeDetails.getAttachments() != null && !tradeDetails.getAttachments().isEmpty()) {
            log.info("Updated trade analysis images provided: {} images", tradeDetails.getAttachments().size());
            // Create a new list with both existing and new attachments
            if (originalTrade.getAttachments() != null && !originalTrade.getAttachments().isEmpty()) {
                List<Attachment> mergedAttachments = new ArrayList<>(originalTrade.getAttachments());
                mergedAttachments.addAll(tradeDetails.getAttachments());
                tradeDetails.setAttachments(mergedAttachments);
                log.info("Merged {} existing and {} new attachments for trade {}", 
                        originalTrade.getAttachments().size(), tradeDetails.getAttachments().size(), tradeId);
            }
            // Otherwise keep the new attachments as is
        } else if (originalTrade.getAttachments() != null && !originalTrade.getAttachments().isEmpty()) {
            // Preserve existing images if not provided in update
            tradeDetails.setAttachments(new ArrayList<>(originalTrade.getAttachments()));
            log.info("Preserving existing {} attachments for trade {}", 
                    originalTrade.getAttachments().size(), tradeId);
        }
        tradeProcessingService.processTradeDetails(List.of(tradeDetails.getTradeId()), tradeDetails.getPortfolioId(), tradeDetails.getUserId());
        return tradeDetailsService.saveTradeDetails(tradeDetails);
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
        log.info("Service: Processing batch of {} trades", tradeDetailsList.size());
        
        // Validate all trades in batch
        for (TradeDetails trade : tradeDetailsList) {
            tradeValidator.validateTrade(trade);
        }
        
        log.info("All trades in batch passed validation");
        
        // Generate trade IDs for new trades
        tradeDetailsList.forEach(trade -> {
            if (trade.getTradeId() == null || trade.getTradeId().isEmpty()) {
                trade.setTradeId(UUID.randomUUID().toString());
            }
        });
        
        // Log information about trade analysis images in the batch
        long tradesWithImages = tradeDetailsList.stream()
                .filter(trade -> trade.getAttachments() != null && !trade.getAttachments().isEmpty())
                .count();
        
        if (tradesWithImages > 0) {
            log.info("{} out of {} trades in batch contain attachments", tradesWithImages, tradeDetailsList.size());
        }
        
        return tradeDetailsService.saveAllTradeDetails(tradeDetailsList);
    }
    
    @Override
    public List<TradeDetails> getTradeDetailsByTradeIds(List<String> tradeIds) {
        if (tradeIds == null || tradeIds.isEmpty()) {
            log.warn("Empty trade IDs list provided");
            return Collections.emptyList();
        }
        
        log.info("Service: Fetching trade details for {} trade IDs in a single database call", tradeIds.size());
        
        // Use the new efficient method that makes a single database call
        List<TradeDetails> tradeDetails = tradeDetailsService.findModelsByTradeIds(tradeIds);
        
        log.info("Found {} trades out of {} requested IDs", tradeDetails.size(), tradeIds.size());
        return tradeDetails;
    }
}
