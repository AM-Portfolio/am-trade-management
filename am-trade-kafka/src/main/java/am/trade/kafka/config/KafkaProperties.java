package am.trade.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration("kafkaProperties")
@ConfigurationProperties(prefix = "am.trade.kafka")
public class KafkaProperties {
    private String bootstrapServers ;
    private String consumerGroupId ;
    private String tradeEventsTopic ;
    private String orderEventsTopic;
    
    private int retries = 3;
    private long retryBackoffMs = 1000;
    private int maxPollRecords = 500;
    private int maxPollIntervalMs = 300000;
    private int sessionTimeoutMs = 10000;
    private int heartbeatIntervalMs = 3000;
    private int maxInFlightRequestsPerConnection = 5;
    private boolean enableAutoCommit = false;
    private String autoOffsetReset ;
    private Properties properties;

    @Data
    public static class Properties {
        private String securityProtocol;
        private String saslMechanism;
        private String saslJaasConfig;
    }
}


