package am.trade.analytics.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response model for the historical market data API
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalMarketDataResponse {
    
    @JsonProperty("fromDate")
    private String fromDate;
    
    @JsonProperty("toDate")
    private String toDate;
    
    @JsonProperty("symbol")
    private String symbol;
    
    @JsonProperty("interval")
    private String interval;
    
    @JsonProperty("count")
    private Integer count;
    
    @JsonProperty("processingTimeMs")
    private Long processingTimeMs;
    
    @JsonProperty("data")
    private MarketData data;
}
