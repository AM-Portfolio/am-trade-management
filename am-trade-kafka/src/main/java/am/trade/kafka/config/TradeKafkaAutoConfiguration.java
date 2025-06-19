package am.trade.kafka.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import am.trade.kafka.producer.KafkaProducerService;

/**
 * Auto-configuration for Trade Kafka components.
 */
@AutoConfiguration(after = KafkaAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@EnableConfigurationProperties(KafkaProperties.class)
@Import({KafkaConfig.class})
public class TradeKafkaAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public KafkaProducerService kafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        return new KafkaProducerService(kafkaTemplate);
    }
}
