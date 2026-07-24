package am.trade.api.client;

import com.am.security.context.UserContext;

import am.trade.api.config.MarketDataApiConfig;
import am.trade.common.models.market.MarketDataResponse;
import am.trade.common.models.market.MarketDataResponseWrapper;
import am.trade.common.models.market.OhlcDataRequest;
import am.trade.exceptions.MarketDataUnavailableException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.RestClientException;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class MarketDataApiClient {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final MarketDataApiConfig config;
    private final RestTemplate restTemplate;

    @Value("${am.trade.market-data.l1-cache.enabled:true}")
    private boolean isL1CacheEnabled;

    private final com.github.benmanes.caffeine.cache.LoadingCache<String, Double> localCache;

    public MarketDataApiClient(MarketDataApiConfig config, org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder) {
        this.config = config;
        this.restTemplate = restTemplateBuilder
                .rootUri(config.getBaseUrl())
                .setConnectTimeout(java.time.Duration.ofSeconds(3))
                .setReadTimeout(java.time.Duration.ofSeconds(10))
                .defaultHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                .defaultHeader("Accept", "application/json")
                .defaultHeader("Content-Type", "application/json")
                .additionalInterceptors((request, body, execution) -> {
                    String token = UserContext.getToken();
                    if (token != null) {
                        if (token.startsWith("Bearer ")) {
                            request.getHeaders().set("Authorization", token);
                        } else {
                            request.getHeaders().setBearerAuth(token);
                        }
                    }
                    return execution.execute(request, body);
                })
                .build();
                
        this.localCache = com.github.benmanes.caffeine.cache.Caffeine.newBuilder()
                .expireAfterWrite(5, java.util.concurrent.TimeUnit.MINUTES)
                .maximumSize(10_000)
                .build(new com.github.benmanes.caffeine.cache.CacheLoader<String, Double>() {
                    @Override
                    public Double load(String key) throws Exception {
                        return fetchFromApi(java.util.Collections.singletonList(key)).get(key);
                    }

                    public Map<String, Double> loadAll(Iterable<? extends String> keys) throws Exception {
                        return fetchFromApi(keys);
                    }
                });
    }

    public Map<String, Double> getCurrentPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return new HashMap<>();
        }

        List<String> cleanSymbols = symbols.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(symbol -> {
                    // Remove provider prefix if present (e.g., "NSE:RELIANCE" -> "RELIANCE")
                    if (symbol.contains(":")) {
                        return symbol.substring(symbol.lastIndexOf(":") + 1);
                    }
                    return symbol;
                })
                .collect(java.util.stream.Collectors.toList());

        if (cleanSymbols.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Double> filteredResult = new HashMap<>();
        if (isL1CacheEnabled) {
            Map<String, Double> result = localCache.getAll(cleanSymbols);
            if (result != null) {
                // Filter out negative cache values (-1.0)
                result.forEach((k, v) -> {
                    if (v != null && v >= 0.0) {
                        filteredResult.put(k, v);
                    }
                });
            }
        } else {
            Map<String, Double> result = fetchFromApi(cleanSymbols);
            if (result != null) {
                result.forEach((k, v) -> {
                    if (v != null && v >= 0.0) {
                        filteredResult.put(k, v);
                    }
                });
            }
        }
        return filteredResult;
    }

    private Map<String, Double> fetchFromApi(Iterable<? extends String> missingSymbols) {
        Map<String, Double> currentPrices = new HashMap<>();
        String symbolsParam = String.join(",", missingSymbols);
        
        if (symbolsParam.isEmpty()) {
            return currentPrices;
        }

        OhlcDataRequest request = OhlcDataRequest.builder()
                .symbols(symbolsParam)
                .timeFrame("1D")
                .refresh(false)
                .indexSymbol(false)
                .build();

        log.debug("Fetching OHLC data for {} from {}", symbolsParam, config.getOhlcEndpoint());

        try {
            JsonNode responseNode = restTemplate.postForObject(
                    config.getOhlcEndpoint(),
                    request,
                    JsonNode.class);

            log.debug("Raw market data response for symbols {}: {}", symbolsParam, responseNode);

            if (responseNode != null) {
                JsonNode dataNode = responseNode.has("data") ? responseNode.get("data") : responseNode;
                
                Map<String, MarketDataResponse> responses = MAPPER.convertValue(
                        dataNode, 
                        new TypeReference<Map<String, MarketDataResponse>>() {}
                );

                if (responses != null) {
                    responses.forEach((key, response) -> {
                        String symbolKey = key.contains(":") ? key.substring(key.lastIndexOf(":") + 1) : key;
                        Double price = response.getLastPrice();
                        if (price != null) {
                            currentPrices.put(symbolKey, price);
                        } else {
                            log.warn("Received null price for symbol {}", symbolKey);
                        }
                    });
                }
            }
        } catch (RestClientException e) {
            log.error("Failed to fetch market data from API for symbols: {}", symbolsParam, e);
            // Do not throw MarketDataUnavailableException here; instead, let the negative caching below 
            // populate the failed symbols with -1.0 so Caffeine doesn't infinitely retry and cause cache penetration.
        }

        // NEGATIVE CACHING: Prevent Cache Penetration by caching missing/failed keys as -1.0
        // This ensures Caffeine doesn't retry them constantly if the upstream is down or missing data.
        for (String sym : missingSymbols) {
            currentPrices.putIfAbsent(sym, -1.0);
        }

        return currentPrices;
    }
}
