package am.trade.analytics.client.impl;

import am.trade.analytics.client.MarketDataClient;
import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import am.trade.analytics.model.historicaldata.HistoricalDataRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

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
        
        // Create a request object from the parameters
        HistoricalDataRequest request = HistoricalDataRequest.builder()
                .symbols(symbol)
                .fromDate(from.toLocalDate())
                .toDate(to.toLocalDate())
                .interval(interval)
                .continuous(continuous)
                .build();
        
        // Call the new method that uses POST
        return fetchHistoricalData(request);
    }
    
    @Override
    @Retryable(value = {RestClientException.class}, 
               maxAttempts = 3, 
               backoff = @Backoff(delay = 1000, multiplier = 2))
    public HistoricalMarketDataResponse fetchHistoricalData(HistoricalDataRequest request) {
        log.info("Fetching historical data with request: {}", request);
        
        String url = baseUrl + historicalDataPath;
        
        try {
            log.debug("Making POST request to: {}", url);
            ResponseEntity<HistoricalMarketDataResponse> response = restTemplate.postForEntity(
                    url, request, HistoricalMarketDataResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                HistoricalMarketDataResponse responseBody = response.getBody();
                if (responseBody != null) {
                    log.info("Successfully fetched historical data for {}, received {} data points", 
                            request.getSymbols(), responseBody.getCount());
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
