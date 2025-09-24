package am.trade.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import am.trade.common.models.FavoriteFilter;

@Repository
public interface FavoriteFilterRepository extends MongoRepository<FavoriteFilter, String> {
    
    /**
     * Find all favorite filters for a specific user
     * 
     * @param userId The user ID
     * @return List of favorite filters
     */
    List<FavoriteFilter> findByUserId(String userId);
    
    /**
     * Find a user's default filter
     * 
     * @param userId The user ID
     * @return The default filter if exists
     */
    Optional<FavoriteFilter> findByUserIdAndIsDefaultTrue(String userId);
    
    /**
     * Find a specific filter by ID and user ID
     * 
     * @param id The filter ID
     * @param userId The user ID
     * @return The filter if exists
     */
    Optional<FavoriteFilter> findByIdAndUserId(String id, String userId);
    
    /**
     * Delete a specific filter by ID and user ID
     * 
     * @param id The filter ID
     * @param userId The user ID
     */
    void deleteByIdAndUserId(String id, String userId);
}
