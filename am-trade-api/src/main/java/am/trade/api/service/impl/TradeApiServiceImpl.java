package am.trade.api.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import am.trade.api.dto.FilterTradeDetailsRequest;
import am.trade.api.dto.FilterTradeDetailsResponse;
import am.trade.api.dto.FavoriteFilterResponse;
import am.trade.api.service.FavoriteFilterService;
import am.trade.exceptions.TradeFieldValidationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import am.trade.api.service.TradeApiService;
import am.trade.api.service.TradeManagementService;
import am.trade.api.validation.TradeValidator;
import am.trade.common.models.Attachment;
import am.trade.common.models.DerivativeInfo;
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
    private final FavoriteFilterService favoriteFilterService;
    
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
                    !Objects.equals(updatedTrade.getEntryInfo().getPrice(), originalTrade.getEntryInfo().getPrice())) {
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
    
    @Override
    public FilterTradeDetailsResponse filterTradeDetails(FilterTradeDetailsRequest request, org.springframework.data.domain.Pageable pageable) {
        log.info("Filtering trade details for user: {} with filter ID: {}", 
                request.getUserId(), request.getFavoriteFilterId());
        
        validateFilterRequest(request);
        
        // If favorite filter ID is provided, merge with saved filter
        FilterTradeDetailsRequest effectiveFilter = request;
        String appliedFilterName = null;
        
        if (request.getFavoriteFilterId() != null && !request.getFavoriteFilterId().isEmpty()) {
            effectiveFilter = mergeFavoriteFilter(request);
            appliedFilterName = getFilterName(request.getUserId(), request.getFavoriteFilterId());
        }
        
        // Apply filters and get all matching trades
        List<TradeDetails> filteredTrades = applyFilters(effectiveFilter);
        
        // Apply sorting and pagination if Pageable is provided
        if (pageable != null && pageable.isPaged()) {
            List<TradeDetails> sortedTrades = applySortingWithPageable(filteredTrades, pageable);
            PaginationResult paginationResult = applyPaginationWithPageable(sortedTrades, pageable);
            return buildFilterResponse(paginationResult.trades, filteredTrades.size(), 
                    effectiveFilter, appliedFilterName, paginationResult);
        }
        
        // Return all results without pagination
        return buildFilterResponse(filteredTrades, filteredTrades.size(), 
                effectiveFilter, appliedFilterName, null);
    }
    
    private void validateFilterRequest(FilterTradeDetailsRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("User ID is required");
        }
    }
    
    private FilterTradeDetailsRequest mergeFavoriteFilter(FilterTradeDetailsRequest request) {
        try {
            // Get saved filter configuration
            FavoriteFilterResponse savedFilter = favoriteFilterService.getFilterById(
                    request.getUserId(), 
                    request.getFavoriteFilterId());
            
            // If request has no metrics config, use saved filter's config
            if (request.getMetricsConfig() == null) {
                return FilterTradeDetailsRequest.builder()
                        .userId(request.getUserId())
                        .metricsConfig(savedFilter.getFilterConfig())
                        .favoriteFilterId(request.getFavoriteFilterId())
                        .build();
            }
            
            // Otherwise, merge configurations (request overrides saved filter)
            am.trade.common.models.MetricsFilterConfig mergedConfig = 
                    am.trade.common.models.MetricsFilterConfig.builder()
                    .portfolioIds(mergeList(request.getMetricsConfig().getPortfolioIds(), 
                            savedFilter.getFilterConfig().getPortfolioIds()))
                    .instruments(mergeList(request.getMetricsConfig().getInstruments(), 
                            savedFilter.getFilterConfig().getInstruments()))
                    .dateRange(request.getMetricsConfig().getDateRange() != null ? 
                            request.getMetricsConfig().getDateRange() : 
                            savedFilter.getFilterConfig().getDateRange())
                    .timePeriod(request.getMetricsConfig().getTimePeriod() != null ?
                            request.getMetricsConfig().getTimePeriod() :
                            savedFilter.getFilterConfig().getTimePeriod())
                    .tradeCharacteristics(request.getMetricsConfig().getTradeCharacteristics() != null ?
                            request.getMetricsConfig().getTradeCharacteristics() :
                            savedFilter.getFilterConfig().getTradeCharacteristics())
                    .profitLossFilters(request.getMetricsConfig().getProfitLossFilters() != null ?
                            request.getMetricsConfig().getProfitLossFilters() :
                            savedFilter.getFilterConfig().getProfitLossFilters())
                    .instrumentFilters(request.getMetricsConfig().getInstrumentFilters() != null ?
                            request.getMetricsConfig().getInstrumentFilters() :
                            savedFilter.getFilterConfig().getInstrumentFilters())
                    .metricTypes(request.getMetricsConfig().getMetricTypes() != null ?
                            request.getMetricsConfig().getMetricTypes() :
                            savedFilter.getFilterConfig().getMetricTypes())
                    .groupBy(request.getMetricsConfig().getGroupBy() != null ?
                            request.getMetricsConfig().getGroupBy() :
                            savedFilter.getFilterConfig().getGroupBy())
                    .build();
            
            return FilterTradeDetailsRequest.builder()
                    .userId(request.getUserId())
                    .metricsConfig(mergedConfig)
                    .favoriteFilterId(request.getFavoriteFilterId())
                    .build();
        } catch (Exception e) {
            log.warn("Could not merge with favorite filter: {}", e.getMessage());
            return request;
        }
    }
    
    private List<String> mergeList(List<String> requested, List<String> saved) {
        return requested != null && !requested.isEmpty() ? requested : saved;
    }
    
    private String getFilterName(String userId, String filterId) {
        try {
            FavoriteFilterResponse filter = favoriteFilterService.getFilterById(userId, filterId);
            return filter.getName();
        } catch (Exception e) {
            log.warn("Could not get filter name: {}", e.getMessage());
            return null;
        }
    }
    
    private List<TradeDetails> applyFilters(FilterTradeDetailsRequest filter) {
        // Get all trades for the user's portfolios
        List<TradeDetails> allTrades = new ArrayList<>();
        
        if (filter.getMetricsConfig() == null) {
            log.warn("No metrics configuration provided in filter request");
            return Collections.emptyList();
        }
        
        List<String> portfolioIds = filter.getMetricsConfig().getPortfolioIds();
        if (portfolioIds != null && !portfolioIds.isEmpty()) {
            for (String portfolioId : portfolioIds) {
                allTrades.addAll(tradeManagementService.getTradesBySymbols(portfolioId, null));
            }
        } else {
            log.warn("No portfolio IDs provided in filter request");
            return Collections.emptyList();
        }
        
        // Apply all filters
        return allTrades.stream()
                .filter(trade -> matchesFilter(trade, filter.getMetricsConfig()))
                .collect(Collectors.toList());
    }
    
    private boolean matchesFilter(TradeDetails trade, am.trade.common.models.MetricsFilterConfig config) {
        // Symbol/Instrument filter
        if (config.getInstruments() != null && !config.getInstruments().isEmpty()) {
            if (!config.getInstruments().contains(trade.getSymbol())) {
                return false;
            }
        }
        
        // Date range filter
        if (config.getDateRange() != null && trade.getEntryInfo() != null) {
            LocalDate tradeDate = trade.getEntryInfo().getTimestamp().toLocalDate();
            
            if (config.getDateRange().get("startDate") != null) {
                LocalDate startDate = LocalDate.parse(config.getDateRange().get("startDate").toString());
                if (tradeDate.isBefore(startDate)) {
                    return false;
                }
            }
            
            if (config.getDateRange().get("endDate") != null) {
                LocalDate endDate = LocalDate.parse(config.getDateRange().get("endDate").toString());
                if (tradeDate.isAfter(endDate)) {
                    return false;
                }
            }
        }
        
        // Trade characteristics filter (status, strategy, position type, tags, etc.)
        if (config.getTradeCharacteristics() != null) {
            // Status filter
            if (config.getTradeCharacteristics().get("statuses") != null) {
                @SuppressWarnings("unchecked")
                List<String> statuses = (List<String>) config.getTradeCharacteristics().get("statuses");
                if (!statuses.contains(trade.getStatus().name())) {
                    return false;
                }
            }
            
            // Strategy filter
            if (config.getTradeCharacteristics().get("strategies") != null) {
                @SuppressWarnings("unchecked")
                List<String> strategies = (List<String>) config.getTradeCharacteristics().get("strategies");
                if (!strategies.contains(trade.getStrategy())) {
                    return false;
                }
            }
            
            // Position type filter
            if (config.getTradeCharacteristics().get("positionTypes") != null) {
                @SuppressWarnings("unchecked")
                List<String> positionTypes = (List<String>) config.getTradeCharacteristics().get("positionTypes");
                if (trade.getTradePositionType() == null || 
                    !positionTypes.contains(trade.getTradePositionType().name())) {
                    return false;
                }
            }
            
            // Tags filter
            if (config.getTradeCharacteristics().get("tags") != null) {
                @SuppressWarnings("unchecked")
                List<String> tags = (List<String>) config.getTradeCharacteristics().get("tags");
                if (trade.getTags() == null || trade.getTags().isEmpty()) {
                    return false;
                }
                boolean hasAnyTag = tags.stream()
                        .anyMatch(tag -> trade.getTags().contains(tag));
                if (!hasAnyTag) {
                    return false;
                }
            }
            
            // Holding time filters
            if (config.getTradeCharacteristics().get("minHoldingTimeHours") != null && trade.getMetrics() != null) {
                Integer minHours = (Integer) config.getTradeCharacteristics().get("minHoldingTimeHours");
                if (trade.getMetrics().getHoldingTimeHours() < minHours) {
                    return false;
                }
            }
            
            if (config.getTradeCharacteristics().get("maxHoldingTimeHours") != null && trade.getMetrics() != null) {
                Integer maxHours = (Integer) config.getTradeCharacteristics().get("maxHoldingTimeHours");
                if (trade.getMetrics().getHoldingTimeHours() > maxHours) {
                    return false;
                }
            }
        }
        
        // Profit/Loss filters
        if (config.getProfitLossFilters() != null && trade.getMetrics() != null) {
            if (config.getProfitLossFilters().get("minProfitLoss") != null) {
                Double minPL = ((Number) config.getProfitLossFilters().get("minProfitLoss")).doubleValue();
                if (trade.getMetrics().getProfitLoss().doubleValue() < minPL) {
                    return false;
                }
            }
            
            if (config.getProfitLossFilters().get("maxProfitLoss") != null) {
                Double maxPL = ((Number) config.getProfitLossFilters().get("maxProfitLoss")).doubleValue();
                if (trade.getMetrics().getProfitLoss().doubleValue() > maxPL) {
                    return false;
                }
            }
        }
        
        // Instrument filters (option type, strike range, expiry, etc.)
        if (config.getInstrumentFilters() != null && 
            trade.getInstrumentInfo() != null && 
            trade.getInstrumentInfo().getDerivativeInfo() != null) {
            
            DerivativeInfo derivativeInfo = trade.getInstrumentInfo().getDerivativeInfo();
            
            // Option type filter (CE/PE)
            if (config.getInstrumentFilters().get("optionType") != null) {
                String optionType = config.getInstrumentFilters().get("optionType").toString();
                Boolean isCall = derivativeInfo.getIsCall();
                if (isCall == null) {
                    return false;
                }
                String tradeOptionType = isCall ? "CE" : "PE";
                if (!tradeOptionType.equals(optionType)) {
                    return false;
                }
            }
            
            // Strike price range
            if (config.getInstrumentFilters().get("minStrike") != null && 
                derivativeInfo.getStrikePrice() != null) {
                Double minStrike = ((Number) config.getInstrumentFilters().get("minStrike")).doubleValue();
                if (derivativeInfo.getStrikePrice().doubleValue() < minStrike) {
                    return false;
                }
            }
            
            if (config.getInstrumentFilters().get("maxStrike") != null && 
                derivativeInfo.getStrikePrice() != null) {
                Double maxStrike = ((Number) config.getInstrumentFilters().get("maxStrike")).doubleValue();
                if (derivativeInfo.getStrikePrice().doubleValue() > maxStrike) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private List<TradeDetails> applySortingWithPageable(List<TradeDetails> trades, org.springframework.data.domain.Pageable pageable) {
        if (pageable == null || pageable.getSort().isUnsorted() || trades.isEmpty()) {
            return trades;
        }
        
        return trades.stream()
                .sorted(createComparatorFromSort(pageable.getSort()))
                .collect(Collectors.toList());
    }
    
    private java.util.Comparator<TradeDetails> createComparatorFromSort(org.springframework.data.domain.Sort sort) {
        java.util.Comparator<TradeDetails> comparator = null;
        
        for (org.springframework.data.domain.Sort.Order order : sort) {
            java.util.Comparator<TradeDetails> fieldComparator = createComparatorForField(order.getProperty(), order.isDescending());
            if (comparator == null) {
                comparator = fieldComparator;
            } else {
                comparator = comparator.thenComparing(fieldComparator);
            }
        }
        
        return comparator != null ? comparator : (t1, t2) -> 0;
    }
    
    private java.util.Comparator<TradeDetails> createComparatorForField(String field, boolean descending) {
        java.util.Comparator<TradeDetails> comparator = switch (field.toLowerCase()) {
            case "entrydate", "tradedate", "entrytimestamp" -> java.util.Comparator.comparing(
                    t -> t.getEntryInfo() != null ? t.getEntryInfo().getTimestamp() : null,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "exitdate", "exittimestamp" -> java.util.Comparator.comparing(
                    t -> t.getExitInfo() != null ? t.getExitInfo().getTimestamp() : null,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "profitloss", "pnl" -> java.util.Comparator.comparing(
                    t -> t.getMetrics() != null ? t.getMetrics().getProfitLoss() : null,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "symbol" -> java.util.Comparator.comparing(
                    t -> t.getSymbol() != null ? t.getSymbol() : "",
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "status" -> java.util.Comparator.comparing(
                    t -> t.getStatus() != null ? t.getStatus().name() : "",
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "strategy" -> java.util.Comparator.comparing(
                    t -> t.getStrategy() != null ? t.getStrategy() : "",
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            case "holdingtimehours", "holdingtime" -> java.util.Comparator.comparing(
                    t -> t.getMetrics() != null ? t.getMetrics().getHoldingTimeHours() : null,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
            default -> java.util.Comparator.comparing(
                    t -> t.getEntryInfo() != null ? t.getEntryInfo().getTimestamp() : null,
                    java.util.Comparator.nullsLast(java.util.Comparator.naturalOrder()));
        };
        
        return descending ? comparator.reversed() : comparator;
    }
    
    private PaginationResult applyPaginationWithPageable(List<TradeDetails> trades, org.springframework.data.domain.Pageable pageable) {
        if (pageable == null || pageable.isUnpaged()) {
            return new PaginationResult(trades, 0, trades.size(), 1, true, true);
        }
        
        int totalItems = trades.size();
        int pageNumber = pageable.getPageNumber();
        int pageSize = pageable.getPageSize();
        
        // Calculate pagination
        int totalPages = (int) Math.ceil((double) totalItems / pageSize);
        int startIndex = (int) pageable.getOffset();
        int endIndex = Math.min(startIndex + pageSize, totalItems);
        
        // Handle out of bounds
        if (startIndex >= totalItems) {
            return new PaginationResult(
                    Collections.emptyList(), 
                    pageNumber, 
                    pageSize, 
                    totalPages,
                    pageNumber == 0,
                    true
            );
        }
        
        List<TradeDetails> paginatedTrades = trades.subList(startIndex, endIndex);
        
        return new PaginationResult(
                paginatedTrades,
                pageNumber,
                pageSize,
                totalPages,
                pageNumber == 0,
                pageNumber >= totalPages - 1
        );
    }
    
    private static class PaginationResult {
        final List<TradeDetails> trades;
        final int page;
        final int size;
        final int totalPages;
        final boolean isFirst;
        final boolean isLast;
        
        PaginationResult(List<TradeDetails> trades, int page, int size, int totalPages, 
                        boolean isFirst, boolean isLast) {
            this.trades = trades;
            this.page = page;
            this.size = size;
            this.totalPages = totalPages;
            this.isFirst = isFirst;
            this.isLast = isLast;
        }
    }
    
    private FilterTradeDetailsResponse buildFilterResponse(
            List<TradeDetails> trades,
            long totalCount,
            FilterTradeDetailsRequest filter,
            String appliedFilterName,
            PaginationResult paginationResult) {
        
        return FilterTradeDetailsResponse.builder()
                .trades(trades)
                .totalCount(totalCount)
                .appliedFilterName(appliedFilterName)
                .filterSummary(buildFilterSummary(filter))
                .page(paginationResult != null ? paginationResult.page : null)
                .size(paginationResult != null ? paginationResult.size : null)
                .totalPages(paginationResult != null ? paginationResult.totalPages : null)
                .isFirst(paginationResult != null ? paginationResult.isFirst : null)
                .isLast(paginationResult != null ? paginationResult.isLast : null)
                .build();
    }
    
    private FilterTradeDetailsResponse.FilterSummary buildFilterSummary(FilterTradeDetailsRequest filter) {
        if (filter.getMetricsConfig() == null) {
            return FilterTradeDetailsResponse.FilterSummary.builder().build();
        }
        
        am.trade.common.models.MetricsFilterConfig config = filter.getMetricsConfig();
        
        String dateRange = null;
        if (config.getDateRange() != null) {
            Object startDate = config.getDateRange().get("startDate");
            Object endDate = config.getDateRange().get("endDate");
            dateRange = String.format("%s to %s", 
                    startDate != null ? startDate : "any",
                    endDate != null ? endDate : "any");
        }
        
        String profitLossRange = null;
        if (config.getProfitLossFilters() != null) {
            Object minPL = config.getProfitLossFilters().get("minProfitLoss");
            Object maxPL = config.getProfitLossFilters().get("maxProfitLoss");
            if (minPL != null || maxPL != null) {
                profitLossRange = String.format("%s to %s",
                        minPL != null ? minPL : "any",
                        maxPL != null ? maxPL : "any");
            }
        }
        
        String holdingTimeRange = null;
        if (config.getTradeCharacteristics() != null) {
            Object minHours = config.getTradeCharacteristics().get("minHoldingTimeHours");
            Object maxHours = config.getTradeCharacteristics().get("maxHoldingTimeHours");
            if (minHours != null || maxHours != null) {
                holdingTimeRange = String.format("%s to %s hours",
                        minHours != null ? minHours : "any",
                        maxHours != null ? maxHours : "any");
            }
        }
        
        // Extract statuses from trade characteristics
        List<String> statuses = null;
        if (config.getTradeCharacteristics() != null && 
            config.getTradeCharacteristics().get("statuses") != null) {
            @SuppressWarnings("unchecked")
            List<String> statusList = (List<String>) config.getTradeCharacteristics().get("statuses");
            statuses = statusList;
        }
        
        // Extract strategies from trade characteristics
        List<String> strategies = null;
        if (config.getTradeCharacteristics() != null && 
            config.getTradeCharacteristics().get("strategies") != null) {
            @SuppressWarnings("unchecked")
            List<String> strategyList = (List<String>) config.getTradeCharacteristics().get("strategies");
            strategies = strategyList;
        }
        
        return FilterTradeDetailsResponse.FilterSummary.builder()
                .portfolioIds(config.getPortfolioIds())
                .symbols(config.getInstruments())
                .statuses(statuses)
                .dateRange(dateRange)
                .strategies(strategies)
                .profitLossRange(profitLossRange)
                .holdingTimeRange(holdingTimeRange)
                .build();
    }
}
