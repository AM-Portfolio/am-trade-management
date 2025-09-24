package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Model representing portfolio-level metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioMetrics {
    // Trade counts
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private int breakEvenTrades;
    private int openPositions;
    
    // Performance metrics
    private BigDecimal winRate;
    private BigDecimal lossRate;
    private BigDecimal profitFactor;
    private BigDecimal expectancy;
    
    // Value metrics
    private BigDecimal totalValue;
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
    private BigDecimal netProfitLoss;
    private BigDecimal netProfitLossPercentage;
    
    // Risk metrics
    private BigDecimal maxDrawdown;
    private BigDecimal maxDrawdownPercentage;
    private BigDecimal sharpeRatio;
    private BigDecimal sortinoRatio;
    
    // Time-based metrics
    private Map<String, BigDecimal> monthlyReturns;
    private Map<String, BigDecimal> weeklyReturns;
}
