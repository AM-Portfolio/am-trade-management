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
 * Model class representing a portfolio with its associated trades and metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioModel {
    private String portfolioId;
    private String name;
    private String description;
    private String ownerId;
    private boolean active;
    private String currency;
    private BigDecimal initialCapital;
    private BigDecimal currentCapital;
    private LocalDateTime createdDate;
    private LocalDateTime lastUpdatedDate;
    
    // Portfolio metrics
    private PortfolioMetrics metrics;
    
    // List of trades in this portfolio
    private List<TradeDetails> trades;
    
    // Asset allocation
    private List<AssetAllocation> assetAllocations;
    
    /**
     * Model representing portfolio-level metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioMetrics {
        // Trade counts
        private int totalTrades;
        private int winningTrades;
        private int losingTrades;
        private int breakEvenTrades;
        private int openPositions;
        
        // Performance metrics
        private BigDecimal winRate;
        private BigDecimal lossRate;
        private BigDecimal profitFactor;
        private BigDecimal expectancy;
        
        // Value metrics
        private BigDecimal totalValue;
        private BigDecimal totalProfit;
        private BigDecimal totalLoss;
        private BigDecimal netProfitLoss;
        private BigDecimal netProfitLossPercentage;
        
        // Risk metrics
        private BigDecimal maxDrawdown;
        private BigDecimal maxDrawdownPercentage;
        private BigDecimal sharpeRatio;
        private BigDecimal sortinoRatio;
        
        // Time-based metrics
        private Map<String, BigDecimal> monthlyReturns;
        private Map<String, BigDecimal> weeklyReturns;
    }
    
    /**
     * Model representing asset allocation in the portfolio
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetAllocation {
        private String assetClass;
        private BigDecimal currentPercentage;
        private BigDecimal targetPercentage;
        private BigDecimal variance;
    }
}
