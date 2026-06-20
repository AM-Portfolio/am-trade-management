package am.trade.common.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration class for the common module
 * Ensures that all components in the common module are properly scanned
 */
@Configuration
@ComponentScan(basePackages = "am.trade.common")
public class CommonAutoConfiguration {
    // Configuration is handled through component scanning
}
