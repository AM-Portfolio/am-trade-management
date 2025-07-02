package am.trade.persistence.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import am.trade.common.models.AssetAllocation;
import am.trade.common.models.PortfolioMetrics;
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
    private List<String> trades;
    
    // List of winning trades in this portfolio
    private List<String> winningTrades;
    
    // List of losing trades in this portfolio
    private List<String> losingTrades;
    
    // Asset allocation
    private List<AssetAllocation> assetAllocations;
}
