package am.trade.common.models;

import java.math.BigDecimal;

import am.trade.common.models.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionInfo {
    private TradeType tradeType;
    private String auction;
    private Integer quantity;
    private BigDecimal price;
    private Integer lotSize;
}
