package am.trade.analytics.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * Represents a single market data point from the historical data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketDataPoint {
    
    @JsonProperty("time")
    private List<Integer> time;
    
    @JsonProperty("open")
    private BigDecimal open;
    
    @JsonProperty("high")
    private BigDecimal high;
    
    @JsonProperty("low")
    private BigDecimal low;
    
    @JsonProperty("close")
    private BigDecimal close;
    
    @JsonProperty("volume")
    private Long volume;
}
