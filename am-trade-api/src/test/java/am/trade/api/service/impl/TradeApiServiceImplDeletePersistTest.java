package am.trade.api.service.impl;

import am.trade.api.service.FavoriteFilterService;
import am.trade.api.service.TradeManagementService;
import am.trade.api.validation.TradeValidator;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.services.publisher.TradeHoldingEventPublisher;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.PortfolioSyncInstrumentResolver;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Edge cases: DELETE / DELETE_PORTFOLIO must not re-persist deleted trades.
 */
@ExtendWith(MockitoExtension.class)
class TradeApiServiceImplDeletePersistTest {

    @Mock private TradeManagementService tradeManagementService;
    @Mock private TradeProcessingService tradeProcessingService;
    @Mock private TradeDetailsService tradeDetailsService;
    @Mock private PortfolioPersistenceService portfolioPersistenceService;
    @Mock private TradeValidator tradeValidator;
    @Mock private FavoriteFilterService favoriteFilterService;
    @Mock private TradeHoldingEventPublisher tradeHoldingEventPublisher;
    @Mock private PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;

    private TradeApiServiceImpl service;

    @BeforeEach
    void setUp() {
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

    @Test
    void publishPortfolioSyncEvent_delete_doesNotResolveOrSave() {
        TradeDetails trade = TradeDetails.builder()
                .tradeId("del-1")
                .portfolioId("p1")
                .userId("u1")
                .symbol("RELIANCE")
                .instrumentInfo(InstrumentInfo.builder().symbol("RELIANCE").build())
                .build();

        ReflectionTestUtils.invokeMethod(service, "publishPortfolioSyncEvent", trade, "DELETE");

        verify(portfolioSyncInstrumentResolver, never()).resolveForSync(any(TradeDetails.class));
        verify(tradeDetailsService, never()).saveTradeDetails(any());
        verify(tradeHoldingEventPublisher).publishHoldingUpdate(any());
    }

    @Test
    void publishBulkPortfolioSyncEvent_deletePortfolio_doesNotSaveTrades() {
        TradeDetails trade = TradeDetails.builder()
                .tradeId("del-2")
                .portfolioId("p1")
                .userId("u1")
                .symbol("TCS")
                .instrumentInfo(InstrumentInfo.builder().symbol("TCS").build())
                .build();

        service.publishBulkPortfolioSyncEvent("p1", "Upstox", "u1", List.of(trade), "DELETE_PORTFOLIO");

        verify(portfolioSyncInstrumentResolver, never()).resolveForSync(anyList());
        verify(tradeDetailsService, never()).saveAllTradeDetails(anyList());
        verify(tradeHoldingEventPublisher).publishHoldingUpdate(any());
    }

    @Test
    void publishBulkPortfolioSyncEvent_replaceAll_doesResolveAndSave() {
        TradeDetails trade = TradeDetails.builder()
                .tradeId("live-1")
                .portfolioId("p1")
                .userId("u1")
                .symbol("INFY")
                .instrumentInfo(InstrumentInfo.builder().symbol("INFY").build())
                .build();

        service.publishBulkPortfolioSyncEvent("p1", "Upstox", "u1", List.of(trade), "REPLACE_ALL");

        verify(portfolioSyncInstrumentResolver).resolveForSync(anyList());
        verify(tradeDetailsService).saveAllTradeDetails(anyList());
        verify(tradeHoldingEventPublisher).publishHoldingUpdate(any());
    }
}
