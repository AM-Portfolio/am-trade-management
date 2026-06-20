package am.trade.sdk.client;

import am.trade.sdk.config.SdkConfiguration;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Portfolio API Client
 */
@Slf4j
public class PortfolioApiClient extends BaseApiClient {

    public PortfolioApiClient(SdkConfiguration config) {
        super(config);
    }

    public Map<String, Object> getPortfolioById(String portfolioId) {
        log.debug("Getting portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/portfolio-summary/" + portfolioId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getAllPortfolios(int page, int pageSize) {
        log.debug("Getting all portfolios");
        return new HashMap<>();
    }

    public Map<String, Object> createPortfolio(Map<String, Object> portfolioData) {
        log.debug("Creating portfolio");
        JsonObject response = post("/api/v1/portfolio-summary", portfolioData);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> updatePortfolio(String portfolioId, Map<String, Object> portfolioData) {
        log.debug("Updating portfolio: {}", portfolioId);
        JsonObject response = put("/api/v1/portfolio-summary/" + portfolioId, portfolioData);
        return gson.fromJson(response, Map.class);
    }

    public boolean deletePortfolio(String portfolioId) {
        log.debug("Deleting portfolio: {}", portfolioId);
        delete("/api/v1/portfolio-summary/" + portfolioId);
        return true;
    }

    public Map<String, Object> getPortfolioSummary(String portfolioId) {
        log.debug("Getting portfolio summary: {}", portfolioId);
        JsonObject response = get("/api/v1/portfolio-summary/" + portfolioId + "/summary");
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getPortfolioPerformance(String portfolioId) {
        log.debug("Getting portfolio performance: {}", portfolioId);
        JsonObject response = get("/api/v1/portfolio-summary/" + portfolioId + "/performance");
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getPortfolioStats(String portfolioId) {
        log.debug("Getting portfolio stats: {}", portfolioId);
        JsonObject response = get("/api/v1/portfolio-summary/" + portfolioId + "/stats");
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> comparePortfolios(List<String> portfolioIds) {
        log.debug("Comparing portfolios");
        Map<String, Object> request = new HashMap<>();
        request.put("portfolio_ids", portfolioIds);
        JsonObject response = post("/api/v1/portfolio-summary/compare", request);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> exportPortfolio(String portfolioId, String format) {
        log.debug("Exporting portfolio: {} as {}", portfolioId, format);
        JsonObject response = get("/api/v1/portfolio-summary/" + portfolioId + "/export?format=" + format);
        return gson.fromJson(response, Map.class);
    }
}
