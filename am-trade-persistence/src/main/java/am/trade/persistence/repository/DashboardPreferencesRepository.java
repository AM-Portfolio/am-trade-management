package am.trade.persistence.repository;

import am.trade.common.models.DashboardPreferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for dashboard preferences
 */
@Repository
public interface DashboardPreferencesRepository extends MongoRepository<DashboardPreferences, String> {
    
    /**
     * Find dashboard preferences by user ID
     * 
     * @param userId User ID
     * @return Optional containing dashboard preferences if found
     */
    Optional<DashboardPreferences> findByUserId(String userId);
    
    /**
     * Delete dashboard preferences by user ID
     * 
     * @param userId User ID
     */
    void deleteByUserId(String userId);
}
