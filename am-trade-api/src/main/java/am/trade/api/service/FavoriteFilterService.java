package am.trade.api.service;

import java.util.List;

import am.trade.api.dto.FavoriteFilterRequest;
import am.trade.api.dto.FavoriteFilterResponse;

/**
 * Service for managing user's favorite metric filters
 */
public interface FavoriteFilterService {
    
    /**
     * Create a new favorite filter for a user
     * 
     * @param userId The user ID
     * @param request The filter request
     * @return The created filter
     */
    FavoriteFilterResponse createFilter(String userId, FavoriteFilterRequest request);
    
    /**
     * Update an existing favorite filter
     * 
     * @param userId The user ID
     * @param filterId The filter ID
     * @param request The updated filter request
     * @return The updated filter
     */
    FavoriteFilterResponse updateFilter(String userId, String filterId, FavoriteFilterRequest request);
    
    /**
     * Get all favorite filters for a user
     * 
     * @param userId The user ID
     * @return List of favorite filters
     */
    List<FavoriteFilterResponse> getUserFilters(String userId);
    
    /**
     * Get a specific favorite filter by ID
     * 
     * @param userId The user ID
     * @param filterId The filter ID
     * @return The filter if found
     */
    FavoriteFilterResponse getFilterById(String userId, String filterId);
    
    /**
     * Delete a favorite filter
     * 
     * @param userId The user ID
     * @param filterId The filter ID
     * @return true if deleted successfully
     */
    boolean deleteFilter(String userId, String filterId);
    
    /**
     * Get the user's default filter if exists
     * 
     * @param userId The user ID
     * @return The default filter or null
     */
    FavoriteFilterResponse getDefaultFilter(String userId);
    
    /**
     * Set a filter as the default for a user
     * 
     * @param userId The user ID
     * @param filterId The filter ID
     * @return The updated filter
     */
    FavoriteFilterResponse setDefaultFilter(String userId, String filterId);
}
