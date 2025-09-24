package am.trade.common.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model for profit and loss heatmap data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfitLossHeatmapData {
    
    /**
     * Type of time granularity for the heatmap
     */
    public enum GranularityType {
        YEARLY,     // Year-wise data
        MONTHLY,    // Month-wise data for a specific year
        DAILY       // Day-wise data for a specific month
    }
    
    // The type of granularity for this heatmap data
    private GranularityType granularityType;
    
    // Detailed profit/loss data for each period
    // For YEARLY: periodId is year (e.g., "2025")
    // For MONTHLY: periodId is month (e.g., "2025-06")
    // For DAILY: periodId is day (e.g., "2025-06-25")
    private List<PeriodProfitLossData> periodData;
    
    // Total profit/loss for the entire period
    private BigDecimal totalProfitLoss;
    
    // Count of winning trades in this period
    private int winCount;
    
    // Count of losing trades in this period
    private int lossCount;
    
    // Win rate percentage
    private BigDecimal winRate;
    
    // Optional list of all trade details if requested
    private List<TradeDetails> tradeDetails;
    
    // Optional list of winning trades if requested
    private List<TradeDetails> winTrades;
    
    // Optional list of losing trades if requested
    private List<TradeDetails> lossTrades;

    
}
