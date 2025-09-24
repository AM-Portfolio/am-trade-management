package am.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Filter criteria for trade characteristics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeCharacteristicsFilter {

    @Schema(description = "Filter trades by specific strategies")
    private Set<String> strategies;
    
    @Schema(description = "Filter trades by specific tags")
    private Set<String> tags;
    
    @Schema(description = "Filter trades by trade direction (LONG, SHORT)")
    private Set<String> directions;
    
    @Schema(description = "Filter trades by specific trade statuses (OPEN, CLOSED)")
    private Set<String> statuses;
    
    @Schema(description = "Filter trades by minimum holding time in hours")
    private Integer minHoldingTimeHours;
    
    @Schema(description = "Filter trades by maximum holding time in hours")
    private Integer maxHoldingTimeHours;
}
