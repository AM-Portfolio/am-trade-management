package am.trade.kafka.consumer;

import am.trade.kafka.model.OmsFillEvent;
import am.trade.kafka.service.KafkaIdempotencyService;
import am.trade.kafka.service.OmsFillJournalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(name = "am.trade.kafka.oms-fills.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class OmsFillConsumerService {

    @Value("${am.trade.kafka.oms-fills.topic:am-oms-fills}")
    private String topicName;

    @Value("${am.trade.kafka.oms-fills.consumer-group-id:am-oms-fills-group}")
    private String consumerGroupId;

    private final ObjectMapper objectMapper;
    private final OmsFillJournalService omsFillJournalService;
    private final KafkaIdempotencyService kafkaIdempotencyService;

    public OmsFillConsumerService(ObjectMapper objectMapper,
                                 OmsFillJournalService omsFillJournalService,
                                 KafkaIdempotencyService kafkaIdempotencyService) {
        this.objectMapper = objectMapper;
        this.omsFillJournalService = omsFillJournalService;
        this.kafkaIdempotencyService = kafkaIdempotencyService;
    }

    @KafkaListener(topics = "${am.trade.kafka.oms-fills.topic:am-oms-fills}",
            groupId = "${am.trade.kafka.oms-fills.consumer-group-id:am-oms-fills-group}",
            containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment acknowledgment) throws Exception {
        OmsFillEvent fill = objectMapper.readValue(message, OmsFillEvent.class);
        String messageId = fill.getOrderId() != null ? fill.getOrderId() : fill.getEventId();
        if (messageId != null && kafkaIdempotencyService.isAlreadyProcessed(messageId)) {
            acknowledgment.acknowledge();
            return;
        }
        omsFillJournalService.apply(fill);
        if (messageId != null) {
            kafkaIdempotencyService.markAsProcessed(messageId, topicName, consumerGroupId);
        }
        acknowledgment.acknowledge();
    }
}
