package am.trade.analytics.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Auto-configuration for Trade Analytics module
 * Enables component scanning and imports necessary configurations
 */
@Configuration
@ComponentScan(basePackages = {"am.trade.analytics"})
@Import({
    RedisConfig.class,
    TradeSamplingConfig.class
})
public class TradeAnalyticsAutoConfiguration {
    // Configuration is handled by annotations
}
