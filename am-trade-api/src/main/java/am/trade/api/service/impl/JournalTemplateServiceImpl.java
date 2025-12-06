package am.trade.api.service.impl;

import am.trade.api.dto.*;
import am.trade.api.service.JournalTemplateService;
import am.trade.api.service.TradeJournalService;
import am.trade.common.models.JournalTemplate;
import am.trade.common.models.TemplateField;
import am.trade.common.models.enums.JournalTemplateCategory;
import am.trade.persistence.repository.JournalTemplateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of JournalTemplateService for managing journal templates
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class JournalTemplateServiceImpl implements JournalTemplateService {

    private final JournalTemplateRepository templateRepository;
    private final TradeJournalService tradeJournalService;

    @Override
    public JournalTemplateResponse createTemplate(JournalTemplateRequest request) {
        log.debug("Creating journal template: {}", request.getName());

        JournalTemplate template = JournalTemplate.builder()
                .id(UUID.randomUUID().toString())
                .name(request.getName())
                .description(request.getDescription())
                .category(request.getCategory())
                .fields(convertFieldRequests(request.getFields()))
                .isSystemTemplate(request.getIsSystemTemplate() != null ? request.getIsSystemTemplate() : false)
                .isRecommended(request.getIsRecommended() != null ? request.getIsRecommended() : false)
                .usageCount(0)
                .createdBy(request.getCreatedBy())
                .favoriteUserIds(new ArrayList<>())
                .tags(request.getTags())
                .thumbnailUrl(request.getThumbnailUrl())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        JournalTemplate savedTemplate = templateRepository.save(template);
        log.info("Template created with ID: {}", savedTemplate.getId());

        return convertToResponse(savedTemplate, request.getCreatedBy());
    }

    @Override
    public JournalTemplateResponse getTemplate(String templateId, String userId) {
        log.debug("Getting template with ID: {}", templateId);

        JournalTemplate template = findTemplateById(templateId);
        return convertToResponse(template, userId);
    }

    @Override
    public JournalTemplateResponse updateTemplate(String templateId, JournalTemplateRequest request) {
        log.debug("Updating template with ID: {}", templateId);

        JournalTemplate template = findTemplateById(templateId);

        // System templates can only be updated by admins (basic check)
        if (Boolean.TRUE.equals(template.getIsSystemTemplate()) &&
                !template.getCreatedBy().equals(request.getCreatedBy())) {
            throw new IllegalArgumentException("Cannot update system template");
        }

        // Update fields
        template.setName(request.getName());
        template.setDescription(request.getDescription());
        template.setCategory(request.getCategory());
        template.setFields(convertFieldRequests(request.getFields()));
        template.setIsRecommended(
                request.getIsRecommended() != null ? request.getIsRecommended() : template.getIsRecommended());
        template.setTags(request.getTags());
        template.setThumbnailUrl(request.getThumbnailUrl());
        template.setUpdatedAt(LocalDateTime.now());

        JournalTemplate updatedTemplate = templateRepository.save(template);
        log.info("Template updated with ID: {}", updatedTemplate.getId());

        return convertToResponse(updatedTemplate, request.getCreatedBy());
    }

    @Override
    public void deleteTemplate(String templateId, String userId) {
        log.debug("Deleting template with ID: {}", templateId);

        JournalTemplate template = findTemplateById(templateId);

        // System templates cannot be deleted
        if (Boolean.TRUE.equals(template.getIsSystemTemplate())) {
            throw new IllegalArgumentException("Cannot delete system template");
        }

        // Only creator can delete
        if (!template.getCreatedBy().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete template created by another user");
        }

        templateRepository.deleteById(templateId);
        log.info("Template deleted with ID: {}", templateId);
    }

    @Override
    public List<JournalTemplateResponse> getAllTemplates(String userId, JournalTemplateCategory category,
            String searchQuery) {
        log.debug("Getting all templates for user: {}, category: {}, search: {}", userId, category, searchQuery);

        List<JournalTemplate> templates;

        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            templates = templateRepository.findByNameContainingIgnoreCase(searchQuery);
        } else if (category != null) {
            templates = templateRepository.findByCategory(category);
        } else {
            templates = templateRepository.findAll();
        }

        return templates.stream()
                .map(template -> convertToResponse(template, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<JournalTemplateResponse> getFavoriteTemplates(String userId) {
        log.debug("Getting favorite templates for user: {}", userId);

        List<JournalTemplate> templates = templateRepository.findByFavoriteUserIdsContaining(userId);
        return templates.stream()
                .map(template -> convertToResponse(template, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<JournalTemplateResponse> getRecommendedTemplates(String userId) {
        log.debug("Getting recommended templates for user: {}", userId);

        List<JournalTemplate> templates = templateRepository.findByIsRecommended(true);
        return templates.stream()
                .map(template -> convertToResponse(template, userId))
                .collect(Collectors.toList());
    }

    @Override
    public List<JournalTemplateResponse> getUserCustomTemplates(String userId) {
        log.debug("Getting custom templates for user: {}", userId);

        List<JournalTemplate> templates = templateRepository.findByCreatedBy(userId);
        return templates.stream()
                .map(template -> convertToResponse(template, userId))
                .collect(Collectors.toList());
    }

    @Override
    public JournalTemplateResponse toggleFavorite(String templateId, String userId) {
        log.debug("Toggling favorite for template: {} and user: {}", templateId, userId);

        JournalTemplate template = findTemplateById(templateId);

        List<String> favoriteUserIds = template.getFavoriteUserIds();
        if (favoriteUserIds == null) {
            favoriteUserIds = new ArrayList<>();
        }

        if (favoriteUserIds.contains(userId)) {
            favoriteUserIds.remove(userId);
            log.info("Removed user {} from favorites for template {}", userId, templateId);
        } else {
            favoriteUserIds.add(userId);
            log.info("Added user {} to favorites for template {}", userId, templateId);
        }

        template.setFavoriteUserIds(favoriteUserIds);
        template.setUpdatedAt(LocalDateTime.now());

        JournalTemplate updatedTemplate = templateRepository.save(template);
        return convertToResponse(updatedTemplate, userId);
    }

    @Override
    public TradeJournalEntryResponse useTemplate(UseTemplateRequest request) {
        log.debug("Using template {} for user {}", request.getTemplateId(), request.getUserId());

        JournalTemplate template = findTemplateById(request.getTemplateId());

        // Increment usage count
        template.setUsageCount(template.getUsageCount() != null ? template.getUsageCount() + 1 : 1);
        template.setUpdatedAt(LocalDateTime.now());
        templateRepository.save(template);

        // Build journal entry content from template and field values
        String content = buildJournalContent(template, request.getFieldValues());
        String title = request.getCustomTitle() != null ? request.getCustomTitle() : template.getName();

        // Create journal entry
        TradeJournalEntryRequest journalRequest = TradeJournalEntryRequest.builder()
                .userId(request.getUserId())
                .tradeId(request.getTradeId())
                .title(title)
                .content(content)
                .customFields(request.getFieldValues())
                .entryDate(LocalDateTime.now())
                .build();

        TradeJournalEntryResponse journalEntry = tradeJournalService.createJournalEntry(journalRequest);
        log.info("Journal entry created from template {} with ID: {}", template.getId(), journalEntry.getId());

        return journalEntry;
    }

    // --- Helper Methods ---

    private JournalTemplate findTemplateById(String templateId) {
        return templateRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with ID: " + templateId));
    }

    private List<TemplateField> convertFieldRequests(List<TemplateFieldRequest> fieldRequests) {
        if (fieldRequests == null) {
            return new ArrayList<>();
        }

        return fieldRequests.stream()
                .map(req -> TemplateField.builder()
                        .fieldId(req.getFieldId())
                        .fieldLabel(req.getFieldLabel())
                        .fieldType(req.getFieldType())
                        .placeholder(req.getPlaceholder())
                        .defaultValue(req.getDefaultValue())
                        .required(req.getRequired())
                        .order(req.getOrder())
                        .options(req.getOptions())
                        .minLength(req.getMinLength())
                        .maxLength(req.getMaxLength())
                        .validationPattern(req.getValidationPattern())
                        .helpText(req.getHelpText())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TemplateFieldResponse> convertFieldResponses(List<TemplateField> fields) {
        if (fields == null) {
            return new ArrayList<>();
        }

        return fields.stream()
                .map(field -> TemplateFieldResponse.builder()
                        .fieldId(field.getFieldId())
                        .fieldLabel(field.getFieldLabel())
                        .fieldType(field.getFieldType())
                        .placeholder(field.getPlaceholder())
                        .defaultValue(field.getDefaultValue())
                        .required(field.getRequired())
                        .order(field.getOrder())
                        .options(field.getOptions())
                        .minLength(field.getMinLength())
                        .maxLength(field.getMaxLength())
                        .validationPattern(field.getValidationPattern())
                        .helpText(field.getHelpText())
                        .build())
                .collect(Collectors.toList());
    }

    private JournalTemplateResponse convertToResponse(JournalTemplate template, String userId) {
        boolean isFavorite = template.getFavoriteUserIds() != null &&
                template.getFavoriteUserIds().contains(userId);

        return JournalTemplateResponse.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .category(template.getCategory())
                .fields(convertFieldResponses(template.getFields()))
                .isSystemTemplate(template.getIsSystemTemplate())
                .isRecommended(template.getIsRecommended())
                .usageCount(template.getUsageCount())
                .createdBy(template.getCreatedBy())
                .isFavorite(isFavorite)
                .tags(template.getTags())
                .thumbnailUrl(template.getThumbnailUrl())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }

    private String buildJournalContent(JournalTemplate template, Map<String, Object> fieldValues) {
        StringBuilder content = new StringBuilder();
        content.append("# ").append(template.getName()).append("\n\n");

        if (template.getDescription() != null) {
            content.append(template.getDescription()).append("\n\n");
        }

        if (template.getFields() != null) {
            // Sort fields by order
            List<TemplateField> sortedFields = template.getFields().stream()
                    .sorted(Comparator.comparing(f -> f.getOrder() != null ? f.getOrder() : Integer.MAX_VALUE))
                    .collect(Collectors.toList());

            for (TemplateField field : sortedFields) {
                content.append("## ").append(field.getFieldLabel()).append("\n");

                Object value = fieldValues != null ? fieldValues.get(field.getFieldId()) : null;
                if (value != null) {
                    content.append(value.toString()).append("\n\n");
                } else if (field.getDefaultValue() != null) {
                    content.append(field.getDefaultValue()).append("\n\n");
                } else {
                    content.append("_No response_\n\n");
                }
            }
        }

        return content.toString();
    }
}
