package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Model class representing a trade summary with associated portfolios, trade details, and metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSummary {
    private String id;
    private String userId;
    private String name;
    private String description;
    private String ownerId;
    private boolean active;
    private String currency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    
    // Trade metrics
    private PortfolioMetrics metrics;
    
    // List of all trade details in this summary
    private List<TradeDetails> tradeDetails;
    
    // List of portfolios this trade summary belongs to
    private List<String> portfolioIds;
    
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
    
    // Legacy performance statistics (maintained for backward compatibility)
    private BigDecimal totalProfitLoss;
    private BigDecimal winRate;
    private BigDecimal averageHoldingTime;
    private BigDecimal averagePositionSize;
    
    // Legacy risk metrics (maintained for backward compatibility)
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;
    private BigDecimal sortinoRatio;
    
    // Asset allocation
    private List<AssetAllocation> assetAllocations;
    
    // Trade tags for categorization
    private List<String> tags;
}
