package am.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Filter criteria for profit/loss metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossFilter {

    @Schema(description = "Filter trades by minimum profit/loss amount")
    private Double minProfitLoss;
    
    @Schema(description = "Filter trades by maximum profit/loss amount")
    private Double maxProfitLoss;
    
    @Schema(description = "Filter trades by minimum position size")
    private Double minPositionSize;
    
    @Schema(description = "Filter trades by maximum position size")
    private Double maxPositionSize;
}
