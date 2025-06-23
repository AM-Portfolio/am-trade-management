package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Model representing asset allocation in the portfolio
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetAllocation {
    private String assetClass;
    private BigDecimal currentPercentage;
    private BigDecimal targetPercentage;
    private BigDecimal variance;
}
