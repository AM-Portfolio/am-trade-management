package am.trade.analytics.service.impl;

import am.trade.analytics.model.TradeReplay;
import am.trade.analytics.model.dto.TradeReplayRequest;
import am.trade.analytics.model.dto.TradeReplayResponse;
import am.trade.analytics.service.TradeReplayService;
import am.trade.analytics.service.TradeSamplingService;
import am.trade.analytics.util.TradeAnalyticsUtils;
import am.trade.common.models.PriceDataPoint;
import am.trade.analytics.mapper.TradeReplayMapper;
import am.trade.analytics.mapper.PersistenceModelMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import am.trade.persistence.repository.TradeReplayRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the TradeReplayService interface
 * Provides functionality for trade replay analysis based on entry and exit dates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeReplayServiceImpl implements TradeReplayService {

    private final TradeReplayRepository tradeReplayRepository;
    private final TradeAnalyticsUtils analyticsUtils;
    private final TradeReplayMapper tradeReplayMapper;
    private final TradeSamplingService tradeSamplingService;
    private final PersistenceModelMapper persistenceModelMapper;

    @Override
    public TradeReplayResponse createTradeReplay(TradeReplayRequest request) {
        log.info("Creating trade replay for symbol: {}, entry date: {}, exit date: {}", 
                request.getSymbol(), request.getEntryDate(), request.getExitDate());
        
        // Generate a unique replay ID
        String replayId = UUID.randomUUID().toString();
        
        // Calculate holding period in days
        int holdingPeriodDays = (int) ChronoUnit.DAYS.between(request.getEntryDate(), request.getExitDate());
        
        // Fetch historical price data for the symbol during the holding period
        List<PriceDataPoint> priceDataPoints = analyticsUtils.fetchHistoricalPriceData(
                request.getSymbol(), request.getEntryDate(), request.getExitDate());
        
        // Calculate profit/loss
        BigDecimal profitLoss = calculateProfitLoss(
                request.getEntryPrice(), request.getExitPrice(), 
                request.getPositionSize(), request.getSide());
        
        // Calculate profit/loss percentage
        BigDecimal profitLossPercentage = calculateProfitLossPercentage(
                request.getEntryPrice(), request.getExitPrice(), request.getSide());
        
        // Calculate max drawdown and max profit
        BigDecimal[] maxDrawdownAndProfit = calculateMaxDrawdownAndProfit(
                priceDataPoints, request.getEntryPrice(), request.getSide());
        BigDecimal maxDrawdown = maxDrawdownAndProfit[0];
        BigDecimal maxProfit = maxDrawdownAndProfit[1];
        
        // Calculate max drawdown and max profit percentages
        BigDecimal maxDrawdownPercentage = maxDrawdown.divide(request.getEntryPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
        BigDecimal maxProfitPercentage = maxProfit.divide(request.getEntryPrice(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
                
        // Calculate volatility and average daily movement
        BigDecimal volatility = analyticsUtils.calculateVolatility(priceDataPoints);
        BigDecimal averageDailyMovement = analyticsUtils.calculateAverageDailyMovement(priceDataPoints);
        
        log.info("Calculated analytics - Volatility: {}%, Average Daily Movement: {}%", 
                volatility, averageDailyMovement);
        
        // Determine if this trade should be stored based on sampling strategy
        boolean shouldStore = tradeSamplingService.shouldStoreTradeReplay(
                request, profitLossPercentage, volatility);
        
        // Create the trade replay entity using the mapper
        TradeReplay tradeReplay = tradeReplayMapper.createTradeReplayEntity(
                request, 
                replayId, 
                profitLoss, 
                profitLossPercentage, 
                maxDrawdown, 
                maxDrawdownPercentage, 
                maxProfit, 
                maxProfitPercentage, 
                holdingPeriodDays, 
                volatility, 
                averageDailyMovement, 
                priceDataPoints);
        
        TradeReplay savedReplay;
        
        // Only save to MongoDB if sampling strategy determines it should be stored
        if (shouldStore) {
            log.info("Storing trade replay {} based on sampling strategy", replayId);
            am.trade.persistence.entity.TradeReplay persistenceEntity = persistenceModelMapper.toPersistenceEntity(tradeReplay);
            am.trade.persistence.entity.TradeReplay savedEntity = tradeReplayRepository.save(persistenceEntity);
            savedReplay = persistenceModelMapper.toAnalyticsModel(savedEntity);
        } else {
            log.info("Skipping storage of trade replay {} based on sampling strategy", replayId);
            savedReplay = tradeReplay; // Use the unsaved entity for response
        }
        
        // Update sampling statistics
        tradeSamplingService.updateSamplingStatistics(tradeReplay, shouldStore);
        
        // Create and return the response using the mapper
        return tradeReplayMapper.toTradeReplayResponse(savedReplay);
    }

    @Override
    public Optional<TradeReplay> getTradeReplayById(String replayId) {
        log.info("Fetching trade replay with ID: {}", replayId);
        Optional<am.trade.persistence.entity.TradeReplay> entityOptional = tradeReplayRepository.findByReplayId(replayId);
        return entityOptional.map(persistenceModelMapper::toAnalyticsModel);
    }

    @Override
    public List<TradeReplay> findTradeReplaysBySymbol(String symbol) {
        log.info("Finding trade replays for symbol: {}", symbol);
        List<am.trade.persistence.entity.TradeReplay> entities = tradeReplayRepository.findBySymbol(symbol);
        return persistenceModelMapper.toAnalyticsModelList(entities);
    }

    @Override
    public List<TradeReplay> findTradeReplaysByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Finding trade replays between {} and {}", startDate, endDate);
        List<am.trade.persistence.entity.TradeReplay> entities = tradeReplayRepository.findByDateRange(startDate, endDate);
        return persistenceModelMapper.toAnalyticsModelList(entities);
    }

    @Override
    public List<TradeReplay> findTradeReplaysByPortfolioId(String portfolioId) {
        log.info("Finding trade replays for portfolio ID: {}", portfolioId);
        List<am.trade.persistence.entity.TradeReplay> entities = tradeReplayRepository.findByPortfolioId(portfolioId);
        return persistenceModelMapper.toAnalyticsModelList(entities);
    }

    @Override
    public List<TradeReplay> findTradeReplaysByStrategyId(String strategyId) {
        log.info("Finding trade replays for strategy ID: {}", strategyId);
        List<am.trade.persistence.entity.TradeReplay> entities = tradeReplayRepository.findByStrategyId(strategyId);
        return persistenceModelMapper.toAnalyticsModelList(entities);
    }

    @Override
    public List<TradeReplay> findTradeReplaysByOriginalTradeId(String tradeId) {
        log.info("Finding trade replays for original trade ID: {}", tradeId);
        List<am.trade.persistence.entity.TradeReplay> entities = tradeReplayRepository.findByOriginalTradeId(tradeId);
        return persistenceModelMapper.toAnalyticsModelList(entities);
    }

    @Override
    public boolean deleteTradeReplay(String replayId) {
        log.info("Deleting trade replay with ID: {}", replayId);
        return tradeReplayRepository.deleteByReplayId(replayId) > 0;
    }

    /**
     * Calculate profit/loss based on entry price, exit price, position size, and side
     */
    private BigDecimal calculateProfitLoss(BigDecimal entryPrice, BigDecimal exitPrice, 
                                          Integer positionSize, am.trade.models.enums.OrderSide side) {
        BigDecimal priceDifference = exitPrice.subtract(entryPrice);
        if (side == am.trade.models.enums.OrderSide.SELL) {
            priceDifference = priceDifference.negate();
        }
        return priceDifference.multiply(BigDecimal.valueOf(positionSize));
    }

    /**
     * Calculate profit/loss percentage based on entry price, exit price, and side
     */
    private BigDecimal calculateProfitLossPercentage(BigDecimal entryPrice, BigDecimal exitPrice, 
                                                   am.trade.models.enums.OrderSide side) {
        BigDecimal priceDifference = exitPrice.subtract(entryPrice);
        if (side == am.trade.models.enums.OrderSide.SELL) {
            priceDifference = priceDifference.negate();
        }
        return priceDifference.divide(entryPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * Calculate maximum drawdown and maximum profit during the holding period
     * Returns an array where index 0 is max drawdown and index 1 is max profit
     */
    private BigDecimal[] calculateMaxDrawdownAndProfit(List<PriceDataPoint> priceDataPoints, 
                                                     BigDecimal entryPrice, 
                                                     am.trade.models.enums.OrderSide side) {
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal maxProfit = BigDecimal.ZERO;
        
        for (PriceDataPoint dataPoint : priceDataPoints) {
            BigDecimal currentPrice = dataPoint.getClose();
            BigDecimal priceDifference = currentPrice.subtract(entryPrice);
            
            if (side == am.trade.models.enums.OrderSide.SELL) {
                priceDifference = priceDifference.negate();
            }
            
            if (priceDifference.compareTo(BigDecimal.ZERO) < 0) {
                // Negative difference means drawdown
                BigDecimal drawdown = priceDifference.abs();
                if (drawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = drawdown;
                }
            } else {
                // Positive difference means profit
                if (priceDifference.compareTo(maxProfit) > 0) {
                    maxProfit = priceDifference;
                }
            }
        }
        
        return new BigDecimal[] { maxDrawdown, maxProfit };
    }
}
