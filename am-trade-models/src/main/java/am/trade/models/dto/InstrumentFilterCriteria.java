package am.trade.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * Criteria for filtering trades based on instrument characteristics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstrumentFilterCriteria {

    private Set<String> marketSegments;
    
    private Set<String> baseSymbols;
    
    private Set<String> indexTypes;
    
    private Set<String> derivativeTypes;
}
