package am.trade.models.shared;

import java.math.BigDecimal;

import am.trade.models.shared.enums.TradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ExecutionInfo {
    private TradeType tradeType;
    private String auction;
    private Integer quantity;
    private BigDecimal price;
    private Integer lotSize;
}
