package am.trade.analytics.service.impl;

import am.trade.analytics.config.TradeSamplingConfig;
import am.trade.analytics.model.TradeReplay;
import am.trade.analytics.model.dto.TradeReplayRequest;
import am.trade.analytics.service.TradeSamplingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of TradeSamplingService that uses Redis to track user activity patterns
 * and applies sampling strategies based on configuration
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSamplingServiceImpl implements TradeSamplingService {

    private final TradeSamplingConfig samplingConfig;
    private final RedisTemplate<String, Object> redisTemplate;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String USER_TRADE_COUNT_KEY = "trade:count:%s:%s"; // user:date
    private static final String USER_SAMPLED_COUNT_KEY = "trade:sampled:%s:%s"; // user:date
    
    @Override
    public boolean shouldStoreTradeReplay(TradeReplayRequest request, BigDecimal profitLossPercentage, BigDecimal volatility) {
        // If sampling is disabled, always store
        if (!samplingConfig.isEnabled()) {
            return true;
        }
        
        String userId = request.getPortfolioId(); // Using portfolioId as a proxy for userId
        String dateKey = LocalDate.now().format(DATE_FORMATTER);
        String userTradeCountKey = String.format(USER_TRADE_COUNT_KEY, userId, dateKey);
        
        // Increment the user's trade count for today
        Long tradeCount = redisTemplate.opsForValue().increment(userTradeCountKey, 1);
        
        // Set expiry for the counter (7 days)
        if (tradeCount != null && tradeCount == 1) {
            redisTemplate.expire(userTradeCountKey, 7, TimeUnit.DAYS);
        }
        
        // Always store if under threshold
        if (tradeCount != null && tradeCount <= samplingConfig.getDailyTradeThreshold()) {
            return true;
        }
        
        // Check if this is a significant trade (by P&L)
        if (samplingConfig.isPreserveSignificantTrades() && 
            profitLossPercentage != null && 
            profitLossPercentage.abs().doubleValue() >= samplingConfig.getSignificantProfitLossThreshold()) {
            log.debug("Storing significant trade with P&L {}% for user {}", profitLossPercentage, userId);
            return true;
        }
        
        // Check if this is a high volatility trade
        if (samplingConfig.isPreserveHighVolatilityTrades() && 
            volatility != null && 
            volatility.doubleValue() >= samplingConfig.getHighVolatilityThreshold()) {
            log.debug("Storing high volatility trade with volatility {} for user {}", volatility, userId);
            return true;
        }
        
        // Apply sampling rate
        if (tradeCount != null) {
            boolean shouldStore = tradeCount % samplingConfig.getSamplingRate() == 0;
            log.debug("User {} has {} trades today, sampling rate is {}, shouldStore: {}", 
                    userId, tradeCount, samplingConfig.getSamplingRate(), shouldStore);
            return shouldStore;
        }
        
        // Default to storing if something went wrong with the counter
        return true;
    }
    
    @Override
    public void updateSamplingStatistics(TradeReplay tradeReplay, boolean wasStored) {
        if (!samplingConfig.isEnabled()) {
            return;
        }
        
        String userId = tradeReplay.getPortfolioId();
        String dateKey = tradeReplay.getCreatedDate().toLocalDate().format(DATE_FORMATTER);
        String userSampledCountKey = String.format(USER_SAMPLED_COUNT_KEY, userId, dateKey);
        
        if (wasStored) {
            // Increment the count of sampled trades
            Long sampledCount = redisTemplate.opsForValue().increment(userSampledCountKey, 1);
            
            // Set expiry for the counter (7 days)
            if (sampledCount != null && sampledCount == 1) {
                redisTemplate.expire(userSampledCountKey, 7, TimeUnit.DAYS);
            }
        }
    }
}
