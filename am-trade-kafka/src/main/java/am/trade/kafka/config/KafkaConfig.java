package am.trade.kafka.config;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.core.*;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:am-trade-group}")
    private String groupId;

    @Autowired
    private org.springframework.boot.autoconfigure.kafka.KafkaProperties kafkaProperties;

    private Map<String, Object> getSecurityProperties() {
        Map<String, Object> props = new HashMap<>();
        Map<String, String> properties = kafkaProperties.getProperties();
        String protocol = properties.get("security.protocol");
        
        if (protocol != null && (protocol.equals("SASL_SSL") || protocol.equals("SASL_PLAINTEXT"))) {
            props.put("security.protocol", protocol);
            props.put("sasl.mechanism", properties.get("sasl.mechanism"));
            props.put("sasl.jaas.config", properties.get("sasl.jaas.config"));
            log.debug("✅ DEBUG KAFKA ENV: System.getenv(KAFKA_SECURITY_PROTOCOL)={}", System.getenv("KAFKA_SECURITY_PROTOCOL"));
            log.debug("✅ DEBUG KAFKA PROP: properties.get(security.protocol)={}", protocol);
            log.debug("✅ DEBUG KAFKA ENV: System.getenv(KAFKA_SASL_MECHANISM)={}", System.getenv("KAFKA_SASL_MECHANISM"));
            log.debug("✅ DEBUG KAFKA: Successfully injected SASL configuration: {}", protocol);
        } else {
            log.debug("⚠️ DEBUG KAFKA: No SASL configuration found, defaulting to PLAINTEXT. Protocol was: {}", protocol);
        }
        return props;
    }

    @Bean(name = "kafkaAdmin")
    @Primary
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configs.putAll(getSecurityProperties());
        return new KafkaAdmin(configs);
    }

    @Bean(name = "kafkaConsumerFactory")
    @Primary
    public ConsumerFactory<String, Object> kafkaConsumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.putAll(getSecurityProperties());
        return new DefaultKafkaConsumerFactory<>(props);
    }

    /**
     * Dead Letter Publishing Recoverer — the "penalty box" for unprocessable messages.
     *
     * <p>When the {@link DefaultErrorHandler} gives up on a message (after exhausting
     * all retries), it calls this recoverer. The recoverer publishes the raw, failed
     * message to a new Kafka topic named: {@code [original-topic].DLT}
     *
     * <p>Example: a bad message on {@code am-trade-update} → goes to {@code am-trade-update.DLT}
     *
     * <p>This keeps the main consumer moving without losing the bad message — an engineer
     * can inspect the DLT topic later, fix the bug, and replay the messages.
     *
     * @param kafkaTemplate the producer template used to publish to the DLT topic
     */
    @Bean
    public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(KafkaTemplate<String, Object> kafkaTemplate) {
        return new DeadLetterPublishingRecoverer(kafkaTemplate);
    }

    /**
     * Error handler that retries failed messages before sending them to the DLT.
     *
     * <p><b>Retry policy (FixedBackOff):</b>
     * <ul>
     *   <li>3 retry attempts, each 1 second apart</li>
     *   <li>After all retries are exhausted, the recoverer publishes to the DLT</li>
     *   <li>The offset is then committed so the consumer can continue</li>
     * </ul>
     *
     * <p><b>Why 3 retries?</b> Transient errors (DB blip, network timeout) often resolve
     * within a few seconds. 3 retries with 1s gaps catches most of these without
     * hammering the system. Permanent errors (bad JSON, missing field) fail all 3
     * retries and correctly land in the DLT.
     *
     * @param recoverer the DLT publisher to call after retries are exhausted
     */
    @Bean
    public DefaultErrorHandler defaultErrorHandler(DeadLetterPublishingRecoverer recoverer) {
        // FixedBackOff(intervalMs, maxAttempts): retry up to 3 times, waiting 1 second between each
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean(name = "kafkaListenerContainerFactory")
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            DefaultErrorHandler errorHandler) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(kafkaConsumerFactory());
        // MANUAL_IMMEDIATE: offset is committed only after acknowledgment.acknowledge() is called.
        // Without this, acknowledgment() calls are no-ops and failed messages are permanently lost.
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        // Wire in the error handler — handles retries and DLT publishing on failure
        factory.setCommonErrorHandler(errorHandler);
        return factory;
    }

    @Bean(name = "kafkaProducerFactory")
    @Primary
    public ProducerFactory<String, Object> kafkaProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.putAll(getSecurityProperties());
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean(name = "kafkaTemplate")
    @Primary
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(kafkaProducerFactory());
    }

    // Topic Definitions
    @Value("${am.trade.kafka.holding-update.topic:am-portfolio}")
    private String holdingUpdateTopic;

    @Bean
    public NewTopic tradeHoldingUpdateTopic() {
        // Topic name is driven by application-kafka.yml so Trade and Portfolio always agree.
        return new NewTopic(holdingUpdateTopic, 3, (short) 1);
    }

    @Bean
    public NewTopic portfolioUpdateTopic() {
        return new NewTopic("am-portfolio-update", 3, (short) 1);
    }
}
