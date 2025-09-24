package am.trade.models.document;

import am.trade.models.document.statistics.AdditionalMetrics;
import am.trade.models.document.statistics.PerformanceMetrics;
import am.trade.models.document.statistics.RiskMetrics;
import am.trade.models.document.statistics.TradeCounts;
import am.trade.models.document.statistics.ValueMetrics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.DocumentReference;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Portfolio document model that contains a collection of trade statistics
 * and aggregated portfolio-level metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "portfolios")
public class Portfolio {
    @Id
    private String id;
    
    @Indexed
    @Field("name")
    private String name;
    
    @Field("description")
    private String description;
    
    @Field("creation_date")
    private LocalDateTime creationDate;
    
    @Field("last_updated")
    private LocalDateTime lastUpdated;
    
    @Indexed
    @Field("owner_id")
    private String ownerId;
    
    @Field("is_active")
    private Boolean isActive;
    
    @Field("currency")
    private String currency;
    
    @Field("initial_capital")
    private Double initialCapital;
    
    @Field("current_capital")
    private Double currentCapital;
    
    // List of trade statistics associated with this portfolio
    @DocumentReference(lazy = true)
    @Field("trade_statistics")
    private List<TradeStatistics> tradeStatistics;
    
    // Aggregated portfolio metrics
    @Field("trade_counts")
    private TradeCounts tradeCounts;
    
    @Field("performance")
    private PerformanceMetrics performanceMetrics;
    
    @Field("value")
    private ValueMetrics valueMetrics;
    
    @Field("risk")
    private RiskMetrics riskMetrics;
    
    // Additional portfolio-specific fields
    @Field("risk_tolerance")
    private String riskTolerance; // e.g., "Conservative", "Moderate", "Aggressive"
    
    @Field("investment_horizon")
    private String investmentHorizon; // e.g., "Short-term", "Medium-term", "Long-term"
    
    @Field("portfolio_type")
    private String portfolioType; // e.g., "Growth", "Income", "Value", "Blend"
    
    @Field("target_allocation")
    private List<AssetAllocation> targetAllocations;
    
    @Field("actual_allocation")
    private List<AssetAllocation> actualAllocations;
    
    /**
     * Nested class for asset allocation information
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AssetAllocation {
        @Field("asset_class")
        private String assetClass;
        
        @Field("percentage")
        private Double percentage;
        
        @Field("target_percentage")
        private Double targetPercentage;
        
        @Field("variance")
        private Double variance;
    }
}
