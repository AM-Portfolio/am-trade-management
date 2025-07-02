package am.trade.services.service.impl;

import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import am.trade.common.models.TradeModel;
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
@Slf4j
public class TradeProcessingServiceImpl implements TradeProcessingService {

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
     * 
     * @param modelInstrumentInfo The instrument info from TradeModel
     * @return Converted InstrumentInfo object
     */
    private am.trade.common.models.InstrumentInfo convertToInstrumentInfo(am.trade.common.models.InstrumentInfo modelInstrumentInfo) {
        if (modelInstrumentInfo == null) {
            return null;
        }
        
        return am.trade.common.models.InstrumentInfo.builder()
                .symbol(modelInstrumentInfo.getSymbol())
                .isin(modelInstrumentInfo.getIsin())
                .exchange(modelInstrumentInfo.getExchange())
                .segment(modelInstrumentInfo.getSegment())
                .series(modelInstrumentInfo.getSeries())
                .build();
    }

    @Override
    public void processTradeDetails(List<String> tradeIds, String portfolioId) {
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
        
        processTradeDetailsAndGetPortfolio(allTradeIds, portfolioId);
    }

    private PortfolioModel processTradeDetailsAndGetPortfolio(List<String> tradeIds, String portfolioId) {

        // Calculate portfolio-level metrics
        PortfolioMetrics portfolioMetrics = calculatePortfolioMetrics(tradeIds);
        
        // Check if portfolio already exists
        Optional<PortfolioModel> existingPortfolio = portfolioPersistenceService.findByPortfolioId(portfolioId);
        
        PortfolioModel portfolioModel;
        
        if (existingPortfolio.isPresent()) {
            // Update existing portfolio
            log.info("Updating existing portfolio with ID: {}", portfolioId);
            portfolioModel = existingPortfolio.get();
            
            // Update metrics and trades
            portfolioModel.setMetrics(portfolioMetrics);
            portfolioModel.setTradeIds(tradeIds);
            portfolioModel.setLastUpdatedDate(LocalDateTime.now());
        } else {
            // Create new portfolio
            log.info("Creating new portfolio with ID: {}", portfolioId);
            portfolioModel = PortfolioModel.builder()
                .portfolioId(portfolioId)
                .name("Portfolio " + portfolioId) // Default name, can be updated later
                .description("Auto-generated portfolio from trades")
                .active(true)
                .createdDate(LocalDateTime.now())
                .lastUpdatedDate(LocalDateTime.now())
                .tradeIds(tradeIds)
                .metrics(portfolioMetrics)
                .build();
        }
        
        // Save the portfolio model to the database
        portfolioModel = portfolioPersistenceService.savePortfolio(portfolioModel);

        return portfolioModel;
    }
    
    /**
     * Calculate portfolio-level metrics from trade details
     */
    private PortfolioMetrics calculatePortfolioMetrics(List<String> tradeIds) {
        List<TradeDetails> tradeDetails = tradeDetailsService.findModelsByTradeIds(tradeIds);
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
    public List<TradeDetails> 
    processTradeModels(List<TradeModel> trades, String portfolioId) {
        if (trades == null || trades.isEmpty()) {
            return new ArrayList<>();
        }

        // Group trades by symbol to handle multiple securities
        Map<String, List<TradeModel>> tradesBySymbol = trades.stream()
                .collect(Collectors.groupingBy(trade -> trade.getInstrumentInfo().getSymbol()));
        
        // Process each group of trades separately
        List<TradeDetails> result = new ArrayList<>();
        for (Map.Entry<String, List<TradeModel>> entry : tradesBySymbol.entrySet()) {
            String symbol = entry.getKey();
            List<TradeModel> symbolTrades = entry.getValue();
            
            // Sort trades by execution time
            List<TradeModel> sortedTrades = symbolTrades.stream()
                    .sorted(Comparator.comparing(trade -> trade.getBasicInfo().getOrderExecutionTime()))
                    .collect(Collectors.toList());
            
            if (sortedTrades.isEmpty()) {
                continue;
            }
            
            // Identify separate trade cycles (buy-sell cycles) within the same symbol
            List<List<TradeModel>> tradeCycles = identifyTradeCycles(sortedTrades);
            
            // Process each trade cycle separately
            for (List<TradeModel> tradeCycle : tradeCycles) {
                if (tradeCycle.isEmpty()) {
                    continue;
                }
                
                // Get the first trade to determine if it's a LONG or SHORT position
                TradeModel firstTrade = tradeCycle.get(0);
                TradePositionType tradePositionType = determineTradeType(firstTrade);
                
                // Process the trades to build entry and exit information
                EntryExitInfo entryInfo = calculateEntryInfo(tradeCycle, tradePositionType);
                EntryExitInfo exitInfo = calculateExitInfo(tradeCycle, tradePositionType);
                
                // For SHORT positions, swap entry and exit info since the first trade is a SELL (entry) and last trade is a BUY (exit)
                // which is the reverse of LONG positions
                if (tradePositionType == TradePositionType.SHORT) {
                    EntryExitInfo temp = entryInfo;
                    entryInfo = exitInfo;
                    exitInfo = temp;
                }
                
                // Calculate trade metrics
                TradeMetrics metrics = calculateTradeMetrics(entryInfo, exitInfo, tradePositionType);
                
                // Determine trade status
                TradeStatus status = determineTradeStatus(entryInfo, exitInfo, metrics);
                
                // Build the complete trade model
                TradeDetails tradeDetails = TradeDetails.builder()
                        .tradeId(UUID.randomUUID().toString()) // Generate a unique ID for the trade
                        .portfolioId(portfolioId)
                        .symbol(symbol)
                        .instrumentInfo(convertToInstrumentInfo(firstTrade.getInstrumentInfo()))
                        .tradePositionType(tradePositionType)
                        .status(status)
                        .entryInfo(entryInfo)
                        .exitInfo(exitInfo)
                        .metrics(metrics)
                        .tradeExecutions(tradeCycle)
                        .build();
                
                result.add(tradeDetails);
            }
        }
        
        return result;
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
     * Extract the symbol from the trades
     */
    private String extractSymbol(List<TradeModel> trades) {
        // For simplicity, we'll use the symbol from the first trade
        // In a real implementation, you might want to validate that all trades have the same symbol
        return trades.get(0).getInstrumentInfo().getSymbol();
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
        if (tradePositionType == TradePositionType.LONG) {
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
        
        // Subtract fees
        BigDecimal totalFees = entryInfo.getFees().add(exitInfo.getFees());
        profitLoss = profitLoss.subtract(totalFees);
        
        // Calculate profit/loss percentage
        BigDecimal initialInvestment = entryInfo.getTotalValue();
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
}
