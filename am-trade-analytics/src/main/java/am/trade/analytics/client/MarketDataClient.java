package am.trade.analytics.client;

import am.trade.analytics.client.model.HistoricalMarketDataResponse;

import java.time.LocalDateTime;

/**
 * Client interface for interacting with the market data API
 */
public interface MarketDataClient {
    
    /**
     * Fetch historical market data for a symbol between start and end dates
     * 
     * @param symbol The stock or index symbol
     * @param from The start date and time
     * @param to The end date and time
     * @param interval The data interval (e.g., "60minute", "1day")
     * @param continuous Whether to use continuous data
     * @return Historical market data response
     */
    HistoricalMarketDataResponse fetchHistoricalData(
            String symbol, 
            LocalDateTime from, 
            LocalDateTime to, 
            String interval, 
            boolean continuous);
}
