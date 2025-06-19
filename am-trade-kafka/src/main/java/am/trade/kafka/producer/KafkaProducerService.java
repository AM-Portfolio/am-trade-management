package am.trade.kafka.producer;

import am.trade.kafka.model.BaseEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

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
}
