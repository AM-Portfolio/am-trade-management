package am.trade.api.service.impl;

import am.trade.api.dto.DashboardPreferencesRequest;
import am.trade.api.dto.DashboardPreferencesResponse;
import am.trade.api.service.UserPreferencesService;
import am.trade.common.models.DashboardPreferences;
import am.trade.persistence.repository.DashboardPreferencesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserPreferencesService for managing user dashboard preferences
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserPreferencesServiceImpl implements UserPreferencesService {

    private final DashboardPreferencesRepository dashboardPreferencesRepository;

    @Override
    public DashboardPreferencesResponse getDashboardPreferences(String userId) {
        log.debug("Getting dashboard preferences for user: {}", userId);
        
        Optional<DashboardPreferences> preferencesOpt = dashboardPreferencesRepository.findByUserId(userId);
        
        if (preferencesOpt.isPresent()) {
            return convertToResponse(preferencesOpt.get());
        } else {
            log.info("No dashboard preferences found for user: {}, returning default preferences", userId);
            return createDefaultPreferences(userId);
        }
    }

    @Override
    public DashboardPreferencesResponse saveDashboardPreferences(String userId, DashboardPreferencesRequest request) {
        log.debug("Saving dashboard preferences for user: {}", userId);
        
        // Check if preferences already exist
        Optional<DashboardPreferences> existingPrefs = dashboardPreferencesRepository.findByUserId(userId);
        
        DashboardPreferences preferences;
        if (existingPrefs.isPresent()) {
            preferences = updateExistingPreferences(existingPrefs.get(), request);
        } else {
            preferences = createNewPreferences(userId, request);
        }
        
        // Save to repository
        DashboardPreferences savedPreferences = dashboardPreferencesRepository.save(preferences);
        log.info("Dashboard preferences saved for user: {}", userId);
        
        return convertToResponse(savedPreferences);
    }

    @Override
    public DashboardPreferencesResponse resetDashboardPreferences(String userId) {
        log.debug("Resetting dashboard preferences for user: {}", userId);
        
        // Check if user exists (this would be done by a user service in a real implementation)
        // For now, we'll just assume the user exists
        
        // Delete existing preferences if any
        dashboardPreferencesRepository.deleteByUserId(userId);
        log.info("Deleted existing dashboard preferences for user: {}", userId);
        
        // Return default preferences
        return createDefaultPreferences(userId);
    }
    
    /**
     * Convert a DashboardPreferences entity to a DashboardPreferencesResponse DTO
     */
    private DashboardPreferencesResponse convertToResponse(DashboardPreferences preferences) {
        DashboardPreferencesResponse.WidgetLayout widgetLayout = null;
        
        if (preferences.getWidgetLayout() != null) {
            List<DashboardPreferencesResponse.WidgetLayout.Widget> widgets = new ArrayList<>();
            
            for (DashboardPreferences.WidgetLayout.Widget widget : preferences.getWidgetLayout().getWidgets()) {
                widgets.add(DashboardPreferencesResponse.WidgetLayout.Widget.builder()
                        .id(widget.getId())
                        .type(widget.getType())
                        .row(widget.getRow())
                        .col(widget.getCol())
                        .sizeX(widget.getSizeX())
                        .sizeY(widget.getSizeY())
                        .config(widget.getConfig())
                        .build());
            }
            
            widgetLayout = DashboardPreferencesResponse.WidgetLayout.builder()
                    .widgets(widgets)
                    .build();
        }
        
        return DashboardPreferencesResponse.builder()
                .id(preferences.getId())
                .userId(preferences.getUserId())
                .defaultMetricTypes(preferences.getDefaultMetricTypes())
                .favoritePortfolioIds(preferences.getFavoritePortfolioIds())
                .defaultTimeRange(preferences.getDefaultTimeRange())
                .customDaysRange(preferences.getCustomDaysRange())
                .showProfitLossChart(preferences.isShowProfitLossChart())
                .showWinRateChart(preferences.isShowWinRateChart())
                .showTradeFrequencyChart(preferences.isShowTradeFrequencyChart())
                .showInstrumentDistributionChart(preferences.isShowInstrumentDistributionChart())
                .widgetLayout(widgetLayout)
                .additionalCharts(preferences.getAdditionalCharts())
                .createdAt(preferences.getCreatedAt())
                .updatedAt(preferences.getUpdatedAt())
                .build();
    }
    
    /**
     * Create default dashboard preferences for a user
     */
    private DashboardPreferencesResponse createDefaultPreferences(String userId) {
        LocalDateTime now = LocalDateTime.now();
        
        return DashboardPreferencesResponse.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .defaultMetricTypes(Arrays.asList("PROFIT_LOSS", "WIN_RATE", "TRADE_COUNT"))
                .defaultTimeRange("MONTHLY")
                .showProfitLossChart(true)
                .showWinRateChart(true)
                .showTradeFrequencyChart(true)
                .showInstrumentDistributionChart(true)
                .additionalCharts(new HashMap<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    /**
     * Update existing preferences with new values from request
     */
    private DashboardPreferences updateExistingPreferences(DashboardPreferences existing, DashboardPreferencesRequest request) {
        existing.setDefaultMetricTypes(request.getDefaultMetricTypes());
        existing.setFavoritePortfolioIds(request.getFavoritePortfolioIds());
        existing.setDefaultTimeRange(request.getDefaultTimeRange());
        existing.setCustomDaysRange(request.getCustomDaysRange());
        existing.setShowProfitLossChart(request.isShowProfitLossChart());
        existing.setShowWinRateChart(request.isShowWinRateChart());
        existing.setShowTradeFrequencyChart(request.isShowTradeFrequencyChart());
        existing.setShowInstrumentDistributionChart(request.isShowInstrumentDistributionChart());
        existing.setAdditionalCharts(request.getAdditionalCharts());
        existing.setUpdatedAt(LocalDateTime.now());
        
        // Update widget layout if provided
        if (request.getWidgetLayout() != null) {
            DashboardPreferences.WidgetLayout widgetLayout = new DashboardPreferences.WidgetLayout();
            List<DashboardPreferences.WidgetLayout.Widget> widgets = new ArrayList<>();
            
            for (DashboardPreferencesRequest.WidgetLayout.Widget requestWidget : request.getWidgetLayout().getWidgets()) {
                DashboardPreferences.WidgetLayout.Widget widget = new DashboardPreferences.WidgetLayout.Widget();
                widget.setId(requestWidget.getId());
                widget.setType(requestWidget.getType());
                widget.setRow(requestWidget.getRow());
                widget.setCol(requestWidget.getCol());
                widget.setSizeX(requestWidget.getSizeX());
                widget.setSizeY(requestWidget.getSizeY());
                widget.setConfig(requestWidget.getConfig());
                widgets.add(widget);
            }
            
            widgetLayout.setWidgets(widgets);
            existing.setWidgetLayout(widgetLayout);
        }
        
        return existing;
    }
    
    /**
     * Create new preferences from request
     */
    private DashboardPreferences createNewPreferences(String userId, DashboardPreferencesRequest request) {
        LocalDateTime now = LocalDateTime.now();
        
        DashboardPreferences preferences = new DashboardPreferences();
        preferences.setId(UUID.randomUUID().toString());
        preferences.setUserId(userId);
        preferences.setDefaultMetricTypes(request.getDefaultMetricTypes());
        preferences.setFavoritePortfolioIds(request.getFavoritePortfolioIds());
        preferences.setDefaultTimeRange(request.getDefaultTimeRange());
        preferences.setCustomDaysRange(request.getCustomDaysRange());
        preferences.setShowProfitLossChart(request.isShowProfitLossChart());
        preferences.setShowWinRateChart(request.isShowWinRateChart());
        preferences.setShowTradeFrequencyChart(request.isShowTradeFrequencyChart());
        preferences.setShowInstrumentDistributionChart(request.isShowInstrumentDistributionChart());
        preferences.setAdditionalCharts(request.getAdditionalCharts());
        preferences.setCreatedAt(now);
        preferences.setUpdatedAt(now);
        
        // Set widget layout if provided
        if (request.getWidgetLayout() != null) {
            DashboardPreferences.WidgetLayout widgetLayout = new DashboardPreferences.WidgetLayout();
            List<DashboardPreferences.WidgetLayout.Widget> widgets = new ArrayList<>();
            
            for (DashboardPreferencesRequest.WidgetLayout.Widget requestWidget : request.getWidgetLayout().getWidgets()) {
                DashboardPreferences.WidgetLayout.Widget widget = new DashboardPreferences.WidgetLayout.Widget();
                widget.setId(requestWidget.getId());
                widget.setType(requestWidget.getType());
                widget.setRow(requestWidget.getRow());
                widget.setCol(requestWidget.getCol());
                widget.setSizeX(requestWidget.getSizeX());
                widget.setSizeY(requestWidget.getSizeY());
                widget.setConfig(requestWidget.getConfig());
                widgets.add(widget);
            }
            
            widgetLayout.setWidgets(widgets);
            preferences.setWidgetLayout(widgetLayout);
        }
        
        return preferences;
    }
}
