package am.trade.dashboard.service.metrics;

import am.trade.common.models.RiskMetrics;
import am.trade.common.models.TradeDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating risk metrics from trade data
 */
@Service
@Slf4j
public class RiskMetricsService {

    private static final int SCALE = 4;
    private static final java.math.RoundingMode ROUNDING_MODE = HALF_UP;

    /**
     * Calculate risk metrics from a list of trades
     */
    public RiskMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new RiskMetrics();
        }
        
        RiskMetrics metrics = new RiskMetrics();
        
        // Sort trades by date for chronological analysis
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        // Calculate drawdown metrics
        BigDecimal cumulativeProfitLoss = BigDecimal.ZERO;
        BigDecimal maxCumulativeProfitLoss = BigDecimal.ZERO;
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal currentDrawdown = BigDecimal.ZERO;
        
        // For Calmar ratio calculation
        BigDecimal annualReturn = BigDecimal.ZERO;
        
        // For volatility calculation
        List<BigDecimal> dailyReturns = new ArrayList<>();
        Map<LocalDateTime, BigDecimal> dailyProfitLoss = new HashMap<>();
        
        // For risk of ruin
        int consecutiveLosses = 0;
        int maxConsecutiveLosses = 0;
        BigDecimal largestLoss = BigDecimal.ZERO;
        
        // For position sizing analysis
        BigDecimal totalPositionSize = BigDecimal.ZERO;
        int positionSizeCount = 0;
        BigDecimal maxPositionSize = BigDecimal.ZERO;
        List<BigDecimal> positionSizes = new ArrayList<>();
        
        // Process each trade for drawdown and other metrics
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null) {
                continue;
            }
            
            BigDecimal profitLoss = trade.getMetrics().getProfitLoss();
            
            // Calculate position size if available
            if (trade.getEntryInfo() != null && trade.getEntryInfo().getPrice() != null && 
                trade.getEntryInfo().getQuantity() != null) {
                BigDecimal positionSize = trade.getEntryInfo().getPrice()
                    .multiply(BigDecimal.valueOf(trade.getEntryInfo().getQuantity().doubleValue()));
                
                totalPositionSize = totalPositionSize.add(positionSize);
                positionSizeCount++;
                maxPositionSize = maxPositionSize.max(positionSize);
                positionSizes.add(positionSize);
                
                // Store daily P/L for volatility calculation
                LocalDateTime tradeDate = trade.getEntryInfo().getTimestamp().toLocalDate().atStartOfDay();
                dailyProfitLoss.merge(tradeDate, profitLoss, BigDecimal::add);
            }
            
            // Update cumulative P/L
            cumulativeProfitLoss = cumulativeProfitLoss.add(profitLoss);
            
            // Update maximum cumulative P/L
            if (cumulativeProfitLoss.compareTo(maxCumulativeProfitLoss) > 0) {
                maxCumulativeProfitLoss = cumulativeProfitLoss;
                currentDrawdown = BigDecimal.ZERO;
            } else {
                // Calculate current drawdown
                currentDrawdown = maxCumulativeProfitLoss.subtract(cumulativeProfitLoss);
                // Update max drawdown if current is larger
                if (currentDrawdown.compareTo(maxDrawdown) > 0) {
                    maxDrawdown = currentDrawdown;
                }
            }
            
            // Track consecutive losses for risk of ruin
            if (profitLoss.compareTo(BigDecimal.ZERO) < 0) {
                consecutiveLosses++;
                // Track largest loss
                if (profitLoss.abs().compareTo(largestLoss) > 0) {
                    largestLoss = profitLoss.abs();
                }
            } else {
                maxConsecutiveLosses = Math.max(maxConsecutiveLosses, consecutiveLosses);
                consecutiveLosses = 0;
            }
        }
        
        // Finalize max consecutive losses check
        maxConsecutiveLosses = Math.max(maxConsecutiveLosses, consecutiveLosses);
        
        // Calculate average position size
        BigDecimal averagePositionSize = positionSizeCount > 0 ?
                totalPositionSize.divide(BigDecimal.valueOf(positionSizeCount), SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate position size standard deviation for consistency
        BigDecimal positionSizeVariance = BigDecimal.ZERO;
        if (positionSizeCount > 1) {
            for (BigDecimal size : positionSizes) {
                BigDecimal diff = size.subtract(averagePositionSize);
                positionSizeVariance = positionSizeVariance.add(diff.multiply(diff));
            }
            positionSizeVariance = positionSizeVariance.divide(
                BigDecimal.valueOf(positionSizeCount - 1), SCALE, ROUNDING_MODE);
        }
        BigDecimal positionSizeStdDev = sqrt(positionSizeVariance);
        
        // Calculate daily returns for volatility
        for (BigDecimal dailyPL : dailyProfitLoss.values()) {
            dailyReturns.add(dailyPL);
        }
        
        // Calculate volatility (standard deviation of returns)
        BigDecimal volatility = calculateStandardDeviation(dailyReturns);
        
        // Calculate Sharpe Ratio (assuming risk-free rate of 0 for simplicity)
        BigDecimal averageDailyReturn = calculateAverage(dailyReturns);
        BigDecimal sharpeRatio = volatility.compareTo(BigDecimal.ZERO) > 0 ?
                averageDailyReturn.divide(volatility, SCALE, ROUNDING_MODE)
                    .multiply(BigDecimal.valueOf(Math.sqrt(252))) : // Annualized
                BigDecimal.ZERO;
        
        // Calculate Sortino Ratio (only considering negative returns for downside risk)
        List<BigDecimal> negativeReturns = dailyReturns.stream()
                .filter(r -> r.compareTo(BigDecimal.ZERO) < 0)
                .collect(Collectors.toList());
        
        BigDecimal downsideDeviation = calculateStandardDeviation(negativeReturns);
        BigDecimal sortinoRatio = downsideDeviation.compareTo(BigDecimal.ZERO) > 0 ?
                averageDailyReturn.divide(downsideDeviation, SCALE, ROUNDING_MODE)
                    .multiply(BigDecimal.valueOf(Math.sqrt(252))) :
                BigDecimal.ZERO;
        
        // Calculate Calmar Ratio (annualized return / max drawdown)
        BigDecimal calmarRatio = maxDrawdown.compareTo(BigDecimal.ZERO) > 0 ?
                annualReturn.divide(maxDrawdown, SCALE, ROUNDING_MODE) :
                BigDecimal.ZERO;
        
        // Calculate risk of ruin based on win rate, risk-reward ratio and position sizing
        BigDecimal winRate = BigDecimal.ZERO;
        BigDecimal lossRate = BigDecimal.ZERO;
        int winCount = 0;
        
        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                if (trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0) {
                    winCount++;
                }
            }
        }
        
        if (!trades.isEmpty()) {
            winRate = BigDecimal.valueOf((double) winCount / trades.size());
            lossRate = BigDecimal.ONE.subtract(winRate);
        }
        
        // Simple risk of ruin calculation (simplified version)
        BigDecimal riskOfRuin = BigDecimal.ONE;
        if (winRate.compareTo(lossRate) > 0) {
            // Only calculate if win rate > loss rate to avoid negative values
            BigDecimal ratio = lossRate.divide(winRate, SCALE, ROUNDING_MODE);
            // Simplified formula for 50 trades
            riskOfRuin = ratio.pow(50).setScale(SCALE, ROUNDING_MODE);
        }
        
        // Set calculated metrics
        metrics.setMaxDrawdown(maxDrawdown);
        metrics.setSharpeRatio(sharpeRatio);
        metrics.setSortinoRatio(sortinoRatio);
        metrics.setCalmarRatio(calmarRatio);
        metrics.setAveragePositionSize(averagePositionSize);
        metrics.setConsecutiveLossesToRuin(maxConsecutiveLosses);
        metrics.setLargestPositionSize(largestLoss);
        
        // Set additional fields if they exist in the RiskMetrics class
        try {
            metrics.getClass().getMethod("setVolatility", BigDecimal.class).invoke(metrics, volatility);
            metrics.getClass().getMethod("setMaxPositionSize", BigDecimal.class).invoke(metrics, maxPositionSize);
            metrics.getClass().getMethod("setPositionSizeConsistency", BigDecimal.class).invoke(metrics, positionSizeStdDev);
            metrics.getClass().getMethod("setRiskOfRuin", BigDecimal.class).invoke(metrics, riskOfRuin);
        } catch (Exception e) {
            log.warn("Some risk metrics fields are not available in the RiskMetrics class: {}", e.getMessage());
        }
        
        return metrics;
    }
    
    /**
     * Calculate square root of a BigDecimal
     */
    private BigDecimal sqrt(BigDecimal value) {
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal x = BigDecimal.valueOf(Math.sqrt(value.doubleValue()));
        return x.setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate standard deviation of a list of BigDecimal values
     */
    private BigDecimal calculateStandardDeviation(List<BigDecimal> values) {
        if (values == null || values.isEmpty() || values.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal mean = calculateAverage(values);
        BigDecimal variance = BigDecimal.ZERO;
        
        for (BigDecimal value : values) {
            BigDecimal diff = value.subtract(mean);
            variance = variance.add(diff.multiply(diff));
        }
        
        variance = variance.divide(BigDecimal.valueOf(values.size() - 1), SCALE, ROUNDING_MODE);
        return sqrt(variance);
    }
    
    /**
     * Calculate average of a list of BigDecimal values
     */
    private BigDecimal calculateAverage(List<BigDecimal> values) {
        if (values == null || values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal sum = values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return sum.divide(BigDecimal.valueOf(values.size()), SCALE, ROUNDING_MODE);
    }
}
