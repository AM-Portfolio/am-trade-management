package am.trade.kafka.model;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import am.trade.common.models.TradeModel;
import am.trade.common.models.enums.BrokerType;
import am.trade.common.models.enums.FNOTradeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event model for trade updates to be sent via Kafka
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeUpdateEvent {
    private UUID id;
    private String userId;
    private BrokerType brokerType;
    private LocalDateTime timestamp;
    private FNOTradeType tradeType; // FNO or EQUITY
    private List<TradeModel> trades;
}
