package am.trade.analytics.client.impl;

import am.trade.analytics.client.MarketDataClient;
import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * REST implementation of the MarketDataClient interface
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RestMarketDataClient implements MarketDataClient {

    private final RestTemplate restTemplate;
    
    @Value("${market-data.api.base-url:http://localhost:8084}")
    private String baseUrl;
    
    @Value("${market-data.api.historical-data-path:/api/v1/market-data/historical-data}")
    private String historicalDataPath;
    
    @Value("${market-data.api.retry.max-attempts:3}")
    private int maxRetryAttempts;
    
    @Value("${market-data.api.retry.initial-backoff-ms:1000}")
    private long initialBackoffMs;
    
    @Value("${market-data.api.retry.multiplier:2.0}")
    private double backoffMultiplier;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    @Retryable(value = {RestClientException.class}, maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public HistoricalMarketDataResponse fetchHistoricalData(
            String symbol, 
            LocalDateTime from, 
            LocalDateTime to, 
            String interval, 
            boolean continuous) {
        
        log.info("Fetching historical data for symbol: {}, from: {}, to: {}, interval: {}, continuous: {}", 
                symbol, from, to, interval, continuous);
        
        String url = UriComponentsBuilder.fromUriString(baseUrl + historicalDataPath)
                .queryParam("symbol", symbol)
                .queryParam("from", from.format(DATE_TIME_FORMATTER))
                .queryParam("to", to.format(DATE_TIME_FORMATTER))
                .queryParam("interval", interval)
                .queryParam("continuous", continuous)
                .build()
                .toUriString();
        
        try {
            log.debug("Making request to: {}", url);
            ResponseEntity<HistoricalMarketDataResponse> response = restTemplate.getForEntity(
                    url, HistoricalMarketDataResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                HistoricalMarketDataResponse responseBody = response.getBody();
                if (responseBody != null) {
                    log.info("Successfully fetched historical data for {}, received {} data points", 
                            symbol, responseBody.getCount());
                    return responseBody;
                } else {
                    log.error("Received empty response body");
                    throw new RuntimeException("Received empty response body");
                }
            } else {
                log.error("Failed to fetch historical data. Status code: {}", response.getStatusCode());
                throw new RuntimeException("Failed to fetch historical data. Status code: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error fetching historical data: {}", e.getMessage(), e);
            throw new RuntimeException("Error fetching historical data: " + e.getMessage(), e);
        }
    }
}
