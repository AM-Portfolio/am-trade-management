package am.trade.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

/**
 * Fallback Kafka configuration active when Kafka is disabled (e.g. in local development).
 * Provides dummy beans for KafkaTemplate and ConcurrentKafkaListenerContainerFactory to satisfy 
 * dependencies and prevent automatic connections to a Kafka broker.
 */
@Configuration
@ConditionalOnProperty(name = "am.trade.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LocalKafkaConfig {

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new DummyKafkaTemplate();
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(Collections.emptyMap()));
        // CRITICAL: Disable auto-startup so the listener containers do not start 
        // and try to connect to a non-existent Kafka broker on localhost.
        factory.setAutoStartup(false);
        return factory;
    }

    private static class DummyKafkaTemplate extends KafkaTemplate<String, Object> {
        @SuppressWarnings("unchecked")
        public DummyKafkaTemplate() {
            super(new DefaultKafkaProducerFactory<>(Collections.emptyMap()));
        }

        @Override
        public CompletableFuture<SendResult<String, Object>> send(ProducerRecord<String, Object> record) {
            // Return a dummy completed future to simulate successful publish
            return CompletableFuture.completedFuture(new SendResult<>(record, null));
        }
    }
}
