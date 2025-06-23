package am.trade.services.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Summary statistics for trades in a specific period
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSummary {
    // Identification
    private String portfolioId;
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Trade counts
    private int totalTrades;
    private int winningTrades;
    private int losingTrades;
    private int breakEvenTrades;
    private int openPositions;
    
    // Financial metrics
    private BigDecimal totalProfit;
    private BigDecimal totalLoss;
    private BigDecimal netProfitLoss;
    private BigDecimal winRate;
    private BigDecimal profitFactor;
    private BigDecimal averageWin;
    private BigDecimal averageLoss;
    private BigDecimal largestWin;
    private BigDecimal largestLoss;
    
    // Risk metrics
    private BigDecimal maxDrawdown;
    private BigDecimal riskRewardRatio;
    private BigDecimal returnOnCapital;
    
    // Time metrics
    private double averageHoldingTimeDays;
    private double averageWinningTradeDurationDays;
    private double averageLosingTradeDurationDays;
}
