package am.trade.app.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central observability wiring:
 * 1. Enables @Observed annotation support via AOP
 * 2. MongoDB and Redis tracing are wired in their own config beans.
 * Relies on am-observability-lib zero-config for HTTP, @Service, and request-log filtering.
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }
}
