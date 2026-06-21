package am.trade.kafka.consumer;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import am.trade.kafka.model.TradeUpdateEvent;
import am.trade.kafka.service.KafkaIdempotencyService;

import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import am.trade.services.publisher.TradeHoldingEventPublisher;
import am.trade.common.models.TradeDetails;
import am.trade.services.publisher.TradeHoldingEventPublisher;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@ConditionalOnProperty(name = "am.trade.kafka.trade.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class TradeConsumerService {

    /**
     * The actual topic name resolved from application-kafka.yml.
     * Cannot use @RequiredArgsConstructor here because @Value fields
     * must be injected by Spring, not passed through a constructor.
     */

    @Value("${am.trade.kafka.trade.topic:am-trade}")
    private String topicName;

    @Value("${am.trade.kafka.trade.consumer-group-id:am-trade-group}")
    private String consumerGroupId;

    private final ObjectMapper objectMapper;
    private final TradeProcessingService tradeProcessingService;
    private final TradeDetailsService tradeDetailsService;
    /**
     * Publisher that sends TradeHoldingEvents to the am-holding-update topic.
     * Portfolio listens to this topic to recalculate holdings after each trade.
     */
    private final TradeHoldingEventPublisher tradeHoldingEventPublisher;
    
    /**
     * Idempotency guard — prevents duplicate processing when Kafka redelivers messages.
     * See {@link KafkaIdempotencyService} for the full explanation.
     */
    private final KafkaIdempotencyService kafkaIdempotencyService;


    public TradeConsumerService(ObjectMapper objectMapper,
                                TradeProcessingService tradeProcessingService,
                                TradeDetailsService tradeDetailsService,
                                TradeHoldingEventPublisher tradeHoldingEventPublisher,
                                KafkaIdempotencyService kafkaIdempotencyService) {
        this.objectMapper = objectMapper;
        this.tradeProcessingService = tradeProcessingService;
        this.tradeDetailsService = tradeDetailsService;
        this.tradeHoldingEventPublisher = tradeHoldingEventPublisher;
        this.kafkaIdempotencyService = kafkaIdempotencyService;
    }

    @KafkaListener(topics = "${am.trade.kafka.trade.topic}", 
                  groupId = "${am.trade.kafka.trade.consumer-group-id}",
                  containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment acknowledgment) throws Exception {
        log.info("Received message: {}", message);

        // Step 1: Deserialize first so we can extract the message ID for the idempotency check.
        // If this throws (bad JSON), we let it propagate — the DefaultErrorHandler will retry
        // and eventually route the broken message to the DLT topic.
        TradeUpdateEvent event = objectMapper.readValue(message, TradeUpdateEvent.class);
        log.info("Converted to event: {}", event);

        // Step 2: Idempotency check.
        // If this message ID has already been successfully processed (e.g., Kafka redelivery),
        // we skip it and acknowledge immediately. No duplicate trades.
        String messageId = event.getId() != null ? event.getId().toString() : null;
        // Use the @Value-injected fields — NOT hardcoded placeholder strings
        String topic = topicName;
        String groupId = consumerGroupId;

        if (messageId != null && kafkaIdempotencyService.isAlreadyProcessed(messageId)) {
            log.warn("Duplicate trade event detected — skipping. eventId: {}, userId: {}",
                    messageId, event.getUserId());
            acknowledgment.acknowledge();
            return;
        }

        // Step 3: Process the event — business logic lives here.
        // Any exception here will propagate to the DefaultErrorHandler which will:
        //   a) Retry up to 3 times (1 second apart)
        //   b) After all retries fail: publish to the .DLT topic and commit the offset
        processMessage(event);

        // Step 4: Mark as processed AFTER successful processing.
        // This order matters: if we marked it BEFORE processing and then crashed,
        // we would permanently lose this trade (marked as done but never actually saved).
        if (messageId != null) {
            kafkaIdempotencyService.markAsProcessed(messageId, topic, groupId);
        }

        // Step 5: Acknowledge the message (commit the Kafka offset).
        acknowledgment.acknowledge();
        log.info("Trade event processed and acknowledged successfully. eventId: {}", messageId);
    }

    private void processMessage(TradeUpdateEvent event) {
        log.info("Processing trade update event with {} trades for user: {}", event.getTrades().size(), event.getUserId());

        // Step 1: Convert raw TradeModels to TradeDetails and persist them
        List<TradeDetails> tradeDetails = tradeProcessingService.processTradeModels(event.getTrades(), event.getPortfolioId());
        List<TradeDetails> savedTrades = tradeDetailsService.saveAllTradeDetails(tradeDetails);

        // Step 2: Run portfolio aggregation (e.g., compute net position per symbol)
        tradeProcessingService.processTradeDetails(
            savedTrades.stream().map(TradeDetails::getTradeId).collect(Collectors.toList()),
            event.getPortfolioId(),
            event.getUserId()
        );

        // Step 3: Notify Portfolio service via am-portfolio-update topic.
        // Portfolio expects a batch synchronization payload (PortfolioSyncEvent)
        // containing all the relevant equities.

        Map<String, TradeDetails> latestTradePerSymbol = savedTrades.stream()
            .filter(t -> t.getSymbol() != null)
            .collect(Collectors.toMap(
                TradeDetails::getSymbol,
                t -> t,
                (existing, replacement) -> replacement
            ));

        List<am.trade.models.kafka.EquityPosition> equities = latestTradePerSymbol.values().stream()
            .map(trade -> {
                BigDecimal quantity = trade.getEntryInfo() != null && trade.getEntryInfo().getQuantity() != null
                    ? BigDecimal.valueOf(trade.getEntryInfo().getQuantity())
                    : BigDecimal.ZERO;

                BigDecimal price = trade.getEntryInfo() != null && trade.getEntryInfo().getPrice() != null
                    ? trade.getEntryInfo().getPrice()
                    : BigDecimal.ZERO;

                String assetType = trade.getInstrumentInfo() != null && trade.getInstrumentInfo().getSegment() != null 
                    ? trade.getInstrumentInfo().getSegment().name() 
                    : "EQUITY";

                String isin = trade.getInstrumentInfo() != null ? trade.getInstrumentInfo().getIsin() : null;

                return am.trade.models.kafka.EquityPosition.builder()
                    .symbol(trade.getSymbol())
                    .assetType(assetType)
                    .quantity(quantity)
                    .avgBuyingPrice(price)
                    .investmentValue(price.multiply(quantity))
                    .isin(isin)
                    // The Trade database does not store sector, industry, or marketCap natively.
                    // Leaving them null for Portfolio to hydrate or ignore.
                    .sector(null)
                    .industry(null)
                    .marketCap(null)
                    .build();
            })
            .collect(Collectors.toList());

        try {
            am.trade.models.kafka.PortfolioSyncEvent syncEvent = am.trade.models.kafka.PortfolioSyncEvent.builder()
                .id(event.getId() != null ? event.getId().toString() : java.util.UUID.randomUUID().toString())
                .brokerType(event.getBrokerType() != null ? event.getBrokerType().name() : "UNKNOWN")
                .userId(event.getUserId())
                .equities(equities)
                .timestamp(LocalDateTime.now())
                .build();

            log.info("Publishing batch holding update to Portfolio with {} equities. userId: {}",
                     equities.size(), event.getUserId());
                     
            tradeHoldingEventPublisher.publishHoldingUpdate(syncEvent);

        } catch (Exception e) {
            log.error("Failed to publish batch holding update. Trades are still saved.", e);
        }

        log.info("Successfully processed {} trades and notified Portfolio. portfolioId: {}",
                 savedTrades.size(), event.getPortfolioId());
    }
}
