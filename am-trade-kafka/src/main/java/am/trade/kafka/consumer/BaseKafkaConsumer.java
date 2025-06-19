package am.trade.kafka.consumer;

import am.trade.kafka.model.BaseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
public abstract class BaseKafkaConsumer<T extends BaseEvent> {

    private final ObjectMapper objectMapper;
    private final Class<T> eventType;

    protected BaseKafkaConsumer(ObjectMapper objectMapper, Class<T> eventType) {
        this.objectMapper = objectMapper;
        this.eventType = eventType;
    }

    @KafkaListener(topics = "#{__listener.topic}", groupId = "#{__listener.groupId}")
    public void consume(@Payload String message, Acknowledgment ack) {
        try {
            T event = objectMapper.readValue(message, eventType);
            log.debug("Received event: {}", event);
            processEvent(event);
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Error processing message: {}", message, e);
            // Handle deserialization error (e.g., send to DLQ)
        } catch (Exception e) {
            log.error("Error processing event: {}", message, e);
            // Handle processing error
        }
    }

    protected abstract String getTopic();
    protected abstract String getGroupId();
    protected abstract void processEvent(T event);
}
