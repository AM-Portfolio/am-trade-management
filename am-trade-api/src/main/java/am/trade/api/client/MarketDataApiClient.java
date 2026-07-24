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

    private final MarketDataApiConfig config;
    private final RestTemplate restTemplate;

    @Value("${am.trade.market-data.l1-cache.enabled:true}")
    private boolean isL1CacheEnabled;

    private final com.github.benmanes.caffeine.cache.LoadingCache<String, Double> localCache;

    public MarketDataApiClient(MarketDataApiConfig config, org.springframework.boot.web.client.RestTemplateBuilder restTemplateBuilder) {
        this.config = config;
        this.restTemplate = restTemplateBuilder
                .rootUri(config.getBaseUrl())
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

        // Clean symbols (remove exchange prefix if present) and filter out nulls
        List<String> cleanSymbols = symbols.stream()
                .filter(java.util.Objects::nonNull)
                .map(symbol -> {
                    if (symbol.contains(":")) {
                        return symbol.substring(symbol.lastIndexOf(":") + 1);
                    }
                    return symbol;
                })
                .collect(java.util.stream.Collectors.toList());

        if (cleanSymbols.isEmpty()) {
            return new HashMap<>();
        }

        if (isL1CacheEnabled) {
            // Caffeine LoadingCache automatically coalesces concurrent bulk requests (Cache Stampede protection).
            // Threads requesting the exact same missing keys will block on the single thread that is fetching them,
            // instead of all trying to fetch them concurrently.
            Map<String, Double> result = localCache.getAll(cleanSymbols);
            return result != null ? result : new HashMap<>();
        } else {
            return fetchFromApi(cleanSymbols);
        }
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
            Map rawMap = restTemplate.postForObject(
                    config.getOhlcEndpoint(),
                    request,
                    Map.class);

            log.info("Raw market data response for symbols {}: {}", symbolsParam, rawMap);

            if (rawMap != null) {
                Object actualData = rawMap.containsKey("data") ? rawMap.get("data") : rawMap;
                if (actualData instanceof Map) {
                    Map<?, ?> dataToProcess = (Map<?, ?>) actualData;
                    ObjectMapper mapper = new ObjectMapper();
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

                    for (Object key : dataToProcess.keySet()) {
                        try {
                            Object value = dataToProcess.get(key);
                            MarketDataResponse response = mapper.convertValue(value, MarketDataResponse.class);
                            String symbolKey = String.valueOf(key);
                            if (symbolKey.contains(":")) {
                                symbolKey = symbolKey.substring(symbolKey.lastIndexOf(":") + 1);
                            }
                            
                            Double price = response.getLastPrice();
                            if (price != null) {
                                currentPrices.put(symbolKey, price);
                            } else {
                                log.warn("Received null price for symbol {}", symbolKey);
                            }
                            
                        } catch (Exception e) {
                            log.error("Error converting response for symbol {}", key, e);
                        }
                    }
                }
            }
            return currentPrices;
        } catch (Exception e) {
            log.error("Failed to fetch market data from API for symbols: {}", symbolsParam, e);
            return currentPrices;
        }
    }
}
