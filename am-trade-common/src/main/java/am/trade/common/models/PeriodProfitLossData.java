package am.trade.common.models;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for profit and loss data for a specific period (day/month/year)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PeriodProfitLossData {
    
    // The period identifier (e.g., "2025", "2025-06", "2025-06-25")
    private String periodId;
    
    // Profit/loss amount for this period
    private BigDecimal profitLoss;
    
    // Count of winning trades in this period
    private int winCount;
    
    // Count of losing trades in this period
    private int lossCount;
    
    // Win rate percentage for this period
    private BigDecimal winRate;
    
    // Average profit per winning trade
    private BigDecimal avgWinAmount;
    
    // Average loss per losing trade
    private BigDecimal avgLossAmount;
    
    // Largest winning trade
    private BigDecimal maxWinAmount;
    
    // Largest losing trade
    private BigDecimal maxLossAmount;
}
