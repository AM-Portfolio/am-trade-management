package am.trade.sdk.client;

import am.trade.sdk.config.SdkConfiguration;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * Analytics API Client
 */
@Slf4j
public class AnalyticsApiClient extends BaseApiClient {

    public AnalyticsApiClient(SdkConfiguration config) {
        super(config);
    }

    public Map<String, Object> getTradeMetrics(String portfolioId) {
        log.debug("Getting trade metrics for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/metrics/" + portfolioId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getTradeSummary(String portfolioId, String period) {
        log.debug("Getting trade summary - portfolio: {}, period: {}", portfolioId, period);
        JsonObject response = get("/api/v1/trade-summary/" + portfolioId + "?period=" + period);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getAnalyticsData(String portfolioId) {
        log.debug("Getting analytics data for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/analytics/trade-replays/" + portfolioId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getTradeReplay(String tradeId) {
        log.debug("Getting trade replay: {}", tradeId);
        JsonObject response = get("/api/v1/analytics/trade-replays/" + tradeId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getPnlHeatmap(String portfolioId) {
        log.debug("Getting P&L heatmap for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/heatmap/" + portfolioId);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getPerformanceChart(String portfolioId, String period) {
        log.debug("Getting performance chart - portfolio: {}, period: {}", portfolioId, period);
        JsonObject response = get("/api/v1/analytics/" + portfolioId + "/performance?period=" + period);
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getRiskAnalysis(String portfolioId) {
        log.debug("Getting risk analysis for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/analytics/" + portfolioId + "/risk");
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> getCorrelationMatrix(String portfolioId) {
        log.debug("Getting correlation matrix for portfolio: {}", portfolioId);
        JsonObject response = get("/api/v1/analytics/" + portfolioId + "/correlation");
        return gson.fromJson(response, Map.class);
    }

    public Map<String, Object> comparePerformance(java.util.List<String> portfolioIds, String period) {
        log.debug("Comparing performance for {} portfolios", portfolioIds.size());
        Map<String, Object> request = new HashMap<>();
        request.put("portfolio_ids", portfolioIds);
        request.put("period", period);
        JsonObject response = post("/api/v1/analytics/compare", request);
        return gson.fromJson(response, Map.class);
    }
}
