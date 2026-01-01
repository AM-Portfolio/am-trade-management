package am.trade.persistence.repository;

import am.trade.common.models.DailyBehaviorTracking;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for daily behavior tracking
 */
@Repository
public interface DailyBehaviorRepository extends MongoRepository<DailyBehaviorTracking, String> {
    
    /**
     * Find daily behavior tracking by user ID and date
     * 
     * @param userId User ID
     * @param date Date
     * @return Optional daily behavior tracking
     */
    Optional<DailyBehaviorTracking> findByUserIdAndDate(String userId, LocalDate date);
    
    /**
     * Find daily behavior tracking by user ID and date range
     * 
     * @param userId User ID
     * @param startDate Start date
     * @param endDate End date
     * @return List of daily behavior tracking
     */
    List<DailyBehaviorTracking> findByUserIdAndDateBetweenOrderByDateDesc(
            String userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * Delete by user ID and date
     * 
     * @param userId User ID
     * @param date Date
     */
    void deleteByUserIdAndDate(String userId, LocalDate date);
}
