package am.trade.app.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;

/**
 * Central observability wiring:
 * 1. Enables @Observed annotation support via AOP
 * 2. Wires MongoDB command-level tracing for Spring Boot 3.2.x
 */
@Configuration
public class ObservabilityConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoObservabilityCustomizer(
            ObservationRegistry observationRegistry) {
        return builder -> builder.addCommandListener(
            new MongoObservationCommandListener(observationRegistry)
        );
    }
}
