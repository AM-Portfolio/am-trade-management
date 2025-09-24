package am.trade.persistence.entity;

import am.trade.common.models.PriceDataPoint;
import am.trade.models.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Document model for trade replay analysis
 * Stores information about trade replays including entry and exit points
 */
@Document(collection = "trade_replays")
@CompoundIndex(name = "idx_symbol_entry_exit", def = "{'symbol': 1, 'entry_date': 1, 'exit_date': 1}")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeReplay {

    @Id
    private String id;
    
    @Indexed
    @Field("replay_id")
    private String replayId;
    
    @Indexed
    private String symbol;
    
    @Field("entry_date")
    private LocalDateTime entryDate;
    
    @Field("exit_date")
    private LocalDateTime exitDate;
    
    @Field("entry_price")
    private BigDecimal entryPrice;
    
    @Field("exit_price")
    private BigDecimal exitPrice;
    
    private OrderSide side;
    
    @Field("position_size")
    private Integer positionSize;
    
    @Field("profit_loss")
    private BigDecimal profitLoss;
    
    @Field("profit_loss_percentage")
    private BigDecimal profitLossPercentage;
    
    @Field("max_drawdown")
    private BigDecimal maxDrawdown;
    
    @Field("max_drawdown_percentage")
    private BigDecimal maxDrawdownPercentage;
    
    @Field("max_profit")
    private BigDecimal maxProfit;
    
    @Field("max_profit_percentage")
    private BigDecimal maxProfitPercentage;
    
    @Field("holding_period_days")
    private Integer holdingPeriodDays;
    
    @Field("volatility")
    private BigDecimal volatility;
    
    @Field("average_daily_movement")
    private BigDecimal averageDailyMovement;
    
    @Field("price_data_points")
    private List<PriceDataPoint> priceDataPoints;
    
    @Field("replay_notes")
    private List<String> replayNotes;
    
    @Field("original_trade_id")
    private String originalTradeId;
    
    @Field("strategy_id")
    private String strategyId;
    
    @Field("portfolio_id")
    private String portfolioId;
    
    @Field("created_date")
    private LocalDateTime createdDate;
    
    @Field("last_modified_date")
    private LocalDateTime lastModifiedDate;
}
