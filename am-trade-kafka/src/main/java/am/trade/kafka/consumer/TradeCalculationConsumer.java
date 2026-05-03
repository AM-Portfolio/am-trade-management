package am.trade.kafka.consumer;

import am.trade.kafka.service.TradeCalculationService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeCalculationConsumer {

    private final ObjectMapper objectMapper;
    private final TradeCalculationService tradeCalculationService;

    @KafkaListener(
        topics = "am-trigger-calculation",
        groupId = "am-trade-management",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        try {
            log.info("[TradeCalcConsumer] Received trigger: {}", record.value());

            JsonNode jsonNode = objectMapper.readTree(record.value());

            if (jsonNode.has("userId")) {
                String userId = jsonNode.get("userId").asText();
                String portfolioId = jsonNode.has("portfolioId") ? jsonNode.get("portfolioId").asText() : null;

                // Call the service we created in Step 3
                tradeCalculationService.processCalculation(userId, portfolioId);
            }
        } catch (Exception e) {
            log.error("[TradeCalcConsumer] Error processing trigger", e);
        }
    }
}
