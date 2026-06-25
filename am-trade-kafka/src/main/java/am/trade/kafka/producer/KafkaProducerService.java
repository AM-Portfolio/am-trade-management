package am.trade.kafka.producer;

import am.trade.kafka.model.BaseEvent;
import am.trade.models.kafka.PortfolioSyncEvent;
import am.trade.services.publisher.TradeHoldingEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
public class KafkaProducerService implements TradeHoldingEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${am.trade.kafka.holding-update.topic:am-portfolio}")
    private String holdingUpdateTopic;

    public KafkaProducerService(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public <T extends BaseEvent> void sendEvent(String topic, T event) {
        log.info("Sending event to topic {}: {}", topic, event);
        
        CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(topic, event.getEventId(), event);
        
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.debug("Successfully sent event to topic {}: {}", topic, event);
            } else {
                log.error("Error sending event to topic {}: {}", topic, event, ex);
            }
        });
    }

    @Override
    public void publishHoldingUpdate(PortfolioSyncEvent event) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    sendToKafka(event);
                }
            });
        } else {
            // No active transaction, send immediately
            sendToKafka(event);
        }
    }

    private void sendToKafka(PortfolioSyncEvent event) {
        log.info("Sending holding update event for userId: {}, equities count: {}", 
                 event.getUserId(), event.getEquities() != null ? event.getEquities().size() : 0);

        CompletableFuture<SendResult<String, Object>> future = 
            kafkaTemplate.send(holdingUpdateTopic, event.getId(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Holding update event sent successfully for userId: {}", event.getUserId());
            } else {
                log.error("Failed to send holding update event for userId: {}", 
                          event.getUserId(), ex);
            }
        });
    }
}
