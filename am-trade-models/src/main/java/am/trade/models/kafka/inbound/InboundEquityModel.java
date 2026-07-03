package am.trade.models.kafka.inbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class InboundEquityModel {
    private UUID id;
    private String symbol;
    private String name;
    private Double quantity;
    private Double avgBuyingPrice;
    private Double currentPrice;
    private Double currentValue;
    private Double investmentValue;
    private Double profitLoss;
    private Double profitLossPercentage;
    
    // Additional equity specific fields mapped loosely if needed
    private String isin;
    private String sector;
}
