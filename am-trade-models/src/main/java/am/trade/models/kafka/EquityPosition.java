package am.trade.models.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EquityPosition {
    private String symbol;
    private String assetType; // e.g. "EQUITY"
    private BigDecimal quantity;
    private BigDecimal avgBuyingPrice;
    private BigDecimal investmentValue;
    private String isin;
    private String sector;
    private String industry;
    private String marketCap;
}
