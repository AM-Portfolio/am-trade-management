package am.trade.services.service.impl;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.AssetAllocation;
import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.TradeModel;
import am.trade.common.models.enums.AssetClass;
import am.trade.common.models.enums.MarketSegment;
import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;
import am.trade.common.models.enums.TradeType;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of TradeProcessingService that processes trade executions into complete trades
 * with proper handling of long/short positions, average pricing, and square-offs
 */
@Service
public class TradeProcessingServiceImpl implements TradeProcessingService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TradeProcessingServiceImpl.class);

    private final TradeDetailsService tradeDetailsService;
    private final PortfolioPersistenceService portfolioPersistenceService;
    
    public TradeProcessingServiceImpl(TradeDetailsService tradeDetailsService, PortfolioPersistenceService portfolioPersistenceService) {
        this.tradeDetailsService = tradeDetailsService;
        this.portfolioPersistenceService = portfolioPersistenceService;
    }

    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    /**
     * Converts InstrumentInfo from TradeModel to standalone InstrumentInfo class
     * with enhanced symbol parsing for derivatives
     * 
     * @param modelInstrumentInfo The instrument info from TradeModel
     * @return Converted InstrumentInfo object with properly parsed symbol and segment
     */
    private InstrumentInfo convertToInstrumentInfo(InstrumentInfo modelInstrumentInfo) {
        if (modelInstrumentInfo == null) {
            return null;
        }
        
        String symbol = modelInstrumentInfo.getSymbol();
        
        // Use the domain-driven parsing logic to extract proper symbol and segment
        InstrumentInfo parsedInfo = InstrumentInfo.fromRawSymbol(symbol);
        if (parsedInfo != null) {
            // Copy over any additional information from the original model
            parsedInfo.setIsin(modelInstrumentInfo.getIsin());
            if (modelInstrumentInfo.getExchange() != null) {
                parsedInfo.setExchange(modelInstrumentInfo.getExchange());
            }
            if (modelInstrumentInfo.getSeries() != null) {
                parsedInfo.setSeries(modelInstrumentInfo.getSeries());
            }
            
            // Use the original description if available, otherwise generate one
            if (modelInstrumentInfo.getDescription() != null && !modelInstrumentInfo.getDescription().isEmpty()) {
                parsedInfo.setDescription(modelInstrumentInfo.getDescription());
            } else {
                parsedInfo.setDescription(parsedInfo.getFormattedDescription());
            }
            
            // Copy over additional fields
            parsedInfo.setCurrency(modelInstrumentInfo.getCurrency());
            parsedInfo.setLotSize(modelInstrumentInfo.getLotSize());
            
            // If the original had derivative info but parsing didn't detect it, copy it over
            if (parsedInfo.getDerivativeInfo() == null && modelInstrumentInfo.getDerivativeInfo() != null) {
                parsedInfo.setDerivativeInfo(modelInstrumentInfo.getDerivativeInfo());
            }
            
            return parsedInfo;
        }
        
        // Fallback to original conversion if parsing fails
        return InstrumentInfo.builder()
                .symbol(symbol)
                .rawSymbol(symbol)
                .isin(modelInstrumentInfo.getIsin())
                .exchange(modelInstrumentInfo.getExchange())
                .segment(modelInstrumentInfo.getSegment())
                .series(modelInstrumentInfo.getSeries())
                .description(modelInstrumentInfo.getDescription())
                .currency(modelInstrumentInfo.getCurrency())
                .lotSize(modelInstrumentInfo.getLotSize())
                .build();
    }

    @Override
    public void processTradeDetails(List<String> tradeIds, String portfolioId, String userId) {
        if (tradeIds == null || tradeIds.isEmpty()) {
            return;
        }
        
        // Check if portfolio already exists
        Optional<PortfolioModel> existingPortfolio = portfolioPersistenceService.findByPortfolioId(portfolioId);
        
        Set<String> uniqueTradeIds = new HashSet<>();
        
        if (existingPortfolio.isPresent()) {
            // Combine existing trades with new ones
            List<String> existingTrades = existingPortfolio.get().getTradeIds();
            if (existingTrades != null && !existingTrades.isEmpty()) {
                log.info("Combining {} existing trades with {} new trades for portfolio {}", 
                        existingTrades.size(), tradeIds.size(), portfolioId);
                
                // Add all existing trades to the set to ensure uniqueness
                uniqueTradeIds.addAll(existingTrades);
            }
        }
        
        // Add all new trades to the set (duplicates will be automatically eliminated)
        uniqueTradeIds.addAll(tradeIds);
        
        log.info("After removing duplicates: {} unique trades for portfolio {}", 
                uniqueTradeIds.size(), portfolioId);
                
        // Convert back to list for further processing
        List<String> allTradeIds = new ArrayList<>(uniqueTradeIds);
        processTradeDetailsAndGetPortfolio(allTradeIds, portfolioId, userId);
    }

    public void processTradeDetailsWithObjects(List<TradeDetails> trades, String portfolioId, String userId) {
        if (trades == null || trades.isEmpty()) {
            return;
        }
        
        List<String> tradeIds = trades.stream().map(TradeDetails::getTradeId).collect(Collectors.toList());
        log.info("Processing {} trade objects for portfolio {}", trades.size(), portfolioId);
        
        processTradeDetailsWithObjects(trades, tradeIds, portfolioId, userId);
    }

    private PortfolioModel processTradeDetailsAndGetPortfolio(List<String> tradeIds, String portfolioId, String userId) {
        // Fallback for ID-only processing
        List<TradeDetails> trades = tradeDetailsService.findModelsByTradeIds(tradeIds);
        return processTradeDetailsWithObjects(trades, tradeIds, portfolioId, userId);
    }

    private PortfolioModel processTradeDetailsWithObjects(List<TradeDetails> trades, List<String> tradeIds, String portfolioId, String userId) {

        // Ensure all individual trades have calculated metrics and get the refreshed list
        List<TradeDetails> refreshedTrades = refreshTradeMetricsWithList(trades);

        // Calculate portfolio-level metrics using the refreshed list
        PortfolioMetrics portfolioMetrics = calculatePortfolioMetrics(refreshedTrades, tradeIds);
        
        // Check if portfolio already exists
        Optional<PortfolioModel> existingPortfolio = portfolioPersistenceService.findByPortfolioId(portfolioId);
        
        PortfolioModel portfolioModel;
        
        if (existingPortfolio.isPresent()) {
            // Update existing portfolio
            log.info("Updating existing portfolio with ID: {}", portfolioId);
            portfolioModel = existingPortfolio.get();
            
            // Update metrics, trades and allocations
            portfolioModel.setMetrics(portfolioMetrics);
            portfolioModel.setTradeIds(tradeIds);
            portfolioModel.setAssetAllocations(calculateAssetAllocation(refreshedTrades));
            portfolioModel.setLastUpdatedDate(LocalDateTime.now());
        } else {
            // Create new portfolio
            log.info("Creating new portfolio with ID: {}", portfolioId);
            portfolioModel = PortfolioModel.builder()
                .portfolioId(portfolioId)
                .ownerId(userId)
                .name(portfolioId)
                .description("Auto-generated portfolio from trades")
                .active(true)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .tradeIds(tradeIds)
                .metrics(portfolioMetrics)
                .assetAllocations(calculateAssetAllocation(refreshedTrades))
                .build();
        }
        
        // Save the portfolio model to the database
        return portfolioPersistenceService.savePortfolio(portfolioModel);
    }

    /**
     * Ensures all trades in a list have their metrics calculated.
     * This acts as a synchronous fallback for the Kafka-based calculation pipeline.
     */
    private List<TradeDetails> refreshTradeMetricsWithList(List<TradeDetails> trades) {
        if (trades == null) return new ArrayList<>();
        
        boolean anyUpdated = false;

        for (TradeDetails trade : trades) {
            // Recalculate if metrics are null OR if profitLoss is zero (might be stale/initial value)
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null || 
                trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) == 0) {
                
                log.info("Refreshing metrics for trade: {} (Current P/L: {})", 
                    trade.getTradeId(), 
                    trade.getMetrics() != null ? trade.getMetrics().getProfitLoss() : "null");
                
                TradeMetrics metrics = calculateTradeMetrics(
                    trade.getEntryInfo(), 
                    trade.getExitInfo(), 
                    trade.getTradePositionType()
                );
                
                trade.setMetrics(metrics);
                
                // Determine and set status
                TradeStatus status = determineTradeStatus(trade.getEntryInfo(), trade.getExitInfo(), metrics);
                trade.setStatus(status);
                
                tradeDetailsService.saveTradeDetails(trade);
                anyUpdated = true;
            }

            // Ensure InstrumentInfo is populated for allocation calculation (even if metrics were not refreshed)
            if (trade.getInstrumentInfo() == null || trade.getInstrumentInfo().getSegment() == MarketSegment.UNKNOWN) {
                // Use factory method to build proper InstrumentInfo
                InstrumentInfo info = InstrumentInfo.fromRawSymbol(trade.getSymbol());
                if (info != null) {
                    trade.setInstrumentInfo(info);
                    // We must save if we updated the instrument info
                    tradeDetailsService.saveTradeDetails(trade);
                }
            }
        }
        
        return trades;
    }
    
    /**
     * Calculate portfolio-level metrics from trade details
     */
    private PortfolioMetrics calculatePortfolioMetrics(List<TradeDetails> tradeDetails, List<String> tradeIds) {
        if (tradeDetails == null || tradeDetails.isEmpty()) {
            return PortfolioMetrics.builder().build();
        }
        // Initialize counters and accumulators
        int totalTrades = tradeIds.size();
        int winningTrades = 0;
        int losingTrades = 0;
        int breakEvenTrades = 0;
        int openPositions = 0;
        
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        
        // Process each trade to calculate portfolio metrics
        for (TradeDetails trade : tradeDetails) {
            // Count trades by status
            switch (trade.getStatus()) {
                case WIN:
                    winningTrades++;
                    if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                        totalProfit = totalProfit.add(trade.getMetrics().getProfitLoss());
                    }
                    break;
                case LOSS:
                    losingTrades++;
                    if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                        totalLoss = totalLoss.add(trade.getMetrics().getProfitLoss().abs());
                    }
                    break;
                case BREAK_EVEN:
                    breakEvenTrades++;
                    break;
                case OPEN:
                    openPositions++;
                    break;
            }
            
            // Add to total value
            if (trade.getEntryInfo() != null && trade.getEntryInfo().getTotalValue() != null) {
                totalValue = totalValue.add(trade.getEntryInfo().getTotalValue());
            }
        }
        
        // Calculate win rate and loss rate
        BigDecimal winRate = BigDecimal.ZERO;
        BigDecimal lossRate = BigDecimal.ZERO;
        int closedTrades = winningTrades + losingTrades + breakEvenTrades;
        
        if (closedTrades > 0) {
            winRate = BigDecimal.valueOf(winningTrades)
                    .divide(BigDecimal.valueOf(closedTrades), DECIMAL_SCALE, ROUNDING_MODE)
                    .multiply(BigDecimal.valueOf(100));
            
            lossRate = BigDecimal.valueOf(losingTrades)
                    .divide(BigDecimal.valueOf(closedTrades), DECIMAL_SCALE, ROUNDING_MODE)
                    .multiply(BigDecimal.valueOf(100));
        }
        
        // Calculate profit factor
        BigDecimal profitFactor = totalLoss.compareTo(BigDecimal.ZERO) > 0
                ? totalProfit.divide(totalLoss, DECIMAL_SCALE, ROUNDING_MODE)
                : totalProfit.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(999) : BigDecimal.ONE;
        
        // Calculate net profit/loss
        BigDecimal netProfitLoss = totalProfit.subtract(totalLoss);
        
        // Calculate net profit/loss percentage
        BigDecimal netProfitLossPercentage = totalValue.compareTo(BigDecimal.ZERO) > 0
                ? netProfitLoss.divide(totalValue, DECIMAL_SCALE, ROUNDING_MODE).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        // Calculate expectancy
        BigDecimal expectancy = closedTrades > 0
                ? netProfitLoss.divide(BigDecimal.valueOf(closedTrades), DECIMAL_SCALE, ROUNDING_MODE)
                : BigDecimal.ZERO;
        
        // Build and return portfolio metrics
        return PortfolioMetrics.builder()
                .totalTrades(totalTrades)
                .winningTrades(winningTrades)
                .losingTrades(losingTrades)
                .breakEvenTrades(breakEvenTrades)
                .openPositions(openPositions)
                .winRate(winRate)
                .lossRate(lossRate)
                .profitFactor(profitFactor)
                .expectancy(expectancy)
                .totalValue(totalValue)
                .totalProfit(totalProfit)
                .totalLoss(totalLoss)
                .netProfitLoss(netProfitLoss)
                .netProfitLossPercentage(netProfitLossPercentage)
                .build();
    }

    @Override
    public List<TradeDetails> processTradeModels(List<TradeModel> trades, String portfolioId) {
        if (trades == null || trades.isEmpty()) {
            return new ArrayList<>();
        }

        // Group trades by symbol to handle multiple securities
        Map<String, List<TradeModel>> tradesBySymbol = groupTradesBySymbol(trades);
        
        // Process each group of trades separately
        List<TradeDetails> result = new ArrayList<>();
        for (Map.Entry<String, List<TradeModel>> entry : tradesBySymbol.entrySet()) {
            String symbol = entry.getKey();
            List<TradeModel> symbolTrades = entry.getValue();
            
            List<TradeDetails> symbolTradeDetails = processSymbolTrades(symbolTrades, symbol, portfolioId);
            result.addAll(symbolTradeDetails);
        }
        
        return result;
    }
    
    /**
     * Group trades by their symbol
     * 
     * @param trades List of trade models
     * @return Map of trades grouped by symbol
     */
    private Map<String, List<TradeModel>> groupTradesBySymbol(List<TradeModel> trades) {
        return trades.stream()
                .collect(Collectors.groupingBy(trade -> trade.getInstrumentInfo().getSymbol()));
    }
    
    /**
     * Process all trades for a specific symbol
     * 
     * @param symbolTrades Trades for a specific symbol
     * @param symbol The symbol being processed
     * @param portfolioId The portfolio ID
     * @return List of trade details for this symbol
     */
    private List<TradeDetails> processSymbolTrades(List<TradeModel> symbolTrades, String symbol, String portfolioId) {
        List<TradeDetails> result = new ArrayList<>();
        
        // Sort trades by execution time
        List<TradeModel> sortedTrades = sortTradesByExecutionTime(symbolTrades);
        
        if (sortedTrades.isEmpty()) {
            return result;
        }
        
        // Identify separate trade cycles (buy-sell cycles) within the same symbol
        List<List<TradeModel>> tradeCycles = identifyTradeCycles(sortedTrades);
        
        // Process each trade cycle separately
        for (List<TradeModel> tradeCycle : tradeCycles) {
            if (tradeCycle.isEmpty()) {
                continue;
            }
            
            TradeDetails tradeDetails = processTradeCycle(tradeCycle, symbol, portfolioId);
            result.add(tradeDetails);
        }
        
        return result;
    }
    
    /**
     * Sort trades by execution time in ascending order
     * 
     * @param trades List of trades to sort
     * @return Sorted list of trades
     */
    private List<TradeModel> sortTradesByExecutionTime(List<TradeModel> trades) {
        return trades.stream()
                .sorted(Comparator.comparing(trade -> trade.getBasicInfo().getOrderExecutionTime()))
                .collect(Collectors.toList());
    }
    
    /**
     * Process a single trade cycle (buy-sell cycle) to create a TradeDetails object
     * 
     * @param tradeCycle List of trades in a single cycle
     * @param symbol The symbol being traded
     * @param portfolioId The portfolio ID
     * @return TradeDetails object representing the complete trade
     */
    private TradeDetails processTradeCycle(List<TradeModel> tradeCycle, String symbol, String portfolioId) {
        // Get the first trade to determine if it's a LONG or SHORT position
        TradeModel firstTrade = tradeCycle.get(0);
        TradePositionType tradePositionType = determineTradeType(firstTrade);
        
        // Get entry and exit information
        EntryExitInfo entryInfo = calculateEntryInfo(tradeCycle, tradePositionType);
        EntryExitInfo exitInfo = calculateExitInfo(tradeCycle, tradePositionType);
        
        // Handle SHORT positions differently
        entryInfo = adjustEntryExitForShortPositions(entryInfo, exitInfo, tradePositionType);
        
        // Calculate trade metrics
        TradeMetrics metrics = calculateTradeMetrics(entryInfo, exitInfo, tradePositionType);
        
        // Determine trade status
        TradeStatus status = determineTradeStatus(entryInfo, exitInfo, metrics);
        
        // Build and return the complete trade details
        return buildTradeDetails(tradeCycle, firstTrade, symbol, portfolioId, tradePositionType, 
                                status, entryInfo, exitInfo, metrics);
    }
    
    /**
     * For SHORT positions, swap entry and exit info since the first trade is a SELL (entry) 
     * and last trade is a BUY (exit), which is the reverse of LONG positions
     * 
     * @param entryInfo Original entry info
     * @param exitInfo Original exit info
     * @param tradePositionType The position type (LONG or SHORT)
     * @return Adjusted entry info
     */
    private EntryExitInfo adjustEntryExitForShortPositions(EntryExitInfo entryInfo, 
                                                          EntryExitInfo exitInfo,
                                                          TradePositionType tradePositionType) {
        if (tradePositionType == TradePositionType.SHORT) {
            // Swap entry and exit for SHORT positions
            return exitInfo;
        }
        return entryInfo;
    }
    
    /**
     * Build a TradeDetails object from the processed trade data
     * 
     * @param tradeCycle The trade cycle
     * @param firstTrade The first trade in the cycle
     * @param symbol The symbol being traded
     * @param portfolioId The portfolio ID
     * @param tradePositionType The position type (LONG or SHORT)
     * @param status The trade status
     * @param entryInfo The entry information
     * @param exitInfo The exit information
     * @param metrics The calculated trade metrics
     * @return Complete TradeDetails object
     */
    private TradeDetails buildTradeDetails(List<TradeModel> tradeCycle, TradeModel firstTrade,
                                           String symbol, String portfolioId, 
                                           TradePositionType tradePositionType, TradeStatus status,
                                           EntryExitInfo entryInfo, EntryExitInfo exitInfo, 
                                           TradeMetrics metrics) {
        
        InstrumentInfo instrumentInfo = convertToInstrumentInfo(firstTrade.getInstrumentInfo());
        return TradeDetails.builder()
                .tradeId(UUID.randomUUID().toString()) // Generate a unique ID for the trade
                .portfolioId(portfolioId)
                .symbol(instrumentInfo.getSymbol())
                .instrumentInfo(instrumentInfo)
                .tradePositionType(tradePositionType)
                .status(status)
                .entryInfo(entryInfo)
                .exitInfo(exitInfo)
                .metrics(metrics)
                .tradeExecutions(tradeCycle)
                .build();
    }



    @Override
    public TradeDetails getCurrentPosition(String symbol, String portfolioId) {
        // This would typically involve querying a database or cache for the current position
        // For now, we'll return null as a placeholder
        log.info("Getting current position for symbol {} in portfolio {}", symbol, portfolioId);
        return null;
    }
    
    /**
     * Sorts winning trades by profit in descending order (highest profit first)
     * 
     * @param trades List of all trades
     * @return List of winning trades sorted by profit (highest to lowest)
     */
    private List<TradeDetails> sortWinningTrades(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new ArrayList<>();
        }
        
        return trades.stream()
                .filter(trade -> trade.getStatus() == TradeStatus.WIN)
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null)
                .sorted((t1, t2) -> t2.getMetrics().getProfitLoss().compareTo(t1.getMetrics().getProfitLoss()))
                .collect(Collectors.toList());
    }
    
    /**
     * Sorts losing trades by loss amount in descending order (highest loss first)
     * Loss amounts are typically negative, so we sort by the absolute value
     * 
     * @param trades List of all trades
     * @return List of losing trades sorted by loss amount (highest to lowest)
     */
    private List<TradeDetails> sortLosingTrades(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new ArrayList<>();
        }
        
        return trades.stream()
                .filter(trade -> trade.getStatus() == TradeStatus.LOSS)
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null)
                .sorted((t1, t2) -> t1.getMetrics().getProfitLoss().compareTo(t2.getMetrics().getProfitLoss()))
                .collect(Collectors.toList());
    }
    
    /**
     * Determine if the trade is LONG or SHORT based on the first trade
     * If first trade is BUY, it's a LONG position
     * If first trade is SELL, it's a SHORT position
     */
    private TradePositionType determineTradeType(TradeModel firstTrade) {
        TradeType tradeType = firstTrade.getBasicInfo().getTradeType();
        
        // If the first trade is a BUY, it's a LONG position
        // If the first trade is a SELL, it's a SHORT position
        if (TradeType.BUY.equals(tradeType)) {
            return TradePositionType.LONG;
        } else {
            return TradePositionType.SHORT;
        }
    }
    
    /**
     * Calculate entry information based on the trade type and trades
     */
    private EntryExitInfo calculateEntryInfo(List<TradeModel> trades, TradePositionType tradePositionType) {
        List<TradeModel> entryTrades = new ArrayList<>();
        
        // For LONG positions, entry trades are BUY orders
        // For SHORT positions, entry trades are SELL orders
        for (TradeModel trade : trades) {
            TradeType tradeExecutionType = trade.getBasicInfo().getTradeType();
            
            if ((tradePositionType == TradePositionType.LONG && tradeExecutionType == TradeType.BUY) ||
                (tradePositionType == TradePositionType.SHORT && tradeExecutionType == TradeType.SELL)) {
                entryTrades.add(trade);
            }
        }
        
        if (entryTrades.isEmpty()) {
            return null;
        }
        
        // Calculate average entry price and total quantity
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal weightedPriceSum = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        LocalDateTime firstEntryTime = entryTrades.get(0).getBasicInfo().getOrderExecutionTime();
        
        for (TradeModel trade : entryTrades) {
            BigDecimal quantity = BigDecimal.valueOf(trade.getExecutionInfo().getQuantity());
            BigDecimal price = trade.getExecutionInfo().getPrice();
            
            totalQuantity = totalQuantity.add(quantity);
            weightedPriceSum = weightedPriceSum.add(price.multiply(quantity));
            
            // Calculate trade value
            BigDecimal tradeValue = price.multiply(quantity);
            totalValue = totalValue.add(tradeValue);
            
            // Sum up fees if available
            if (trade.getCharges() != null) {
                totalFees = totalFees.add(trade.getCharges().getTotalTaxes());
            }
        }
        
        // Calculate average entry price
        BigDecimal averageEntryPrice = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? weightedPriceSum.divide(totalQuantity, DECIMAL_SCALE, ROUNDING_MODE)
                : BigDecimal.ZERO;
        
        // Ensure totalValue is calculated if missing
        if (totalValue.compareTo(BigDecimal.ZERO) == 0 && averageEntryPrice.compareTo(BigDecimal.ZERO) > 0) {
            totalValue = averageEntryPrice.multiply(totalQuantity);
        }
        
        return EntryExitInfo.builder()
                .timestamp(firstEntryTime)
                .price(averageEntryPrice)
                .quantity(totalQuantity.intValue())
                .totalValue(totalValue)
                .fees(totalFees)
                .build();
    }
    
    /**
     * Calculate exit information based on the trade type and trades
     */
    private EntryExitInfo calculateExitInfo(List<TradeModel> trades, TradePositionType tradePositionType) {
        List<TradeModel> exitTrades = new ArrayList<>();
        
        // For LONG positions, exit trades are SELL orders
        // For SHORT positions, exit trades are BUY orders
        for (TradeModel trade : trades) {
            TradeType tradeExecutionType = trade.getBasicInfo().getTradeType();
            
            if ((tradePositionType == TradePositionType.LONG && tradeExecutionType == TradeType.SELL) ||
                (tradePositionType == TradePositionType.SHORT && tradeExecutionType == TradeType.BUY)) {
                exitTrades.add(trade);
            }
        }
        
        if (exitTrades.isEmpty()) {
            return null; // Position is still open
        }
        
        // Calculate average exit price and total quantity
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal weightedPriceSum = BigDecimal.ZERO;
        BigDecimal totalValue = BigDecimal.ZERO;
        BigDecimal totalFees = BigDecimal.ZERO;
        LocalDateTime lastExitTime = exitTrades.get(exitTrades.size() - 1).getBasicInfo().getOrderExecutionTime();
        
        for (TradeModel trade : exitTrades) {
            BigDecimal quantity = BigDecimal.valueOf(trade.getExecutionInfo().getQuantity());
            BigDecimal price = trade.getExecutionInfo().getPrice();
            
            totalQuantity = totalQuantity.add(quantity);
            weightedPriceSum = weightedPriceSum.add(price.multiply(quantity));
            
            // Calculate trade value
            BigDecimal tradeValue = price.multiply(quantity);
            totalValue = totalValue.add(tradeValue);
            
            // Sum up fees if available
            if (trade.getCharges() != null) {
                totalFees = totalFees.add(trade.getCharges().getTotalTaxes());
            }
        }
        
        // Calculate average exit price
        BigDecimal averageExitPrice = totalQuantity.compareTo(BigDecimal.ZERO) > 0 
                ? weightedPriceSum.divide(totalQuantity, DECIMAL_SCALE, ROUNDING_MODE)
                : BigDecimal.ZERO;
        
        // Ensure totalValue is calculated if missing
        if (totalValue.compareTo(BigDecimal.ZERO) == 0 && averageExitPrice.compareTo(BigDecimal.ZERO) > 0) {
            totalValue = averageExitPrice.multiply(totalQuantity);
        }
        
        return EntryExitInfo.builder()
                .timestamp(lastExitTime)
                .price(averageExitPrice)
                .quantity(totalQuantity.intValue())
                .totalValue(totalValue)
                .fees(totalFees)
                .build();
    }
    
    /**
     * Calculate trade metrics based on entry and exit information
     */
    private TradeMetrics calculateTradeMetrics(
        EntryExitInfo entryInfo, 
        EntryExitInfo exitInfo,
            TradePositionType tradePositionType) {
        
        // If position is still open (no exit info), return basic metrics
        if (exitInfo == null) {
            return TradeMetrics.builder()
                    .profitLoss(BigDecimal.ZERO)
                    .profitLossPercentage(BigDecimal.ZERO)
                    .returnOnEquity(BigDecimal.ZERO)
                    .build();
        }
        
        // Calculate profit/loss
        BigDecimal profitLoss;
        // Default to LONG for calculation if position type is missing
        TradePositionType positionType = tradePositionType != null ? tradePositionType : TradePositionType.LONG;
        
        if (positionType == TradePositionType.LONG) {
            // For LONG positions: (exitPrice - entryPrice) * quantity
            profitLoss = exitInfo.getPrice()
                    .subtract(entryInfo.getPrice())
                    .multiply(BigDecimal.valueOf(entryInfo.getQuantity()));
        } else {
            // For SHORT positions: (entryPrice - exitPrice) * quantity
            profitLoss = entryInfo.getPrice()
                    .subtract(exitInfo.getPrice())
                    .multiply(BigDecimal.valueOf(entryInfo.getQuantity()));
        }
        
        // Subtract fees (safely handle nulls)
        BigDecimal entryFees = entryInfo.getFees() != null ? entryInfo.getFees() : BigDecimal.ZERO;
        BigDecimal exitFees = exitInfo.getFees() != null ? exitInfo.getFees() : BigDecimal.ZERO;
        BigDecimal totalFees = entryFees.add(exitFees);
        profitLoss = profitLoss.subtract(totalFees);
        
        // Calculate profit/loss percentage
        BigDecimal initialInvestment = entryInfo.getTotalValue();
        
        // Safety fallback: If totalValue is null, calculate it from price * quantity
        if (initialInvestment == null || initialInvestment.compareTo(BigDecimal.ZERO) == 0) {
            initialInvestment = entryInfo.getPrice().multiply(BigDecimal.valueOf(entryInfo.getQuantity()));
        }
        
        BigDecimal profitLossPercentage = initialInvestment.compareTo(BigDecimal.ZERO) > 0
                ? profitLoss.divide(initialInvestment, DECIMAL_SCALE, ROUNDING_MODE).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        // Calculate return on equity (assuming 100% margin for simplicity)
        BigDecimal returnOnEquity = profitLossPercentage;
        
        // Calculate holding time
        long holdingTimeDays = 0;
        long holdingTimeHours = 0;
        long holdingTimeMinutes = 0;
        
        if (entryInfo.getTimestamp() != null && exitInfo.getTimestamp() != null) {
            Duration holdingTime = Duration.between(entryInfo.getTimestamp(), exitInfo.getTimestamp());
            holdingTimeDays = holdingTime.toDays();
            holdingTimeHours = holdingTime.toHours() % 24;
            holdingTimeMinutes = holdingTime.toMinutes() % 60;
        }
        
        // For now, we'll set risk amount and reward amount to simple values
        // In a real implementation, these would be calculated based on stop loss and take profit levels
        BigDecimal riskAmount = initialInvestment.multiply(BigDecimal.valueOf(0.02)); // Assume 2% risk
        BigDecimal rewardAmount = profitLoss.compareTo(BigDecimal.ZERO) > 0 ? profitLoss : riskAmount.multiply(BigDecimal.valueOf(2));
        
        // Calculate risk/reward ratio
        BigDecimal riskRewardRatio = riskAmount.compareTo(BigDecimal.ZERO) > 0 && rewardAmount.compareTo(BigDecimal.ZERO) > 0
                ? rewardAmount.divide(riskAmount, DECIMAL_SCALE, ROUNDING_MODE)
                : BigDecimal.ONE;
        
        return TradeMetrics.builder()
                .profitLoss(profitLoss)
                .profitLossPercentage(profitLossPercentage)
                .returnOnEquity(returnOnEquity)
                .riskAmount(riskAmount)
                .rewardAmount(rewardAmount)
                .riskRewardRatio(riskRewardRatio)
                .holdingTimeDays(holdingTimeDays)
                .holdingTimeHours(holdingTimeHours)
                .holdingTimeMinutes(holdingTimeMinutes)
                .build();
    }
    
    /**
     * Determine the trade status based on entry/exit info and metrics
     */
    /**
     * Identifies separate trade cycles (buy-sell cycles) within the same symbol.
     * For example, if you buy 50, sell 50, then buy 10, sell 10, this should be treated as two separate trades.
     */
    private List<List<TradeModel>> identifyTradeCycles(List<TradeModel> sortedTrades) {
        List<List<TradeModel>> tradeCycles = new ArrayList<>();
        if (sortedTrades.isEmpty()) {
            return tradeCycles;
        }
        
        // Determine if the overall position is LONG or SHORT based on the first trade
        TradeModel firstTrade = sortedTrades.get(0);
        TradePositionType positionType = determineTradeType(firstTrade);
        
        // Track the current position size
        int currentPosition = 0;
        List<TradeModel> currentCycle = new ArrayList<>();
        
        for (TradeModel trade : sortedTrades) {
            TradeType tradeType = trade.getBasicInfo().getTradeType();
            int quantity = trade.getExecutionInfo().getQuantity() != null ? trade.getExecutionInfo().getQuantity() : 0;
            
            // Add the trade to the current cycle
            currentCycle.add(trade);
            
            // Update position size based on trade type
            if ((positionType == TradePositionType.LONG && tradeType == TradeType.BUY) ||
                (positionType == TradePositionType.SHORT && tradeType == TradeType.SELL)) {
                // Increasing position
                currentPosition += quantity;
            } else {
                // Decreasing position
                currentPosition -= quantity;
            }
            
            // If position is completely closed (back to zero), end the current cycle
            if (currentPosition == 0 && !currentCycle.isEmpty()) {
                tradeCycles.add(new ArrayList<>(currentCycle));
                currentCycle.clear();
            }
        }
        
        // If there's an open position left, add it as the final cycle
        if (!currentCycle.isEmpty()) {
            tradeCycles.add(currentCycle);
        }
        
        return tradeCycles;
    }
    
    private TradeStatus determineTradeStatus(
        EntryExitInfo entryInfo,
        EntryExitInfo exitInfo,
        TradeMetrics metrics) {
        
        // If no exit info, position is still open
        if (exitInfo == null) {
            return TradeStatus.OPEN;
        }
        
        // Check if quantities match (position is fully closed)
        if (!Objects.equals(entryInfo.getQuantity(), exitInfo.getQuantity())) {
            return TradeStatus.OPEN; // Partially closed position
        }
        
        // Determine if it's a win, loss, or break-even
        if (metrics.getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
            return TradeStatus.WIN;
        } else if (metrics.getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
            return TradeStatus.LOSS;
        } else {
            return TradeStatus.BREAK_EVEN;
        }
    }

    /**
     * Calculates asset allocation based on trade counts across different asset classes
     * 
     * @param trades The list of trade details
     * @return List of AssetAllocation objects
<<<<<<< HEAD
=======
     */
    private List<AssetAllocation> calculateAssetAllocation(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("Calculating asset allocation for {} trades", trades.size());

        // Group trades by AssetClass
        Map<AssetClass, Long> countsByClass = trades.stream()
            .map(t -> {
                MarketSegment segment = t.getInstrumentInfo() != null ? t.getInstrumentInfo().getSegment() : MarketSegment.UNKNOWN;
                if (segment == MarketSegment.EQUITY || segment == MarketSegment.EQ) {
                    return AssetClass.STOCK;
                } else if (segment != null && segment.isDerivative()) {
                    if (segment == MarketSegment.EQUITY_OPTIONS || segment == MarketSegment.INDEX_OPTIONS || segment == MarketSegment.OPT) {
                        return AssetClass.OPTION;
                    } else {
                        return AssetClass.FUTURES;
                    }
                } else if (segment == MarketSegment.CURRENCY) {
                    return AssetClass.FOREX;
                } else if (segment == MarketSegment.COMMODITY) {
                    return AssetClass.COMMODITY;
                }
                return AssetClass.OTHER;
            })
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        long totalTrades = (long) trades.size();

        return countsByClass.entrySet().stream()
            .map(entry -> AssetAllocation.builder()
                .assetClass(entry.getKey())
                .currentPercentage(BigDecimal.valueOf(entry.getValue())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTrades), 2, RoundingMode.HALF_UP))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Helper to get a reliable timestamp for a trade, falling back to tradeDate if executionTime is null.
