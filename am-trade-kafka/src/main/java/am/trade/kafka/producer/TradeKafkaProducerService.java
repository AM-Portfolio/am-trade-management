package am.trade.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeKafkaProducerService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    // This is the topic the Gateway is already listening to
    private static final String TRADE_UPDATE_TOPIC = "am-trade-update";

    public void sendTradeUpdate(String userId, Object payload) {
        try {
            // We use userId as the "Key" so Kafka keeps updates for the same user in order
            ProducerRecord<String, Object> record = new ProducerRecord<>(TRADE_UPDATE_TOPIC, userId, payload);

            kafkaTemplate.send(record).whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("[TradeProducer] ✅ Sent update for User: {}", userId);
                } else {
                    log.error("[TradeProducer] ❌ Failed to send for User: {}", userId, ex);
                }
            });
        } catch (Exception e) {
            log.error("[TradeProducer] Error in producer", e);
        }
    }
}
