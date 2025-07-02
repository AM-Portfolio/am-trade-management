package am.trade.services.service;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeModel;

import java.util.List;

/**
 * Service interface for processing trade executions into complete trades
 * and calculating trade metrics
 */
public interface TradeProcessingService {
    
    /**
     * Process a list of trade executions (buy/sell orders) into complete trades
     * A complete trade is formed when a position is squared off (buy followed by sell or vice versa)
     * 
     * @param trades List of trades to process
     * @param portfolioId The portfolio ID
     * @return List of complete trades with WIN, LOSS, or OPEN status
     */
    List<TradeDetails> processTradeModels(List<TradeModel> trades, String portfolioId);

    /**
     * Process a list of trade executions (buy/sell orders) into complete trades
     * A complete trade is formed when a position is squared off (buy followed by sell or vice versa)
     * 
     * @param trades List of trades to process
     * @param portfolioId The portfolio ID
     * @return List of complete trades with WIN, LOSS, or OPEN status
     */
    void processTradeDetails(List<String> tradeIds, String portfolioId);
    
    /**
     * Get the current status of an open position
     * 
     * @param symbol The trading symbol
     * @param portfolioId The portfolio ID
     * @return Current position details or null if no open position
     */
    TradeDetails getCurrentPosition(String symbol, String portfolioId);
}
