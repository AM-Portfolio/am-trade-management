package am.trade.api.dto;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.RiskMetrics;
import am.trade.common.models.StrategyPerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.TradePatternMetrics;
import am.trade.common.models.TradeTimingMetrics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for trade metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MetricsResponse {

    private List<String> portfolioIds;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
    
    private int totalTradesCount;
    
    private List<TradeDetails> tradeDetails;
    
    private PerformanceMetrics performanceMetrics;
    
    private RiskMetrics riskMetrics;
    
    private TradeDistributionMetrics distributionMetrics;
    
    private TradeTimingMetrics timingMetrics;
    
    private TradePatternMetrics patternMetrics;
    
    private Map<String, StrategyPerformanceMetrics> strategyMetrics;
    
    private Map<String, Map<String, Object>> groupedMetrics;
    
    private Map<String, Object> metadata;
}
