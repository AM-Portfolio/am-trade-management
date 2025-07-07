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
 * Composite trade summary model that combines basic and detailed metrics.
 * This class serves as a facade for the split storage model where basic metrics
 * and detailed metrics are stored in separate MongoDB documents.
 * 
 * This class is primarily used for backward compatibility and for presenting
 * a unified view of trade data to clients when needed.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSummary {
    // Basic summary fields
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
    
    // Basic trade metrics
    private PortfolioMetrics basicMetrics;
    private List<String> portfolioIds;
    private int totalTradeCount;
    private int winningTradeCount;
    private int losingTradeCount;
    
    // Legacy performance statistics (maintained for backward compatibility)
    private BigDecimal totalProfitLoss;
    private BigDecimal winRate;
    private BigDecimal averageHoldingTime;
    private BigDecimal averagePositionSize;
    
    // Reference to detailed metrics document
    private String detailedMetricsId;
    
    // Detailed metrics fields (populated on demand)
    private List<TradeDetails> tradeDetails;
    private List<TradeDetails> winningTrades;
    private List<TradeDetails> losingTrades;
    private PerformanceMetrics performanceMetrics;
    private RiskMetrics riskMetrics;
    private TradeDistributionMetrics distributionMetrics;
    private TradeTimingMetrics timingMetrics;
    private Map<String, StrategyPerformanceMetrics> strategyMetrics;
    private TradePatternMetrics patternMetrics;
    private TradingFeedback tradingFeedback;
    
    // Legacy risk metrics (maintained for backward compatibility)
    private BigDecimal maxDrawdown;
    private BigDecimal sharpeRatio;
    private BigDecimal sortinoRatio;
    
    // Asset allocation
    private List<AssetAllocation> assetAllocations;
    
    // Trade tags for categorization
    private List<String> tags;
    
    /**
     * Creates a composite TradeSummary from a basic summary and detailed metrics.
     * 
     * @param basic The basic trade summary containing essential information
     * @param detailed The detailed trade metrics and analysis
     * @return A composite TradeSummary object with all data combined
     */
    public static TradeSummary fromBasicAndDetailed(TradeSummaryBasic basic, TradeSummaryDetailed detailed) {
        if (basic == null) {
            throw new IllegalArgumentException("TradeSummaryBasic cannot be null");
        }
        
        TradeSummary composite = new TradeSummary();
        
        // Copy basic fields
        composite.setId(basic.getId());
        composite.setUserId(basic.getUserId());
        composite.setName(basic.getName());
        composite.setDescription(basic.getDescription());
        composite.setOwnerId(basic.getOwnerId());
        composite.setActive(basic.isActive());
        composite.setCurrency(basic.getCurrency());
        composite.setStartDate(basic.getStartDate());
        composite.setEndDate(basic.getEndDate());
        composite.setCreatedDate(basic.getCreatedDate());
        composite.setLastUpdatedDate(basic.getLastUpdatedDate());
        
        // Copy basic metrics
        composite.setBasicMetrics(basic.getBasicMetrics());
        composite.setPortfolioIds(basic.getPortfolioIds());
        composite.setTotalTradeCount(basic.getTotalTradeCount());
        composite.setWinningTradeCount(basic.getWinningTradeCount());
        composite.setLosingTradeCount(basic.getLosingTradeCount());
        
        // Copy legacy performance statistics
        composite.setTotalProfitLoss(basic.getTotalProfitLoss());
        composite.setWinRate(basic.getWinRate());
        composite.setAverageHoldingTime(basic.getAverageHoldingTime());
        composite.setAveragePositionSize(basic.getAveragePositionSize());
        
        // Set reference to detailed metrics
        composite.setDetailedMetricsId(basic.getDetailedMetricsId());
        
        // Copy detailed metrics if available
        if (detailed != null) {
            composite.setTradeDetails(detailed.getTradeDetails());
            composite.setWinningTrades(detailed.getWinningTrades());
            composite.setLosingTrades(detailed.getLosingTrades());
            composite.setPerformanceMetrics(detailed.getPerformanceMetrics());
            composite.setRiskMetrics(detailed.getRiskMetrics());
            composite.setDistributionMetrics(detailed.getDistributionMetrics());
            composite.setTimingMetrics(detailed.getTimingMetrics());
            composite.setStrategyMetrics(detailed.getStrategyMetrics());
            composite.setPatternMetrics(detailed.getPatternMetrics());
            composite.setTradingFeedback(detailed.getTradingFeedback());
        }
        
        return composite;
    }
    
    /**
     * Extracts a TradeSummaryBasic object from this composite TradeSummary.
     * 
     * @return A TradeSummaryBasic object containing only the basic summary data
     */
    public TradeSummaryBasic toBasicSummary() {
        return TradeSummaryBasic.builder()
                .id(this.id)
                .userId(this.userId)
                .name(this.name)
                .description(this.description)
                .ownerId(this.ownerId)
                .active(this.active)
                .currency(this.currency)
                .startDate(this.startDate)
                .endDate(this.endDate)
                .createdDate(this.createdDate)
                .lastUpdatedDate(this.lastUpdatedDate)
                .basicMetrics(this.basicMetrics)
                .portfolioIds(this.portfolioIds)
                .totalTradeCount(this.totalTradeCount)
                .winningTradeCount(this.winningTradeCount)
                .losingTradeCount(this.losingTradeCount)
                .totalProfitLoss(this.totalProfitLoss)
                .winRate(this.winRate)
                .averageHoldingTime(this.averageHoldingTime)
                .averagePositionSize(this.averagePositionSize)
                .detailedMetricsId(this.detailedMetricsId)
                .build();
    }
    
    /**
     * Extracts a TradeSummaryDetailed object from this composite TradeSummary.
     * 
     * @return A TradeSummaryDetailed object containing only the detailed metrics and analysis
     */
    public TradeSummaryDetailed toDetailedSummary() {
        return TradeSummaryDetailed.builder()
                .id(this.detailedMetricsId)
                .tradeSummaryBasicId(this.id)
                .tradeDetails(this.tradeDetails)
                .winningTrades(this.winningTrades)
                .losingTrades(this.losingTrades)
                .performanceMetrics(this.performanceMetrics)
                .riskMetrics(this.riskMetrics)
                .distributionMetrics(this.distributionMetrics)
                .timingMetrics(this.timingMetrics)
                .strategyMetrics(this.strategyMetrics)
                .patternMetrics(this.patternMetrics)
                .tradingFeedback(this.tradingFeedback)
                .lastCalculatedTimestamp(LocalDateTime.now())
                .build();
    }
}
