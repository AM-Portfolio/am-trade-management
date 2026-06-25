package am.trade.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "am.trade.kafka")
public class KafkaProperties {
    private String bootstrapServers = "localhost:9092";
    private String consumerGroupId = "am-trade-group";
    private String tradeEventsTopic = "trade-events";
    private String orderEventsTopic = "order-events";
    private int retries = 3;
    private long retryBackoffMs = 1000;
    private int maxPollRecords = 500;
    private int maxPollIntervalMs = 300000;
    private int sessionTimeoutMs = 10000;
    private int heartbeatIntervalMs = 3000;
    private int maxInFlightRequestsPerConnection = 5;
    private boolean enableAutoCommit = false;
    private String autoOffsetReset = "earliest";
}
