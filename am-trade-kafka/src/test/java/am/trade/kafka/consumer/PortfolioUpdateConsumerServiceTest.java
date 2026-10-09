package am.trade.kafka.consumer;

import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.models.kafka.inbound.InboundEquityModel;
import am.trade.models.kafka.inbound.PortfolioUpdateInboundEvent;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.PortfolioSyncInstrumentResolver;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.TradeProcessingService;
import am.trade.services.service.TradeSummaryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioUpdateConsumerServiceTest {

    @Mock private ObjectMapper objectMapper;
    @Mock private TradeDetailsService tradeDetailsService;
    @Mock private PortfolioService portfolioService;
    @Mock private TradeProcessingService tradeProcessingService;
    @Mock private TradeSummaryService tradeSummaryService;
    @Mock private PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;
    @Mock private Acknowledgment acknowledgment;

    private PortfolioUpdateConsumerService consumer;

    @BeforeEach
    void setUp() {
        consumer = new PortfolioUpdateConsumerService(
                objectMapper,
                tradeDetailsService,
                portfolioService,
                tradeProcessingService,
                tradeSummaryService,
                portfolioSyncInstrumentResolver);
        ReflectionTestUtils.setField(consumer, "demoPortfolioId", "demo-shared-id");
    }

    @Test
    void upsertPortfolio_sameName_remapsToExistingId() {
        String owner = "user-1";
        String existingId = "existing-upstox";
        String newId = "new-upstox-uuid";

        when(portfolioService.findByPortfolioId(newId)).thenReturn(Optional.empty());
        when(portfolioService.findByOwnerId(owner)).thenReturn(List.of(
                PortfolioModel.builder().portfolioId(existingId).name("Upstox").ownerId(owner).build()));

        String kept = ReflectionTestUtils.invokeMethod(
                consumer, "upsertPortfolio", newId, owner, "Upstox", "UPSTOX");

        assertEquals(existingId, kept);
        verify(portfolioService, never()).savePortfolio(any());
    }

    @Test
    void processInbound_sameNameRemap_createsTradesUnderExistingId() throws Exception {
        String owner = "user-1";
        String existingId = "existing-upstox";
        String newId = UUID.randomUUID().toString();

        when(portfolioService.findByPortfolioId(newId)).thenReturn(Optional.empty());
        when(portfolioService.findByOwnerId(owner)).thenReturn(List.of(
                PortfolioModel.builder().portfolioId(existingId).name("Upstox").ownerId(owner).build()));
        when(tradeDetailsService.findModelsByPortfolioId(existingId)).thenReturn(new ArrayList<>());
        when(tradeDetailsService.saveAllTradeDetails(anyList())).thenAnswer(inv -> {
            List<TradeDetails> saved = inv.getArgument(0);
            return saved == null ? List.of() : new ArrayList<>(saved);
        });

        PortfolioUpdateInboundEvent event = PortfolioUpdateInboundEvent.builder()
                .id(UUID.randomUUID())
                .portfolioId(newId)
                .userId(owner)
                .name("Upstox")
                .brokerType("UPSTOX")
                .action("UPDATE")
                .source("DOCUMENT")
                .equities(List.of(InboundEquityModel.builder()
                        .symbol("RELIANCE")
                        .quantity(10.0)
                        .avgBuyingPrice(100.0)
                        .build()))
                .build();

        ReflectionTestUtils.invokeMethod(consumer, "processInboundPortfolioEvent", event);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TradeDetails>> captor = ArgumentCaptor.forClass(List.class);
        verify(tradeDetailsService).saveAllTradeDetails(captor.capture());
        assertEquals(existingId, captor.getValue().get(0).getPortfolioId());
        verify(tradeProcessingService).processTradeDetailsWithObjects(anyList(), eq(existingId), eq(owner));
    }

    @Test
    void upsertPortfolio_crossOwner_returnsNull() {
        String portfolioId = "p-owned-by-other";
        when(portfolioService.findByPortfolioId(portfolioId)).thenReturn(Optional.of(
                PortfolioModel.builder().portfolioId(portfolioId).ownerId("owner-A").build()));

        String kept = ReflectionTestUtils.invokeMethod(
                consumer, "upsertPortfolio", portfolioId, "owner-B", "Upstox", "UPSTOX");

        assertNull(kept);
        verify(portfolioService, never()).savePortfolio(any());
    }

    @Test
    void processInbound_update_crossOwner_skipped() {
        String portfolioId = "p-owned-by-other";
        when(portfolioService.findByPortfolioId(portfolioId)).thenReturn(Optional.of(
                PortfolioModel.builder().portfolioId(portfolioId).ownerId("owner-A").name("Upstox").build()));

        PortfolioUpdateInboundEvent event = PortfolioUpdateInboundEvent.builder()
                .id(UUID.randomUUID())
                .portfolioId(portfolioId)
                .userId("owner-B")
                .name("Upstox")
                .brokerType("UPSTOX")
                .action("UPDATE")
                .source("DOCUMENT")
                .equities(List.of(InboundEquityModel.builder()
                        .symbol("RELIANCE")
                        .quantity(10.0)
                        .avgBuyingPrice(100.0)
                        .build()))
                .build();

        ReflectionTestUtils.invokeMethod(consumer, "processInboundPortfolioEvent", event);

        verify(tradeDetailsService, never()).saveAllTradeDetails(anyList());
        verify(tradeDetailsService, never()).findModelsByPortfolioId(anyString());
        verify(tradeProcessingService, never()).processTradeDetailsWithObjects(anyList(), anyString(), anyString());
        verify(portfolioService, never()).savePortfolio(any());
    }

    @Test
    void deleteOwnedPortfolio_crossOwner_skipped() {
        String portfolioId = "p-owned-by-other";
        when(portfolioService.findByPortfolioId(portfolioId)).thenReturn(Optional.of(
                PortfolioModel.builder().portfolioId(portfolioId).ownerId("owner-A").build()));

        ReflectionTestUtils.invokeMethod(consumer, "deleteOwnedPortfolio", portfolioId, "owner-B");

        verify(portfolioService, never()).deleteByPortfolioId(anyString());
        verify(tradeDetailsService, never()).deleteByPortfolioId(anyString());
        verify(tradeDetailsService, never()).deleteByTradeId(anyString());
    }

    @Test
    void deleteOwnedPortfolio_ownerMatch_deletesPortfolioTradesAndSummaries() {
        String portfolioId = "p-mine";
        String owner = "owner-A";
        when(portfolioService.findByPortfolioId(portfolioId)).thenReturn(Optional.of(
                PortfolioModel.builder().portfolioId(portfolioId).ownerId(owner).build()));
        when(tradeSummaryService.findBasicByPortfolioId(portfolioId)).thenReturn(List.of());

        ReflectionTestUtils.invokeMethod(consumer, "deleteOwnedPortfolio", portfolioId, owner);

        verify(portfolioService).deleteByPortfolioId(portfolioId);
        verify(tradeDetailsService).deleteByPortfolioId(portfolioId);
    }

    @Test
    void deleteOwnedPortfolio_missingPortfolio_skipsDeletions() {
        String portfolioId = "orphan-id";
        String owner = "owner-A";
        when(portfolioService.findByPortfolioId(portfolioId)).thenReturn(Optional.empty());

        ReflectionTestUtils.invokeMethod(consumer, "deleteOwnedPortfolio", portfolioId, owner);

        verify(portfolioService, never()).deleteByPortfolioId(anyString());
        verify(tradeDetailsService, never()).deleteByTradeId(anyString());
        verify(tradeDetailsService, never()).deleteByPortfolioId(anyString());
    }

    @Test
    void consume_ignoresSharedDemoPortfolioId() throws Exception {
        String json = "{}";
        PortfolioUpdateInboundEvent event = PortfolioUpdateInboundEvent.builder()
                .id(UUID.randomUUID())
                .portfolioId("demo-shared-id")
                .userId("anyone")
                .source("DOCUMENT")
                .build();
        when(objectMapper.readValue(json, PortfolioUpdateInboundEvent.class)).thenReturn(event);

        consumer.consume(json, acknowledgment);

        verify(acknowledgment).acknowledge();
        verify(portfolioService, never()).savePortfolio(any());
        verify(tradeDetailsService, never()).saveAllTradeDetails(anyList());
    }
}
