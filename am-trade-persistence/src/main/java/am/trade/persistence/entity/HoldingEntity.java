package am.trade.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a single aggregated holding (e.g., an equity position) within a Portfolio.
 * This class is embedded directly inside the PortfolioEntity document in MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoldingEntity {
    
    private String symbol;
    private String assetType;
    private BigDecimal quantity;
    private BigDecimal avgBuyingPrice;
    private BigDecimal investmentValue;
    
    // Optional metadata
    private String isin;
    private String sector;
    private String industry;
    private String marketCap;
}
