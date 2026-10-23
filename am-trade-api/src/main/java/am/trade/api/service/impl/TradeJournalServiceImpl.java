package am.trade.api.service.impl;

import am.trade.api.dto.*;
import am.trade.api.service.TradeJournalService;
import am.trade.common.models.*;
import am.trade.common.util.JournalContentPlainText;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.repository.TradeDetailsRepository;
import am.trade.persistence.repository.JournalAnalyticsAggregation;
import am.trade.persistence.repository.TradeJournalRepository;
import am.trade.services.service.PortfolioPersistenceService;
import com.am.security.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of TradeJournalService for managing trade journal entries
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeJournalServiceImpl implements TradeJournalService {

    private static final Set<String> FOLLOWED_TRUE = Set.of("YES", "TRUE", "Y", "1");

    /**
     * Cache key MUST include the caller userId. Keying only by entryId allowed
     * cross-user IDOR on cache hit (ownership check skipped).
     */
    private static final String JOURNAL_CACHE = "trade-journals";
    private static final String JOURNAL_CACHE_KEY =
            "T(com.am.security.context.UserContext).getUserIdOrThrow() + '::' + #entryId";
    private static final String JOURNAL_CACHE_KEY_RESULT =
            "T(com.am.security.context.UserContext).getUserIdOrThrow() + '::' + #result.id";

    private final TradeJournalRepository tradeJournalRepository;
    private final TradeDetailsRepository tradeDetailsRepository;
    private final PortfolioPersistenceService portfolioPersistenceService;

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY_RESULT)
    public TradeJournalEntryResponse createJournalEntry(TradeJournalEntryRequest request) {
        log.debug("Creating journal entry for user: {}", UserContext.getUserIdOrThrow());

        validateRequest(request);
        assertTradeIdOwnedIfPresent(request.getTradeId());
        assertPortfolioOwnedIfPresent(request.getPortfolioId());
        assertPortfolioMatchesTradeIfBothPresent(request.getPortfolioId(), request.getTradeId());

        TradeJournalEntry entry = convertToEntity(request);
        entry.setId(UUID.randomUUID().toString());
        syncContentPlain(entry);

        applyAutoCalculations(entry);

        LocalDateTime now = LocalDateTime.now();
        entry.setCreatedAt(now);
        entry.setUpdatedAt(now);

        TradeJournalEntry savedEntry = tradeJournalRepository.save(entry);
        log.info("Journal entry created with ID: {}", savedEntry.getId());

        return convertToResponse(savedEntry);
    }

    @Override
    @Cacheable(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse getJournalEntry(String entryId) {
        log.debug("Getting journal entry with ID: {}", entryId);
        // Always authorize — cache key is user-scoped so a hit cannot serve another user's entry.
        TradeJournalEntry entry = findOwnedEntry(entryId);
        return convertToResponse(entry);
    }

    @Override
    public Page<TradeJournalEntryResponse> getJournalEntriesByUser(String userId, Pageable pageable) {
        log.debug("Getting journal entries for user: {}", userId);
        return tradeJournalRepository.findByUserIdOrderByEntryDateDesc(userId, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public Page<TradeJournalEntryResponse> searchJournalEntries(
            String userId,
            String journalStatus,
            String entryType,
            String symbol,
            String setup,
            String folderId,
            String playbookId,
            List<String> tagIds,
            String tradeId,
            String portfolioId,
            LocalDate startDate,
            LocalDate endDate,
            String q,
            Pageable pageable) {

        LocalDateTime start = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime end = endDate != null ? endDate.atTime(LocalTime.MAX) : null;

        return tradeJournalRepository.searchEntries(
                        userId, journalStatus, entryType, symbol, setup, folderId, playbookId,
                        tagIds, tradeId, portfolioId, start, end, q, pageable)
                .map(this::convertToResponse);
    }

    @Override
    public List<TradeJournalEntryResponse> getJournalEntriesByTrade(String tradeId) {
        log.debug("Getting journal entries for trade: {}", tradeId);
        String userId = UserContext.getUserIdOrThrow();
        return tradeJournalRepository.findByUserIdAndTradeIdOrderByEntryDateDesc(userId, tradeId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<TradeJournalEntryResponse> getJournalEntriesByDateRange(
            String userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        return getJournalEntriesByDateRange(userId, startDate, endDate, null, pageable);
    }

    @Override
    public Page<TradeJournalEntryResponse> getJournalEntriesByDateRange(
            String userId, LocalDate startDate, LocalDate endDate, String portfolioId, Pageable pageable) {

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

        if (StringUtils.hasText(portfolioId)) {
            return tradeJournalRepository
                    .findByUserIdAndPortfolioIdAndEntryDateBetweenOrderByEntryDateDesc(
                            userId, portfolioId, startDateTime, endDateTime, pageable)
                    .map(this::convertToResponse);
        }

        return tradeJournalRepository
                .findByUserIdAndEntryDateBetweenOrderByEntryDateDesc(userId, startDateTime, endDateTime, pageable)
                .map(this::convertToResponse);
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse updateJournalEntry(String entryId, TradeJournalEntryRequest request) {
        log.debug("Updating journal entry with ID: {}", entryId);
        validateRequest(request);
        assertTradeIdOwnedIfPresent(request.getTradeId());
        assertPortfolioOwnedIfPresent(request.getPortfolioId());
        assertPortfolioMatchesTradeIfBothPresent(request.getPortfolioId(), request.getTradeId());

        TradeJournalEntry existingEntry = findOwnedEntry(entryId);

        // Null-safe merge: omit request nulls so classic/partial clients do not wipe fields.
        if (request.getTitle() != null) {
            existingEntry.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            existingEntry.setContent(request.getContent());
            syncContentPlain(existingEntry);
        }
        if (request.getEntryType() != null) {
            existingEntry.setEntryType(request.getEntryType());
        }
        if (request.getJournalStatus() != null) {
            existingEntry.setJournalStatus(request.getJournalStatus());
        }
        if (request.getSymbol() != null) {
            existingEntry.setSymbol(request.getSymbol());
        }
        if (request.getSetup() != null) {
            existingEntry.setSetup(request.getSetup());
        }
        if (request.getTradeDirection() != null) {
            existingEntry.setTradeDirection(request.getTradeDirection());
        }
        if (request.getFolderId() != null) {
            existingEntry.setFolderId(request.getFolderId());
        }
        if (request.getPlaybookId() != null) {
            existingEntry.setPlaybookId(request.getPlaybookId());
        }
        if (request.getPortfolioId() != null) {
            existingEntry.setPortfolioId(request.getPortfolioId());
        }
        if (StringUtils.hasText(request.getTradeId())) {
            existingEntry.setTradeId(request.getTradeId());
        }
        if (request.getPreTradePlan() != null) {
            existingEntry.setPreTradePlan(mergePreTradePlan(existingEntry.getPreTradePlan(), request.getPreTradePlan()));
        }
        if (request.getTradeExecution() != null) {
            existingEntry.setTradeExecution(
                    mergeTradeExecution(existingEntry.getTradeExecution(), request.getTradeExecution()));
        }
        if (request.getPostTradeReview() != null) {
            existingEntry.setPostTradeReview(
                    mergePostTradeReview(existingEntry.getPostTradeReview(), request.getPostTradeReview()));
        }
        if (request.getPlanAdherenceScore() != null) {
            existingEntry.setPlanAdherenceScore(request.getPlanAdherenceScore());
        }
        if (request.getChecklistCompletionPct() != null) {
            existingEntry.setChecklistCompletionPct(request.getChecklistCompletionPct());
        }
        if (request.getCustomFields() != null) {
            existingEntry.setCustomFields(request.getCustomFields());
        }
        if (request.getEntryDate() != null) {
            existingEntry.setEntryDate(request.getEntryDate());
        }
        if (request.getImageUrls() != null) {
            existingEntry.setImageUrls(request.getImageUrls());
        }
        if (request.getAttachments() != null) {
            existingEntry.setAttachments(request.getAttachments());
        }
        if (request.getChartUrls() != null) {
            existingEntry.setChartUrls(request.getChartUrls());
        }
        if (request.getDocumentUrls() != null) {
            existingEntry.setDocumentUrls(request.getDocumentUrls());
        }
        if (request.getVideoUrls() != null) {
            existingEntry.setVideoUrls(request.getVideoUrls());
        }
        if (request.getExternalUrls() != null) {
            existingEntry.setExternalUrls(request.getExternalUrls());
        }
        if (request.getRelatedTradeIds() != null) {
            existingEntry.setRelatedTradeIds(request.getRelatedTradeIds());
        }
        if (request.getTagIds() != null) {
            existingEntry.setTagIds(request.getTagIds());
        }
        if (request.getBehaviorPatternSummaries() != null) {
            existingEntry.setBehaviorPatternSummaries(request.getBehaviorPatternSummaries());
        }
        existingEntry.setUpdatedAt(LocalDateTime.now());

        applyAutoCalculations(existingEntry);

        TradeJournalEntry updatedEntry = tradeJournalRepository.save(existingEntry);
        log.info("Journal entry updated with ID: {}", updatedEntry.getId());
        return convertToResponse(updatedEntry);
    }

    @Override
    @CacheEvict(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public void deleteJournalEntry(String entryId) {
        log.debug("Deleting journal entry with ID: {}", entryId);
        findOwnedEntry(entryId);
        tradeJournalRepository.deleteById(entryId);
        log.info("Journal entry deleted with ID: {}", entryId);
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse updatePrePlan(String entryId, PreTradePlan preTradePlan) {
        log.debug("Updating pre-trade plan for journal entry ID: {}", entryId);
        TradeJournalEntry entry = findOwnedEntry(entryId);
        entry.setPreTradePlan(mergePreTradePlan(entry.getPreTradePlan(), preTradePlan));
        if (!StringUtils.hasText(entry.getJournalStatus())
                || JournalStatus.DRAFT.name().equalsIgnoreCase(entry.getJournalStatus())) {
            entry.setJournalStatus(JournalStatus.PLANNED.name());
        }
        applyAutoCalculations(entry);
        entry.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse updateExecution(String entryId, TradeExecution tradeExecution) {
        log.debug("Updating trade execution for journal entry ID: {}", entryId);
        TradeJournalEntry entry = findOwnedEntry(entryId);
        entry.setTradeExecution(mergeTradeExecution(entry.getTradeExecution(), tradeExecution));

        String status = entry.getJournalStatus();
        if (status == null
                || JournalStatus.PLANNED.name().equalsIgnoreCase(status)
                || JournalStatus.DRAFT.name().equalsIgnoreCase(status)) {
            entry.setJournalStatus(JournalStatus.OPEN.name());
        }

        applyAutoCalculations(entry);
        entry.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse updatePostReview(
            String entryId, PostTradeReview postTradeReview, Boolean markCompleted) {
        log.debug("Updating post-trade review for journal entry ID: {}", entryId);
        TradeJournalEntry entry = findOwnedEntry(entryId);
        entry.setPostTradeReview(mergePostTradeReview(entry.getPostTradeReview(), postTradeReview));
        applyAutoCalculations(entry);

        if (Boolean.TRUE.equals(markCompleted)) {
            entry.setJournalStatus(JournalStatus.COMPLETED.name());
        }

        entry.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse linkTrade(String entryId, String tradeId) {
        log.debug("Linking trade ID: {} to journal entry ID: {}", tradeId, entryId);
        if (!StringUtils.hasText(tradeId)) {
            throw new IllegalArgumentException("tradeId is required");
        }

        TradeJournalEntry entry = findOwnedEntry(entryId);
        TradeDetailsEntity trade = tradeDetailsRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found with ID: " + tradeId));
        assertTradeOwnedByCurrentUser(trade);

        entry.setTradeId(tradeId);

        if (!StringUtils.hasText(entry.getSymbol()) && StringUtils.hasText(trade.getSymbol())) {
            entry.setSymbol(trade.getSymbol());
        }

        if (entry.getTradeExecution() == null) {
            entry.setTradeExecution(new TradeExecution());
        }
        TradeExecution execution = entry.getTradeExecution();

        if (trade.getEntryInfo() != null) {
            if (execution.getEntryDateTime() == null && trade.getEntryInfo().getTimestamp() != null) {
                execution.setEntryDateTime(trade.getEntryInfo().getTimestamp());
            }
            if (execution.getActualEntryPrice() == null && trade.getEntryInfo().getPrice() != null) {
                execution.setActualEntryPrice(trade.getEntryInfo().getPrice().doubleValue());
            }
            if (execution.getQuantity() == null && trade.getEntryInfo().getQuantity() != null) {
                execution.setQuantity(trade.getEntryInfo().getQuantity().doubleValue());
            }
        }

        if (trade.getTradePositionType() != null && !StringUtils.hasText(entry.getTradeDirection())) {
            entry.setTradeDirection(trade.getTradePositionType().name());
        }

        if (entry.getPostTradeReview() == null) {
            entry.setPostTradeReview(new PostTradeReview());
        }
        PostTradeReview review = entry.getPostTradeReview();

        if (trade.getExitInfo() != null) {
            if (review.getExitDateTime() == null && trade.getExitInfo().getTimestamp() != null) {
                review.setExitDateTime(trade.getExitInfo().getTimestamp());
            }
            if (review.getActualExitPrice() == null && trade.getExitInfo().getPrice() != null) {
                review.setActualExitPrice(trade.getExitInfo().getPrice().doubleValue());
            }
        }

        if (review.getActualPnl() == null && trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
            review.setActualPnl(trade.getMetrics().getProfitLoss().doubleValue());
        }

        applyAutoCalculations(entry);
        entry.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    @Override
    public JournalSummaryResponse getSummary(String userId, LocalDate start, LocalDate end, String portfolioId) {
        try {
            LocalDateTime rangeStart = start.atStartOfDay();
            LocalDateTime rangeEnd = end.atTime(LocalTime.MAX);
            return tradeJournalRepository
                    .aggregateSummary(userId, rangeStart, rangeEnd, portfolioId)
                    .map(this::mapSummaryAggregation)
                    .orElseGet(() -> getSummaryInMemory(userId, start, end, portfolioId));
        } catch (Exception ex) {
            log.warn("Journal summary aggregation failed, using in-memory fallback: {}", ex.getMessage());
            return getSummaryInMemory(userId, start, end, portfolioId);
        }
    }

    private JournalSummaryResponse mapSummaryAggregation(JournalAnalyticsAggregation.Summary agg) {
        int pnlCount = agg.getPnlCount();
        int rrCount = agg.getRrCount();
        return JournalSummaryResponse.builder()
                .totalTrades(agg.getTotalTrades())
                .totalPnl(pnlCount > 0 ? round2(agg.getTotalPnl()) : 0.0)
                .winRate(pnlCount > 0 ? round2((agg.getWins() * 100.0) / pnlCount) : null)
                .avgRR(rrCount > 0 ? round2(agg.getRrSum() / rrCount) : null)
                .plannedCount(agg.getPlannedCount())
                .openCount(agg.getOpenCount())
                .completedCount(agg.getCompletedCount())
                .archivedCount(agg.getArchivedCount())
                .build();
    }

    private JournalSummaryResponse getSummaryInMemory(String userId, LocalDate start, LocalDate end, String portfolioId) {
        List<TradeJournalEntry> entries = loadEntriesInRange(userId, start, end, portfolioId);

        long planned = 0, open = 0, completed = 0, archived = 0;
        double totalPnl = 0;
        int pnlCount = 0;
        int wins = 0;
        double rrSum = 0;
        int rrCount = 0;
        long tradeLike = 0;

        for (TradeJournalEntry e : entries) {
            String status = e.getJournalStatus() != null ? e.getJournalStatus().toUpperCase() : "";
            switch (status) {
                case "PLANNED" -> planned++;
                case "OPEN" -> open++;
                case "COMPLETED" -> completed++;
                case "ARCHIVED" -> archived++;
                default -> { /* ignore */ }
            }

            if (isTradeLike(e)) {
                tradeLike++;
            }

            Double pnl = extractPnl(e);
            if (pnl != null) {
                totalPnl += pnl;
                pnlCount++;
                if (pnl > 0) {
                    wins++;
                }
            }

            Double rr = extractRR(e);
            if (rr != null) {
                rrSum += rr;
                rrCount++;
            }
        }

        return JournalSummaryResponse.builder()
                .totalTrades(tradeLike)
                .totalPnl(pnlCount > 0 ? round2(totalPnl) : 0.0)
                .winRate(pnlCount > 0 ? round2((wins * 100.0) / pnlCount) : null)
                .avgRR(rrCount > 0 ? round2(rrSum / rrCount) : null)
                .plannedCount(planned)
                .openCount(open)
                .completedCount(completed)
                .archivedCount(archived)
                .build();
    }

    @Override
    public List<MistakeAnalysisResponse> getMistakesSummary(String userId, LocalDate start, LocalDate end, String portfolioId) {
        try {
            LocalDateTime rangeStart = start.atStartOfDay();
            LocalDateTime rangeEnd = end.atTime(LocalTime.MAX);
            JournalAnalyticsAggregation.MistakesResult agg =
                    tradeJournalRepository.aggregateMistakes(userId, rangeStart, rangeEnd, portfolioId);
            return mapMistakesAggregation(agg);
        } catch (Exception ex) {
            log.warn("Journal mistakes aggregation failed, using in-memory fallback: {}", ex.getMessage());
            return getMistakesSummaryInMemory(userId, start, end, portfolioId);
        }
    }

    private List<MistakeAnalysisResponse> mapMistakesAggregation(
            JournalAnalyticsAggregation.MistakesResult agg) {
        final long totalLosses = agg.getTotalLosses();
        List<MistakeAnalysisResponse> results = new ArrayList<>();
        for (JournalAnalyticsAggregation.MistakeGroup group : agg.getGroups()) {
            long count = group.getOccurrenceCount();
            double impact = group.getTotalPnlImpact();
            long lossOccurrences = group.getLossOccurrences();
            results.add(MistakeAnalysisResponse.builder()
                    .mistakeCategory(group.getMistakeCategory())
                    .occurrenceCount(count)
                    .totalPnlImpact(round2(impact))
                    .avgPnlPerOccurrence(count > 0 ? round2(impact / count) : 0.0)
                    .percentageOfLosses(totalLosses > 0
                            ? round2((lossOccurrences * 100.0) / totalLosses)
                            : 0.0)
                    .build());
        }
        results.sort(Comparator.comparingLong(MistakeAnalysisResponse::getOccurrenceCount).reversed());
        return results;
    }

    private List<MistakeAnalysisResponse> getMistakesSummaryInMemory(
            String userId, LocalDate start, LocalDate end, String portfolioId) {
        List<TradeJournalEntry> entries = loadEntriesInRange(userId, start, end, portfolioId);

        Map<String, List<TradeJournalEntry>> byMistake = new LinkedHashMap<>();
        long lossCount = 0;

        for (TradeJournalEntry e : entries) {
            if (e.getPostTradeReview() == null) {
                continue;
            }
            String category = e.getPostTradeReview().getMistakeCategory();
            if (!StringUtils.hasText(category) || MistakeCategory.NONE.name().equalsIgnoreCase(category)) {
                continue;
            }
            byMistake.computeIfAbsent(category.toUpperCase(), k -> new ArrayList<>()).add(e);

            Double pnl = extractPnl(e);
            if (pnl != null && pnl < 0) {
                lossCount++;
            }
        }

        final long totalLosses = lossCount;
        List<MistakeAnalysisResponse> results = new ArrayList<>();

        for (Map.Entry<String, List<TradeJournalEntry>> mapEntry : byMistake.entrySet()) {
            List<TradeJournalEntry> group = mapEntry.getValue();
            double impact = 0;
            long lossOccurrences = 0;
            for (TradeJournalEntry e : group) {
                Double pnl = extractPnl(e);
                if (pnl != null) {
                    impact += pnl;
                    if (pnl < 0) {
                        lossOccurrences++;
                    }
                }
            }
            long count = group.size();
            results.add(MistakeAnalysisResponse.builder()
                    .mistakeCategory(mapEntry.getKey())
                    .occurrenceCount(count)
                    .totalPnlImpact(round2(impact))
                    .avgPnlPerOccurrence(count > 0 ? round2(impact / count) : 0.0)
                    .percentageOfLosses(totalLosses > 0 ? round2((lossOccurrences * 100.0) / totalLosses) : 0.0)
                    .build());
        }

        results.sort(Comparator.comparingLong(MistakeAnalysisResponse::getOccurrenceCount).reversed());
        return results;
    }

    @Override
    public List<LessonLearnedResponse> getLessons(String userId, LocalDate start, LocalDate end, int limit, String portfolioId) {
        int effectiveLimit = limit > 0 ? limit : 10;
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.atTime(LocalTime.MAX);
        return tradeJournalRepository
                .findLessonsInRange(userId, rangeStart, rangeEnd, portfolioId, effectiveLimit)
                .stream()
                .map(e -> LessonLearnedResponse.builder()
                        .entryId(e.getId())
                        .symbol(e.getSymbol())
                        .lessonLearned(e.getPostTradeReview().getLessonLearned())
                        .entryDate(e.getEntryDate())
                        .mistakeCategory(e.getPostTradeReview().getMistakeCategory())
                        .executionScore(e.getPostTradeReview().getExecutionScore())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public JournalAdherenceResponse getAdherence(String userId, LocalDate start, LocalDate end, String portfolioId) {
        try {
            LocalDateTime rangeStart = start.atStartOfDay();
            LocalDateTime rangeEnd = end.atTime(LocalTime.MAX);
            return tradeJournalRepository
                    .aggregateAdherence(userId, rangeStart, rangeEnd, portfolioId)
                    .map(this::mapAdherenceAggregation)
                    .orElseGet(() -> getAdherenceInMemory(userId, start, end, portfolioId));
        } catch (Exception ex) {
            log.warn("Journal adherence aggregation failed, using in-memory fallback: {}", ex.getMessage());
            return getAdherenceInMemory(userId, start, end, portfolioId);
        }
    }

    private JournalAdherenceResponse mapAdherenceAggregation(JournalAnalyticsAggregation.Adherence agg) {
        int adherenceCount = agg.getAdherenceCount();
        int checklistCount = agg.getChecklistCount();
        int execCount = agg.getExecCount();
        long sampleSize = Math.max(adherenceCount, Math.max(checklistCount, execCount));
        return JournalAdherenceResponse.builder()
                .avgPlanAdherenceScore(adherenceCount > 0 && agg.getAvgPlanAdherenceScore() != null
                        ? round2(agg.getAvgPlanAdherenceScore())
                        : null)
                .avgChecklistCompletionPct(checklistCount > 0 && agg.getAvgChecklistCompletionPct() != null
                        ? round2(agg.getAvgChecklistCompletionPct())
                        : null)
                .avgExecutionScore(execCount > 0 && agg.getAvgExecutionScore() != null
                        ? round2(agg.getAvgExecutionScore())
                        : null)
                .sampleSize(sampleSize)
                .build();
    }

    private JournalAdherenceResponse getAdherenceInMemory(String userId, LocalDate start, LocalDate end, String portfolioId) {
        List<TradeJournalEntry> entries = loadEntriesInRange(userId, start, end, portfolioId);

        double adherenceSum = 0;
        int adherenceCount = 0;
        double checklistSum = 0;
        int checklistCount = 0;
        double execSum = 0;
        int execCount = 0;

        for (TradeJournalEntry e : entries) {
            if (e.getPlanAdherenceScore() != null) {
                adherenceSum += e.getPlanAdherenceScore();
                adherenceCount++;
            }
            if (e.getChecklistCompletionPct() != null) {
                checklistSum += e.getChecklistCompletionPct();
                checklistCount++;
            }
            if (e.getPostTradeReview() != null && e.getPostTradeReview().getExecutionScore() != null) {
                execSum += e.getPostTradeReview().getExecutionScore();
                execCount++;
            }
        }

        long sampleSize = Math.max(adherenceCount, Math.max(checklistCount, execCount));

        return JournalAdherenceResponse.builder()
                .avgPlanAdherenceScore(adherenceCount > 0 ? round2(adherenceSum / adherenceCount) : null)
                .avgChecklistCompletionPct(checklistCount > 0 ? round2(checklistSum / checklistCount) : null)
                .avgExecutionScore(execCount > 0 ? round2(execSum / execCount) : null)
                .sampleSize(sampleSize)
                .build();
    }

    @Override
    public JournalReportCardResponse getReportCard(String userId, LocalDate start, LocalDate end, String portfolioId) {
        return JournalReportCardResponse.builder()
                .from(start)
                .to(end)
                .summary(getSummary(userId, start, end, portfolioId))
                .mistakes(getMistakesSummary(userId, start, end, portfolioId))
                .adherence(getAdherence(userId, start, end, portfolioId))
                .topLessons(getLessons(userId, start, end, 5, portfolioId))
                .build();
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse addAttachment(String entryId, JournalAttachmentRequest request) {
        log.debug("Adding attachment to journal entry ID: {}", entryId);
        TradeJournalEntry entry = findOwnedEntry(entryId);
        if (request == null || !StringUtils.hasText(request.getFileUrl())) {
            throw new IllegalArgumentException("fileUrl is required for attachment");
        }

        List<Attachment> attachments = entry.getAttachments();
        if (attachments == null) {
            attachments = new ArrayList<>();
            entry.setAttachments(attachments);
        }

        attachments.add(Attachment.builder()
                .fileName(request.getFileName())
                .fileUrl(request.getFileUrl())
                .fileType(request.getFileType())
                .description(request.getDescription())
                .uploadedAt(LocalDateTime.now())
                .build());

        entry.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    @Override
    @CachePut(value = JOURNAL_CACHE, key = JOURNAL_CACHE_KEY)
    public TradeJournalEntryResponse removeAttachment(String entryId, String fileUrl) {
        log.debug("Removing attachment from journal entry ID: {}", entryId);
        TradeJournalEntry entry = findOwnedEntry(entryId);
        if (entry.getAttachments() != null) {
            entry.getAttachments().removeIf(a -> Objects.equals(a.getFileUrl(), fileUrl));
        }
        entry.setUpdatedAt(LocalDateTime.now());
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    @Override
    public List<Attachment> listAttachments(String entryId) {
        TradeJournalEntry entry = findOwnedEntry(entryId);
        return entry.getAttachments() != null ? entry.getAttachments() : Collections.emptyList();
    }

    @Override
    @CacheEvict(value = JOURNAL_CACHE, allEntries = true)
    public int bulkArchive(String userId, List<String> entryIds) {
        List<TradeJournalEntry> entries = tradeJournalRepository.findByUserIdAndIdIn(userId, entryIds);
        LocalDateTime now = LocalDateTime.now();
        for (TradeJournalEntry e : entries) {
            e.setJournalStatus(JournalStatus.ARCHIVED.name());
            e.setUpdatedAt(now);
        }
        tradeJournalRepository.saveAll(entries);
        return entries.size();
    }

    @Override
    @CacheEvict(value = JOURNAL_CACHE, allEntries = true)
    public int bulkDelete(String userId, List<String> entryIds) {
        List<TradeJournalEntry> entries = tradeJournalRepository.findByUserIdAndIdIn(userId, entryIds);
        tradeJournalRepository.deleteAll(entries);
        return entries.size();
    }

    @Override
    @CacheEvict(value = JOURNAL_CACHE, allEntries = true)
    public int bulkAddTags(String userId, List<String> entryIds, List<String> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return 0;
        }
        List<TradeJournalEntry> entries = tradeJournalRepository.findByUserIdAndIdIn(userId, entryIds);
        LocalDateTime now = LocalDateTime.now();
        for (TradeJournalEntry e : entries) {
            Set<String> tags = new LinkedHashSet<>();
            if (e.getTagIds() != null) {
                tags.addAll(e.getTagIds());
            }
            tags.addAll(tagIds);
            e.setTagIds(new ArrayList<>(tags));
            e.setUpdatedAt(now);
        }
        tradeJournalRepository.saveAll(entries);
        return entries.size();
    }

    @Override
    public String exportCsv(String userId, LocalDate start, LocalDate end, String portfolioId) {
        List<TradeJournalEntry> entries = loadEntriesInRange(userId, start, end, portfolioId);
        StringBuilder csv = new StringBuilder();
        csv.append("id,symbol,title,entryType,journalStatus,entryDate,tradeDirection,pnl,rMultiple,mistakeCategory,planAdherenceScore,checklistCompletionPct\n");

        for (TradeJournalEntry e : entries) {
            Double pnl = extractPnl(e);
            Double rMult = e.getPostTradeReview() != null ? e.getPostTradeReview().getActualRMultiple() : null;
            String mistake = e.getPostTradeReview() != null ? e.getPostTradeReview().getMistakeCategory() : "";
            csv.append(csvCell(e.getId())).append(',')
                    .append(csvCell(e.getSymbol())).append(',')
                    .append(csvCell(e.getTitle())).append(',')
                    .append(csvCell(e.getEntryType())).append(',')
                    .append(csvCell(e.getJournalStatus())).append(',')
                    .append(csvCell(e.getEntryDate() != null ? e.getEntryDate().toString() : "")).append(',')
                    .append(csvCell(e.getTradeDirection())).append(',')
                    .append(pnl != null ? pnl : "").append(',')
                    .append(rMult != null ? rMult : "").append(',')
                    .append(csvCell(mistake)).append(',')
                    .append(e.getPlanAdherenceScore() != null ? e.getPlanAdherenceScore() : "").append(',')
                    .append(e.getChecklistCompletionPct() != null ? e.getChecklistCompletionPct() : "")
                    .append('\n');
        }
        return csv.toString();
    }

    @Override
    public List<TradeJournalEntryResponse> createFromTrades(FromTradesRequest request) {
        if (request == null || request.getTradeIds() == null || request.getTradeIds().isEmpty()) {
            throw new IllegalArgumentException("tradeIds are required");
        }

        String status = StringUtils.hasText(request.getJournalStatus())
                ? request.getJournalStatus()
                : JournalStatus.DRAFT.name();

        List<TradeJournalEntryResponse> created = new ArrayList<>();
        for (String tradeId : request.getTradeIds()) {
            created.add(createStubFromTrade(tradeId, status, request.getPortfolioId()));
        }
        return created;
    }

    @Override
    public JournalImportResponse importCsv(String csvContent, Boolean createJournalStubs) {
        if (!StringUtils.hasText(csvContent)) {
            throw new IllegalArgumentException("csv content is required");
        }

        boolean createStubs = createJournalStubs == null || Boolean.TRUE.equals(createJournalStubs);
        String[] lines = csvContent.replace("\r\n", "\n").replace('\r', '\n').split("\n");
        if (lines.length == 0) {
            throw new IllegalArgumentException("csv is empty");
        }

        String headerLine = lines[0].trim();
        if (!StringUtils.hasText(headerLine)) {
            throw new IllegalArgumentException("csv header is required");
        }

        String[] headers = splitCsvLine(headerLine);
        int tradeIdIdx = -1;
        for (int i = 0; i < headers.length; i++) {
            String h = headers[i].trim().replace("\"", "");
            if ("tradeId".equalsIgnoreCase(h)) {
                tradeIdIdx = i;
            }
        }
        if (tradeIdIdx < 0) {
            throw new IllegalArgumentException("csv header must include tradeId");
        }

        int created = 0;
        List<JournalImportError> errors = new ArrayList<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (!StringUtils.hasText(line) || !StringUtils.hasText(line.trim())) {
                continue;
            }
            int row = i + 1;
            try {
                String[] cols = splitCsvLine(line);
                String tradeId = tradeIdIdx < cols.length ? cols[tradeIdIdx].trim().replace("\"", "") : "";
                if (!StringUtils.hasText(tradeId)) {
                    throw new IllegalArgumentException("tradeId is required");
                }
                if (createStubs) {
                    createStubFromTrade(tradeId, JournalStatus.DRAFT.name(), null);
                    created++;
                }
            } catch (Exception e) {
                errors.add(JournalImportError.builder()
                        .row(row)
                        .message(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName())
                        .build());
            }
        }

        return JournalImportResponse.builder()
                .created(created)
                .errors(errors)
                .build();
    }

    // --- Helpers ---

    private TradeJournalEntryResponse createStubFromTrade(String tradeId, String status, String portfolioId) {
        String userId = UserContext.getUserIdOrThrow();
        LocalDateTime now = LocalDateTime.now();

        TradeDetailsEntity trade = tradeDetailsRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found with ID: " + tradeId));
        assertTradeOwnedByCurrentUser(trade);

        if (StringUtils.hasText(portfolioId) && StringUtils.hasText(trade.getPortfolioId())
                && !portfolioId.equals(trade.getPortfolioId())) {
            throw new IllegalArgumentException("portfolioId must match the trade's portfolio");
        }
        String resolvedPortfolioId = StringUtils.hasText(portfolioId) ? portfolioId : trade.getPortfolioId();
        assertPortfolioOwnedIfPresent(resolvedPortfolioId);

        List<TradeJournalEntry> existingEntries =
                tradeJournalRepository.findByUserIdAndTradeIdOrderByEntryDateDesc(userId, tradeId);
        if (!existingEntries.isEmpty()) {
            // Keep newest; drop duplicates from racey creates
            for (int i = 1; i < existingEntries.size(); i++) {
                tradeJournalRepository.delete(existingEntries.get(i));
            }
            TradeJournalEntry existing = existingEntries.get(0);
            // DRAFT stubs: refresh trade-sourced fields only (never wipe notes/tags/outcome)
            if (existing.getJournalStatus() == null
                    || JournalStatus.DRAFT.name().equalsIgnoreCase(existing.getJournalStatus())) {
                applyTradeSourcedFields(existing, trade, false);
                existing.setUpdatedAt(now);
                applyAutoCalculations(existing);
                return convertToResponse(tradeJournalRepository.save(existing));
            }
            return convertToResponse(existing);
        }

        TradeJournalEntry entry = TradeJournalEntry.builder()
                .id(UUID.randomUUID().toString())
                .userId(userId)
                .portfolioId(resolvedPortfolioId)
                .tradeId(tradeId)
                .title("Trade journal: " + (trade.getSymbol() != null ? trade.getSymbol() : tradeId))
                .entryType(JournalEntryType.TRADE_JOURNAL.name())
                .journalStatus(status)
                .entryDate(trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null
                        ? trade.getEntryInfo().getTimestamp()
                        : now)
                .createdAt(now)
                .updatedAt(now)
                .build();
        applyTradeSourcedFields(entry, trade, true);
        applyAutoCalculations(entry);
        return convertToResponse(tradeJournalRepository.save(entry));
    }

    /**
     * Copies buy/sell/PnL/symbol/direction from the trade. Never clears content, tags, or
     * user-set review narrative fields. When {@code replaceReviewShell} is false, only fills
     * null execution/review numeric/time fields and always updates symbol/direction/PnL/prices.
     */
    private void applyTradeSourcedFields(
            TradeJournalEntry entry, TradeDetailsEntity trade, boolean replaceReviewShell) {
        entry.setSymbol(trade.getSymbol());
        if (trade.getTradePositionType() != null) {
            entry.setTradeDirection(trade.getTradePositionType().name());
        }

        TradeExecution execution = entry.getTradeExecution() != null
                ? entry.getTradeExecution()
                : new TradeExecution();
        if (trade.getEntryInfo() != null) {
            if (trade.getEntryInfo().getTimestamp() != null
                    && (replaceReviewShell || execution.getEntryDateTime() == null)) {
                execution.setEntryDateTime(trade.getEntryInfo().getTimestamp());
            }
            if (trade.getEntryInfo().getPrice() != null) {
                execution.setActualEntryPrice(trade.getEntryInfo().getPrice().doubleValue());
            }
            if (trade.getEntryInfo().getQuantity() != null) {
                execution.setQuantity(trade.getEntryInfo().getQuantity().doubleValue());
            }
        }
        entry.setTradeExecution(execution);

        PostTradeReview review = entry.getPostTradeReview() != null
                ? entry.getPostTradeReview()
                : new PostTradeReview();
        if (trade.getExitInfo() != null) {
            if (trade.getExitInfo().getTimestamp() != null
                    && (replaceReviewShell || review.getExitDateTime() == null)) {
                review.setExitDateTime(trade.getExitInfo().getTimestamp());
            }
            if (trade.getExitInfo().getPrice() != null) {
                review.setActualExitPrice(trade.getExitInfo().getPrice().doubleValue());
            }
        }
        if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
            review.setActualPnl(trade.getMetrics().getProfitLoss().doubleValue());
        }
        entry.setPostTradeReview(review);
    }

    private PreTradePlan mergePreTradePlan(PreTradePlan existing, PreTradePlan incoming) {
        if (incoming == null) {
            return existing;
        }
        if (existing == null) {
            existing = new PreTradePlan();
        }
        if (incoming.getSetupDescription() != null) {
            existing.setSetupDescription(incoming.getSetupDescription());
        }
        if (incoming.getEntryRationale() != null) {
            existing.setEntryRationale(incoming.getEntryRationale());
        }
        if (incoming.getMarketContext() != null) {
            existing.setMarketContext(incoming.getMarketContext());
        }
        if (incoming.getStrategy() != null) {
            existing.setStrategy(incoming.getStrategy());
        }
        if (incoming.getSetup() != null) {
            existing.setSetup(incoming.getSetup());
        }
        if (incoming.getPlannedEntryPrice() != null) {
            existing.setPlannedEntryPrice(incoming.getPlannedEntryPrice());
        }
        if (incoming.getPlannedStopLoss() != null) {
            existing.setPlannedStopLoss(incoming.getPlannedStopLoss());
        }
        if (incoming.getPlannedTarget() != null) {
            existing.setPlannedTarget(incoming.getPlannedTarget());
        }
        if (incoming.getPlannedQuantity() != null) {
            existing.setPlannedQuantity(incoming.getPlannedQuantity());
        }
        if (incoming.getPlannedRiskAmount() != null) {
            existing.setPlannedRiskAmount(incoming.getPlannedRiskAmount());
        }
        if (incoming.getPlannedRiskPercent() != null) {
            existing.setPlannedRiskPercent(incoming.getPlannedRiskPercent());
        }
        if (incoming.getPlannedRRRatio() != null) {
            existing.setPlannedRRRatio(incoming.getPlannedRRRatio());
        }
        if (incoming.getSetupChecklist() != null) {
            existing.setSetupChecklist(incoming.getSetupChecklist());
        }
        if (incoming.getConfirmedChecklistItems() != null) {
            existing.setConfirmedChecklistItems(incoming.getConfirmedChecklistItems());
        }
        return existing;
    }

    private TradeExecution mergeTradeExecution(TradeExecution existing, TradeExecution incoming) {
        if (incoming == null) {
            return existing;
        }
        if (existing == null) {
            existing = new TradeExecution();
        }
        if (incoming.getEntryDateTime() != null) {
            existing.setEntryDateTime(incoming.getEntryDateTime());
        }
        if (incoming.getActualEntryPrice() != null) {
            existing.setActualEntryPrice(incoming.getActualEntryPrice());
        }
        if (incoming.getQuantity() != null) {
            existing.setQuantity(incoming.getQuantity());
        }
        if (incoming.getBroker() != null) {
            existing.setBroker(incoming.getBroker());
        }
        if (incoming.getOrderType() != null) {
            existing.setOrderType(incoming.getOrderType());
        }
        if (incoming.getExternalOrderId() != null) {
            existing.setExternalOrderId(incoming.getExternalOrderId());
        }
        if (incoming.getNotes() != null) {
            existing.setNotes(incoming.getNotes());
        }
        return existing;
    }

    private PostTradeReview mergePostTradeReview(PostTradeReview existing, PostTradeReview incoming) {
        if (incoming == null) {
            return existing;
        }
        if (existing == null) {
            existing = new PostTradeReview();
        }
        if (incoming.getExitDateTime() != null) {
            existing.setExitDateTime(incoming.getExitDateTime());
        }
        if (incoming.getActualExitPrice() != null) {
            existing.setActualExitPrice(incoming.getActualExitPrice());
        }
        if (incoming.getActualPnl() != null) {
            existing.setActualPnl(incoming.getActualPnl());
        }
        if (incoming.getActualRMultiple() != null) {
            existing.setActualRMultiple(incoming.getActualRMultiple());
        }
        if (incoming.getFollowedStopLoss() != null) {
            existing.setFollowedStopLoss(incoming.getFollowedStopLoss());
        }
        if (incoming.getFollowedTarget() != null) {
            existing.setFollowedTarget(incoming.getFollowedTarget());
        }
        if (incoming.getTradeOutcome() != null) {
            existing.setTradeOutcome(incoming.getTradeOutcome());
        }
        if (incoming.getWhatWentWell() != null) {
            existing.setWhatWentWell(incoming.getWhatWentWell());
        }
        if (incoming.getWhatCouldBeImproved() != null) {
            existing.setWhatCouldBeImproved(incoming.getWhatCouldBeImproved());
        }
        if (incoming.getLessonLearned() != null) {
            existing.setLessonLearned(incoming.getLessonLearned());
        }
        if (incoming.getMistakeCategory() != null) {
            existing.setMistakeCategory(incoming.getMistakeCategory());
        }
        if (incoming.getExecutionScore() != null) {
            existing.setExecutionScore(incoming.getExecutionScore());
        }
        if (incoming.getEmotionalState() != null) {
            existing.setEmotionalState(incoming.getEmotionalState());
        }
        if (incoming.getPostChecklist() != null) {
            existing.setPostChecklist(incoming.getPostChecklist());
        }
        if (incoming.getCompletedChecklistItems() != null) {
            existing.setCompletedChecklistItems(incoming.getCompletedChecklistItems());
        }
        return existing;
    }

    private void assertTradeIdOwnedIfPresent(String tradeId) {
        if (!StringUtils.hasText(tradeId)) {
            return;
        }
        TradeDetailsEntity trade = tradeDetailsRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found with ID: " + tradeId));
        assertTradeOwnedByCurrentUser(trade);
    }

    private String[] splitCsvLine(String line) {
        return line.split(",", -1);
    }

    TradeJournalEntry convertToEntity(TradeJournalEntryRequest request) {
        return TradeJournalEntry.builder()
                .userId(UserContext.getUserIdOrThrow())
                .portfolioId(request.getPortfolioId())
                .tradeId(request.getTradeId())
                .title(request.getTitle())
                .content(request.getContent())
                .entryType(request.getEntryType())
                .journalStatus(request.getJournalStatus())
                .symbol(request.getSymbol())
                .setup(request.getSetup())
                .tradeDirection(request.getTradeDirection())
                .folderId(request.getFolderId())
                .playbookId(request.getPlaybookId())
                .preTradePlan(request.getPreTradePlan())
                .tradeExecution(request.getTradeExecution())
                .postTradeReview(request.getPostTradeReview())
                .planAdherenceScore(request.getPlanAdherenceScore())
                .checklistCompletionPct(request.getChecklistCompletionPct())
                .customFields(request.getCustomFields())
                .entryDate(request.getEntryDate())
                .imageUrls(request.getImageUrls())
                .attachments(request.getAttachments())
                .chartUrls(request.getChartUrls())
                .documentUrls(request.getDocumentUrls())
                .videoUrls(request.getVideoUrls())
                .externalUrls(request.getExternalUrls())
                .relatedTradeIds(request.getRelatedTradeIds())
                .tagIds(request.getTagIds())
                .behaviorPatternSummaries(request.getBehaviorPatternSummaries())
                .build();
    }

    TradeJournalEntryResponse convertToResponse(TradeJournalEntry entry) {
        return TradeJournalEntryResponse.builder()
                .id(entry.getId())
                .userId(entry.getUserId())
                .portfolioId(entry.getPortfolioId())
                .tradeId(entry.getTradeId())
                .title(entry.getTitle())
                .content(entry.getContent())
                .entryType(entry.getEntryType())
                .journalStatus(entry.getJournalStatus())
                .symbol(entry.getSymbol())
                .setup(entry.getSetup())
                .tradeDirection(entry.getTradeDirection())
                .folderId(entry.getFolderId())
                .playbookId(entry.getPlaybookId())
                .preTradePlan(entry.getPreTradePlan())
                .tradeExecution(entry.getTradeExecution())
                .postTradeReview(entry.getPostTradeReview())
                .planAdherenceScore(entry.getPlanAdherenceScore())
                .checklistCompletionPct(entry.getChecklistCompletionPct())
                .customFields(entry.getCustomFields())
                .entryDate(entry.getEntryDate())
                .imageUrls(entry.getImageUrls())
                .attachments(entry.getAttachments())
                .chartUrls(entry.getChartUrls())
                .documentUrls(entry.getDocumentUrls())
                .videoUrls(entry.getVideoUrls())
                .externalUrls(entry.getExternalUrls())
                .relatedTradeIds(entry.getRelatedTradeIds())
                .tagIds(entry.getTagIds())
                .behaviorPatternSummaries(entry.getBehaviorPatternSummaries())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }

    void applyAutoCalculations(TradeJournalEntry entry) {
        computePlannedRiskAndRR(entry.getPreTradePlan());
        computeActualPnlAndR(entry);
        entry.setChecklistCompletionPct(computeChecklistCompletion(entry));
        entry.setPlanAdherenceScore(computePlanAdherence(entry));
    }

    private void computePlannedRiskAndRR(PreTradePlan plan) {
        if (plan == null) {
            return;
        }
        Double entry = plan.getPlannedEntryPrice();
        Double stop = plan.getPlannedStopLoss();
        Double target = plan.getPlannedTarget();
        Double qty = plan.getPlannedQuantity() != null ? plan.getPlannedQuantity() : 1.0;

        if (entry != null && stop != null && plan.getPlannedRiskAmount() == null) {
            plan.setPlannedRiskAmount(Math.abs(entry - stop) * qty);
        }
        if (entry != null && stop != null && target != null && plan.getPlannedRRRatio() == null) {
            double risk = Math.abs(entry - stop);
            if (risk > 0) {
                plan.setPlannedRRRatio(Math.abs(target - entry) / risk);
            }
        }
    }

    private void computeActualPnlAndR(TradeJournalEntry entry) {
        PostTradeReview review = entry.getPostTradeReview();
        TradeExecution execution = entry.getTradeExecution();
        PreTradePlan plan = entry.getPreTradePlan();

        if (review == null || execution == null) {
            return;
        }

        Double entryPrice = execution.getActualEntryPrice();
        Double exitPrice = review.getActualExitPrice();
        Double qty = execution.getQuantity() != null ? execution.getQuantity() : 1.0;

        if (review.getActualPnl() == null && entryPrice != null && exitPrice != null) {
            boolean isShort = TradeDirection.SHORT.name().equalsIgnoreCase(entry.getTradeDirection());
            double pnl = isShort
                    ? (entryPrice - exitPrice) * qty
                    : (exitPrice - entryPrice) * qty;
            review.setActualPnl(pnl);
        }

        if (review.getActualRMultiple() == null && review.getActualPnl() != null && plan != null) {
            Double risk = plan.getPlannedRiskAmount();
            if (risk == null && plan.getPlannedEntryPrice() != null && plan.getPlannedStopLoss() != null) {
                risk = Math.abs(plan.getPlannedEntryPrice() - plan.getPlannedStopLoss()) * qty;
            }
            if (risk != null && risk > 0) {
                review.setActualRMultiple(review.getActualPnl() / risk);
            }
        }
    }

    private Double computeChecklistCompletion(TradeJournalEntry entry) {
        int total = 0;
        int done = 0;

        PreTradePlan plan = entry.getPreTradePlan();
        if (plan != null && plan.getSetupChecklist() != null && !plan.getSetupChecklist().isEmpty()) {
            total += plan.getSetupChecklist().size();
            if (plan.getConfirmedChecklistItems() != null) {
                done += plan.getConfirmedChecklistItems().size();
            }
        }

        PostTradeReview review = entry.getPostTradeReview();
        if (review != null && review.getPostChecklist() != null && !review.getPostChecklist().isEmpty()) {
            total += review.getPostChecklist().size();
            if (review.getCompletedChecklistItems() != null) {
                done += review.getCompletedChecklistItems().size();
            }
        }

        if (total == 0) {
            return entry.getChecklistCompletionPct();
        }
        return round2((done * 100.0) / total);
    }

    private Double computePlanAdherence(TradeJournalEntry entry) {
        PostTradeReview review = entry.getPostTradeReview();
        if (review == null) {
            return entry.getPlanAdherenceScore();
        }

        List<Double> parts = new ArrayList<>();

        if (StringUtils.hasText(review.getFollowedStopLoss())) {
            parts.add(isFollowed(review.getFollowedStopLoss()) ? 100.0 : 0.0);
        }
        if (StringUtils.hasText(review.getFollowedTarget())) {
            parts.add(isFollowed(review.getFollowedTarget()) ? 100.0 : 0.0);
        }

        Double checklist = entry.getChecklistCompletionPct();
        if (checklist == null) {
            checklist = computeChecklistCompletion(entry);
        }
        if (checklist != null) {
            parts.add(checklist);
        }

        if (parts.isEmpty()) {
            return entry.getPlanAdherenceScore();
        }

        double sum = 0;
        for (Double p : parts) {
            sum += p;
        }
        return round2(sum / parts.size());
    }

    private boolean isFollowed(String value) {
        return FOLLOWED_TRUE.contains(value.trim().toUpperCase());
    }

    private TradeJournalEntry findOwnedEntry(String entryId) {
        TradeJournalEntry entry = tradeJournalRepository.findById(entryId)
                .orElseThrow(() -> new IllegalArgumentException("Journal entry not found with ID: " + entryId));

        String currentUser = UserContext.getUserIdOrThrow();
        if (!currentUser.equals(entry.getUserId())) {
            throw new IllegalArgumentException("Cannot access journal entry that belongs to another user");
        }
        return entry;
    }

    private void validateRequest(TradeJournalEntryRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Title is required");
        }
        if (request.getEntryDate() == null) {
            throw new IllegalArgumentException("Entry date is required");
        }
    }

    private void assertTradeOwnedByCurrentUser(TradeDetailsEntity trade) {
        String userId = UserContext.getUserIdOrThrow();
        if (trade.getUserId() == null || !userId.equals(trade.getUserId())) {
            throw new IllegalArgumentException("Cannot use a trade belonging to another user");
        }
    }

    private void assertPortfolioOwnedIfPresent(String portfolioId) {
        if (!StringUtils.hasText(portfolioId)) {
            return;
        }
        String userId = UserContext.getUserIdOrThrow();
        Optional<PortfolioModel> portfolio = portfolioPersistenceService.findByPortfolioId(portfolioId);
        if (portfolio.isEmpty()) {
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }
        if (!userId.equals(portfolio.get().getOwnerId())) {
            throw new IllegalArgumentException("You are not authorized to use this portfolio");
        }
    }

    private void assertPortfolioMatchesTradeIfBothPresent(String portfolioId, String tradeId) {
        if (!StringUtils.hasText(portfolioId) || !StringUtils.hasText(tradeId)) {
            return;
        }
        TradeDetailsEntity trade = tradeDetailsRepository.findByTradeId(tradeId)
                .orElseThrow(() -> new IllegalArgumentException("Trade not found with ID: " + tradeId));
        if (StringUtils.hasText(trade.getPortfolioId()) && !portfolioId.equals(trade.getPortfolioId())) {
            throw new IllegalArgumentException("portfolioId must match the trade's portfolio");
        }
    }

    private List<TradeJournalEntry> loadEntriesInRange(
            String userId, LocalDate start, LocalDate end, String portfolioId) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end dates are required");
        }
        LocalDateTime rangeStart = start.atStartOfDay();
        LocalDateTime rangeEnd = end.atTime(LocalTime.MAX);
        if (StringUtils.hasText(portfolioId)) {
            return tradeJournalRepository.findByUserIdAndPortfolioIdAndEntryDateBetween(
                    userId, portfolioId, rangeStart, rangeEnd);
        }
        return tradeJournalRepository.findByUserIdAndEntryDateBetween(userId, rangeStart, rangeEnd);
    }

    private void syncContentPlain(TradeJournalEntry entry) {
        entry.setContentPlain(JournalContentPlainText.extract(entry.getContent()));
    }

    private boolean isTradeLike(TradeJournalEntry e) {
        if (StringUtils.hasText(e.getTradeId())) {
            return true;
        }
        String type = e.getEntryType();
        if (type == null) {
            return false;
        }
        return JournalEntryType.TRADE_JOURNAL.name().equalsIgnoreCase(type)
                || JournalEntryType.PRE_PLAN.name().equalsIgnoreCase(type)
                || JournalEntryType.POST_REVIEW.name().equalsIgnoreCase(type)
                || JournalEntryType.TRADE_NOTE.name().equalsIgnoreCase(type)
                || JournalEntryType.MISSED.name().equalsIgnoreCase(type);
    }

    private Double extractPnl(TradeJournalEntry e) {
        if (e.getPostTradeReview() != null && e.getPostTradeReview().getActualPnl() != null) {
            return e.getPostTradeReview().getActualPnl();
        }
        return null;
    }

    private Double extractRR(TradeJournalEntry e) {
        if (e.getPostTradeReview() != null && e.getPostTradeReview().getActualRMultiple() != null) {
            return e.getPostTradeReview().getActualRMultiple();
        }
        if (e.getPreTradePlan() != null && e.getPreTradePlan().getPlannedRRRatio() != null) {
            return e.getPreTradePlan().getPlannedRRRatio();
        }
        return null;
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private String csvCell(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
