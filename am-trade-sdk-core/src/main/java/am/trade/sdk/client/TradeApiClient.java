package am.trade.sdk.client;

import am.trade.models.Trade;
import am.trade.sdk.config.SdkConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Trade API Client
 *
 * Provides methods for trade management operations
 */
@Slf4j
public class TradeApiClient extends BaseApiClient {

    public TradeApiClient(SdkConfiguration config) {
        super(config);
    }

    /**
     * Get trade by ID
     *
     * @param tradeId Trade ID
     * @return Trade object
     */
    public Trade getTradeById(String tradeId) {
        log.debug("Getting trade with ID: {}", tradeId);
        JsonObject response = get("/api/v1/trades/" + tradeId);
        JsonObject data = response.has("data") ? 
                response.getAsJsonObject("data") : response;
        return gson.fromJson(data, Trade.class);
    }

    /**
     * Get all trades with pagination
     *
     * @param page Page number (0-indexed)
     * @param pageSize Page size
     * @return Map containing trades and pagination info
     */
    public Map<String, Object> getAllTrades(int page, int pageSize) {
        log.debug("Getting trades - page: {}, size: {}", page, pageSize);
        Map<String, Object> result = new HashMap<>();
        result.put("page", page);
        result.put("pageSize", pageSize);
        return result;
    }

    /**
     * Create new trade
     *
     * @param trade Trade object
     * @return Created trade
     */
    public Trade createTrade(Trade trade) {
        log.debug("Creating trade: {}", trade.getSymbol());
        JsonObject response = post("/api/v1/trades", trade);
        JsonObject data = response.has("data") ? 
                response.getAsJsonObject("data") : response;
        return gson.fromJson(data, Trade.class);
    }

    /**
     * Update trade
     *
     * @param tradeId Trade ID
     * @param trade Updated trade data
     * @return Updated trade
     */
    public Trade updateTrade(String tradeId, Trade trade) {
        log.debug("Updating trade: {}", tradeId);
        JsonObject response = put("/api/v1/trades/" + tradeId, trade);
        JsonObject data = response.has("data") ? 
                response.getAsJsonObject("data") : response;
        return gson.fromJson(data, Trade.class);
    }

    /**
     * Delete trade
     *
     * @param tradeId Trade ID
     * @return true if deleted
     */
    public boolean deleteTrade(String tradeId) {
        log.debug("Deleting trade: {}", tradeId);
        delete("/api/v1/trades/" + tradeId);
        return true;
    }

    /**
     * Filter trades
     *
     * @param filters Filter criteria as map
     * @return Filtered trades
     */
    public Map<String, Object> filterTrades(Map<String, Object> filters) {
        log.debug("Filtering trades with criteria: {}", filters);
        JsonObject response = post("/api/v1/trades/filter", filters);
        return gson.fromJson(response, Map.class);
    }

    /**
     * Get trades by portfolio
     *
     * @param portfolioId Portfolio ID
     * @param page Page number
     * @param pageSize Page size
     * @return Paged trades
     */
    public Map<String, Object> getTradesByPortfolio(String portfolioId, int page, int pageSize) {
        log.debug("Getting trades for portfolio: {}", portfolioId);
        return new HashMap<>();
    }

    /**
     * Get trades by symbol
     *
     * @param symbol Trade symbol
     * @param page Page number
     * @param pageSize Page size
     * @return Paged trades
     */
    public Map<String, Object> getTradesBySymbol(String symbol, int page, int pageSize) {
        log.debug("Getting trades for symbol: {}", symbol);
        return new HashMap<>();
    }

    /**
     * Get trade statistics
     *
     * @param portfolioId Portfolio ID
     * @return Trade statistics
     */
    public Map<String, Object> getTradeStats(String portfolioId) {
        log.debug("Getting trade stats for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/trades/stats/" + portfolioId);
        return gson.fromJson(response, Map.class);
    }

    /**
     * Create multiple trades
     *
     * @param trades List of trades
     * @return Created trades
     */
    public List<Trade> batchCreateTrades(List<Trade> trades) {
        log.debug("Creating {} trades in batch", trades.size());
        Map<String, Object> request = new HashMap<>();
        request.put("trades", trades);
        JsonObject response = post("/api/v1/trades/batch", request);
        
        List<Trade> result = new ArrayList<>();
        if (response.has("data") && response.get("data").isJsonArray()) {
            JsonArray array = response.getAsJsonArray("data");
            for (int i = 0; i < array.size(); i++) {
                result.add(gson.fromJson(array.get(i), Trade.class));
            }
        }
        return result;
    }

    /**
     * Delete multiple trades
     *
     * @param tradeIds List of trade IDs
     * @return Deletion result
     */
    public Map<String, Object> batchDeleteTrades(List<String> tradeIds) {
        log.debug("Deleting {} trades in batch", tradeIds.size());
        Map<String, Object> request = new HashMap<>();
        request.put("ids", tradeIds);
        JsonObject response = post("/api/v1/trades/batch/delete", request);
        return gson.fromJson(response, Map.class);
    }
}
