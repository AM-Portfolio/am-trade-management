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

    private final Cache<String, Double> localCache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    public MarketDataApiClient(MarketDataApiConfig config, RestTemplateBuilder restTemplateBuilder) {
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
    }

    public Map<String, Double> getCurrentPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, Double> result = new HashMap<>();
        List<String> missingSymbols = new java.util.ArrayList<>();
        java.time.Instant now = java.time.Instant.now();

        if (isL1CacheEnabled) {
            // 1. Check local cache
            for (String symbol : symbols) {
                String cleanSymbol = symbol;
                if (cleanSymbol != null && cleanSymbol.contains(":")) {
                    cleanSymbol = cleanSymbol.substring(cleanSymbol.lastIndexOf(":") + 1);
                }
                Double cachedPrice = localCache.getIfPresent(cleanSymbol);
                if (cachedPrice != null) {
                    result.put(cleanSymbol, cachedPrice);
                } else {
                    missingSymbols.add(symbol); // Request with original symbol format
                }
            }

            // 2. If all symbols are cached, return immediately
            if (missingSymbols.isEmpty()) {
                log.debug("All symbols retrieved from local cache.");
                return result;
            }
        } else {
            // Feature flag disabled, fetch everything
            missingSymbols.addAll(symbols);
        }

        // 3. Fetch missing symbols from upstream
        String symbolsParam = String.join(",", missingSymbols);
        OhlcDataRequest request = OhlcDataRequest.builder()
                .symbols(symbolsParam)
                .timeFrame("1D")
                .refresh(false) // Set to false to avoid hammering the upstream market data provider on every request
                .indexSymbol(false)
                .build();

        log.debug("Fetching OHLC data for {} from {}", symbolsParam, config.getOhlcEndpoint());

        try {
            Map rawMap = restTemplate.postForObject(
                    config.getOhlcEndpoint(),
                    request,
                    Map.class);

            log.info("Raw market data response for symbols {}: {}", symbolsParam, rawMap);

            Map<String, Double> currentPrices = new HashMap<>();
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
                            currentPrices.put(symbolKey, price);
                            
                            if (isL1CacheEnabled) {
                                // Update local cache
                                localCache.put(symbolKey, price);
                            }
                        } catch (Exception e) {
                            log.error("Error converting response for symbol {}", key, e);
                        }
                    }
                }
            }
            
            // Merge newly fetched prices into the result
            result.putAll(currentPrices);
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch market data from API for symbols: {}", symbolsParam, e);
            return result; // Return whatever we found in the cache
        }
    }
}
