package am.trade.app.config;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.data.mongodb.observability.MongoObservationCommandListener;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.handler.DefaultTracingObservationHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;

/**
 * Central observability wiring:
 * 1. Enables @Observed annotation support via AOP
 * 2. Wires MongoDB command-level tracing
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

    @Bean
    @ConditionalOnMissingBean(name = "mongoTracingObservationRegistryCustomizer")
    public ObservationRegistryCustomizer<ObservationRegistry> mongoTracingObservationRegistryCustomizer(
            ObjectProvider<Tracer> tracerProvider) {
        return registry -> {
            Tracer tracer = tracerProvider.getIfAvailable();
            if (tracer != null) {
                registry.observationConfig().observationHandler(
                    new DefaultTracingObservationHandler(tracer)
                );
            }
        };
    }
}
