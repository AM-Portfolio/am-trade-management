package am.trade.services.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import am.trade.models.enums.OrderSide;
import am.trade.models.enums.OrderStatus;
import am.trade.models.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for Trade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeDTO {
    
    private String id;
    private String tradeId;
    private String orderId;
    private String symbol;
    private LocalDateTime tradeDate;
    private LocalDateTime settlementDate;
    private OrderSide side;
    private OrderType type;
    private OrderStatus status;
    private BigDecimal quantity;
    private BigDecimal price;
    private BigDecimal totalValue;
    private String executionVenue;
    private String counterpartyId;
    private String portfolioId;
    private String strategyId;
    private String traderId;
    private BigDecimal commissionFee;
    private BigDecimal otherFees;
    private String notes;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdBy;
    private String lastModifiedBy;
}
