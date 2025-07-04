package am.trade.services.service.metrics;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating performance metrics from trade data
 */
@Service
@Slf4j
public class PerformanceMetricsService {

    private static final int SCALE = 4;
    private static final java.math.RoundingMode ROUNDING_MODE = HALF_UP;

    /**
     * Calculate performance metrics from a list of trades
     */
    public PerformanceMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new PerformanceMetrics();
        }
        
        PerformanceMetrics metrics = new PerformanceMetrics();
        
        // Calculate profitability metrics
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        BigDecimal totalWinAmount = BigDecimal.ZERO;
        BigDecimal totalLossAmount = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        int breakEvenCount = 0;
        
        // Track consecutive wins/losses for streaks
        int currentWinStreak = 0;
        int currentLossStreak = 0;
        int maxWinStreak = 0;
        int maxLossStreak = 0;
        
        // Sort trades by date for chronological analysis
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Calculate total trading days
        LocalDateTime firstTradeDate = sortedTrades.get(0).getEntryInfo().getTimestamp();
        LocalDateTime lastTradeDate = sortedTrades.get(sortedTrades.size() - 1).getEntryInfo().getTimestamp();
        long totalTradingDays = Duration.between(firstTradeDate, lastTradeDate).toDays() + 1;
        
        // Calculate total invested amount for ROI
        BigDecimal totalInvestedAmount = BigDecimal.ZERO;
        
        // Process each trade
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() == null) {
                continue;
            }
            
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            if (profitLoss == null) {
                continue;
            }
            
            // Add to total P/L
            totalProfitLoss = totalProfitLoss.add(profitLoss);
            
            // Calculate position size if available
            if (trade.getEntryInfo() != null && trade.getEntryInfo().getPrice() != null && 
                trade.getEntryInfo().getQuantity() != null) {
                BigDecimal positionSize = trade.getEntryInfo().getPrice()
                    .multiply(new BigDecimal(trade.getEntryInfo().getQuantity().toString()));
                totalInvestedAmount = totalInvestedAmount.add(positionSize);
            }
            
            // Track wins and losses
            int comparison = profitLoss.compareTo(BigDecimal.ZERO);
            if (comparison > 0) {
                // Win
                winCount++;
                totalWinAmount = totalWinAmount.add(profitLoss);
                currentWinStreak++;
                currentLossStreak = 0;
                maxWinStreak = Math.max(maxWinStreak, currentWinStreak);
            } else if (comparison < 0) {
                // Loss
                lossCount++;
                totalLossAmount = totalLossAmount.add(profitLoss.abs());
                currentLossStreak++;
                currentWinStreak = 0;
                maxLossStreak = Math.max(maxLossStreak, currentLossStreak);
            } else {
                // Break even
                breakEvenCount++;
                currentWinStreak = 0;
                currentLossStreak = 0;
            }
        }
        
        // Calculate total trades
        int totalTrades = trades.size();
        
        // Calculate win rate
        BigDecimal winRate = totalTrades > 0 ? 
                BigDecimal.valueOf(winCount * 100.0 / totalTrades).setScale(2, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        // Calculate average win and loss
        BigDecimal averageWin = winCount > 0 ? 
                totalWinAmount.divide(BigDecimal.valueOf(winCount), SCALE, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        BigDecimal averageLoss = lossCount > 0 ? 
                totalLossAmount.divide(BigDecimal.valueOf(lossCount), SCALE, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        // Calculate profit factor
        BigDecimal profitFactor = totalLossAmount.compareTo(BigDecimal.ZERO) > 0 ? 
                totalWinAmount.divide(totalLossAmount, SCALE, ROUNDING_MODE) : 
                BigDecimal.ZERO;
        
        // Calculate expectancy
        BigDecimal expectancy = BigDecimal.ZERO;
        if (totalTrades > 0) {
            BigDecimal winProbability = BigDecimal.valueOf((double) winCount / totalTrades);
            BigDecimal lossProbability = BigDecimal.valueOf((double) lossCount / totalTrades);
            
            expectancy = winProbability.multiply(averageWin)
                    .subtract(lossProbability.multiply(averageLoss))
                    .setScale(SCALE, ROUNDING_MODE);
        }
        
        // Calculate ROI
        BigDecimal roi = totalInvestedAmount.compareTo(BigDecimal.ZERO) > 0 ?
                totalProfitLoss.divide(totalInvestedAmount, SCALE, ROUNDING_MODE)
                    .multiply(BigDecimal.valueOf(100)).setScale(2, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate average trades per day
        BigDecimal tradesPerDay = totalTradingDays > 0 ?
                BigDecimal.valueOf((double) totalTrades / totalTradingDays).setScale(2, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate average holding time
        BigDecimal totalHoldingHours = BigDecimal.ZERO;
        int tradesWithHoldingTime = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getEntryInfo() != null && trade.getExitInfo() != null &&
                trade.getEntryInfo().getTimestamp() != null && trade.getExitInfo().getTimestamp() != null) {
                
                Duration holdingTime = Duration.between(
                    trade.getEntryInfo().getTimestamp(), 
                    trade.getExitInfo().getTimestamp()
                );
                
                totalHoldingHours = totalHoldingHours.add(
                    BigDecimal.valueOf(holdingTime.toHours())
                );
                tradesWithHoldingTime++;
            }
        }
        
        BigDecimal averageHoldingTimeHours = tradesWithHoldingTime > 0 ?
                totalHoldingHours.divide(BigDecimal.valueOf(tradesWithHoldingTime), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Set all calculated metrics
        // Core performance metrics
        metrics.setTotalProfitLoss(totalProfitLoss);
        metrics.setWinRate(winRate);
        metrics.setProfitFactor(profitFactor);
        metrics.setExpectancy(expectancy);
        
        // Consistency metrics
        metrics.setAverageWinningTrade(averageWin);
        metrics.setAverageLosingTrade(averageLoss);
        
        // Streak information
        metrics.setLongestWinningStreak(maxWinStreak);
        metrics.setLongestLosingStreak(maxLossStreak);
        
        // Time metrics
        metrics.setAverageHoldingTimeOverall(averageHoldingTimeHours);
        
        // Efficiency metrics
        metrics.setReturnOnCapital(roi);
        
        return metrics;
    }
}
