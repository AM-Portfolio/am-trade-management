package am.trade.analytics.service;

import am.trade.analytics.model.TradeReplay;
import am.trade.analytics.model.dto.TradeReplayRequest;

/**
 * Service interface for determining whether a trade replay should be sampled (stored)
 * based on configured sampling strategies
 */
public interface TradeSamplingService {
    
    /**
     * Determines whether a trade replay should be stored based on sampling strategy
     * 
     * @param request The trade replay request
     * @param profitLossPercentage The calculated profit/loss percentage
     * @param volatility The calculated volatility
     * @return true if the trade should be stored, false if it should be skipped
     */
    boolean shouldStoreTradeReplay(TradeReplayRequest request, 
                                  java.math.BigDecimal profitLossPercentage, 
                                  java.math.BigDecimal volatility);
    
    /**
     * Updates the sampling statistics after a trade replay is processed
     * 
     * @param tradeReplay The trade replay that was processed
     * @param wasStored Whether the trade was stored or skipped
     */
    void updateSamplingStatistics(TradeReplay tradeReplay, boolean wasStored);
}
