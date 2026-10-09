package am.trade.api.service.impl;

import am.trade.common.models.AssetAllocation;
import am.trade.common.models.PortfolioModel;
import am.trade.models.enums.AssetClass;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
    void normalize_collapsesSameNameDifferentIds_prefersRealOverDemo() {
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
        assertEquals(otherId, out.get(0).getPortfolioId());
        assertEquals("Upstox", out.get(0).getName());
    }

    @Test
    void normalize_keepsTwoDistinctNonDemoPortfoliosWithSameName() {
        String owner = "owner-" + UUID.randomUUID();
        String idA = UUID.randomUUID().toString();
        String idB = UUID.randomUUID().toString();
        PortfolioModel a = PortfolioModel.builder()
                .portfolioId(idA)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .build();
        PortfolioModel b = PortfolioModel.builder()
                .portfolioId(idB)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t4"))
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(List.of(a, b), owner);

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(p -> idA.equals(p.getPortfolioId())));
        assertTrue(out.stream().anyMatch(p -> idB.equals(p.getPortfolioId())));
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

    @Test
    void normalize_cloneDemo_preservesAssetAllocations() {
        String owner = "owner-" + UUID.randomUUID();
        AssetAllocation equity = AssetAllocation.builder()
                .assetClass(AssetClass.STOCK)
                .currentPercentage(BigDecimal.valueOf(70))
                .targetPercentage(BigDecimal.valueOf(60))
                .build();
        PortfolioModel row = PortfolioModel.builder()
                .portfolioId(demoId)
                .name("Zerodha")
                .ownerId("demo-owner")
                .assetAllocations(List.of(equity))
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(List.of(row), owner);

        assertEquals(1, out.size());
        assertEquals(DEMO_DISPLAY_NAME, out.get(0).getName());
        assertEquals(1, out.get(0).getAssetAllocations().size());
        assertEquals(AssetClass.STOCK, out.get(0).getAssetAllocations().get(0).getAssetClass());
    }

    @Test
    void normalize_sameId_prefersNewerLastUpdatedOverLargerTradeCount() {
        String owner = "owner-" + UUID.randomUUID();
        String id = UUID.randomUUID().toString();
        LocalDateTime older = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime newer = LocalDateTime.of(2026, 10, 1, 0, 0);
        PortfolioModel staleMoreTrades = PortfolioModel.builder()
                .portfolioId(id)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1", "t2", "t3"))
                .lastUpdatedDate(older)
                .build();
        PortfolioModel freshFewerTrades = PortfolioModel.builder()
                .portfolioId(id)
                .name("Upstox")
                .ownerId(owner)
                .tradeIds(List.of("t1"))
                .lastUpdatedDate(newer)
                .build();

        List<PortfolioModel> out = service.normalizeOwnerPortfolios(
                List.of(staleMoreTrades, freshFewerTrades), owner);

        assertEquals(1, out.size());
        assertEquals(1, out.get(0).getTradeIds().size());
        assertEquals(newer, out.get(0).getLastUpdatedDate());
    }

    @Test
    void normalize_nullAndEmpty_returnsEmpty() {
        assertTrue(service.normalizeOwnerPortfolios(null, "o").isEmpty());
        assertTrue(service.normalizeOwnerPortfolios(List.of(), "o").isEmpty());
    }

    @Test
    void getPortfolioSummaries_blankOwner_throws() {
        try {
            service.getPortfolioSummariesByOwnerId("  ");
            throw new AssertionError("expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Owner ID"));
        }
    }
}
