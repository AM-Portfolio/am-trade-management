package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Model representing trade-specific metrics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeMetrics {
    // Profit/Loss metrics
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    /** 1D P&L vs previous close (Portfolio-equivalent day move). */
    private BigDecimal todayProfitLoss;
    private BigDecimal todayProfitLossPercentage;
    /**
     * COST = vs avg entry; IMPORT_MARK = vs statement/import LTP when cost unknown;
     * DAY_ONLY = only 1D filled (no overall baseline).
     */
    private String unrealizedBasis;
    private BigDecimal returnOnEquity;
    
    // Risk metrics
    private BigDecimal riskAmount;
    private BigDecimal rewardAmount;
    private BigDecimal riskRewardRatio;
    
    // Time metrics
    private Long holdingTimeDays;
    private Long holdingTimeHours;
    private Long holdingTimeMinutes;
    
    // Maximum values during trade
    private BigDecimal maxAdverseExcursion;  // Maximum loss during trade
    private BigDecimal maxFavorableExcursion;  // Maximum profit during trade
}
