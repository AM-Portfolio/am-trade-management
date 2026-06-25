package am.trade.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * MongoDB document that tracks every Kafka message we have successfully processed.
 *
 * <p><b>Purpose (Idempotency):</b> Kafka guarantees "at-least-once" delivery.
 * In practice this means the same message can arrive more than once — for example,
 * if the consumer crashes after writing trades to the DB but before committing
 * the Kafka offset. Without this guard, we would write duplicate trades.
 *
 * <p><b>How it works:</b>
 * <ol>
 *   <li>Before processing a {@code TradeUpdateEvent}, the consumer checks whether
 *       {@code messageId} (the event's UUID) already exists in this collection.</li>
 *   <li>If it exists → duplicate. Skip processing, acknowledge immediately.</li>
 *   <li>If it does NOT exist → first time. Process the event, then insert a new
 *       {@code ProcessedKafkaMessage} document to mark it as done.</li>
 * </ol>
 *
 * <p><b>TTL (Time-to-live):</b> The {@code @Indexed(expireAfterSeconds)} on
 * {@code processedAt} tells MongoDB to automatically delete documents older
 * than 7 days (604800 seconds). This keeps the collection small and prevents
 * unbounded disk growth in production.
 *
 * <p><b>Design note:</b> We intentionally keep this entity in the persistence
 * module (not in the kafka module) so that the DB schema is co-located with all
 * other MongoDB entities and is easy to find.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "processed_kafka_messages")
public class ProcessedKafkaMessage {

    /**
     * The unique message identifier — typically the UUID from the Kafka event payload.
     * This IS the MongoDB document ID, so MongoDB automatically enforces uniqueness.
     * Inserting a duplicate ID will throw a {@code DuplicateKeyException}, which we
     * catch in the service to safely handle race conditions.
     */
    @Id
    private String messageId;

    /** The Kafka topic this message came from. Useful for debugging the DLT. */
    private String topic;

    /** The consumer group that processed this message. */
    private String consumerGroup;

    /**
     * The timestamp when this message was successfully processed.
     * MongoDB uses this field to drive the TTL index — documents are deleted
     * automatically after 7 days (604800 seconds).
     */
    @Indexed(expireAfterSeconds = 604800) // 7 days
    private Instant processedAt;
}
