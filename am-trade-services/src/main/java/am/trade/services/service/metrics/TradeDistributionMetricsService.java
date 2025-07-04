package am.trade.services.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.enums.AssetClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating trade distribution metrics from trade data
 */
@Service
@Slf4j
public class TradeDistributionMetricsService {

    // Rounding mode for calculations
    private static final java.math.RoundingMode ROUNDING_MODE = HALF_UP;

    /**
     * Calculate distribution metrics from a list of trades
     */
    public TradeDistributionMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new TradeDistributionMetrics();
        }
        
        TradeDistributionMetrics metrics = new TradeDistributionMetrics();
        
        // Group trades by various dimensions
        Map<String, List<TradeDetails>> tradesByDay = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByMonth = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByAssetClass = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByStrategy = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByDuration = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByPositionSize = new HashMap<>();
        
        // Process each trade
        for (TradeDetails trade : trades) {
            // Skip trades without entry info
            if (trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null) {
                continue;
            }
            
            // Group by day of week
            String dayOfWeek = trade.getEntryInfo().getTimestamp().getDayOfWeek().toString();
            tradesByDay.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(trade);
            
            // Group by month
            String month = trade.getEntryInfo().getTimestamp().getMonth().toString();
            tradesByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(trade);
            
            // Group by asset class if available
            if (trade.getInstrumentInfo() != null && trade.getInstrumentInfo().getSegment() != null) {
                String assetClass = trade.getInstrumentInfo().getSegment().toString();
                tradesByAssetClass.computeIfAbsent(assetClass, k -> new ArrayList<>()).add(trade);
            }
            
            // Group by strategy
            String strategy = trade.getStrategy() != null ? trade.getStrategy() : "UNKNOWN";
            tradesByStrategy.computeIfAbsent(strategy, k -> new ArrayList<>()).add(trade);
            
            // Group by duration
            String durationCategory = calculateDurationCategory(trade);
            tradesByDuration.computeIfAbsent(durationCategory, k -> new ArrayList<>()).add(trade);
            
            // Group by position size
            String positionSizeCategory = calculatePositionSizeCategory(trade);
            tradesByPositionSize.computeIfAbsent(positionSizeCategory, k -> new ArrayList<>()).add(trade);
        }
        
        // Calculate performance by day of week
        Map<String, BigDecimal> profitByDay = calculateProfitByCategory(tradesByDay);
        // Win rate by day calculated but not stored as there's no corresponding field in the domain model
        calculateWinRateByCategory(tradesByDay);
        
        // Calculate performance by month
        Map<String, BigDecimal> profitByMonth = calculateProfitByCategory(tradesByMonth);
        // Win rate by month calculated but not stored as there's no corresponding field in the domain model
        calculateWinRateByCategory(tradesByMonth);
        
        // Calculate performance by asset class
        Map<String, BigDecimal> profitByAssetClass = calculateProfitByCategory(tradesByAssetClass);
        Map<String, BigDecimal> winRateByAssetClass = calculateWinRateByCategory(tradesByAssetClass);
        
        // Calculate performance by strategy
        Map<String, BigDecimal> profitByStrategy = calculateProfitByCategory(tradesByStrategy);
        Map<String, BigDecimal> winRateByStrategy = calculateWinRateByCategory(tradesByStrategy);
        
        // Calculate performance by duration
        Map<String, BigDecimal> profitByDuration = calculateProfitByCategory(tradesByDuration);
        Map<String, BigDecimal> winRateByDuration = calculateWinRateByCategory(tradesByDuration);
        
        // Calculate performance by position size
        Map<String, BigDecimal> profitByPositionSize = calculateProfitByCategory(tradesByPositionSize);
        Map<String, BigDecimal> winRateByPositionSize = calculateWinRateByCategory(tradesByPositionSize);
        
        // Set metrics
        metrics.setTradesByDay(convertToTradeCount(tradesByDay));
        metrics.setProfitByDay(profitByDay);
        // No setWinRateByDay method in TradeDistributionMetrics domain model
        
        metrics.setTradesByMonth(convertToTradeCount(tradesByMonth));
        metrics.setProfitByMonth(profitByMonth);
        
        // Convert string-based maps to enum-based maps for asset class
        Map<AssetClass, Integer> tradeCountByAssetClass = new HashMap<>();
        Map<AssetClass, BigDecimal> profitByAssetClassEnum = new HashMap<>();
        Map<AssetClass, BigDecimal> winRateByAssetClassEnum = new HashMap<>();
        
        tradesByAssetClass.forEach((key, value) -> {
            try {
                // Use fromCode method instead of valueOf since AssetClass is not a standard enum
                AssetClass assetClass = AssetClass.fromCode(key, null);
                tradeCountByAssetClass.put(assetClass, value.size());
                profitByAssetClassEnum.put(assetClass, profitByAssetClass.getOrDefault(key, BigDecimal.ZERO));
                winRateByAssetClassEnum.put(assetClass, winRateByAssetClass.getOrDefault(key, BigDecimal.ZERO));
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert {} to AssetClass enum", key);
            }
        });
        
        metrics.setTradeCountByAssetClass(tradeCountByAssetClass);
        metrics.setProfitByAssetClass(profitByAssetClassEnum);
        metrics.setWinRateByAssetClass(winRateByAssetClassEnum);
        
        metrics.setTradeCountByStrategy(convertToTradeCount(tradesByStrategy));
        metrics.setProfitByStrategy(profitByStrategy);
        metrics.setWinRateByStrategy(winRateByStrategy);
        
        metrics.setTradesByDuration(convertToTradeCount(tradesByDuration));
        metrics.setProfitByDuration(profitByDuration);
        metrics.setWinRateByDuration(winRateByDuration);
        
        metrics.setTradesByPositionSize(convertToTradeCount(tradesByPositionSize));
        metrics.setProfitByPositionSize(profitByPositionSize);
        metrics.setWinRateByPositionSize(winRateByPositionSize);
        
        return metrics;
    }
    
    /**
     * Calculate duration category for a trade
     */
    private String calculateDurationCategory(TradeDetails trade) {
        if (trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null || 
            trade.getExitInfo() == null || trade.getExitInfo().getTimestamp() == null) {
            return "UNKNOWN";
        }
        
        java.time.Duration duration = java.time.Duration.between(
            trade.getEntryInfo().getTimestamp(), 
            trade.getExitInfo().getTimestamp()
        );
        
        long hours = duration.toHours();
        
        if (hours < 1) {
            return "INTRADAY_SHORT";
        } else if (hours < 8) {
            return "INTRADAY_LONG";
        } else if (hours < 24) {
            return "SINGLE_DAY";
        } else if (hours < 24 * 7) {
            return "LESS_THAN_WEEK";
        } else if (hours < 24 * 30) {
            return "LESS_THAN_MONTH";
        } else {
            return "LONG_TERM";
        }
    }
    
    /**
     * Calculate position size category for a trade
     */
    private String calculatePositionSizeCategory(TradeDetails trade) {
        if (trade.getEntryInfo() == null || trade.getEntryInfo().getPrice() == null || 
            trade.getEntryInfo().getQuantity() == null) {
            return "UNKNOWN";
        }
        
        BigDecimal positionSize = trade.getEntryInfo().getPrice()
            .multiply(BigDecimal.valueOf(trade.getEntryInfo().getQuantity().doubleValue()));
        
        // These thresholds should be adjusted based on your typical trading size
        if (positionSize.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return "MICRO";
        } else if (positionSize.compareTo(BigDecimal.valueOf(5000)) < 0) {
            return "SMALL";
        } else if (positionSize.compareTo(BigDecimal.valueOf(20000)) < 0) {
            return "MEDIUM";
        } else if (positionSize.compareTo(BigDecimal.valueOf(50000)) < 0) {
            return "LARGE";
        } else {
            return "EXTRA_LARGE";
        }
    }
    
    /**
     * Calculate profit by category
     */
    private Map<String, BigDecimal> calculateProfitByCategory(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> profitByCategory = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            String category = entry.getKey();
            List<TradeDetails> categoryTrades = entry.getValue();
            
            BigDecimal totalProfit = categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .map(t -> t.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            profitByCategory.put(category, totalProfit);
        }
        
        return profitByCategory;
    }
    
    /**
     * Calculate win rate by category
     */
    private Map<String, BigDecimal> calculateWinRateByCategory(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> winRateByCategory = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            String category = entry.getKey();
            List<TradeDetails> categoryTrades = entry.getValue();
            
            long winCount = categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null && 
                       t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
            
            BigDecimal winRate = categoryTrades.isEmpty() ? BigDecimal.ZERO :
                BigDecimal.valueOf(winCount * 100.0 / categoryTrades.size()).setScale(2, ROUNDING_MODE);
            
            winRateByCategory.put(category, winRate);
        }
        
        return winRateByCategory;
    }
    
    /**
     * Convert map of trades by category to trade count by category
     */
    private Map<String, Integer> convertToTradeCount(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, Integer> tradeCountByCategory = new HashMap<>();
        
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            tradeCountByCategory.put(entry.getKey(), entry.getValue().size());
        }
        
        return tradeCountByCategory;
    }
}
