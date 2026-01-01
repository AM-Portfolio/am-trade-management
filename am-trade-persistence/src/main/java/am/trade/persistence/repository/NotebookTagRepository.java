package am.trade.persistence.repository;

import am.trade.common.models.NotebookTag;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for notebook tags
 */
@Repository
public interface NotebookTagRepository extends MongoRepository<NotebookTag, String> {

    List<NotebookTag> findByUserId(String userId);

    void deleteByUserId(String userId);
}
