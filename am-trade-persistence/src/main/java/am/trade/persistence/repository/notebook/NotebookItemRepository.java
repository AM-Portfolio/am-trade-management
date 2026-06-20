package am.trade.persistence.repository.notebook;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import am.trade.persistence.entity.notebook.NotebookItemEntity;
import am.trade.common.models.notebook.NotebookItemType;

import java.util.List;

@Repository
public interface NotebookItemRepository extends MongoRepository<NotebookItemEntity, String> {
    List<NotebookItemEntity> findByUserIdAndParentId(String userId, String parentId);
    List<NotebookItemEntity> findByUserIdAndType(String userId, NotebookItemType type);
    List<NotebookItemEntity> findByUserId(String userId);
}
