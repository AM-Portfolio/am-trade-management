package am.trade.models.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquityPosition {
    private String symbol;
    private String assetType;         // e.g. "EQUITY", "INDEXFUTURES"

    // --- Entry (BUY) details ---
    private BigDecimal quantity;       // Entry quantity
    private BigDecimal avgBuyingPrice; // Entry price per unit
    private BigDecimal investmentValue;// quantity * avgBuyingPrice

    // --- Exit (SELL) details — only populated when action is SELL ---
    private BigDecimal sellQuantity;   // How many units were sold
    private BigDecimal sellPrice;      // Exit price per unit
    private BigDecimal saleValue;      // sellQuantity * sellPrice

    // --- P&L (only for closed trades: WIN, LOSS, BREAK_EVEN) ---
    private BigDecimal profitLoss;     // Realised P&L = saleValue - investmentValue (proportional)

    // --- Trade lifecycle state ---
    /**
     * "BUY"   — opening a position (OPEN trade)
     * "SELL"  — fully or partially closing a position (WIN / LOSS / BREAK_EVEN)
     * "UPDATE"— metadata edit on an already open trade, no quantity change
     */
    private String action;

    /**
     * The outcome of the trade: OPEN | WIN | LOSS | BREAK_EVEN
     * Lets am-portfolio know how to categorise this event.
     */
    private String tradeStatus;

    // --- Instrument metadata ---
    private String isin;
    private String sector;
    private String industry;
    private String marketCap;
}
