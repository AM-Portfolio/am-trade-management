package am.trade.sdk.client;

import am.trade.sdk.config.SdkConfiguration;
import am.trade.sdk.exception.ApiException;
import am.trade.sdk.exception.NetworkException;
import am.trade.sdk.exception.TimeoutException;
import am.trade.sdk.http.HttpClientFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Base API client for all SDK clients
 *
 * Handles HTTP communication, authentication, error handling, and serialization
 */
@Slf4j
public abstract class BaseApiClient {

    protected final SdkConfiguration config;
    protected final OkHttpClient httpClient;
    protected final Gson gson;
    protected static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * Initialize base client
     *
     * @param config SDK configuration
     */
    protected BaseApiClient(SdkConfiguration config) {
        this.config = config;
        this.httpClient = HttpClientFactory.createHttpClient(config);
        this.gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                .setPrettyPrinting()
                .create();
    }

    /**
     * Make GET request
     *
     * @param endpoint API endpoint (without base URL)
     * @return Response as JsonObject
     * @throws ApiException if API returns error
     * @throws NetworkException if network error occurs
     * @throws TimeoutException if request times out
     */
    protected JsonObject get(String endpoint) {
        return request("GET", endpoint, null);
    }

    /**
     * Make POST request
     *
     * @param endpoint API endpoint
     * @param body Request body object
     * @return Response as JsonObject
     * @throws ApiException if API returns error
     * @throws NetworkException if network error occurs
     * @throws TimeoutException if request times out
     */
    protected JsonObject post(String endpoint, Object body) {
        String jsonBody = gson.toJson(body);
        return request("POST", endpoint, jsonBody);
    }

    /**
     * Make PUT request
     *
     * @param endpoint API endpoint
     * @param body Request body object
     * @return Response as JsonObject
     * @throws ApiException if API returns error
     * @throws NetworkException if network error occurs
     * @throws TimeoutException if request times out
     */
    protected JsonObject put(String endpoint, Object body) {
        String jsonBody = gson.toJson(body);
        return request("PUT", endpoint, jsonBody);
    }

    /**
     * Make DELETE request
     *
     * @param endpoint API endpoint
     * @return Response as JsonObject
     * @throws ApiException if API returns error
     * @throws NetworkException if network error occurs
     * @throws TimeoutException if request times out
     */
    protected JsonObject delete(String endpoint) {
        return request("DELETE", endpoint, null);
    }

    /**
     * Make HTTP request
     *
     * @param method HTTP method
     * @param endpoint API endpoint
     * @param body Request body (JSON string or null)
     * @return Response as JsonObject
     * @throws ApiException if API returns error
     * @throws NetworkException if network error occurs
     * @throws TimeoutException if request times out
     */
    private JsonObject request(String method, String endpoint, String body) {
        String url = config.getApiUrl() + endpoint;

        try {
            log.debug("Making {} request to {}", method, endpoint);

            Request.Builder requestBuilder = new Request.Builder()
                    .url(url);

            // Add authentication if API key is provided
            if (config.getApiKey() != null && !config.getApiKey().isEmpty()) {
                requestBuilder.header("Authorization", "Bearer " + config.getApiKey());
            }

            // Add headers
            requestBuilder.header("Content-Type", "application/json");
            requestBuilder.header("User-Agent", "am-trade-sdk/1.0.0");

            // Add body for POST, PUT
            if (body != null && !method.equals("GET") && !method.equals("DELETE")) {
                RequestBody requestBody = RequestBody.create(body, JSON);
                requestBuilder.method(method, requestBody);
            } else {
                requestBuilder.method(method, null);
            }

            Request request = requestBuilder.build();

            try (Response response = httpClient.newCall(request).execute()) {
                log.debug("Response status: {}", response.code());

                if (!response.isSuccessful()) {
                    handleErrorResponse(response);
                }

                String responseBody = response.body() != null ? response.body().string() : "{}";
                return gson.fromJson(responseBody, JsonObject.class);
            }

        } catch (ApiException e) {
            throw e;
        } catch (IOException e) {
            if (e.getMessage() != null && e.getMessage().contains("timeout")) {
                log.error("Request timeout: {}", e.getMessage());
                throw new TimeoutException(
                        "Request timed out after " + config.getTimeout() + "s",
                        config.getTimeout(),
                        e
                );
            } else {
                log.error("Network error: {}", e.getMessage());
                throw new NetworkException("Failed to connect to " + config.getApiUrl(), e);
            }
        } catch (Exception e) {
            log.error("Request error: {}", e.getMessage());
            throw new NetworkException("Request failed: " + e.getMessage(), e);
        }
    }

    /**
     * Handle error response
     *
     * @param response Error response
     * @throws ApiException Always
     */
    private void handleErrorResponse(Response response) {
        int status = response.code();
        String errorMessage;
        String errorCode = null;

        try {
            String body = response.body() != null ? response.body().string() : "";
            if (!body.isEmpty()) {
                JsonObject json = gson.fromJson(body, JsonObject.class);
                errorMessage = json.has("message") ? 
                        json.get("message").getAsString() : body;
                if (json.has("error_code")) {
                    errorCode = json.get("error_code").getAsString();
                }
            } else {
                errorMessage = "HTTP " + status;
            }
        } catch (Exception e) {
            errorMessage = response.message() != null ? 
                    response.message() : "HTTP " + status;
        }

        log.warn("API error: {} - {}", status, errorMessage);
        throw new ApiException(errorMessage, status, errorCode);
    }

    /**
     * Close client and cleanup resources
     */
    public void close() {
        if (httpClient != null) {
            httpClient.dispatcher().executorService().shutdown();
            log.debug("HTTP client closed");
        }
    }
}
