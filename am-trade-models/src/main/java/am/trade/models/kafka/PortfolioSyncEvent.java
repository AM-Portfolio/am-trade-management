package am.trade.models.kafka;

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
public class PortfolioSyncEvent {
    /**
     * Globally unique ID for this specific event emission.
     * Set by the producer; used by the consumer for idempotent deduplication.
     * Follows the CloudEvents 'id' specification.
     */
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    /**
     * The service that produced this event.
     * Follows the CloudEvents 'source' specification.
     */
    @Builder.Default
    private String source = "am-trade-management";

    /**
     * Schema version of this event payload.
     * Increment when adding breaking changes to the event structure.
     */
    @Builder.Default
    private String dataVersion = "1.0";

    @Builder.Default
    private String eventType = "TRADE_SYNC";
    
    private String id;           // Portfolio UUID in am-trade-management
    private String portfolioId;  // Portfolio name in am-portfolio
    private String action;       // CREATE, UPDATE, DELETE, DELETE_PORTFOLIO
    private Boolean deleteAllTrades;
    private String brokerType;
    private String userId;
    private List<EquityPosition> equities;
    private LocalDateTime timestamp;
}
