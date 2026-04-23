package am.trade.analytics.client.impl;

import am.trade.analytics.client.MarketDataClient;
import am.trade.analytics.client.interceptor.RequestResponseLoggingInterceptor;
import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import am.trade.analytics.model.historicaldata.HistoricalDataRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RequestResponseLoggingInterceptor loggingInterceptor;
    
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
    public HistoricalMarketDataResponse fetchHistoricalData(HistoricalDataRequest request) {
        String url = baseUrl + historicalDataPath;
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Convert request to JSON for logging
            String jsonRequest = objectMapper.writeValueAsString(request);
            
            // Use the logging interceptor directly to log the request and generate curl command
            loggingInterceptor.logRequest(HttpMethod.POST, url, jsonRequest);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize request to JSON: {}", e.getMessage());
            log.info("Fetching historical data with request: {}", request);
        }
        
        try {
            log.debug("Making POST request to: {}", url);
            ResponseEntity<HistoricalMarketDataResponse> response = restTemplate.postForEntity(
                    url, request, HistoricalMarketDataResponse.class);
            
            if (response.getStatusCode().is2xxSuccessful()) {
                HistoricalMarketDataResponse responseBody = response.getBody();
                if (responseBody != null) {
                    // Use the logging interceptor to log the response
                    loggingInterceptor.logResponse(response, startTime);
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
