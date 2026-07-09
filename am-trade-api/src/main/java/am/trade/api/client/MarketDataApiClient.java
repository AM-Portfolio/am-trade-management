package am.trade.api.client;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MarketDataApiClient {

    private final MarketDataApiConfig config;
    private final RestTemplate restTemplate;

    public MarketDataApiClient(MarketDataApiConfig config, RestTemplateBuilder restTemplateBuilder) {
        this.config = config;
        this.restTemplate = restTemplateBuilder.rootUri(config.getBaseUrl()).build();
    }

    public Map<String, Double> getCurrentPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return new HashMap<>();
        }

        String symbolsParam = String.join(",", symbols);
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
                    Map.class
            );
            
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
                            currentPrices.put(String.valueOf(key), response.getLastPrice());
                        } catch (Exception e) {
                            log.error("Error converting response for symbol {}", key, e);
                        }
                    }
                }
            }
            return currentPrices;
        } catch (Exception e) {
            log.error("Failed to fetch market data from API for symbols: {}", symbolsParam, e);
            return new HashMap<>();
        }
    }
}
