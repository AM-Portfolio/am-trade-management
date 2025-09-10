package am.trade.analytics.client.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Interceptor for logging REST API requests and responses
 */
@Component
public class RequestResponseLoggingInterceptor implements ClientHttpRequestInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestResponseLoggingInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // Log the request
        logRequest(request, body);
        
        // Generate and log curl command
        String curlCommand = generateCurlCommand(request, body);
        log.info("Curl command: {}", curlCommand);
        
        // Execute the request
        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long duration = System.currentTimeMillis() - startTime;
        
        // Log the response
        return logResponse(response, duration);
    }
    
    /**
     * Public method to log a request directly
     * 
     * @param method The HTTP method
     * @param url The URL
     * @param jsonBody The JSON request body
     */
    public void logRequest(HttpMethod method, String url, String jsonBody) {
        try {
            log.info("API Request: {} {} - Body: {}", method, url, jsonBody);
            
            // Generate and log curl command
            String curlCommand = generateCurlCommand(method, url, jsonBody);
            log.info("Curl command: {}", curlCommand);
        } catch (Exception e) {
            log.warn("Failed to log request: {}", e.getMessage());
        }
    }
    
    /**
     * Public method to log a response directly
     * 
     * @param response The response entity
     * @param startTime The start time of the request in milliseconds
     */
    public <T> void logResponse(ResponseEntity<T> response, long startTime) {
        try {
            long duration = System.currentTimeMillis() - startTime;
            log.info("API Response: Status: {} - Headers: {} - Body: {} - Duration: {}ms", 
                    response.getStatusCode(), response.getHeaders(), 
                    response.getBody(), duration);
        } catch (Exception e) {
            log.warn("Failed to log response: {}", e.getMessage());
        }
    }
    
    private void logRequest(HttpRequest request, byte[] body) {
        try {
            String requestBody = new String(body, StandardCharsets.UTF_8);
            log.info("API Request: {} {} - Headers: {} - Body: {}", 
                    request.getMethod(), request.getURI(), 
                    request.getHeaders(), requestBody);
        } catch (Exception e) {
            log.warn("Failed to log request: {}", e.getMessage());
        }
    }
    
    private ClientHttpResponse logResponse(ClientHttpResponse response, long duration) {
        try {
            // Copy the response body to a byte array to avoid consuming the stream
            byte[] responseBody = StreamUtils.copyToByteArray(response.getBody());
            String responseBodyStr = new String(responseBody, StandardCharsets.UTF_8);
            
            log.info("API Response: Status: {} - Headers: {} - Body: {} - Duration: {}ms", 
                    response.getStatusCode(), response.getHeaders(), 
                    responseBodyStr, duration);
            
            // Create a new response with the copied body
            return new BufferedClientHttpResponse(response, responseBody);
        } catch (Exception e) {
            log.warn("Failed to log response: {}", e.getMessage());
            return response;
        }
    }
    
    /**
     * Generates a curl command for the given HTTP method, URL and JSON body
     * 
     * @param method The HTTP method
     * @param url The URL
     * @param jsonBody The JSON request body
     * @return A curl command string
     */
    public String generateCurlCommand(HttpMethod method, String url, String jsonBody) {
        StringBuilder curlBuilder = new StringBuilder();
        curlBuilder.append("curl -v ");
        
        // Add HTTP method
        curlBuilder.append("-X ").append(method.name()).append(" ");
        
        // Add URL
        curlBuilder.append("'").append(url).append("' ");
        
        // Add common headers
        curlBuilder.append("-H 'Content-Type: application/json' ");
        curlBuilder.append("-H 'Accept: application/json' ");
        
        // Add request body for methods that support it
        if (jsonBody != null && !jsonBody.isEmpty() && 
            (method == HttpMethod.POST || method == HttpMethod.PUT || method == HttpMethod.PATCH)) {
            // Use single quotes for the data part to avoid escaping issues
            curlBuilder.append("-d '");
            curlBuilder.append(jsonBody);
            curlBuilder.append("'");
        }
        
        return curlBuilder.toString();
    }
    
    private String generateCurlCommand(HttpRequest request, byte[] body) {
        StringBuilder curlBuilder = new StringBuilder();
        curlBuilder.append("curl -v ");
        
        // Add HTTP method
        curlBuilder.append("-X ").append(request.getMethod()).append(" ");
        
        // Add URL
        curlBuilder.append("'").append(request.getURI()).append("' ");
        
        // Add headers
        request.getHeaders().forEach((name, values) -> {
            values.forEach(value -> {
                curlBuilder.append("-H '").append(name).append(": ")
                        .append(value).append("' ");
            });
        });
        
        // Add request body for methods that support it
        if (body != null && body.length > 0 && 
            (request.getMethod() == HttpMethod.POST || 
             request.getMethod() == HttpMethod.PUT || 
             request.getMethod() == HttpMethod.PATCH)) {
            // Use single quotes for the data part to avoid escaping issues
            curlBuilder.append("-d '");
            curlBuilder.append(new String(body, StandardCharsets.UTF_8));
            curlBuilder.append("'");
        }
        
        return curlBuilder.toString();
    }
}
