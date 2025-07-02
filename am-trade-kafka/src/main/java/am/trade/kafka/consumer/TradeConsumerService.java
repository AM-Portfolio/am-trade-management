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

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "am.trade.kafka.trade.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class TradeConsumerService {

    
    private final ObjectMapper objectMapper;
    private final TradeProcessingService tradeProcessingService;
    private final TradeDetailsService tradeDetailsService;

    @KafkaListener(topics = "${am.trade.kafka.trade.topic}", 
                  groupId = "${am.trade.kafka.trade.consumer-group-id}",
                  containerFactory = "kafkaListenerContainerFactory")
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received message: {}", message);
            
            // Convert JSON string to PortfolioUpdateEvent
            TradeUpdateEvent event = objectMapper.readValue(message, TradeUpdateEvent.class);
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
        
        // Convert TradeModel list to Trade entities
        //List<Trade> trades = tradeEventMapper.toTrades(event.getTrades());
        log.debug("Converted {} trade models to trade entities", event.getTrades().size());

        List<TradeDetails> tradeDetails = tradeProcessingService.processTradeModels(event.getTrades(), event.getPortfolioId());
        tradeDetailsService.saveAllTradeDetails(tradeDetails);
        tradeProcessingService.processTradeDetails(tradeDetails.stream().map(TradeDetails::getTradeId).collect(Collectors.toList()), event.getPortfolioId(), event.getUserId());
    
        log.info("Successfully processed trades for portfolioId: {}", event.getPortfolioId());
    }   
}
