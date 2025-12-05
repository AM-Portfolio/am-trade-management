package am.trade.sdk;

import am.trade.sdk.client.AnalyticsApiClient;
import am.trade.sdk.client.FilterApiClient;
import am.trade.sdk.client.JournalApiClient;
import am.trade.sdk.client.PortfolioApiClient;
import am.trade.sdk.client.TradeApiClient;
import am.trade.sdk.config.SdkConfiguration;
import lombok.extern.slf4j.Slf4j;

/**
 * Main entry point for AM Trade Management SDK.
 * 
 * Provides unified access to all trade management APIs including:
 * - Trade CRUD operations
 * - Portfolio analysis
 * - Trade analytics
 * - Journal management
 * - Filter operations
 * 
 * Usage:
 * <pre>
 * {@code
 * AmTradeSdk sdk = AmTradeSdk.builder()
 *     .apiUrl("http://localhost:8073")
 *     .apiKey("your-api-key")
 *     .timeout(30)
 *     .build();
 * 
 * TradeApiClient tradeClient = sdk.getTradeClient();
 * Trade trade = tradeClient.getTradeById("trade-123");
 * }
 * </pre>
 */
@Slf4j
public class AmTradeSdk {

    private final SdkConfiguration configuration;
    private final TradeApiClient tradeClient;
    private final PortfolioApiClient portfolioClient;
    private final AnalyticsApiClient analyticsClient;
    private final JournalApiClient journalClient;
    private final FilterApiClient filterClient;

    /**
     * Initialize SDK with configuration
     */
    private AmTradeSdk(SdkConfiguration configuration) {
        this.configuration = configuration;
        configuration.validate();
        log.info("Initializing AM Trade SDK v{}", this.getVersion());
        
        // Initialize API clients
        this.tradeClient = new TradeApiClient(configuration);
        this.portfolioClient = new PortfolioApiClient(configuration);
        this.analyticsClient = new AnalyticsApiClient(configuration);
        this.journalClient = new JournalApiClient(configuration);
        this.filterClient = new FilterApiClient(configuration);
        
        log.info("AM Trade SDK initialized successfully with configuration: {}", configuration);
    }

    /**
     * Get Trade API client for trade operations
     */
    public TradeApiClient getTradeClient() {
        return tradeClient;
    }

    /**
     * Get Portfolio API client for portfolio operations
     */
    public PortfolioApiClient getPortfolioClient() {
        return portfolioClient;
    }

    /**
     * Get Analytics API client for analytics operations
     */
    public AnalyticsApiClient getAnalyticsClient() {
        return analyticsClient;
    }

    /**
     * Get Journal API client for journal operations
     */
    public JournalApiClient getJournalClient() {
        return journalClient;
    }

    /**
     * Get Filter API client for filter operations
     */
    public FilterApiClient getFilterClient() {
        return filterClient;
    }

    /**
     * Get SDK configuration
     */
    public SdkConfiguration getConfiguration() {
        return configuration;
    }

    /**
     * Get SDK version
     */
    public String getVersion() {
        return "1.0.0";
    }

    /**
     * Shutdown SDK and cleanup resources
     */
    public void shutdown() {
        log.info("Shutting down AM Trade SDK");
        tradeClient.close();
        portfolioClient.close();
        analyticsClient.close();
        journalClient.close();
        filterClient.close();
    }

    /**
     * Builder for creating AmTradeSdk instances
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder class for fluent SDK configuration
     */
    public static class Builder {
        private String apiUrl = "https://api.munish.org";
        private String apiKey;
        private int timeout = 30;
        private int maxRetries = 3;
        private int connectionTimeout = 10;
        private boolean enableLogging = true;

        public Builder apiUrl(String apiUrl) {
            this.apiUrl = apiUrl;
            return this;
        }

        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        public Builder timeout(int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
            return this;
        }

        public Builder connectionTimeout(int connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
            return this;
        }

        public Builder enableLogging(boolean enableLogging) {
            this.enableLogging = enableLogging;
            return this;
        }

        public AmTradeSdk build() {
            SdkConfiguration config = SdkConfiguration.builder()
                    .apiUrl(apiUrl)
                    .apiKey(apiKey)
                    .timeout(timeout)
                    .maxRetries(maxRetries)
                    .connectionTimeout(connectionTimeout)
                    .enableLogging(enableLogging)
                    .build();
            
            return new AmTradeSdk(config);
        }
    }
}
