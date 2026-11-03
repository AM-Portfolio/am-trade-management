package am.trade.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "market-data.api")
public class MarketDataApiConfig {
    private String baseUrl;
    private String ohlcEndpoint;
    /** Historical bars for import-day mark backfill when cost is unknown. */
    private String historicalDataPath = "/v1/market-data/historical-data";
}
