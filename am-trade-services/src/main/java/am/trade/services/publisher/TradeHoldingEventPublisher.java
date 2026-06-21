package am.trade.services.publisher;

import am.trade.models.kafka.PortfolioSyncEvent;

/**
 * Interface for publishing trade holding updates to Kafka
 */
public interface TradeHoldingEventPublisher {
    void publishHoldingUpdate(PortfolioSyncEvent event);
}
