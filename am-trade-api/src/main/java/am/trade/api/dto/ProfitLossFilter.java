package am.trade.api.dto;

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

    private Double minProfitLoss;
    
    private Double maxProfitLoss;
    
    private Double minPositionSize;
    
    private Double maxPositionSize;
}
