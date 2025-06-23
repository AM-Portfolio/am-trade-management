package am.trade.kafka.service.metrics;

import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeModel;

import java.util.List;

/**
 * Service interface for processing trade executions into complete trades
 * and calculating trade metrics
 */
public interface TradeService {
    
    /**
     * Process a list of trade executions (buy/sell orders) into complete trades
     * A complete trade is formed when a position is squared off (buy followed by sell or vice versa)
     * 
     * @param trades List of trades to process
     * @return List of complete trades with WIN, LOSS, or OPEN status
     */
    List<TradeDetails> processTradeModels(List<TradeModel> trades, String portfolioId);
    
    /**
     * Get the current status of an open position
     * 
     * @param symbol The trading symbol
     * @param portfolioId The portfolio ID
     * @return Current position details or null if no open position
     */
    TradeDetails getCurrentPosition(String symbol, String portfolioId);

    /**
     * Process a list of trade executions and build a complete portfolio model
     * with aggregated metrics
     * 
     * @param trades List of trades to process
     * @return Complete portfolio model with trades and metrics
     */
    PortfolioModel processTradeModelsAndGetPortfolio(List<TradeModel> trades, String portfolioId);
}
