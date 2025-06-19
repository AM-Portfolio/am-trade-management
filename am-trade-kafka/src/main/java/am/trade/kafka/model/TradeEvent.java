package am.trade.kafka.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
public class TradeEvent extends BaseEvent {
    private String tradeId;
    private String symbol;
    private BigDecimal price;
    private Double quantity;
    private String side; // BUY or SELL
    private String status;
    private String accountId;
}
