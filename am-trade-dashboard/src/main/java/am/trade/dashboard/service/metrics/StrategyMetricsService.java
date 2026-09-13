package am.trade.dashboard.service.metrics;

import am.trade.common.models.StrategyPerformanceMetrics;
import am.trade.common.models.TradeDetails;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for calculating strategy-specific metrics from trade data
 */
@Service
@Slf4j
public class StrategyMetricsService {

    private static final int SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * Calculate metrics grouped by strategy
     */
    public Map<String, StrategyPerformanceMetrics> calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return new HashMap<>();
        }

        Map<String, StrategyPerformanceMetrics> result = new HashMap<>();

        // Group trades by strategy
        Map<String, List<TradeDetails>> tradesByStrategy = trades.stream()
                .filter(t -> t.getStrategy() != null && !t.getStrategy().trim().isEmpty())
                .collect(Collectors.groupingBy(TradeDetails::getStrategy));

        for (Map.Entry<String, List<TradeDetails>> entry : tradesByStrategy.entrySet()) {
            String strategyName = entry.getKey();
            List<TradeDetails> strategyTrades = entry.getValue();
            
            result.put(strategyName, calculateMetricsForStrategy(strategyName, strategyTrades));
        }

        return result;
    }

    private StrategyPerformanceMetrics calculateMetricsForStrategy(String strategyName, List<TradeDetails> trades) {
        StrategyPerformanceMetrics metrics = new StrategyPerformanceMetrics();
        metrics.setStrategyName(strategyName);
        
        if (trades == null || trades.isEmpty()) {
            return metrics;
        }
        
        int totalTrades = trades.size();
        int winningTrades = 0;
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        BigDecimal totalProfitLoss = BigDecimal.ZERO;

        for (TradeDetails trade : trades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                BigDecimal pl = trade.getMetrics().getProfitLoss();
                totalProfitLoss = totalProfitLoss.add(pl);
                
                if (pl.compareTo(BigDecimal.ZERO) > 0) {
                    winningTrades++;
                    totalProfit = totalProfit.add(pl);
                } else if (pl.compareTo(BigDecimal.ZERO) < 0) {
                    totalLoss = totalLoss.add(pl.abs());
                }
            }
        }
        
        // Total P&L
        metrics.setTotalProfitLoss(totalProfitLoss);
        
        // Win Rate
        BigDecimal winRate = BigDecimal.valueOf(winningTrades)
                .divide(BigDecimal.valueOf(totalTrades), SCALE, ROUNDING_MODE)
                .multiply(BigDecimal.valueOf(100));
        metrics.setWinRate(winRate);
        
        // Profit Factor
        BigDecimal profitFactor = BigDecimal.ZERO;
        if (totalLoss.compareTo(BigDecimal.ZERO) > 0) {
            profitFactor = totalProfit.divide(totalLoss, SCALE, ROUNDING_MODE);
        } else if (totalProfit.compareTo(BigDecimal.ZERO) > 0) {
            profitFactor = BigDecimal.valueOf(999.99); // Arbitrary high number if no losses
        }
        metrics.setProfitFactor(profitFactor);
        
        // Expectancy (Average P&L per trade)
        BigDecimal expectancy = totalProfitLoss.divide(BigDecimal.valueOf(totalTrades), SCALE, ROUNDING_MODE);
        metrics.setExpectancy(expectancy);
        
        // Stub values for more complex metrics
        metrics.setSharpeRatio(BigDecimal.ZERO);
        metrics.setMaxDrawdown(BigDecimal.ZERO);
        metrics.setConsistencyScore(BigDecimal.valueOf(50));
        metrics.setAdaptabilityScore(BigDecimal.valueOf(50));
        
        return metrics;
    }
}
