package am.trade.api.service;

import am.trade.api.dto.*;
import am.trade.common.models.Attachment;
import am.trade.common.models.PostTradeReview;
import am.trade.common.models.PreTradePlan;
import am.trade.common.models.TradeExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

/**
 * Service for managing trade journal entries
 */
public interface TradeJournalService {

    TradeJournalEntryResponse createJournalEntry(TradeJournalEntryRequest request);

    TradeJournalEntryResponse getJournalEntry(String entryId);

    Page<TradeJournalEntryResponse> getJournalEntriesByUser(String userId, Pageable pageable);

    /**
     * Filtered search — optional filters; userId required
     */
    Page<TradeJournalEntryResponse> searchJournalEntries(
            String userId,
            String journalStatus,
            String entryType,
            String symbol,
            String setup,
            String folderId,
            String playbookId,
            List<String> tagIds,
            String tradeId,
            LocalDate startDate,
            LocalDate endDate,
            String q,
            Pageable pageable);

    List<TradeJournalEntryResponse> getJournalEntriesByTrade(String tradeId);

    Page<TradeJournalEntryResponse> getJournalEntriesByDateRange(
            String userId, LocalDate startDate, LocalDate endDate, Pageable pageable);

    TradeJournalEntryResponse updateJournalEntry(String entryId, TradeJournalEntryRequest request);

    void deleteJournalEntry(String entryId);

    TradeJournalEntryResponse updatePrePlan(String entryId, PreTradePlan preTradePlan);

    TradeJournalEntryResponse updateExecution(String entryId, TradeExecution tradeExecution);

    TradeJournalEntryResponse updatePostReview(String entryId, PostTradeReview postTradeReview, Boolean markCompleted);

    TradeJournalEntryResponse linkTrade(String entryId, String tradeId);

    JournalSummaryResponse getSummary(String userId, LocalDate start, LocalDate end);

    List<MistakeAnalysisResponse> getMistakesSummary(String userId, LocalDate start, LocalDate end);

    List<LessonLearnedResponse> getLessons(String userId, LocalDate start, LocalDate end, int limit);

    JournalAdherenceResponse getAdherence(String userId, LocalDate start, LocalDate end);

    JournalReportCardResponse getReportCard(String userId, LocalDate start, LocalDate end);

    TradeJournalEntryResponse addAttachment(String entryId, JournalAttachmentRequest request);

    TradeJournalEntryResponse removeAttachment(String entryId, String fileUrl);

    List<Attachment> listAttachments(String entryId);

    int bulkArchive(String userId, List<String> entryIds);

    int bulkDelete(String userId, List<String> entryIds);

    int bulkAddTags(String userId, List<String> entryIds, List<String> tagIds);

    String exportCsv(String userId, LocalDate start, LocalDate end);

    List<TradeJournalEntryResponse> createFromTrades(FromTradesRequest request);
}
