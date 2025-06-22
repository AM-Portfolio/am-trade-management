package am.trade.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * MongoDB document entity for Portfolio data
 * Maps to the 'portfolios' collection in the database
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "portfolio_trades")
public class PortfolioEntity {
    
    @Id
    private String id;
    
    @Indexed(unique = true)
    private String portfolioId;
    
    private String name;
    private String description;
    
    @Indexed
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
    private List<TradeDetailsEntity> trades;
    
    // Asset allocation
    private List<AssetAllocation> assetAllocations;
    
    /**
     * Embedded document for portfolio metrics
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
        
        // Profit/loss metrics
        private BigDecimal totalProfitLoss;
        private BigDecimal totalProfitLossPercentage;
        private BigDecimal winRate;
        private BigDecimal lossRate;
        private BigDecimal averageWin;
        private BigDecimal averageLoss;
        private BigDecimal largestWin;
        private BigDecimal largestLoss;
        
        // Risk metrics
        private BigDecimal maxDrawdown;
        private BigDecimal maxDrawdownPercentage;
        private BigDecimal sharpeRatio;
        private BigDecimal sortinoRatio;
        private BigDecimal calmarRatio;
    }
    
    /**
     * Embedded document for asset allocation
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetAllocation {
        private String assetClass;
        private String sector;
        private String industry;
        private BigDecimal allocation;
        private BigDecimal currentValue;
        private BigDecimal profitLoss;
        private BigDecimal profitLossPercentage;
    }
}
