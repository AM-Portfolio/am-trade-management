package am.trade.models.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import am.trade.models.base.BaseDocument;
import am.trade.models.enums.OrderSide;
import am.trade.models.enums.OrderStatus;
import am.trade.models.enums.OrderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Document model for trades
 */
@Document(collection = "trades")
@CompoundIndex(name = "idx_symbol_trade_date", def = "{'symbol': 1, 'trade_date': 1}")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trade extends BaseDocument {
    
    @Indexed
    @Field("trade_id")
    private String tradeId;
    
    @Indexed
    @Field("order_id")
    private String orderId;
    
    @Indexed
    private String symbol;
    
    @Field("trade_date")
    private LocalDateTime tradeDate;
    
    @Field("settlement_date")
    private LocalDateTime settlementDate;
    
    private OrderSide side;
    
    private OrderType type;
    
    private OrderStatus status;
    
    private BigDecimal quantity;
    
    private BigDecimal price;
    
    @Field("total_value")
    private BigDecimal totalValue;
    
    @Field("execution_venue")
    private String executionVenue;
    
    @Field("counterparty_id")
    private String counterpartyId;
    
    @Field("portfolio_id")
    private String portfolioId;
    
    @Field("strategy_id")
    private String strategyId;
    
    @Field("trader_id")
    private String traderId;
    
    @Field("commission_fee")
    private BigDecimal commissionFee;
    
    @Field("other_fees")
    private BigDecimal otherFees;
    
    private List<String> notes;
}
