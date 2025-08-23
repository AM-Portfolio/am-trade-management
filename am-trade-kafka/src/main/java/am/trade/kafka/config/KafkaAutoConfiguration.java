// package am.trade.kafka.config;

// import org.springframework.boot.autoconfigure.AutoConfiguration;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.context.annotation.ComponentScan;
// import org.springframework.kafka.annotation.EnableKafka;

// /**
//  * Auto-configuration for the Kafka module
//  * Automatically configures Kafka consumers, producers, and mappers
//  */
// @AutoConfiguration
// @EnableKafka
// @ConditionalOnProperty(prefix = "am.trade.kafka", name = "enabled", havingValue = "true", matchIfMissing = true)
// @ComponentScan(basePackages = {"am.trade.kafka", "am.trade.common"})
// public class KafkaAutoConfiguration {
//     // Configuration is handled through component scanning and properties
// }
