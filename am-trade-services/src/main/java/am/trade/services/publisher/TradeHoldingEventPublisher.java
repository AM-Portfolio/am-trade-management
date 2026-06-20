package am.trade.services.publisher;

import am.trade.models.kafka.TradeHoldingEvent;

/**
 * Interface for publishing trade holding events.
 * Implemented by the Kafka module to break circular dependencies.
 */
public interface TradeHoldingEventPublisher {
    void publishHoldingUpdate(TradeHoldingEvent event);
}
