package am.trade.common.models;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration for metrics filters that can be saved as favorites
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MetricsFilterConfig {
    private List<String> portfolioIds;
    private Map<String, Object> dateRange;
    private Map<String, Object> timePeriod;
    private List<String> metricTypes;
    private List<String> instruments;
    private Map<String, Object> instrumentFilters;
    private Map<String, Object> tradeCharacteristics;
    private Map<String, Object> profitLossFilters;
}
