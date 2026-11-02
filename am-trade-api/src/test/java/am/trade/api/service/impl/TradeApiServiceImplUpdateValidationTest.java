package am.trade.api.service.impl;

import am.trade.api.service.FavoriteFilterService;
import am.trade.api.service.TradeManagementService;
import am.trade.api.validation.TradeValidator;
import am.trade.common.models.EntryExitInfo;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeMetrics;
import am.trade.models.enums.Exchange;
import am.trade.models.enums.MarketSegment;
import am.trade.models.enums.TradePositionType;
import am.trade.models.enums.TradeStatus;
import am.trade.services.publisher.TradeHoldingEventPublisher;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.PortfolioSyncInstrumentResolver;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeApiServiceImplUpdateValidationTest {

    @Mock private TradeManagementService tradeManagementService;
    @Mock private TradeProcessingService tradeProcessingService;
    @Mock private TradeDetailsService tradeDetailsService;
    @Mock private PortfolioPersistenceService portfolioPersistenceService;
    @Mock private FavoriteFilterService favoriteFilterService;
    @Mock private TradeHoldingEventPublisher tradeHoldingEventPublisher;
    @Mock private PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;

    private TradeApiServiceImpl service;
    private final TradeValidator tradeValidator = new TradeValidator();

    @BeforeEach
    void setUp() {
        UserContext.setUserId("owner-1");
        service = new TradeApiServiceImpl(
                tradeManagementService,
                tradeProcessingService,
                tradeDetailsService,
                portfolioPersistenceService,
                tradeValidator,
                favoriteFilterService,
                tradeHoldingEventPublisher,
                portfolioSyncInstrumentResolver);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void updateTrade_closedWithoutExit_rejectsAndDoesNotSave() {
        TradeDetails original = ownedOpenTrade("t-1");
        when(tradeDetailsService.findModelByTradeId("t-1")).thenReturn(Optional.of(original));

        TradeDetails update = ownedOpenTrade("t-1");
        update.setStatus(TradeStatus.WIN);
        update.setExitInfo(null);
        update.setMetrics(TradeMetrics.builder()
                .profitLoss(new BigDecimal("5000"))
                .build());

        assertThrows(IllegalArgumentException.class, () -> service.updateTrade("t-1", update));
        verify(tradeDetailsService, never()).saveTradeDetails(any());
    }

    @Test
    void updateTrade_validClosed_clearsClientMetricsBeforeSave() {
        TradeDetails original = ownedOpenTrade("t-2");
        when(tradeDetailsService.findModelByTradeId("t-2")).thenReturn(Optional.of(original));
        when(tradeDetailsService.saveTradeDetails(any())).thenAnswer(inv -> inv.getArgument(0));

        TradeDetails update = ownedOpenTrade("t-2");
        update.setStatus(TradeStatus.WIN);
        update.setExitInfo(EntryExitInfo.builder()
                .timestamp(original.getEntryInfo().getTimestamp().plusHours(1))
                .price(new BigDecimal("120"))
                .quantity(5)
                .build());
        update.setMetrics(TradeMetrics.builder()
                .profitLoss(new BigDecimal("99999"))
                .build());

        service.updateTrade("t-2", update);

        ArgumentCaptor<TradeDetails> captor = ArgumentCaptor.forClass(TradeDetails.class);
        verify(tradeDetailsService).saveTradeDetails(captor.capture());
        assertNull(captor.getValue().getMetrics());
    }

    private static TradeDetails ownedOpenTrade(String tradeId) {
        LocalDateTime now = LocalDateTime.of(2026, 11, 2, 9, 30);
        return TradeDetails.builder()
                .tradeId(tradeId)
                .portfolioId("pf-1")
                .userId("owner-1")
                .symbol("INFY")
                .status(TradeStatus.OPEN)
                .tradePositionType(TradePositionType.LONG)
                .instrumentInfo(InstrumentInfo.builder()
                        .symbol("INFY")
                        .exchange(Exchange.NSE)
                        .segment(MarketSegment.EQ)
                        .build())
                .entryInfo(EntryExitInfo.builder()
                        .timestamp(now)
                        .price(new BigDecimal("1500"))
                        .quantity(5)
                        .build())
                .build();
    }
}
