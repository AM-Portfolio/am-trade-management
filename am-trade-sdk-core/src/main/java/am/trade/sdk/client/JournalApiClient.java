package am.trade.sdk.client;

import am.trade.sdk.config.SdkConfiguration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Journal API Client
 */
@Slf4j
public class JournalApiClient extends BaseApiClient {

    public JournalApiClient(SdkConfiguration config) {
        super(config);
    }

    public Map<String, Object> getJournalEntry(String entryId) {
        log.debug("Getting journal entry: {}", entryId);
        JsonObject response = get("/api/v1/journal/" + entryId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getAllEntries(String portfolioId, int page, int pageSize) {
        log.debug("Getting journal entries for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/journal/portfolio/" + portfolioId + 
                "?page=" + page + "&page_size=" + pageSize);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> createEntry(Map<String, Object> entryData) {
        log.debug("Creating journal entry");
        JsonObject response = post("/api/v1/journal", entryData);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> updateEntry(String entryId, Map<String, Object> entryData) {
        log.debug("Updating journal entry: {}", entryId);
        JsonObject response = put("/api/v1/journal/" + entryId, entryData);
        return gson.fromJson(response, Map.class);
    }

    public boolean deleteEntry(String entryId) {
        log.debug("Deleting journal entry: {}", entryId);
        delete("/api/v1/journal/" + entryId);
        return true;
    }

    public Map<String, Object> getEntriesByTag(String portfolioId, String tag, int page, int pageSize) {
        log.debug("Getting journal entries by tag: {}", tag);
        JsonObject response = get("/api/v1/journal/portfolio/" + portfolioId + "/tag/" + tag +
                "?page=" + page + "&page_size=" + pageSize);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getEntriesByTrade(String tradeId) {
        log.debug("Getting journal entries for trade: {}", tradeId);
        JsonObject response = get("/api/v1/journal/trade/" + tradeId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> searchEntries(String portfolioId, String query, int page, int pageSize) {
        log.debug("Searching journal entries with query: {}", query);
        JsonObject response = get("/api/v1/journal/portfolio/" + portfolioId + "/search" +
                "?q=" + query + "&page=" + page + "&page_size=" + pageSize);
        return gson.fromJson(response, Map.class);
    }

    public List<Map<String, Object>> bulkCreateEntries(List<Map<String, Object>> entries) {
        log.debug("Creating {} journal entries in batch", entries.size());
        Map<String, Object> request = new HashMap<>();
        request.put("entries", entries);
        JsonObject response = post("/api/v1/journal/bulk", request);
        
        List<Map<String, Object>> result = new ArrayList<>();
        if (response.has("data") && response.get("data").isJsonArray()) {
            JsonArray array = response.getAsJsonArray("data");
            for (int i = 0; i < array.size(); i++) {
                result.add(gson.fromJson(array.get(i), Map.class));
            }
        }
        return result;
    }

    public Map<String, Object> exportEntries(String portfolioId, String format) {
        log.debug("Exporting journal entries as {}", format);
        JsonObject response = get("/api/v1/journal/portfolio/" + portfolioId + "/export?format=" + format);
        return gson.fromJson(response, Map.class);
    }
}
