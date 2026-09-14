package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Inferred book style from holding duration (not a user preference).
 * Does not change Timing session windows.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TradingStyleHint {
    /** SCALPER, INTRADAY, SWING, MIXED, or UNKNOWN */
    private String style;
    /** Share of known-hold trades in the winning class (0–100). */
    private BigDecimal confidencePercent;
    /** Always holding_duration for this hint. */
    private String basis;
    /** Number of trades with valid non-negative hold used for classification. */
    private Integer sampleSize;
}
