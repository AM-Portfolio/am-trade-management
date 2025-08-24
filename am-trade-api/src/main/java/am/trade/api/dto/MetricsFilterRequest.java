package am.trade.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
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

    @NotEmpty(message = "At least one portfolio ID must be specified")
    private List<String> portfolioIds;
    
    @Valid
    private DateRangeFilter dateRange;
    
    @Builder.Default
    private TimePeriodFilter timePeriod = null;
    
    private Set<String> metricTypes;
    
    private Set<String> instruments;
    
    private InstrumentFilterCriteria instrumentFilters;
    
    private TradeCharacteristicsFilter tradeCharacteristics;
    
    private ProfitLossFilter profitLossFilters;
    
    private List<String> groupBy;
    
    @Builder.Default
    private Boolean includeTradeDetails = false;
    
    private Map<String, Object> customFilters;
}
