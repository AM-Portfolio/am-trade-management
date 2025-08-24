package am.trade.exceptions.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Auto-configuration for the exceptions module
 * Ensures that exception handlers and related components are properly registered
 */
@Configuration
@ComponentScan(basePackages = {"am.trade.exceptions.handler"})
public class ExceptionsAutoConfiguration {
    // Configuration class to enable component scanning for exception handlers
}
