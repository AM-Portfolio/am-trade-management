package am.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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

    @Schema(description = "Portfolio IDs to include in metrics calculation")
    private List<String> portfolioIds;
    
    @Schema(description = "Start date for the metrics calculation period")
    private LocalDate startDate;
    
    @Schema(description = "End date for the metrics calculation period")
    private LocalDate endDate;
    
    @Schema(description = "Types of metrics to include in the response (e.g., PERFORMANCE, RISK, DISTRIBUTION, TIMING, PATTERN)")
    private Set<String> metricTypes;
    
    @Schema(description = "Filter trades by specific strategies")
    private Set<String> strategies;
    
    @Schema(description = "Filter trades by specific instruments")
    private Set<String> instruments;
    
    @Schema(description = "Filter trades by specific tags")
    private Set<String> tags;
    
    @Schema(description = "Filter trades by trade direction (LONG, SHORT)")
    private Set<String> directions;
    
    @Schema(description = "Filter trades by specific trade statuses (OPEN, CLOSED)")
    private Set<String> statuses;
    
    @Schema(description = "Filter trades by minimum profit/loss amount")
    private Double minProfitLoss;
    
    @Schema(description = "Filter trades by maximum profit/loss amount")
    private Double maxProfitLoss;
    
    @Schema(description = "Filter trades by minimum position size")
    private Double minPositionSize;
    
    @Schema(description = "Filter trades by maximum position size")
    private Double maxPositionSize;
    
    @Schema(description = "Filter trades by minimum holding time in hours")
    private Integer minHoldingTimeHours;
    
    @Schema(description = "Filter trades by maximum holding time in hours")
    private Integer maxHoldingTimeHours;
    
    @Schema(description = "Group results by specific dimensions (e.g., STRATEGY, INSTRUMENT, TAG, DIRECTION)")
    private Set<String> groupBy;
    
    @Schema(description = "Additional custom filters as key-value pairs")
    private Map<String, Object> customFilters;
}
