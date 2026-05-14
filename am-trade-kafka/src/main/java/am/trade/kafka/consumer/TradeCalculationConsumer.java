package am.trade.kafka.consumer;

import am.trade.kafka.service.TradeCalculationService;
import com.am.observability.flow.FlowLogger;
import com.am.observability.flow.FlowSpan;
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
    private final FlowLogger flowLogger;

    @KafkaListener(
        topics = "am-trigger-calculation",
        groupId = "am-trade-management",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void listen(ConsumerRecord<String, String> record) {
        try (FlowSpan span = flowLogger.start("trade.kafka.consume.trigger_calculation",
                "topic", record.topic(),
                "partition", record.partition(),
                "offset", record.offset(),
                "key", record.key(),
                "payload_bytes", record.value() == null ? 0 : record.value().length())) {
            try {
                log.info("[TradeCalcConsumer] Received trigger: {}", record.value());

                JsonNode jsonNode = objectMapper.readTree(record.value());

                if (jsonNode.has("userId")) {
                    String userId = jsonNode.get("userId").asText();
                    String portfolioId = jsonNode.has("portfolioId") ? jsonNode.get("portfolioId").asText() : null;

                    // Call the service we created in Step 3
                    tradeCalculationService.processCalculation(userId, portfolioId);
                    flowLogger.complete(span, "userId", userId, "portfolioId", portfolioId);
                } else {
                    flowLogger.fail(span, null, "reason", "missing_userId");
                }
            } catch (Exception e) {
                flowLogger.fail(span, e);
                log.error("[TradeCalcConsumer] Error processing trigger", e);
            }
        }
    }
}
