package am.trade.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Request DTO for flexible trade metrics filtering
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricsFilterRequest {

    private List<String> portfolioIds;
    
    private DateRangeFilter dateRange;
    
    private TimePeriodFilter timePeriod = null;
    
    private Set<String> metricTypes;
    
    private Set<String> instruments;
    
    private InstrumentFilterCriteria instrumentFilters;
    
    private TradeCharacteristicsFilter tradeCharacteristics;
    
    private ProfitLossFilter profitLossFilters;
    
    private List<String> groupBy;
    
    private Boolean includeTradeDetails = false;
    
    private Map<String, Object> customFilters;
}
