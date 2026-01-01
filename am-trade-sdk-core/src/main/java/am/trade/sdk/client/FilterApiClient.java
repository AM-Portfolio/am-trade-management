package am.trade.sdk.client;

import am.trade.sdk.config.SdkConfiguration;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Filter API Client
 */
@Slf4j
public class FilterApiClient extends BaseApiClient {

    public FilterApiClient(SdkConfiguration config) {
        super(config);
    }

    public Map<String, Object> getFilterById(String filterId) {
        log.debug("Getting filter: {}", filterId);
        JsonObject response = get("/api/v1/filters/" + filterId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getAllFilters(String portfolioId, int page, int pageSize) {
        log.debug("Getting filters for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/filters/portfolio/" + portfolioId + 
                "?page=" + page + "&page_size=" + pageSize);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> createFilter(Map<String, Object> filterData) {
        log.debug("Creating filter");
        JsonObject response = post("/api/v1/filters", filterData);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> updateFilter(String filterId, Map<String, Object> filterData) {
        log.debug("Updating filter: {}", filterId);
        JsonObject response = put("/api/v1/filters/" + filterId, filterData);
        return gson.fromJson(response, Map.class);
    }

    public boolean deleteFilter(String filterId) {
        log.debug("Deleting filter: {}", filterId);
        delete("/api/v1/filters/" + filterId);
        return true;
    }

    public Map<String, Object> getSharedFilters(int page, int pageSize) {
        log.debug("Getting shared filters");
        JsonObject response = get("/api/v1/filters/shared?page=" + page + "&page_size=" + pageSize);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> shareFilter(String filterId) {
        log.debug("Sharing filter: {}", filterId);
        JsonObject response = put("/api/v1/filters/" + filterId + "/share", new HashMap<>());
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> unshareFilter(String filterId) {
        log.debug("Unsharing filter: {}", filterId);
        JsonObject response = put("/api/v1/filters/" + filterId + "/unshare", new HashMap<>());
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> duplicateFilter(String filterId, String newName) {
        log.debug("Duplicating filter: {}", filterId);
        Map<String, Object> request = new HashMap<>();
        request.put("name", newName);
        JsonObject response = post("/api/v1/filters/" + filterId + "/duplicate", request);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> bulkDeleteFilters(List<String> filterIds) {
        log.debug("Deleting {} filters in batch", filterIds.size());
        Map<String, Object> request = new HashMap<>();
        request.put("ids", filterIds);
        JsonObject response = post("/api/v1/filters/bulk/delete", request);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> exportFilter(String filterId, String format) {
        log.debug("Exporting filter: {} as {}", filterId, format);
        JsonObject response = get("/api/v1/filters/" + filterId + "/export?format=" + format);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> importFilter(Map<String, Object> importData) {
        log.debug("Importing filter");
        JsonObject response = post("/api/v1/filters/import", importData);
        return gson.fromJson(response, Map.class);
    }
}
