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
    
    // List of all trades in this portfolio
    private List<String> tradeIds;
    
    // Sorted lists of winning and losing trades
    private List<String> winningTradeIds;  // Sorted by profit (highest profit first)
    private List<String> losingTradeIds;   // Sorted by loss (highest loss first)
    
    // Asset allocation
    private List<AssetAllocation> assetAllocations;

}
