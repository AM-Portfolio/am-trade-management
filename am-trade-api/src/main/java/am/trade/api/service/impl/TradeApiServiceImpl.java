package am.trade.api.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.enums.TradePositionType;
import am.trade.exceptions.TradeFieldValidationException;
import am.trade.exceptions.model.ErrorDetail;

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
import am.trade.services.publisher.TradeHoldingEventPublisher;
import am.trade.services.mapper.TradeHoldingEventMapper;
import am.trade.models.kafka.TradeHoldingEvent;
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
    private final TradeHoldingEventPublisher tradeHoldingEventPublisher;
    private final TradeHoldingEventMapper tradeHoldingEventMapper;
    
    @Override
    public List<TradeDetails> getTradeDetailsByPortfolioAndSymbols(String portfolioId, List<String> symbols) {
        log.info("Service: Fetching trade details for portfolio: {} with symbols: {}", portfolioId, symbols);
        List<TradeDetails> trades = tradeManagementService.getTradesBySymbols(portfolioId, symbols);

        // Compute P&L at read-time for any closed trade whose metrics are missing or zero.
        // This ensures the Holdings page always shows correct P&L even for trades that
        // were saved before server-side metric calculation was in place.
        for (TradeDetails trade : trades) {
            boolean isClosed = trade.getStatus() == TradeStatus.WIN
                            || trade.getStatus() == TradeStatus.LOSS
                            || trade.getStatus() == TradeStatus.BREAK_EVEN;
            boolean metricsAreMissing = trade.getMetrics() == null
                            || trade.getMetrics().getProfitLoss() == null
                            || trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) == 0;

            log.info("DEBUG TRADE {}: status={}, isClosed={}, metricsNull={}, metricsAreMissing={}, entryInfoNull={}, exitInfoNull={}", 
                trade.getTradeId(), 
                trade.getStatus(), isClosed, 
                trade.getMetrics() == null, metricsAreMissing, 
                trade.getEntryInfo() == null, trade.getExitInfo() == null);

            if (isClosed && metricsAreMissing
                    && trade.getEntryInfo() != null
                    && trade.getExitInfo() != null) {
                // compute P&L in-memory for the API response
                TradeMetrics computed = computePnl(trade.getEntryInfo(), trade.getExitInfo(), trade.getTradePositionType());
                trade.setMetrics(computed);
                // Do NOT persist this back. GET requests must be idempotent and side-effect free.
                // We compute the P&L inline for the payload only.
                log.info("Computed inline P&L for {} trade {}: P&L={}, P&L%={}",
                    trade.getStatus(), trade.getTradeId(),
                    computed.getProfitLoss(), computed.getProfitLossPercentage());
            }
        }
        return trades;
    }

    /**
     * Calculate P&L metrics from raw entry/exit data.
     * All nullable fields (fees, totalValue) are defaulted to ZERO so we never throw NPE.
     */
    private TradeMetrics computePnl(EntryExitInfo entry, EntryExitInfo exit, TradePositionType positionType) {
        if (entry.getPrice() == null || exit.getPrice() == null || entry.getQuantity() == null) {
            return TradeMetrics.builder()
                    .profitLoss(BigDecimal.ZERO)
                    .profitLossPercentage(BigDecimal.ZERO)
                    .returnOnEquity(BigDecimal.ZERO)
                    .build();
        }

        BigDecimal qty       = BigDecimal.valueOf(entry.getQuantity());
        BigDecimal entryFees = entry.getFees()  != null ? entry.getFees()  : BigDecimal.ZERO;
        BigDecimal exitFees  = exit.getFees()   != null ? exit.getFees()   : BigDecimal.ZERO;

        BigDecimal profitLoss;
        if (positionType == TradePositionType.SHORT) {
            // SHORT: sell high, buy back low → profit = (entryPrice - exitPrice) × qty
            profitLoss = entry.getPrice().subtract(exit.getPrice()).multiply(qty);
        } else {
            // LONG (default): buy low, sell high → profit = (exitPrice - entryPrice) × qty
            profitLoss = exit.getPrice().subtract(entry.getPrice()).multiply(qty);
        }
        profitLoss = profitLoss.subtract(entryFees).subtract(exitFees);

        BigDecimal investment = entry.getTotalValue() != null && entry.getTotalValue().compareTo(BigDecimal.ZERO) > 0
                ? entry.getTotalValue()
                : entry.getPrice().multiply(qty);

        BigDecimal pnlPct = investment.compareTo(BigDecimal.ZERO) > 0
                ? profitLoss.divide(investment, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return TradeMetrics.builder()
                .profitLoss(profitLoss)
                .profitLossPercentage(pnlPct)
                .returnOnEquity(pnlPct)
                .build();
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
                
            publishHoldingUpdateEvent(savedTrade, "ADD");
        }
        
        return savedTrade;
    }
    
    private void publishHoldingUpdateEvent(TradeDetails tradeDetails, String updateType) {
        log.info("Publishing portfolio update event for trade {} (status: {}, type: {})", 
                 tradeDetails.getTradeId(), tradeDetails.getStatus(), updateType);
        
        try {
            TradeHoldingEvent event = tradeHoldingEventMapper.toHoldingEvent(tradeDetails, updateType);
            if (event != null) {
                tradeHoldingEventPublisher.publishHoldingUpdate(event);
            }
        } catch (Exception e) {
            log.error("Failed to publish holding update event for trade details: {}. Trade was saved successfully.", 
                      tradeDetails.getTradeId(), e);
        }
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
        TradeStatus oldStatus = originalTrade.getStatus();
        
        // Validate non-editable fields
        validateNonEditableFields(originalTrade, tradeDetails);
        
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
                
            if (oldStatus == TradeStatus.OPEN && savedTrade.getStatus() != TradeStatus.OPEN) {
                publishHoldingUpdateEvent(savedTrade, "REMOVE");
            } else if (oldStatus != TradeStatus.OPEN && savedTrade.getStatus() == TradeStatus.OPEN) {
                publishHoldingUpdateEvent(savedTrade, "ADD");
            } else if (oldStatus == TradeStatus.OPEN && savedTrade.getStatus() == TradeStatus.OPEN) {
                publishHoldingUpdateEvent(savedTrade, "UPDATE");
            }
        }
        
        return savedTrade;
    }
    
    /**
     * Validates that non-editable fields haven't been changed in the update request
     * @param originalTrade The original trade details
     * @param updatedTrade The updated trade details
     * @throws TradeFieldValidationException with HTTP 400 Bad Request if non-editable fields are modified
     */
    private void validateNonEditableFields(TradeDetails originalTrade, TradeDetails updatedTrade) {
        // Create a validation exception builder to collect all validation errors
        TradeFieldValidationException.TradeFieldValidationExceptionBuilder exceptionBuilder = 
                TradeFieldValidationException.fieldBuilder("Trade update contains modifications to non-editable fields");
        
        boolean hasErrors = false;
        
        // Check symbol - non-editable
        if (updatedTrade.getSymbol() != null && !updatedTrade.getSymbol().equals(originalTrade.getSymbol())) {
            exceptionBuilder.addFieldError("symbol", "Symbol cannot be modified");
            hasErrors = true;
        }
        
        // Check portfolio ID - non-editable
        if (updatedTrade.getPortfolioId() != null && !updatedTrade.getPortfolioId().isEmpty() 
                && !updatedTrade.getPortfolioId().equals(originalTrade.getPortfolioId())) {
            exceptionBuilder.addFieldError("portfolioId", "Portfolio ID cannot be modified");
            hasErrors = true;
        }
        
        // Check user ID - non-editable
        if (updatedTrade.getUserId() != null && !updatedTrade.getUserId().equals(originalTrade.getUserId())) {
            exceptionBuilder.addFieldError("userId", "User ID cannot be modified");
            hasErrors = true;
        }
        
        // Check trade position type - non-editable
        if (updatedTrade.getTradePositionType() != null && !updatedTrade.getTradePositionType().equals(originalTrade.getTradePositionType())) {
            exceptionBuilder.addFieldError("tradePositionType", "Trade position type (long/short) cannot be modified");
            hasErrors = true;
        }
        
        // Check entry info - non-editable core fields
        if (updatedTrade.getEntryInfo() != null && originalTrade.getEntryInfo() != null) {
            // Check if price was modified
            if (updatedTrade.getEntryInfo().getPrice() != null && 
                    (originalTrade.getEntryInfo().getPrice() == null ||
                    updatedTrade.getEntryInfo().getPrice().compareTo(originalTrade.getEntryInfo().getPrice()) != 0)) {
                exceptionBuilder.addFieldError("entryInfo.price", "Entry price cannot be modified");
                hasErrors = true;
            }
            
            // Check if quantity was modified
            if (updatedTrade.getEntryInfo().getQuantity() != null && 
                    !Objects.equals(updatedTrade.getEntryInfo().getQuantity(), originalTrade.getEntryInfo().getQuantity())) {
                exceptionBuilder.addFieldError("entryInfo.quantity", "Position quantity cannot be modified");
                hasErrors = true;
            }
        }
        
        // Check instrument info - non-editable
        if (updatedTrade.getInstrumentInfo() != null && originalTrade.getInstrumentInfo() != null &&
                !Objects.equals(updatedTrade.getInstrumentInfo(), originalTrade.getInstrumentInfo())) {
            exceptionBuilder.addFieldError("instrumentInfo", "Instrument information cannot be modified");
            hasErrors = true;
        }
        
        // If any non-editable fields were modified, throw the validation exception with all errors
        if (hasErrors) {
            log.warn("Attempt to modify non-editable fields for trade {}", originalTrade.getTradeId());
            throw exceptionBuilder.build();
        }
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
        
        // Preserve status if missing
        if (updatedTrade.getStatus() == null) {
            updatedTrade.setStatus(originalTrade.getStatus());
        }
        
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
        
        // Preserve metrics — processTradeDetails will recalculate immediately after save.
        // Without this, an update that omits the metrics field clears previously stored P&L.
        if (updatedTrade.getMetrics() == null && originalTrade.getMetrics() != null) {
            updatedTrade.setMetrics(originalTrade.getMetrics());
        }
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
            
            // Identify existing trades to map their old statuses
            List<String> incomingIds = tradeDetailsList.stream()
                .map(TradeDetails::getTradeId)
                .filter(id -> id != null && !id.isEmpty())
                .collect(Collectors.toList());
                
            Map<String, TradeStatus> oldStatusMap = new java.util.HashMap<>();
            if (!incomingIds.isEmpty()) {
                oldStatusMap = tradeDetailsService.findModelsByTradeIds(incomingIds).stream()
                    .collect(Collectors.toMap(TradeDetails::getTradeId, TradeDetails::getStatus));
            }

            // Generate trade IDs for new trades and prepare trades for saving
            prepareTradesForSaving(tradeDetailsList);
            
            // Log batch statistics
            logBatchStatistics(tradeDetailsList);
            
            // Save all trades in a single operation
            List<TradeDetails> savedTrades = tradeDetailsService.saveAllTradeDetails(tradeDetailsList);
            
            // Publish events for each saved trade
            if (savedTrades != null) {
                for (TradeDetails savedTrade : savedTrades) {
                    TradeStatus oldStatus = oldStatusMap.get(savedTrade.getTradeId());
                    
                    if (oldStatus == null) {
                        // New trade
                        publishHoldingUpdateEvent(savedTrade, "ADD");
                    } else {
                        // Existing trade
                        if (oldStatus == TradeStatus.OPEN && savedTrade.getStatus() != TradeStatus.OPEN) {
                            publishHoldingUpdateEvent(savedTrade, "REMOVE");
                        } else if (oldStatus != TradeStatus.OPEN && savedTrade.getStatus() == TradeStatus.OPEN) {
                            publishHoldingUpdateEvent(savedTrade, "ADD");
                        } else if (oldStatus == TradeStatus.OPEN && savedTrade.getStatus() == TradeStatus.OPEN) {
                            publishHoldingUpdateEvent(savedTrade, "UPDATE");
                        }
                    }
                }
            }
            
            return savedTrades;
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
