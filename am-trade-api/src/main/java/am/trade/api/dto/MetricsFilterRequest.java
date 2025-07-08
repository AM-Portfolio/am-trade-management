package am.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request object for filtering trade metrics")
public class MetricsFilterRequest {

    @Schema(description = "Portfolio IDs to include in metrics calculation", required = true)
    @NotEmpty(message = "At least one portfolio ID must be specified")
    private List<String> portfolioIds;
    
    @Schema(description = "Date range for the metrics calculation")
    @Valid
    private DateRangeFilter dateRange;
    
    @Schema(description = "Types of metrics to include in the response (e.g., PERFORMANCE, RISK, DISTRIBUTION, TIMING, PATTERN)")
    private Set<String> metricTypes;
    
    @Schema(description = "Filter trades by specific instruments")
    private Set<String> instruments;
    
    @Schema(description = "Instrument-specific filter criteria")
    private InstrumentFilterCriteria instrumentFilters;
    
    @Schema(description = "Trade characteristics filter criteria")
    private TradeCharacteristicsFilter tradeCharacteristics;
    
    @Schema(description = "Profit/loss filter criteria")
    private ProfitLossFilter profitLossFilters;
    
    @Schema(description = "Group results by specific dimensions (e.g., STRATEGY, INSTRUMENT, TAG, DIRECTION)")
    private Set<String> groupBy;
    
    @Schema(description = "Additional custom filters as key-value pairs")
    private Map<String, Object> customFilters;
}
