package am.trade.dashboard.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import am.trade.services.config.TradeServicesAutoConfiguration;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Auto-configuration for dashboard metrics module
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "am.trade.dashboard", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "am.trade.dashboard")
@Import(TradeServicesAutoConfiguration.class)
public class DashboardAutoConfiguration {
    
    /**
     * Configure circuit breaker for dashboard metrics
     */
    @Bean
    @ConditionalOnMissingBean(name = "dashboardMetricsCircuitBreakerConfig")
    public CircuitBreakerConfig dashboardMetricsCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(java.time.Duration.ofMillis(1000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(10)
                .build();
    }
    
    /**
     * Configure retry for dashboard metrics
     */
    @Bean
    @ConditionalOnMissingBean(name = "dashboardMetricsRetryConfig")
    public RetryConfig dashboardMetricsRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(java.time.Duration.ofMillis(500))
                .retryExceptions(Exception.class)
                .build();
    }
}
