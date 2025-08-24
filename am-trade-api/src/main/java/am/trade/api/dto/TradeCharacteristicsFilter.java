package am.trade.api.dto;

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

    private Set<String> strategies;
    
    private Set<String> tags;
    
    private Set<String> directions;
    
    private Set<String> statuses;
    
    private Integer minHoldingTimeHours;
    
    private Integer maxHoldingTimeHours;
}
