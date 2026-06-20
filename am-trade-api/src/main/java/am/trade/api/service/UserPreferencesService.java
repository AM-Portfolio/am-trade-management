package am.trade.api.service;

import am.trade.api.dto.DashboardPreferencesRequest;
import am.trade.api.dto.DashboardPreferencesResponse;

/**
 * Service for managing user preferences for dashboard and trade views
 */
public interface UserPreferencesService {

    /**
     * Get dashboard preferences for a user
     * 
     * @param userId User ID
     * @return Dashboard preferences
     * @throws IllegalArgumentException if preferences not found
     */
    DashboardPreferencesResponse getDashboardPreferences(String userId);
    
    /**
     * Save dashboard preferences for a user
     * 
     * @param userId User ID
     * @param request Dashboard preferences request
     * @return Saved dashboard preferences
     * @throws IllegalArgumentException if request is invalid
     */
    DashboardPreferencesResponse saveDashboardPreferences(String userId, DashboardPreferencesRequest request);
    
    /**
     * Reset dashboard preferences to default for a user
     * 
     * @param userId User ID
     * @return Default dashboard preferences
     * @throws IllegalArgumentException if user not found
     */
    DashboardPreferencesResponse resetDashboardPreferences(String userId);
}
