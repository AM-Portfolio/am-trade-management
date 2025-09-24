package am.trade.api.dto;

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

    @Schema(description = "Filter trades by market segment (EQUITY, INDEX, EQUITY_FUTURES, INDEX_FUTURES, EQUITY_OPTIONS, INDEX_OPTIONS)")
    private Set<String> marketSegments;
    
    @Schema(description = "Filter trades by specific base symbols (e.g., NIFTY, BANKNIFTY, RELIANCE)")
    private Set<String> baseSymbols;
    
    @Schema(description = "Filter trades by index type (e.g., NIFTY, BANKNIFTY, FINNIFTY)")
    private Set<String> indexTypes;
    
    @Schema(description = "Filter trades by derivative type (FUTURES, OPTIONS)")
    private Set<String> derivativeTypes;
}
