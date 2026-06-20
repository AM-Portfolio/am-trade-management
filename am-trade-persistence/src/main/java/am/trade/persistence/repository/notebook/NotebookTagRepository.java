package am.trade.persistence.repository.notebook;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import am.trade.persistence.entity.notebook.NotebookTagEntity;

import java.util.List;

@Repository
public interface NotebookTagRepository extends MongoRepository<NotebookTagEntity, String> {
    List<NotebookTagEntity> findByUserId(String userId);
}
