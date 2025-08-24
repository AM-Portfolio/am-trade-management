package am.trade.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import am.trade.kafka.model.TradeUpdateEvent;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import am.trade.common.models.TradeDetails;
import am.trade.common.util.JsonConverter;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "am.trade.kafka.enabled", name = "enabled", havingValue = "true", matchIfMissing = true)
public class TradeConsumerService {

    
    private final JsonConverter jsonConverter;
    private final TradeProcessingService tradeProcessingService;
    private final TradeDetailsService tradeDetailsService;

    @KafkaListener(topics = "${spring.kafka.trade-topic}", 
                  groupId = "${spring.kafka.consumer.group-id}",
                  containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received message: {}", message);
            
            // Convert JSON string to PortfolioUpdateEvent
            TradeUpdateEvent event = jsonConverter.fromJson(message, TradeUpdateEvent.class);
            log.info("Converted to event: {}", event);
              
            // Process the event
            processMessage(event);
            
            // If processing was successful, acknowledge the message
            acknowledgment.acknowledge();
            log.info("Message processed and acknowledged successfully");
        } catch (Exception e) {
            log.error("Failed to process message: {}. Error: {}", message, e.getMessage(), e);
        }
    }

    private void processMessage(TradeUpdateEvent event) {
        log.info("Processing trade update event with {} trades", event.getTrades().size());
        
        List<TradeDetails> tradeDetails = tradeProcessingService.processTradeModels(event.getTrades(), event.getPortfolioId());
        tradeDetailsService.saveAllTradeDetails(tradeDetails);
        tradeProcessingService.processTradeDetails(tradeDetails.stream().map(TradeDetails::getTradeId).collect(Collectors.toList()), event.getPortfolioId(), event.getUserId());
    
        log.info("Successfully processed trades for portfolioId: {}", event.getPortfolioId());
    }   
}