>>>>>>> 6c5385cf07e073d9fadc1657da75894383d6da1f
     */
    private List<AssetAllocation> calculateAssetAllocation(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new ArrayList<>();
        }

        log.debug("Calculating asset allocation for {} trades", trades.size());

        // Group trades by AssetClass
        Map<AssetClass, Long> countsByClass = trades.stream()
            .map(t -> {
                MarketSegment segment = t.getInstrumentInfo() != null ? t.getInstrumentInfo().getSegment() : MarketSegment.UNKNOWN;
                if (segment == MarketSegment.EQUITY || segment == MarketSegment.EQ) {
                    return AssetClass.STOCK;
                } else if (segment != null && segment.isDerivative()) {
                    if (segment == MarketSegment.EQUITY_OPTIONS || segment == MarketSegment.INDEX_OPTIONS || segment == MarketSegment.OPT) {
                        return AssetClass.OPTION;
                    } else {
                        return AssetClass.FUTURES;
                    }
                } else if (segment == MarketSegment.CURRENCY) {
                    return AssetClass.FOREX;
                } else if (segment == MarketSegment.COMMODITY) {
                    return AssetClass.COMMODITY;
                }
                return AssetClass.OTHER;
            })
            .collect(Collectors.groupingBy(c -> c, Collectors.counting()));

        long totalTrades = (long) trades.size();

        return countsByClass.entrySet().stream()
            .map(entry -> AssetAllocation.builder()
                .assetClass(entry.getKey())
                .currentPercentage(BigDecimal.valueOf(entry.getValue())
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(totalTrades), 2, RoundingMode.HALF_UP))
                .build())
            .collect(Collectors.toList());
    }
}
