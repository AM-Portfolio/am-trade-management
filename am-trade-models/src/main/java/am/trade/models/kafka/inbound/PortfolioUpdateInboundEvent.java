package am.trade.models.kafka.inbound;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioUpdateInboundEvent {
    private UUID id;
    private String brokerType;
    private String source;
    private String userId;
    private String portfolioId;
    private String name;

    private List<InboundEquityModel> equities;

    private Double totalValue;
    private Double totalInvestment;
    private Double totalGainLoss;
    private Double totalGainLossPercentage;

    private LocalDateTime timestamp;
}
