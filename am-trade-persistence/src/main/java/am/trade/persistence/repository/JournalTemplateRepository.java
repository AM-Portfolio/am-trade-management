package am.trade.persistence.repository;

import am.trade.common.models.JournalTemplate;
import am.trade.common.models.enums.JournalTemplateCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for journal templates
 */
@Repository
public interface JournalTemplateRepository extends MongoRepository<JournalTemplate, String> {

    List<JournalTemplate> findByCategory(JournalTemplateCategory category);

    List<JournalTemplate> findByIsRecommended(Boolean isRecommended);

    List<JournalTemplate> findByFavoriteUserIdsContaining(String userId);

    List<JournalTemplate> findByCreatedBy(String userId);

    List<JournalTemplate> findByIsSystemTemplate(Boolean isSystemTemplate);

    List<JournalTemplate> findByNameContainingIgnoreCase(String name);

    List<JournalTemplate> findByTagsContaining(String tag);
}
