package am.trade.dashboard.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeTimingMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating trade timing metrics from trade data
 */
@Service
@Slf4j
public class TradeTimingMetricsService {

    private static final int SCALE = 4;
    private static final java.math.RoundingMode ROUNDING_MODE = HALF_UP;

    /**
     * Calculate timing metrics from a list of trades
     */
    public TradeTimingMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new TradeTimingMetrics();
        }
        
        TradeTimingMetrics metrics = new TradeTimingMetrics();
        
        // Sort trades by date for chronological analysis
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Calculate entry timing quality
        BigDecimal totalEntryQuality = BigDecimal.ZERO;
        int entryQualityCount = 0;
        
        // Calculate exit timing quality
        BigDecimal totalExitQuality = BigDecimal.ZERO;
        int exitQualityCount = 0;
        
        // Calculate timing improvement over time
        List<BigDecimal> entryQualityTrend = new ArrayList<>();
        List<BigDecimal> exitQualityTrend = new ArrayList<>();
        
        // Process each trade for timing metrics
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() == null) {
                continue;
            }
            
            // Calculate entry timing quality based on MAE (Maximum Adverse Excursion)
            if (trade.getMetrics().getMaxAdverseExcursion() != null && 
                trade.getMetrics().getProfitLoss() != null) {
                
                BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
                BigDecimal mae = trade.getMetrics().getMaxAdverseExcursion();
                
                // Entry quality: how close to the optimal entry point (lower MAE is better)
                // Scale from 0-100 where 100 is perfect timing
                BigDecimal entryQuality;
                if (profitLoss.compareTo(BigDecimal.ZERO) > 0) {
                    // For winning trades, lower MAE relative to profit is better
                    if (mae.compareTo(BigDecimal.ZERO) == 0) {
                        entryQuality = BigDecimal.valueOf(100); // Perfect entry
                    } else {
                        BigDecimal ratio = mae.divide(profitLoss.add(mae), SCALE, ROUNDING_MODE);
                        entryQuality = BigDecimal.valueOf(100).multiply(BigDecimal.ONE.subtract(ratio));
                    }
                } else {
                    // For losing trades, any MAE is bad
                    entryQuality = BigDecimal.valueOf(25); // Base score for losing trades
                }
                
                totalEntryQuality = totalEntryQuality.add(entryQuality);
                entryQualityCount++;
                entryQualityTrend.add(entryQuality);
            }
            
            // Calculate exit timing quality based on MFE (Maximum Favorable Excursion)
            if (trade.getMetrics().getMaxFavorableExcursion() != null && 
                trade.getMetrics().getProfitLoss() != null) {
                
                BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
                BigDecimal mfe = trade.getMetrics().getMaxFavorableExcursion();
                
                // Exit quality: how close to the optimal exit point (higher % of MFE captured is better)
                // Scale from 0-100 where 100 is perfect timing
                BigDecimal exitQuality;
                if (mfe.compareTo(BigDecimal.ZERO) > 0) {
                    // Calculate percentage of maximum potential profit captured
                    BigDecimal capturedRatio = profitLoss.divide(mfe, SCALE, ROUNDING_MODE);
                    exitQuality = BigDecimal.valueOf(100).multiply(capturedRatio);
                    // Cap at 100
                    exitQuality = exitQuality.min(BigDecimal.valueOf(100));
                } else {
                    exitQuality = BigDecimal.valueOf(50); // Neutral if no favorable excursion
                }
                
                totalExitQuality = totalExitQuality.add(exitQuality);
                exitQualityCount++;
                exitQualityTrend.add(exitQuality);
            }
        }
        
        // Calculate average entry and exit quality
        BigDecimal averageEntryQuality = entryQualityCount > 0 ?
                totalEntryQuality.divide(BigDecimal.valueOf(entryQualityCount), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        BigDecimal averageExitQuality = exitQualityCount > 0 ?
                totalExitQuality.divide(BigDecimal.valueOf(exitQualityCount), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate timing improvement trend
        BigDecimal entryTimingImprovement = calculateTrendSlope(entryQualityTrend);
        BigDecimal exitTimingImprovement = calculateTrendSlope(exitQualityTrend);
        
        // Calculate overall timing score
        BigDecimal overallTimingScore = averageEntryQuality.add(averageExitQuality)
                .divide(BigDecimal.valueOf(2), SCALE, ROUNDING_MODE);
        
        // Set calculated metrics
        metrics.setAverageEntryEfficiency(averageEntryQuality);
        metrics.setAverageExitEfficiency(averageExitQuality);
        metrics.setTimingImprovementTrend(entryTimingImprovement);
        metrics.setTimingImprovementTrend(exitTimingImprovement);
        
        return metrics;
    }
    
    /**
     * Calculate trend slope from a list of values
     * Positive value means improving trend, negative means deteriorating
     */
    private BigDecimal calculateTrendSlope(List<BigDecimal> values) {
        if (values == null || values.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        int n = values.size();
        
        // Simple linear regression slope calculation
        BigDecimal sumX = BigDecimal.ZERO;
        BigDecimal sumY = BigDecimal.ZERO;
        BigDecimal sumXY = BigDecimal.ZERO;
        BigDecimal sumXX = BigDecimal.ZERO;
        
        for (int i = 0; i < n; i++) {
            BigDecimal x = BigDecimal.valueOf(i + 1);
            BigDecimal y = values.get(i);
            
            sumX = sumX.add(x);
            sumY = sumY.add(y);
            sumXY = sumXY.add(x.multiply(y));
            sumXX = sumXX.add(x.multiply(x));
        }
        
        BigDecimal slope = sumXY.multiply(BigDecimal.valueOf(n))
            .subtract(sumX.multiply(sumY))
            .divide(
                sumXX.multiply(BigDecimal.valueOf(n))
                .subtract(sumX.multiply(sumX)), 
                SCALE, ROUNDING_MODE
            );
        
        return slope;
    }
}
