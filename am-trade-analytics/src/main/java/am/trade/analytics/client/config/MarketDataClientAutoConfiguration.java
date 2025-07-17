package am.trade.analytics.client.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Auto-configuration for the Market Data Client
 * Enables Spring Retry and imports necessary configurations
 */
@Configuration
@EnableRetry
@PropertySource("classpath:application-market-data-client.yml")
@Import(MarketDataClientConfig.class)
public class MarketDataClientAutoConfiguration {
    // Configuration is handled through annotations and imported classes
}
