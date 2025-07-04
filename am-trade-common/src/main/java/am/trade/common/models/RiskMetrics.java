package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Model for detailed risk metrics of trading activity
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskMetrics {
    // Drawdown metrics
    private BigDecimal maxDrawdown; // Maximum peak-to-trough decline
    private BigDecimal maxDrawdownPercentage;
    private BigDecimal averageDrawdown;
    private BigDecimal currentDrawdown;
    private int recoveryTime; // Days to recover from max drawdown
    
    // Volatility metrics
    private BigDecimal standardDeviation; // Volatility of returns
    private BigDecimal downside; // Negative deviation
    private BigDecimal upside; // Positive deviation
    
    // Risk-adjusted return metrics
    private BigDecimal sharpeRatio; // Return per unit of risk
    private BigDecimal sortinoRatio; // Return per unit of downside risk
    private BigDecimal calmarRatio; // Return / Max drawdown
    private BigDecimal sterlingRatio; // Return / Average drawdown
    
    // Risk exposure metrics
    private BigDecimal averageRiskPerTrade; // Average risk taken per trade
    private BigDecimal riskRewardRatio; // Average risk to reward ratio
    private BigDecimal valueAtRisk; // Maximum expected loss at a confidence level
    
    // Position sizing metrics
    private BigDecimal averagePositionSize;
    private BigDecimal largestPositionSize;
    private BigDecimal positionSizeVariance; // How consistent position sizing is
    
    // Correlation metrics
    private BigDecimal marketCorrelation; // Correlation to benchmark index
    private BigDecimal sectorCorrelation; // Correlation to sector performance
    
    // Risk of ruin
    private BigDecimal probabilityOfRuin; // Chance of losing all capital
    private int consecutiveLossesToRuin; // How many consecutive losses to deplete capital
    
    // Concentration risk
    private BigDecimal topHoldingPercentage; // Percentage in largest position
    private BigDecimal topFiveConcentration; // Percentage in top 5 positions
}
