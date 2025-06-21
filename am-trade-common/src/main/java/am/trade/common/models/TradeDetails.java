package am.trade.common.models;

import am.trade.common.models.enums.TradePositionType;
import am.trade.common.models.enums.TradeStatus;
import am.trade.common.models.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Model class representing a complete trade within a portfolio
 * A complete trade can consist of multiple trade executions (entries and exits)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeDetails {
    private String tradeId;
    private String portfolioId;
    private String symbol;
    private String strategy;
    
    // Trade status (WIN, LOSS, OPEN, BREAK_EVEN)
    private TradeStatus status;
    
    // Trade position type (LONG, SHORT)
    private TradePositionType tradePositionType;
    
    // Entry and exit details
    private EntryExitInfo entryInfo;
    private EntryExitInfo exitInfo;
    
    // Trade metrics
    private TradeMetrics metrics;
    
    // Associated trade executions
    private List<TradeModel> tradeExecutions;
    
    // Trade notes and tags
    private String notes;
    private List<String> tags;
    
    /**
     * Model representing entry or exit information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EntryExitInfo {
        private LocalDateTime timestamp;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal totalValue;
        private BigDecimal fees;
        private String reason;
    }
    
    /**
     * Model representing trade-specific metrics
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeMetrics {
        // Profit/Loss metrics
        private BigDecimal profitLoss;
        private BigDecimal profitLossPercentage;
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
}
