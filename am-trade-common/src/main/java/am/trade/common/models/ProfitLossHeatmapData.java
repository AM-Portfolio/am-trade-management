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
    
    // Average profit per winning trade
    private BigDecimal avgWinAmount;
    
    // Average loss per losing trade
    private BigDecimal avgLossAmount;
    
    // Largest winning trade
    private BigDecimal maxWinAmount;
    
    // Largest losing trade
    private BigDecimal maxLossAmount;
    
    // Optional list of all trade details if requested
    private List<TradeDetails> tradeDetails;
    
    // Optional list of winning trades if requested
    private List<TradeDetails> winTrades;
    
    // Optional list of losing trades if requested
    private List<TradeDetails> lossTrades;

    /**
     * Calculate and populate summary metrics from periodData
     * This method aggregates metrics from all periods and sets the summary fields
     * 
     * @return this object for method chaining
     */
    public ProfitLossHeatmapData calculateSummaryMetricsFromPeriods() {
        if (periodData == null || periodData.isEmpty()) {
            return this;
        }
        
        // Calculate total profit/loss
        this.totalProfitLoss = periodData.stream()
                .filter(period -> period.getProfitLoss() != null)
                .map(PeriodProfitLossData::getProfitLoss)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate win and loss counts
        this.winCount = periodData.stream()
                .mapToInt(PeriodProfitLossData::getWinCount)
                .sum();
                
        this.lossCount = periodData.stream()
                .mapToInt(PeriodProfitLossData::getLossCount)
                .sum();
        
        // Calculate win rate
        int totalTrades = this.winCount + this.lossCount;
        if (totalTrades > 0) {
            this.winRate = new BigDecimal(this.winCount)
                    .multiply(new BigDecimal(100))
                    .divide(new BigDecimal(totalTrades), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.winRate = BigDecimal.ZERO;
        }
        
        // Calculate average win amount
        List<BigDecimal> winAmounts = periodData.stream()
                .filter(period -> period.getAvgWinAmount() != null && period.getWinCount() > 0)
                .map(period -> period.getAvgWinAmount().multiply(new BigDecimal(period.getWinCount())))
                .collect(java.util.stream.Collectors.toList());
                
        if (!winAmounts.isEmpty() && this.winCount > 0) {
            BigDecimal totalWinAmount = winAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            this.avgWinAmount = totalWinAmount.divide(new BigDecimal(this.winCount), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.avgWinAmount = BigDecimal.ZERO;
        }
        
        // Calculate average loss amount
        List<BigDecimal> lossAmounts = periodData.stream()
                .filter(period -> period.getAvgLossAmount() != null && period.getLossCount() > 0)
                .map(period -> period.getAvgLossAmount().multiply(new BigDecimal(period.getLossCount())))
                .collect(java.util.stream.Collectors.toList());
                
        if (!lossAmounts.isEmpty() && this.lossCount > 0) {
            BigDecimal totalLossAmount = lossAmounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            this.avgLossAmount = totalLossAmount.divide(new BigDecimal(this.lossCount), 2, java.math.RoundingMode.HALF_UP);
        } else {
            this.avgLossAmount = BigDecimal.ZERO;
        }
        
        // Calculate max win amount
        this.maxWinAmount = periodData.stream()
                .filter(period -> period.getMaxWinAmount() != null)
                .map(PeriodProfitLossData::getMaxWinAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        // Calculate max loss amount
        this.maxLossAmount = periodData.stream()
                .filter(period -> period.getMaxLossAmount() != null)
                .map(PeriodProfitLossData::getMaxLossAmount)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        return this;
    }
}
