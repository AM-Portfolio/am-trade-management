package am.trade.persistence.repository.journal;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import am.trade.persistence.entity.journal.JournalEntryEntity;

import java.util.List;

@Repository
public interface JournalEntryRepository extends MongoRepository<JournalEntryEntity, String> {
    List<JournalEntryEntity> findByUserId(String userId);
    List<JournalEntryEntity> findByTradeId(String tradeId);
    List<JournalEntryEntity> findByUserIdAndEntryDateBetween(String userId, String startDate, String endDate);
}
