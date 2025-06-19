package am.trade.api.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import am.trade.dashboard.config.DashboardAutoConfiguration;
import am.trade.services.config.TradeServicesAutoConfiguration;

/**
 * Auto-configuration for API module
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "am.trade.api", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "am.trade.api")
@Import({TradeServicesAutoConfiguration.class, DashboardAutoConfiguration.class})
public class ApiAutoConfiguration {
    // Configuration beans will be defined here if needed
}
