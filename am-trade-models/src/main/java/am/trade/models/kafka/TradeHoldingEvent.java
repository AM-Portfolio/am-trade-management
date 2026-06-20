package am.trade.models.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Event produced by Trade Management to notify Portfolio
 * about a new or updated trade (holding change).
 * Field names deliberately match StockHoldingUpdateEvent in portfolio.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TradeHoldingEvent {
    private String id;              // tradeId from Trade
    private String userId;          // ID of the user who made the trade
    private String portfolioId;     // portfolioId from Trade
    private String symbol;          // stock symbol e.g. "RELIANCE"
    private BigDecimal quantity;    // trade quantity (Integer → BigDecimal)
    private BigDecimal averagePrice; // trade price
    private BigDecimal investmentAmount; // price × quantity
    private LocalDateTime timestamp;
    private String updateType;      // "ADD" | "UPDATE" | "REMOVE"

    // Performance metrics — left null by trade side; portfolio calculates these
    private Double overallGainLoss;
    private Double overallGainLossPercentage;
    private Double totalGainLoss;
    private Double totalGainLossPercentage;
}
