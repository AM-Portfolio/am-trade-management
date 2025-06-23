package am.trade.services.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import am.trade.models.mapper.TradeMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;

/**
 * Auto-configuration for trade services
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "am.trade.services", name = "enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "am.trade.services")
public class TradeServicesAutoConfiguration {
    
    /**
     * Configure trade mapper if not already defined
     */
    @Bean
    @ConditionalOnMissingBean
    public TradeMapper tradeMapper() {
        return new TradeMapper();
    }
    
    /**
     * Configure circuit breaker for trade service
     */
    @Bean
    @ConditionalOnMissingBean(name = "tradeServiceCircuitBreakerConfig")
    public CircuitBreakerConfig tradeServiceCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(java.time.Duration.ofMillis(1000))
                .permittedNumberOfCallsInHalfOpenState(2)
                .slidingWindowSize(10)
                .build();
    }
    
    /**
     * Configure retry for trade service
     */
    @Bean
    @ConditionalOnMissingBean(name = "tradeServiceRetryConfig")
    public RetryConfig tradeServiceRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(java.time.Duration.ofMillis(500))
                .retryExceptions(Exception.class)
                .build();
    }
}
