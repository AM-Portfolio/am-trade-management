package am.trade.models.shared;

import am.trade.models.enums.AssetClass;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Model for analyzing the distribution of trades across various dimensions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TradeDistributionMetrics {
    // Time-based distribution
    private Map<String, Integer> tradesByDay;
    private Map<String, Integer> tradesByHour;
    private Map<String, Integer> tradesByMonth;
    private Map<String, Integer> tradesByQuarter;
    private Map<String, Integer> tradesBySession;

    // Performance by time period
    private Map<String, BigDecimal> profitByDay;
    private Map<String, BigDecimal> profitByHour;
    private Map<String, BigDecimal> profitByMonth;
    private Map<String, BigDecimal> profitByQuarter;
    private Map<String, BigDecimal> profitBySession;

    private Map<String, BigDecimal> winRateByDay;
    private Map<String, BigDecimal> winRateByHour;
    private Map<String, BigDecimal> winRateByMonth;
    private Map<String, BigDecimal> winRateBySession;

    private Map<String, BigDecimal> avgPnlByDay;
    private Map<String, BigDecimal> avgPnlByHour;
    private Map<String, BigDecimal> avgPnlByMonth;
    private Map<String, BigDecimal> avgPnlBySession;

    /** Avg PnL / distinct entry dates (eligible only); null when no active days. */
    private Map<String, BigDecimal> avgPnlPerActiveDayByDay;
    private Map<String, BigDecimal> avgPnlPerActiveDayByHour;
    private Map<String, BigDecimal> avgPnlPerActiveDayByMonth;
    private Map<String, BigDecimal> avgPnlPerActiveDayBySession;

    private Map<String, Integer> eligibleTradesByDay;
    private Map<String, Integer> eligibleTradesByHour;
    private Map<String, Integer> eligibleTradesByMonth;
    private Map<String, Integer> eligibleTradesBySession;

    private Map<String, Integer> activeTradingDaysByDay;
    private Map<String, Integer> activeTradingDaysByHour;
    private Map<String, Integer> activeTradingDaysByMonth;
    private Map<String, Integer> activeTradingDaysBySession;

    private Integer activeTradingDaysCount;
    private BigDecimal avgPnlPerActiveDay;

    /** Mean hold duration in minutes (valid entry+exit only); null key when sample=0. */
    private Map<String, BigDecimal> avgHoldMinutesByDay;
    private Map<String, BigDecimal> avgHoldMinutesByHour;
    private Map<String, BigDecimal> avgHoldMinutesByMonth;
    private Map<String, BigDecimal> avgHoldMinutesBySession;

    /**
     * Avg win ÷ |avg loss| for eligible trades in bucket (same as overall winLossRatio).
     * Null when no wins or no losses — not a stop-based R-multiple.
     */
    private Map<String, BigDecimal> riskRewardByDay;
    private Map<String, BigDecimal> riskRewardByHour;
    private Map<String, BigDecimal> riskRewardByMonth;
    private Map<String, BigDecimal> riskRewardBySession;

    /** Best session by avg PnL among buckets with eligible ≥ 3; null if none qualify. */
    private String bestSessionKey;
    private BigDecimal bestSessionAvgPnl;
    
    // Asset class distribution
    private Map<AssetClass, Integer> tradeCountByAssetClass;
    private Map<AssetClass, BigDecimal> profitByAssetClass;
    private Map<AssetClass, BigDecimal> winRateByAssetClass;
    
    // Sector distribution (for stocks)
    private Map<String, Integer> tradeCountBySector;
    private Map<String, BigDecimal> profitBySector;
    private Map<String, BigDecimal> winRateBySector;
    
    // Strategy distribution
    private Map<String, Integer> tradeCountByStrategy;
    private Map<String, BigDecimal> profitByStrategy;
    private Map<String, BigDecimal> winRateByStrategy;
    
    // Trade duration distribution
    private Map<String, Integer> tradesByDuration;
    private Map<String, BigDecimal> profitByDuration;
    private Map<String, BigDecimal> winRateByDuration;
    
    // Position size distribution
    private Map<String, Integer> tradesByPositionSize;
    private Map<String, BigDecimal> profitByPositionSize;
    private Map<String, BigDecimal> winRateByPositionSize;
    
    // Market condition distribution
    private Map<String, Integer> tradesByMarketCondition;
    private Map<String, BigDecimal> profitByMarketCondition;
    private Map<String, BigDecimal> winRateByMarketCondition;
    
    // Trade setup distribution
    private Map<String, Integer> tradesBySetup;
    private Map<String, BigDecimal> profitBySetup;
    private Map<String, BigDecimal> winRateBySetup;

    private Integer skippedMissingEntryCount;
    private Integer openOrMissingPnlCount;
    private Integer badTimestampCount;
    private String timezoneNote;
    private TradingStyleHint tradingStyleHint;
}
