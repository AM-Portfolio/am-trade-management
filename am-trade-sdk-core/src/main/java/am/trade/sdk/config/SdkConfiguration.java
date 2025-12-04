package am.trade.sdk.config;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * SDK Configuration for AM Trade Management SDK.
 *
 * Supports builder pattern for fluent configuration.
 *
 * Example:
 * <pre>
 * {@code
 * SdkConfiguration config = SdkConfiguration.builder()
 *     .apiUrl("http://localhost:8073")
 *     .apiKey("your-api-key")
 *     .timeout(30)
 *     .maxRetries(3)
 *     .build();
 * }
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class SdkConfiguration {

    /**
     * API base URL
     */
    @Builder.Default
    private String apiUrl = "http://localhost:8073";

    /**
     * API authentication key (optional)
     */
    private String apiKey;

    /**
     * Request timeout in seconds
     */
    @Builder.Default
    private int timeout = 30;

    /**
     * Maximum retry attempts
     */
    @Builder.Default
    private int maxRetries = 3;

    /**
     * Connection timeout in seconds
     */
    @Builder.Default
    private int connectionTimeout = 10;

    /**
     * Enable SSL verification
     */
    @Builder.Default
    private boolean verifySsl = true;

    /**
     * Enable request/response logging
     */
    @Builder.Default
    private boolean enableLogging = true;

    /**
     * Validate configuration
     *
     * @throws IllegalArgumentException if configuration is invalid
     */
    public void validate() {
        if (apiUrl == null || apiUrl.isEmpty()) {
            throw new IllegalArgumentException("apiUrl cannot be empty");
        }
        if (!apiUrl.startsWith("http://") && !apiUrl.startsWith("https://")) {
            throw new IllegalArgumentException("apiUrl must start with http:// or https://");
        }
        if (timeout <= 0 || timeout > 300) {
            throw new IllegalArgumentException("timeout must be between 1 and 300 seconds");
        }
        if (connectionTimeout <= 0 || connectionTimeout > 60) {
            throw new IllegalArgumentException("connectionTimeout must be between 1 and 60 seconds");
        }
    }

    /**
     * Get API URL with trailing slash removed
     *
     * @return API URL
     */
    public String getApiUrl() {
        return apiUrl != null ? apiUrl.replaceAll("/$", "") : "http://localhost:8073";
    }

    /**
     * Create a new builder instance
     *
     * @return Builder
     */
    public static SdkConfigurationBuilder defaultBuilder() {
        return builder();
    }

    @Override
    public String toString() {
        return "SdkConfiguration(" +
                "apiUrl='" + apiUrl + '\'' +
                ", apiKey=" + (apiKey != null ? "***" : null) +
                ", timeout=" + timeout +
                ", maxRetries=" + maxRetries +
                ", connectionTimeout=" + connectionTimeout +
                ", verifySsl=" + verifySsl +
                ", enableLogging=" + enableLogging +
                ')';
    }
}
