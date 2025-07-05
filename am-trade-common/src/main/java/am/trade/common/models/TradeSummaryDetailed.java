package am.trade.common.models;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Detailed trade summary model containing comprehensive metrics and analysis.
 * This model is designed to be stored separately in MongoDB and retrieved only
 * when detailed analysis is needed, reducing database load and improving performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trade_summary_detailed")
public class TradeSummaryDetailed {
    @Id
    private String id;
    private String tradeSummaryBasicId; // Reference to the basic summary
    
    // List of all trade details in this summary
    private List<TradeDetails> tradeDetails;
    
    // Categorized trade lists
    private List<TradeDetails> winningTrades;  // Sorted by profit (highest profit first)
    private List<TradeDetails> losingTrades;   // Sorted by loss (highest loss first)
    
    // Detailed metrics
    private PerformanceMetrics performanceMetrics;
    private RiskMetrics riskMetrics;
    private TradeDistributionMetrics distributionMetrics;
    private TradeTimingMetrics timingMetrics;
    private Map<String, StrategyPerformanceMetrics> strategyMetrics; // Key is strategy name
    private TradePatternMetrics patternMetrics; // Trade pattern and psychology metrics
    private TradingFeedback tradingFeedback; // Personalized feedback based on psychology and behavior patterns
    
    // Last calculation timestamp
    private LocalDateTime lastCalculatedTimestamp;
}
