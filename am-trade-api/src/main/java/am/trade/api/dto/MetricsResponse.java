package am.trade.api.dto;

import am.trade.common.models.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for trade metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object containing requested trade metrics")
public class MetricsResponse {

    @Schema(description = "List of portfolio IDs included in the metrics calculation")
    private List<String> portfolioIds;
    
    @Schema(description = "Start date of the metrics calculation period")
    private LocalDate startDate;
    
    @Schema(description = "End date of the metrics calculation period")
    private LocalDate endDate;
    
    @Schema(description = "Total number of trades included in the metrics calculation")
    private int totalTradesCount;
    
    @Schema(description = "Performance metrics if requested")
    private PerformanceMetrics performanceMetrics;
    
    @Schema(description = "Risk metrics if requested")
    private RiskMetrics riskMetrics;
    
    @Schema(description = "Trade distribution metrics if requested")
    private TradeDistributionMetrics distributionMetrics;
    
    @Schema(description = "Trade timing metrics if requested")
    private TradeTimingMetrics timingMetrics;
    
    @Schema(description = "Trade pattern metrics if requested")
    private TradePatternMetrics patternMetrics;
    
    @Schema(description = "Strategy-specific metrics if requested, keyed by strategy name")
    private Map<String, StrategyPerformanceMetrics> strategyMetrics;
    
    @Schema(description = "Grouped metrics results if grouping was requested")
    private Map<String, Map<String, Object>> groupedMetrics;
    
    @Schema(description = "Metadata about the metrics calculation")
    private Map<String, Object> metadata;
}
