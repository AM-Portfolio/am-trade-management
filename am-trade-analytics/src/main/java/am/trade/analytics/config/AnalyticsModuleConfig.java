package am.trade.analytics.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Configuration class for the analytics module
 * Enables component scanning, MongoDB repositories, and retry capabilities
 */
@Configuration
@ComponentScan(basePackages = {"am.trade.analytics"})
@EnableMongoRepositories(basePackages = {"am.trade.analytics"})
@EnableRetry
@ConditionalOnProperty(name = "am.trade.analytics.enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsModuleConfig {

    /**
     * Configure any beans specific to the analytics module here
     */
}
