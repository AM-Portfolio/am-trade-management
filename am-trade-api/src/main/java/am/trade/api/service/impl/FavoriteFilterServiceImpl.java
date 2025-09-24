package am.trade.api.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import am.trade.api.dto.FavoriteFilterRequest;
import am.trade.api.dto.FavoriteFilterResponse;
import am.trade.api.service.FavoriteFilterService;
import am.trade.common.models.FavoriteFilter;
import am.trade.persistence.repository.FavoriteFilterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class FavoriteFilterServiceImpl implements FavoriteFilterService {

    private final FavoriteFilterRepository favoriteFilterRepository;

    @Override
    @Transactional
    public FavoriteFilterResponse createFilter(String userId, FavoriteFilterRequest request) {
        log.info("Creating favorite filter for user: {}", userId);
        
        // If this is set as default, clear any existing default
        if (request.isDefault()) {
            clearExistingDefault(userId);
        }
        
        FavoriteFilter filter = FavoriteFilter.builder()
                .userId(userId)
                .name(request.getName())
                .description(request.getDescription())
                .isDefault(request.isDefault())
                .filterConfig(request.getFilterConfig())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        FavoriteFilter savedFilter = favoriteFilterRepository.save(filter);
        log.info("Created favorite filter with ID: {}", savedFilter.getId());
        
        return mapToResponse(savedFilter);
    }

    @Override
    @Transactional
    public FavoriteFilterResponse updateFilter(String userId, String filterId, FavoriteFilterRequest request) {
        log.info("Updating favorite filter: {} for user: {}", filterId, userId);
        
        FavoriteFilter existingFilter = favoriteFilterRepository.findByIdAndUserId(filterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Filter not found or not owned by user"));
        
        // If this is set as default, clear any existing default
        if (request.isDefault() && !existingFilter.isDefault()) {
            clearExistingDefault(userId);
        }
        
        existingFilter.setName(request.getName());
        existingFilter.setDescription(request.getDescription());
        existingFilter.setDefault(request.isDefault());
        existingFilter.setFilterConfig(request.getFilterConfig());
        existingFilter.setUpdatedAt(LocalDateTime.now());
        
        FavoriteFilter updatedFilter = favoriteFilterRepository.save(existingFilter);
        log.info("Updated favorite filter with ID: {}", updatedFilter.getId());
        
        return mapToResponse(updatedFilter);
    }

    @Override
    public List<FavoriteFilterResponse> getUserFilters(String userId) {
        log.info("Getting all favorite filters for user: {}", userId);
        
        List<FavoriteFilter> filters = favoriteFilterRepository.findByUserId(userId);
        
        return filters.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public FavoriteFilterResponse getFilterById(String userId, String filterId) {
        log.info("Getting favorite filter: {} for user: {}", filterId, userId);
        
        FavoriteFilter filter = favoriteFilterRepository.findByIdAndUserId(filterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Filter not found or not owned by user"));
        
        return mapToResponse(filter);
    }

    @Override
    @Transactional
    public boolean deleteFilter(String userId, String filterId) {
        log.info("Deleting favorite filter: {} for user: {}", filterId, userId);
        
        // Verify filter exists and belongs to user
        FavoriteFilter filter = favoriteFilterRepository.findByIdAndUserId(filterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Filter not found or not owned by user"));
        
        favoriteFilterRepository.delete(filter);
        log.info("Deleted favorite filter with ID: {}", filterId);
        
        return true;
    }

    @Override
    public FavoriteFilterResponse getDefaultFilter(String userId) {
        log.info("Getting default filter for user: {}", userId);
        
        return favoriteFilterRepository.findByUserIdAndIsDefaultTrue(userId)
                .map(this::mapToResponse)
                .orElse(null);
    }

    @Override
    @Transactional
    public FavoriteFilterResponse setDefaultFilter(String userId, String filterId) {
        log.info("Setting filter: {} as default for user: {}", filterId, userId);
        
        // Verify filter exists and belongs to user
        FavoriteFilter filter = favoriteFilterRepository.findByIdAndUserId(filterId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Filter not found or not owned by user"));
        
        // Clear any existing default
        clearExistingDefault(userId);
        
        // Set this filter as default
        filter.setDefault(true);
        filter.setUpdatedAt(LocalDateTime.now());
        
        FavoriteFilter updatedFilter = favoriteFilterRepository.save(filter);
        log.info("Set filter: {} as default for user: {}", filterId, userId);
        
        return mapToResponse(updatedFilter);
    }
    
    /**
     * Clear any existing default filter for a user
     */
    private void clearExistingDefault(String userId) {
        favoriteFilterRepository.findByUserIdAndIsDefaultTrue(userId).ifPresent(defaultFilter -> {
            defaultFilter.setDefault(false);
            defaultFilter.setUpdatedAt(LocalDateTime.now());
            favoriteFilterRepository.save(defaultFilter);
            log.info("Cleared existing default filter for user: {}", userId);
        });
    }
    
    /**
     * Map a FavoriteFilter entity to a FavoriteFilterResponse DTO
     */
    private FavoriteFilterResponse mapToResponse(FavoriteFilter filter) {
        return FavoriteFilterResponse.builder()
                .id(filter.getId())
                .name(filter.getName())
                .description(filter.getDescription())
                .createdAt(filter.getCreatedAt())
                .updatedAt(filter.getUpdatedAt())
                .isDefault(filter.isDefault())
                .filterConfig(filter.getFilterConfig())
                .build();
    }
}
