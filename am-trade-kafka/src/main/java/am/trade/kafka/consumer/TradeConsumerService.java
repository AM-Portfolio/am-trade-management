package am.trade.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import am.trade.kafka.mapper.TradeEventMapper;
import am.trade.kafka.mapper.TradeModelMapper;
import am.trade.kafka.model.TradeUpdateEvent;
import am.trade.models.document.Trade;
import am.trade.models.dto.TradeDTO;
import am.trade.models.mapper.TradeMapper;
import am.trade.services.service.TradeService;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.kafka.portfolio.consumer.enabled", havingValue = "true", matchIfMissing = false)
public class TradeConsumerService {

    private final ObjectMapper objectMapper;
    private final TradeMapper tradeMapper;
    private final TradeService tradeService;
    private final TradeEventMapper tradeEventMapper;
    private final TradeModelMapper tradeModelMapper;

    @KafkaListener(topics = "${app.kafka.portfolio.topic}", 
                  groupId = "${app.kafka.portfolio.consumer.id}",
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
        List<Trade> trades = tradeEventMapper.toTrades(event.getTrades());
        log.debug("Converted {} trade models to trade entities", trades.size());
        
        // Process each trade
        for (Trade trade : trades) {
            // Convert Trade entity to TradeDTO
            TradeDTO tradeDTO = tradeMapper.toDto(trade);
            
            // Set additional metadata from the event
            tradeDTO.setCreatedBy(event.getUserId());
            
            // Create the trade in the system
            log.info("Creating trade with ID: {}", tradeDTO.getTradeId());
            tradeService.createTrade(tradeDTO);
        }
        
        log.info("Successfully processed {} trades", trades.size());
    }   
}
