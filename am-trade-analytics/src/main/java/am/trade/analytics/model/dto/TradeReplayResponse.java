package am.trade.analytics.model.dto;

import am.trade.common.models.PriceDataPoint;
import am.trade.models.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for trade replay responses
 * Contains the results of a trade replay analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeReplayResponse {
    
    private String replayId;
    private String symbol;
    private LocalDateTime entryDate;
    private LocalDateTime exitDate;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private OrderSide side;
    private Integer positionSize;
    private BigDecimal profitLoss;
    private BigDecimal profitLossPercentage;
    private BigDecimal maxDrawdown;
    private BigDecimal maxDrawdownPercentage;
    private BigDecimal maxProfit;
    private BigDecimal maxProfitPercentage;
    private Integer holdingPeriodDays;
    private BigDecimal volatility;
    private BigDecimal averageDailyMovement;
    private String originalTradeId;
    private String strategyId;
    private String portfolioId;
    private List<PriceDataPoint> priceDataPoints;
    private List<String> replayNotes;
    private LocalDateTime createdDate;
}
