package am.trade.analytics.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Represents the market data container from the historical data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketData {
    
    @JsonProperty("tradingSymbol")
    private String tradingSymbol;
    
    @JsonProperty("dataPoints")
    private List<MarketDataPoint> dataPoints;
    
    @JsonProperty("dataPointCount")
    private Integer dataPointCount;
}
