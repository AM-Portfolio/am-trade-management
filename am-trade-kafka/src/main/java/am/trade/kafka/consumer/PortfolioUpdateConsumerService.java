package am.trade.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import am.trade.models.kafka.PortfolioUpdateEventMirror;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Consumes {@code PortfolioUpdateEvent} messages from the
 * {@code am-portfolio-update} Kafka topic.
 * <p>
 * <b>What this does:</b> When Portfolio recalculates a user's holdings
 * (triggered by a trade event, market data update, etc.), it publishes a
 * {@code PortfolioUpdateEvent} to Kafka. This consumer picks it up so Trade
 * can stay in sync — for example, to display up-to-date P&L data on the
 * trade dashboard, or to correlate trade entries with portfolio performance.
 * <p>
 * <b>Why @ConditionalOnProperty?</b> This annotation means Spring will only
 * create this bean if the config property
 * {@code am.trade.kafka.portfolio-update.consumer.enabled} is set to
 * {@code true}. If it's missing or {@code false}, the consumer simply doesn't
 * exist at runtime — zero overhead. This is the standard pattern for optional
 * Kafka consumers.
 * <p>
 * <b>Manual acknowledgment (Acknowledgment ack):</b> We use
 * {@code AckMode.MANUAL_IMMEDIATE} (configured in KafkaConfig). This means
 * we only tell Kafka "I'm done with this message" <em>after</em> we've
 * successfully processed it. If processing fails (exception), the message
 * stays uncommitted and Kafka will re-deliver it on the next poll — giving
 * us automatic retry behavior.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
    name = "am.trade.kafka.portfolio-update.consumer.enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class PortfolioUpdateConsumerService {

    private final ObjectMapper objectMapper;

    /**
     * Kafka listener that processes portfolio update messages.
     * <p>
     * <b>Topic:</b> Configurable via YAML, defaults to {@code am-portfolio-update}.
     * <b>Group ID:</b> {@code am-trade-portfolio-update-group} — unique to Trade
     * so Trade gets its own independent offset tracking (other consumers of the
     * same topic, like the UI WebSocket relay, won't interfere).
     *
     * @param message        Raw JSON string from Kafka
     * @param acknowledgment Manual ack handle — call {@code acknowledge()} on success
     */
    @KafkaListener(
        topics = "${am.trade.kafka.portfolio-update.topic:am-portfolio-update}",
        groupId = "${am.trade.kafka.portfolio-update.consumer-group-id:am-trade-portfolio-update-group}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(String message, Acknowledgment acknowledgment) {
        try {
            log.info("Received portfolio update event");
            log.debug("Portfolio update message payload: {}", message);

            // Deserialize the JSON string into our mirror POJO
            PortfolioUpdateEventMirror event = objectMapper.readValue(
                message, PortfolioUpdateEventMirror.class
            );

            log.info("Processing portfolio update for user: {}, portfolio: {}, equities count: {}",
                    event.getUserId(),
                    event.getPortfolioId(),
                    event.getEquities() != null ? event.getEquities().size() : 0);

            // Process the portfolio update
            processPortfolioUpdate(event);

            // Only acknowledge after successful processing
            acknowledgment.acknowledge();
            log.info("Portfolio update processed and acknowledged for user: {}", event.getUserId());

        } catch (Exception e) {
            // Log and rethrow so Kafka's DefaultErrorHandler can trigger retries
            // and eventually route to DLT if retries are exhausted.
            log.error("Failed to process portfolio update message: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Processes a portfolio update event from the Portfolio service.
     * <p>
     * Currently logs the update summary. This is the extension point where
     * you would add business logic like:
     * <ul>
     *   <li>Updating a local cache of portfolio P&L for the trade dashboard</li>
     *   <li>Triggering trade performance calculations against live portfolio data</li>
     *   <li>Enriching trade records with current holding context</li>
     *   <li>Sending WebSocket notifications to the Trade UI</li>
     * </ul>
     *
     * @param event The deserialized portfolio update event
     */
    private void processPortfolioUpdate(PortfolioUpdateEventMirror event) {
        // Log summary data
        log.info("Portfolio summary — totalValue: {}, totalInvestment: {}, totalGainLoss: {} ({}%)",
                event.getTotalValue(),
                event.getTotalInvestment(),
                event.getTotalGainLoss(),
                event.getTotalGainLossPercentage());

        // Log individual equity holdings
        if (event.getEquities() != null) {
            for (PortfolioUpdateEventMirror.EquitySnapshot equity : event.getEquities()) {
                log.debug("  Equity: {} ({}) — qty: {}, avgPrice: {}, currentPrice: {}, P&L: {} ({}%)",
                        equity.getSymbol(),
                        equity.getName(),
                        equity.getQuantity(),
                        equity.getAvgBuyingPrice(),
                        equity.getCurrentPrice(),
                        equity.getProfitLoss(),
                        equity.getProfitLossPercentage());
            }
        }

        // TODO: Add business logic here as needed
        // Examples:
        // - portfolioCacheService.updateCache(event.getUserId(), event);
        // - tradePerformanceService.recalculateWithPortfolioContext(event);
    }
}
