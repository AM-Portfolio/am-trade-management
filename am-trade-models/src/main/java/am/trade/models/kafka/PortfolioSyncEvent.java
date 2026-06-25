package am.trade.models.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSyncEvent {
    private String id; // Matches the portfolioId or sync transaction ID
    private String brokerType;
    private String userId;
    private List<EquityPosition> equities;
    private LocalDateTime timestamp;
}
