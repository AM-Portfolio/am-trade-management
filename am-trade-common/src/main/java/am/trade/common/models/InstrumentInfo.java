package am.trade.common.models;

import am.trade.common.models.enums.Exchange;
import am.trade.common.models.enums.MarketSegment;
import am.trade.common.models.enums.SeriesType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class representing instrument information for various market instruments
 * including equities, futures, and options
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentInfo {
    // Basic instrument identification
    private String symbol;
    private String isin;
    
    // Market classification
    private Exchange exchange;
    private MarketSegment segment;
    private SeriesType series;
    
    // For derivatives (options and futures)
    private DerivativeInfo derivativeInfo;
    
    // Additional fields
    private String description;
    private String currency;
    private String lotSize;
}
