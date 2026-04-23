package am.trade.models.dto;

import am.trade.models.shared.PerformanceMetrics;
import am.trade.models.shared.RiskMetrics;
import am.trade.models.shared.StrategyPerformanceMetrics;
import am.trade.models.shared.TradeDetails;
import am.trade.models.shared.TradeDistributionMetrics;
import am.trade.models.shared.TradePatternMetrics;
import am.trade.models.shared.TradeTimingMetrics;
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
