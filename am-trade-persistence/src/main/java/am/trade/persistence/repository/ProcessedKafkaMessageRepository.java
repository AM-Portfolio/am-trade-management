package am.trade.persistence.repository;

import am.trade.persistence.entity.ProcessedKafkaMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data MongoDB repository for idempotency tracking.
 *
 * <p>Spring Data automatically provides the {@code existsById(id)} and
 * {@code save(entity)} methods — we don't need to write any query code.
 *
 * <p><b>Why MongoRepository and not a custom query?</b>
 * The {@code @Id} field ({@code messageId}) is MongoDB's primary key, which is
 * natively indexed. So {@code existsById()} is an O(1) index lookup — extremely
 * fast, even at millions of records.
 */
@Repository
public interface ProcessedKafkaMessageRepository extends MongoRepository<ProcessedKafkaMessage, String> {
    // existsById(String id)  — inherited, checks if messageId exists
    // save(ProcessedKafkaMessage entity) — inherited, inserts/updates a document
}
