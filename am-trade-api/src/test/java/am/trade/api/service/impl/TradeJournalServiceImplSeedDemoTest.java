package am.trade.api.service.impl;

import am.trade.api.dto.TradeJournalEntryResponse;
import am.trade.common.models.JournalDemoSeedState;
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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeJournalServiceImplSeedDemoTest {

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
        UserContext.setUserId("demo-user");
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void seedDemoEntriesIfEmpty_whenEmpty_createsLogAndTradeDemos() {
        when(journalDemoSeedStateRepository.existsById("demo-user")).thenReturn(false);
        when(tradeJournalRepository.countByUserId("demo-user")).thenReturn(0L);
        when(tradeJournalRepository.save(any(TradeJournalEntry.class))).thenAnswer(inv -> {
            TradeJournalEntry e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId("id-" + e.getEntryType());
            }
            return e;
        });

        List<TradeJournalEntryResponse> created = service.seedDemoEntriesIfEmpty();

        assertEquals(2, created.size());
        assertTrue(created.stream().anyMatch(e -> "DAILY".equals(e.getEntryType())));
        assertTrue(created.stream().anyMatch(e -> "TRADE_JOURNAL".equals(e.getEntryType())));
        assertTrue(created.stream().allMatch(e ->
                e.getCustomFields() != null && Boolean.TRUE.equals(e.getCustomFields().get("demo"))));

        verify(tradeJournalRepository, times(2)).save(any(TradeJournalEntry.class));
        ArgumentCaptor<JournalDemoSeedState> stateCaptor = ArgumentCaptor.forClass(JournalDemoSeedState.class);
        verify(journalDemoSeedStateRepository).save(stateCaptor.capture());
        assertEquals("demo-user", stateCaptor.getValue().getUserId());
        assertEquals(2, stateCaptor.getValue().getEntriesCreated());
    }

    @Test
    void seedDemoEntriesIfEmpty_whenAlreadySeeded_isNoOp() {
        when(journalDemoSeedStateRepository.existsById("demo-user")).thenReturn(true);

        List<TradeJournalEntryResponse> created = service.seedDemoEntriesIfEmpty();

        assertTrue(created.isEmpty());
        verify(tradeJournalRepository, never()).save(any());
        verify(journalDemoSeedStateRepository, never()).save(any());
    }

    @Test
    void seedDemoEntriesIfEmpty_whenUserHasEntriesButNoMarker_stillCreatesDemos() {
        when(journalDemoSeedStateRepository.existsById("demo-user")).thenReturn(false);
        when(tradeJournalRepository.save(any(TradeJournalEntry.class))).thenAnswer(inv -> {
            TradeJournalEntry e = inv.getArgument(0);
            if (e.getId() == null) {
                e.setId("id-" + e.getEntryType());
            }
            return e;
        });

        List<TradeJournalEntryResponse> created = service.seedDemoEntriesIfEmpty();

        assertEquals(2, created.size());
        verify(tradeJournalRepository, times(2)).save(any(TradeJournalEntry.class));
        verify(journalDemoSeedStateRepository).save(any(JournalDemoSeedState.class));
    }
}
