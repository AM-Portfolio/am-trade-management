package am.trade.kafka.service;

import am.trade.persistence.entity.ProcessedKafkaMessage;
import am.trade.persistence.repository.ProcessedKafkaMessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import java.time.Instant;

/**
 * Provides idempotency guarantees for Kafka consumers.
 *
 * <h2>Why This Exists</h2>
 * Kafka operates under "at-least-once" delivery semantics. This means that in
 * failure scenarios (consumer crash, network blip, broker rebalance), the SAME
 * message can be delivered to your consumer MORE THAN ONCE.
 *
 * <p>Without this service: a trade event delivered twice → two identical trade
 * records saved to MongoDB → corrupt portfolio calculations.
 *
 * <p>With this service: the second delivery is detected and silently skipped.
 *
 * <h2>How It Works (The Two-Step Pattern)</h2>
 * <ol>
 *   <li><b>Before processing:</b> Call {@link #isAlreadyProcessed(String)}.
 *       If it returns {@code true}, skip and acknowledge immediately.</li>
 *   <li><b>After successful processing:</b> Call
 *       {@link #markAsProcessed(String, String, String)} to record the message ID.
 *       MongoDB's unique {@code @Id} constraint prevents race conditions —
 *       a concurrent duplicate insertion will throw {@link DuplicateKeyException},
 *       which we catch and treat as "already processed".</li>
 * </ol>
 *
 * <h2>TTL Cleanup</h2>
 * The underlying {@code ProcessedKafkaMessage} entity has a 7-day TTL index on
 * the {@code processedAt} field. MongoDB automatically deletes old records, so
 * this collection stays small indefinitely.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaIdempotencyService {

    private final ProcessedKafkaMessageRepository repository;

    /**
     * Checks if a Kafka message has already been successfully processed.
     *
     * @param messageId The unique ID of the message (e.g., TradeUpdateEvent.getId().toString())
     * @return {@code true} if already processed (skip it); {@code false} if this is the first time
     */
    public boolean isAlreadyProcessed(String messageId) {
        boolean exists = repository.existsById(messageId);
        if (exists) {
            log.warn("Duplicate Kafka message detected — skipping. messageId: {}", messageId);
        }
        return exists;
    }

    /**
     * Records a Kafka message as successfully processed, preventing future reprocessing.
     *
     * <p>This MUST be called only after the business logic has completed successfully.
     * Calling it before processing would mark the message as done even if processing
     * later fails — defeating the purpose.
     *
     * <p>If a concurrent consumer happens to insert the same {@code messageId}
     * simultaneously (race condition), MongoDB throws a {@link DuplicateKeyException}.
     * We catch it here and log a warning — the net effect is safe: the message
     * was processed exactly once.
     *
     * @param messageId     The unique ID of the message
     * @param topic         The Kafka topic the message came from (for traceability)
     * @param consumerGroup The consumer group ID (for traceability)
     */
    public void markAsProcessed(String messageId, String topic, String consumerGroup) {
        try {
            ProcessedKafkaMessage record = ProcessedKafkaMessage.builder()
                    .messageId(messageId)
                    .topic(topic)
                    .consumerGroup(consumerGroup)
                    .processedAt(Instant.now())
                    .build();
            repository.save(record);
            log.debug("Marked message as processed. messageId: {}, topic: {}", messageId, topic);
        } catch (DuplicateKeyException e) {
            // Race condition: two consumer threads tried to mark the same message simultaneously.
            // The first one won; this one lost. The net result is correct — processed once.
            log.warn("Race condition on idempotency insert — message was already marked. messageId: {}", messageId);
        }
    }
}
