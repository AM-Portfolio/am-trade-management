package am.trade.api.client;

import com.am.security.context.UserContext;

import am.trade.api.config.MarketDataApiConfig;
import am.trade.common.models.market.MarketDataResponse;
import am.trade.common.models.market.MarketDataResponseWrapper;
import am.trade.common.models.market.OhlcDataRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
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
                    public Double load(String key) {
                        return fetchFromApi(java.util.Collections.singletonList(key)).get(key);
                    }

                    @Override
                    public Map<String, Double> loadAll(Iterable<? extends String> keys) {
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
                .collect(Collectors.toList());

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
            org.springframework.http.ResponseEntity<Map<String, Object>> responseEntity = restTemplate.exchange(
                    config.getOhlcEndpoint(),
                    org.springframework.http.HttpMethod.POST,
                    new org.springframework.http.HttpEntity<>(request),
                    new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> rawMap = responseEntity.getBody();
            log.debug("Raw market data response for symbols {}: {}", symbolsParam, rawMap);

            if (rawMap != null) {
                Object actualData = rawMap.containsKey("data") ? rawMap.get("data") : rawMap;
                if (actualData instanceof Map) {
                    Map<?, ?> dataToProcess = (Map<?, ?>) actualData;

                    for (Object key : dataToProcess.keySet()) {
                        try {
                            Object value = dataToProcess.get(key);
                            MarketDataResponse response = MAPPER.convertValue(value, MarketDataResponse.class);

                            String symbolKey = String.valueOf(key);
                            if (symbolKey.contains(":")) {
                                symbolKey = symbolKey.substring(symbolKey.lastIndexOf(":") + 1);
                            }
                            
                            Double price = response.getLastPrice();
                            if (price != null) {
                                currentPrices.put(symbolKey, price);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to parse market data response for key: {}", key, e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch market data from API for symbols: {}", symbolsParam, e);
        }

        // NEGATIVE CACHING: Prevent Cache Penetration by caching missing/failed keys as -1.0
        // This ensures Caffeine doesn't retry them constantly if the upstream is down or missing data.
        for (String sym : missingSymbols) {
            currentPrices.putIfAbsent(sym, -1.0);
        }

        return currentPrices;
    }
}
