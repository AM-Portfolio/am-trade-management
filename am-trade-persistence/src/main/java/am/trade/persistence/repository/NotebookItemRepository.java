package am.trade.persistence.repository;

import am.trade.common.models.NotebookItem;
import am.trade.common.models.enums.NotebookItemType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for notebook items
 */
@Repository
public interface NotebookItemRepository extends MongoRepository<NotebookItem, String> {

    List<NotebookItem> findByUserIdAndParentId(String userId, String parentId);

    List<NotebookItem> findByUserIdAndType(String userId, NotebookItemType type);

    List<NotebookItem> findByUserId(String userId);

    void deleteByUserId(String userId);
}
