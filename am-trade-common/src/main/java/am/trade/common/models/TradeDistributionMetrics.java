package am.trade.common.models;

import am.trade.common.models.enums.AssetClass;
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
    private Map<String, Integer> tradesByDay; // Day of week distribution
    private Map<String, Integer> tradesByHour; // Hour of day distribution
    private Map<String, Integer> tradesByMonth; // Month distribution
    private Map<String, Integer> tradesByQuarter; // Quarter distribution
    
    // Performance by time period
    private Map<String, BigDecimal> profitByDay;
    private Map<String, BigDecimal> profitByHour;
    private Map<String, BigDecimal> profitByMonth;
    private Map<String, BigDecimal> profitByQuarter;
    
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
    private Map<String, Integer> tradesByDuration; // Categorized by duration ranges
    private Map<String, BigDecimal> profitByDuration;
    private Map<String, BigDecimal> winRateByDuration;
    
    // Position size distribution
    private Map<String, Integer> tradesByPositionSize; // Categorized by size ranges
    private Map<String, BigDecimal> profitByPositionSize;
    private Map<String, BigDecimal> winRateByPositionSize;
    
    // Market condition distribution
    private Map<String, Integer> tradesByMarketCondition; // Bull, bear, sideways
    private Map<String, BigDecimal> profitByMarketCondition;
    private Map<String, BigDecimal> winRateByMarketCondition;
    
    // Trade setup distribution
    private Map<String, Integer> tradesBySetup; // Different trade setups
    private Map<String, BigDecimal> profitBySetup;
    private Map<String, BigDecimal> winRateBySetup;
}
