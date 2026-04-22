package am.trade.sdk.client;

import am.trade.sdk.config.SdkConfiguration;
import am.trade.sdk.dto.TradeDTO;
import am.trade.models.document.Trade;
import am.trade.sdk.dto.DTOTransformer;
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
     * Get trade by ID - Returns user-facing DTO
     *
     * @param tradeId Trade ID
     * @return TradeDTO.TradeResponse with safe fields only
     */
    public TradeDTO.TradeResponse getTradeById(String tradeId) {
        log.debug("Getting trade with ID: {}", tradeId);
        JsonObject response = get("/api/v1/trades/" + tradeId);
        JsonObject data = response.has("data") ? 
                response.getAsJsonObject("data") : response;
        
        // Transform to DTO - only safe fields exposed
        Map<String, Object> internalTrade = gson.fromJson(data, Map.class);
        return DTOTransformer.toTradeResponse(internalTrade);
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
     * Create new trade - Accepts user DTO
     *
     * @param request TradeDTO.TradeCreateRequest from user
     * @return TradeDTO.TradeResponse with safe fields only
     */
    public TradeDTO.TradeResponse createTrade(TradeDTO.TradeCreateRequest request) {
        log.debug("Creating trade: {}", request.getSymbol());
        
        // Transform user DTO to internal format
        Map<String, Object> payload = DTOTransformer.fromTradeCreateRequest(request);
        
        JsonObject response = post("/api/v1/trades", payload);
        JsonObject data = response.has("data") ? 
                response.getAsJsonObject("data") : response;
        
        // Transform back to user DTO
        Map<String, Object> internalTrade = gson.fromJson(data, Map.class);
        return DTOTransformer.toTradeResponse(internalTrade);
    }

    /**
     * Update trade - Accepts user DTO
     *
     * @param tradeId Trade ID
     * @param request TradeDTO.TradeUpdateRequest from user
     * @return TradeDTO.TradeResponse with safe fields only
     */
    public TradeDTO.TradeResponse updateTrade(String tradeId, TradeDTO.TradeUpdateRequest request) {
        log.debug("Updating trade: {}", tradeId);
        
        // Transform user DTO to internal format
        Map<String, Object> payload = DTOTransformer.fromTradeUpdateRequest(request);
        
        JsonObject response = put("/api/v1/trades/" + tradeId, payload);
        JsonObject data = response.has("data") ? 
                response.getAsJsonObject("data") : response;
        
        // Transform back to user DTO
        Map<String, Object> internalTrade = gson.fromJson(data, Map.class);
        return DTOTransformer.toTradeResponse(internalTrade);
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

    /**
     * Get trades available in FREE tier
     * 
     * Returns only trades with fields accessible to FREE tier users:
     * id, portfolio_id, symbol, trade_type, quantity, entry_price, entry_date, status, pnl, pnl_percentage
     *
     * @param page Page number (0-indexed)
     * @param pageSize Page size (max 20 for FREE tier)
     * @return Map containing FREE tier trades with pagination
     */
    public Map<String, Object> getTradesByFreeTab(int page, int pageSize) {
        log.info("Fetching trades for FREE tier tab - page: {}, size: {}", page, pageSize);
        
        // Enforce FREE tier limits
        int effectivePageSize = Math.min(pageSize, 20);
        if (pageSize > 20) {
            log.warn("Requested page size {} exceeds FREE tier limit of 20, using 20", pageSize);
        }
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("page", page);
            params.put("size", effectivePageSize);
            params.put("tier", "FREE");
            
            JsonObject response = get("/api/v1/trades/free-tab", params);
            Map<String, Object> result = gson.fromJson(response, Map.class);
            
            log.debug("Successfully fetched FREE tier trades - total results: {}", result.get("totalCount"));
            return result;
        } catch (Exception e) {
            log.error("Error fetching FREE tier trades", e);
            throw new RuntimeException("Failed to fetch FREE tier trades", e);
        }
    }

    /**
     * Get trades available in FREE tier with symbol filter
     * 
     * Filters trades by symbol while respecting FREE tier restrictions
     *
     * @param symbol Trade symbol
     * @param page Page number (0-indexed)
     * @param pageSize Page size (max 20 for FREE tier)
     * @return Map containing filtered FREE tier trades
     */
    public Map<String, Object> getTradesByFreeTabAndSymbol(String symbol, int page, int pageSize) {
        log.info("Fetching FREE tier trades for symbol: {} - page: {}, size: {}", symbol, page, pageSize);
        
        if (symbol == null || symbol.trim().isEmpty()) {
            log.warn("Symbol parameter is empty");
            throw new IllegalArgumentException("Symbol cannot be null or empty");
        }
        
        // Enforce FREE tier limits
        int effectivePageSize = Math.min(pageSize, 20);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("symbol", symbol);
            params.put("page", page);
            params.put("size", effectivePageSize);
            params.put("tier", "FREE");
            
            JsonObject response = get("/api/v1/trades/free-tab/symbol/" + symbol, params);
            Map<String, Object> result = gson.fromJson(response, Map.class);
            
            log.debug("Successfully fetched FREE tier trades for symbol {} - total results: {}", 
                     symbol, result.get("totalCount"));
            return result;
        } catch (Exception e) {
            log.error("Error fetching FREE tier trades for symbol: {}", symbol, e);
            throw new RuntimeException("Failed to fetch FREE tier trades for symbol: " + symbol, e);
        }
    }

    /**
     * Get trades available in FREE tier with status filter
     * 
     * Filters trades by status (WIN, LOSS, etc.) while respecting FREE tier restrictions
     *
     * @param status Trade status
     * @param page Page number (0-indexed)
     * @param pageSize Page size (max 20 for FREE tier)
     * @return Map containing filtered FREE tier trades
     */
    public Map<String, Object> getTradesByFreeTabAndStatus(String status, int page, int pageSize) {
        log.info("Fetching FREE tier trades with status: {} - page: {}, size: {}", status, page, pageSize);
        
        if (status == null || status.trim().isEmpty()) {
            log.warn("Status parameter is empty");
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        // Enforce FREE tier limits
        int effectivePageSize = Math.min(pageSize, 20);
        
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("status", status);
            params.put("page", page);
            params.put("size", effectivePageSize);
            params.put("tier", "FREE");
            
            JsonObject response = get("/api/v1/trades/free-tab/status/" + status, params);
            Map<String, Object> result = gson.fromJson(response, Map.class);
            
            log.debug("Successfully fetched FREE tier trades with status {} - total results: {}", 
                     status, result.get("totalCount"));
            return result;
        } catch (Exception e) {
            log.error("Error fetching FREE tier trades with status: {}", status, e);
            throw new RuntimeException("Failed to fetch FREE tier trades with status: " + status, e);
        }
    }
}
