package am.trade.analytics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

/**
 * Configuration properties for trade replay sampling strategy
 */
@Configuration
@ConfigurationProperties(prefix = "am.trade.analytics.sampling")
@Data
public class TradeSamplingConfig {
    
    /**
     * Whether sampling is enabled
     */
    private boolean enabled = true;
    
    /**
     * Threshold for number of trades per user per day before sampling kicks in
     */
    private int dailyTradeThreshold = 10;
    
    /**
     * Sampling rate for trades exceeding the threshold (1 = store every trade, 2 = store every other trade)
     */
    private int samplingRate = 2;
    
    /**
     * Whether to always store trades with significant P&L (outliers)
     */
    private boolean preserveSignificantTrades = true;
    
    /**
     * P&L percentage threshold to consider a trade significant (e.g., 5.0 = 5%)
     */
    private double significantProfitLossThreshold = 5.0;
    
    /**
     * Whether to always store trades with high volatility
     */
    private boolean preserveHighVolatilityTrades = true;
    
    /**
     * Volatility threshold to consider a trade high volatility
     */
    private double highVolatilityThreshold = 2.0;
}
