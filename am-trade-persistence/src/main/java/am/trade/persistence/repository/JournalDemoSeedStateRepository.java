package am.trade.persistence.repository;

import am.trade.common.models.JournalDemoSeedState;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JournalDemoSeedStateRepository extends MongoRepository<JournalDemoSeedState, String> {
}
