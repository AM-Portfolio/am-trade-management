package am.trade.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "market-data")
public class MarketDataApiConfig {
    private String baseUrl;
    private String ohlcEndpoint;
}
