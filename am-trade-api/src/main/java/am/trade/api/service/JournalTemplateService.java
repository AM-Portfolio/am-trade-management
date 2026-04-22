package am.trade.api.service;

import am.trade.api.dto.JournalTemplateRequest;
import am.trade.api.dto.JournalTemplateResponse;
import am.trade.api.dto.UseTemplateRequest;
import am.trade.api.dto.TradeJournalEntryResponse;
import am.trade.common.models.enums.JournalTemplateCategory;

import java.util.List;

/**
 * Service for managing journal templates
 */
public interface JournalTemplateService {

    // CRUD Operations
    JournalTemplateResponse createTemplate(JournalTemplateRequest request);

    JournalTemplateResponse getTemplate(String templateId, String userId);

    JournalTemplateResponse updateTemplate(String templateId, JournalTemplateRequest request);

    void deleteTemplate(String templateId, String userId);

    // Query Operations
    List<JournalTemplateResponse> getAllTemplates(String userId, JournalTemplateCategory category, String searchQuery);

    List<JournalTemplateResponse> getFavoriteTemplates(String userId);

    List<JournalTemplateResponse> getRecommendedTemplates(String userId);

    List<JournalTemplateResponse> getUserCustomTemplates(String userId);

    // Favorite Operations
    JournalTemplateResponse toggleFavorite(String templateId, String userId);

    // Template Usage
    TradeJournalEntryResponse useTemplate(UseTemplateRequest request);
}
