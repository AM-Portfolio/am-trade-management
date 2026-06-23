package am.trade.kafka.consumer;

import am.trade.kafka.service.KafkaIdempotencyService;
import am.trade.models.kafka.EquityPosition;
import am.trade.models.kafka.PortfolioSyncEvent;
import am.trade.persistence.entity.HoldingEntity;
import am.trade.persistence.entity.PortfolioEntity;
import am.trade.persistence.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioUpdateConsumerService {

    private final KafkaIdempotencyService idempotencyService;
    private final PortfolioRepository portfolioRepository;

    @KafkaListener(
            topics = "${am.trade.kafka.portfolio-update.topic:am-portfolio-update}",
            groupId = "${am.trade.kafka.portfolio-update.consumer-group-id:am-trade-portfolio-update-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePortfolioUpdate(
            @Payload PortfolioSyncEvent event,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.info("Received portfolio update for userId: {}, eventId: {}", event.getUserId(), event.getId());

        // We use the event ID as the deduplication key. 
        // If the same event ID arrives twice, it will be skipped.
        String messageId = event.getId() != null ? event.getId() : messageKey;

        if (idempotencyService.isAlreadyProcessed(messageId)) {
            acknowledgment.acknowledge();
            return;
        }

        try {
            processPortfolioSyncEvent(event);
            idempotencyService.markAsProcessed(messageId, "am-portfolio-update", "am-trade-portfolio-update-group");
            // Acknowledge the message so Kafka knows we've safely persisted it
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to process PortfolioSyncEvent", e);
            // Do not acknowledge so Kafka retries
        }
    }

    private void processPortfolioSyncEvent(PortfolioSyncEvent event) {
        String portfolioId = event.getId(); // Assuming id is portfolioId
        String ownerId = event.getUserId();

        if (ownerId == null) {
            log.warn("PortfolioSyncEvent is missing userId, skipping processing.");
            return;
        }

        // 1. Map Kafka Equities to Mongo HoldingEntities
        List<HoldingEntity> holdings = event.getEquities() != null ? 
            event.getEquities().stream()
                .map(this::mapToHoldingEntity)
                .collect(Collectors.toList()) 
            : List.of();

        // 2. Find existing portfolio or create a new one
        PortfolioEntity portfolio = null;
        if (portfolioId != null) {
            portfolio = portfolioRepository.findByPortfolioId(portfolioId).orElse(null);
        }

        if (portfolio == null) {
            // Fallback: Try to find by ownerId (first active portfolio)
            List<PortfolioEntity> userPortfolios = portfolioRepository.findByOwnerIdAndActive(ownerId, true);
            if (!userPortfolios.isEmpty()) {
                portfolio = userPortfolios.get(0);
            }
        }

        if (portfolio == null) {
            log.info("No active portfolio found for ownerId: {}. Creating a new default portfolio.", ownerId);
            portfolio = PortfolioEntity.builder()
                    .portfolioId(portfolioId != null ? portfolioId : java.util.UUID.randomUUID().toString())
                    .ownerId(ownerId)
                    .name("Default Portfolio")
                    .active(true)
                    .createdDate(LocalDateTime.now())
                    .lastUpdatedDate(LocalDateTime.now())
                    .build();
        }

        // 3. Update holdings and save
        portfolio.setHoldings(holdings);
        portfolio.setLastUpdatedDate(LocalDateTime.now());
        
        portfolioRepository.save(portfolio);
        log.info("Successfully updated portfolio '{}' with {} holdings for user '{}'", 
                 portfolio.getPortfolioId(), holdings.size(), ownerId);
    }

    private HoldingEntity mapToHoldingEntity(EquityPosition equity) {
        return HoldingEntity.builder()
                .symbol(equity.getSymbol())
                .assetType(equity.getAssetType() != null ? equity.getAssetType() : "EQUITY")
                .quantity(equity.getQuantity() != null ? equity.getQuantity() : java.math.BigDecimal.ZERO)
                .avgBuyingPrice(equity.getAvgBuyingPrice() != null ? equity.getAvgBuyingPrice() : java.math.BigDecimal.ZERO)
                .investmentValue(equity.getInvestmentValue() != null ? equity.getInvestmentValue() : java.math.BigDecimal.ZERO)
                .isin(equity.getIsin())
                .sector(equity.getSector())
                .industry(equity.getIndustry())
                .marketCap(equity.getMarketCap())
                .build();
    }
}
