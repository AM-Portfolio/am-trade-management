package am.trade.api.service.impl;

import am.trade.api.dto.TradeJournalEntryRequest;
import am.trade.api.dto.TradeJournalEntryResponse;
import am.trade.common.models.JournalStatus;
import am.trade.common.models.TradeJournalEntry;
import am.trade.persistence.repository.JournalDemoSeedStateRepository;
import am.trade.persistence.repository.TradeDetailsRepository;
import am.trade.persistence.repository.TradeJournalRepository;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeJournalServiceImplDraftStatusTest {

    @Mock
    private TradeJournalRepository tradeJournalRepository;
    @Mock
    private TradeDetailsRepository tradeDetailsRepository;
    @Mock
    private JournalDemoSeedStateRepository journalDemoSeedStateRepository;

    @InjectMocks
    private TradeJournalServiceImpl service;

    @BeforeEach
    void setUp() {
        UserContext.setUserId("draft-user");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void normalizeJournalStatus_blankBecomesDraft() {
        assertEquals(JournalStatus.DRAFT.name(), TradeJournalServiceImpl.normalizeJournalStatus(null));
        assertEquals(JournalStatus.DRAFT.name(), TradeJournalServiceImpl.normalizeJournalStatus("  "));
        assertEquals(JournalStatus.COMPLETED.name(),
                TradeJournalServiceImpl.normalizeJournalStatus("completed"));
        assertEquals(JournalStatus.DRAFT.name(),
                TradeJournalServiceImpl.normalizeJournalStatus("not-a-status"));
    }

    @Test
    void createJournalEntry_whenStatusOmitted_persistsDraft() {
        when(tradeJournalRepository.save(any(TradeJournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeJournalEntryRequest request = TradeJournalEntryRequest.builder()
                .title("Untitled draft")
                .entryType("DAILY")
                .entryDate(LocalDateTime.now())
                .build();

        TradeJournalEntryResponse saved = service.createJournalEntry(request);

        ArgumentCaptor<TradeJournalEntry> captor = ArgumentCaptor.forClass(TradeJournalEntry.class);
        verify(tradeJournalRepository).save(captor.capture());
        assertEquals(JournalStatus.DRAFT.name(), captor.getValue().getJournalStatus());
        assertEquals(JournalStatus.DRAFT.name(), saved.getJournalStatus());
    }

    @Test
    void createJournalEntry_whenStatusProvided_keepsCanonicalName() {
        when(tradeJournalRepository.save(any(TradeJournalEntry.class))).thenAnswer(inv -> inv.getArgument(0));

        TradeJournalEntryRequest request = TradeJournalEntryRequest.builder()
                .title("Done log")
                .entryType("DAILY")
                .journalStatus("completed")
                .entryDate(LocalDateTime.now())
                .build();

        TradeJournalEntryResponse saved = service.createJournalEntry(request);

        assertEquals(JournalStatus.COMPLETED.name(), saved.getJournalStatus());
    }
}
