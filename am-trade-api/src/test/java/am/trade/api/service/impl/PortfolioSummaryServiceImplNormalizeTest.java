package am.trade.api.service.impl;

import am.trade.common.models.PortfolioModel;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioSummaryServiceImplNormalizeTest {

    private static final String DEMO_DISPLAY_NAME = "Demo Portfolio";

    @Mock
    private PortfolioService portfolioService;
    @Mock
    private TradeDetailsService tradeDetailsService;

    private PortfolioSummaryServiceImpl service;

    private final String demoId = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        service = new PortfolioSummaryServiceImpl(portfolioService, tradeDetailsService);
        ReflectionTestUtils.setField(service, "demoPortfolioId", demoId);
    }

    @Test
    void normalize_dedupesSamePortfolioId_andRenamesDemo() {
        String owner = "owner-" + UUID.randomUUID();
        PortfolioModel a = PortfolioModel.builder()
                .portfolioId(demoId)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .build();
        PortfolioModel b = PortfolioModel.builder()
                .portfolioId(demoId)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1"))
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(List.of(a, b), owner);

        assertEquals(1, out.size());
        assertEquals(DEMO_DISPLAY_NAME, out.get(0).getName());
        assertEquals(demoId, out.get(0).getPortfolioId());
        assertEquals(3, out.get(0).getTradeIds().size());
    }

    @Test
    void normalize_collapsesSameNameDifferentIds_prefersDemoId() {
        String owner = "owner-" + UUID.randomUUID();
        String otherId = UUID.randomUUID().toString();
        PortfolioModel upstoxClone = PortfolioModel.builder()
                .portfolioId(otherId)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .build();
        PortfolioModel demoRow = PortfolioModel.builder()
                .portfolioId(demoId)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(List.of(upstoxClone, demoRow), owner);

        assertEquals(1, out.size());
        assertEquals(demoId, out.get(0).getPortfolioId());
        assertEquals(DEMO_DISPLAY_NAME, out.get(0).getName());
    }

    @Test
    void normalize_collapsesTwoUpstoxWithDifferentIds() {
        String owner = "owner-" + UUID.randomUUID();
        PortfolioModel a = PortfolioModel.builder()
                .portfolioId(UUID.randomUUID().toString())
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .build();
        PortfolioModel b = PortfolioModel.builder()
                .portfolioId(UUID.randomUUID().toString())
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(List.of(a, b), owner);

        assertEquals(1, out.size());
        assertEquals("Upstox", out.get(0).getName());
    }

    @Test
    void getPortfolioSummaries_emptyOwner_injectsDemo() {
        String owner = "owner-" + UUID.randomUUID();
        when(portfolioService.findByOwnerId(owner)).thenReturn(List.of());
        when(portfolioService.findByPortfolioId(demoId)).thenReturn(Optional.of(
                PortfolioModel.builder()
                        .portfolioId(demoId)
                        .name("Zerodha")
                        .ownerId("demo-owner")
                        .tradeIds(List.of("d1"))
                        .build()));

        List<PortfolioModel> out = service.getPortfolioSummariesByOwnerId(owner);

        assertEquals(1, out.size());
        assertEquals(DEMO_DISPLAY_NAME, out.get(0).getName());
        assertEquals(demoId, out.get(0).getPortfolioId());
        assertEquals(owner, out.get(0).getOwnerId());
    }

    @Test
    void normalize_renamesDemoIdToDemoPortfolio() {
        String owner = "owner-" + UUID.randomUUID();
        PortfolioModel row = PortfolioModel.builder()
                .portfolioId(demoId)
                .name("Zerodha")
                .ownerId(owner)
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(List.of(row), owner);

        assertEquals(1, out.size());
        assertEquals(DEMO_DISPLAY_NAME, out.get(0).getName());
        assertTrue(out.get(0).getOwnerId().equals(owner));
    }
}
