package am.trade.persistence.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.persistence")
public class PersistenceProperties {
    private MongoProperties mongodb;

    @Data
    public static class MongoProperties {
        private String uri;
        private String database;
    }
}
