package am.trade.api.service.impl;

import am.trade.api.client.AmPortfolioDeleteClient;
import am.trade.api.service.TradeApiService;
import am.trade.common.models.PortfolioModel;
import am.trade.services.service.PortfolioPersistenceService;
import am.trade.services.service.TradeDetailsService;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioApiServiceImplDeleteTest {

    @Mock
    private PortfolioPersistenceService portfolioPersistenceService;
    @Mock
    private TradeApiService tradeApiService;
    @Mock
    private TradeDetailsService tradeDetailsService;
    @Mock
    private AmPortfolioDeleteClient amPortfolioDeleteClient;

    private PortfolioApiServiceImpl service;

    @BeforeEach
    void setUp() {
        UserContext.setUserId("owner-1");
        service = new PortfolioApiServiceImpl(
                portfolioPersistenceService,
                tradeApiService,
                tradeDetailsService,
                amPortfolioDeleteClient);
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void deletePortfolio_missingInTrade_syncDeletesOnAmPortfolio() {
        String portfolioId = "any-broker-portfolio-uuid";
        when(portfolioPersistenceService.findByPortfolioId(portfolioId)).thenReturn(Optional.empty());
        when(amPortfolioDeleteClient.deleteBrokerPortfolio(portfolioId, "owner-1")).thenReturn(true);

        service.deletePortfolio(portfolioId, true);

        verify(amPortfolioDeleteClient).deleteBrokerPortfolio(portfolioId, "owner-1");
        verify(tradeApiService).publishBulkPortfolioSyncEvent(
                eq(portfolioId), eq(portfolioId), eq("owner-1"), anyList(), eq("DELETE_PORTFOLIO"));
        verify(portfolioPersistenceService, never()).deleteByPortfolioId(anyString());
    }

    @Test
    void deletePortfolio_missingInTrade_syncFailurePropagates() {
        String portfolioId = "any-broker-portfolio-uuid";
        when(portfolioPersistenceService.findByPortfolioId(portfolioId)).thenReturn(Optional.empty());
        when(amPortfolioDeleteClient.deleteBrokerPortfolio(portfolioId, "owner-1"))
                .thenThrow(new IllegalStateException("am-portfolio unreachable"));

        assertThrows(IllegalStateException.class, () -> service.deletePortfolio(portfolioId, true));

        verify(tradeApiService, never()).publishBulkPortfolioSyncEvent(
                anyString(), anyString(), anyString(), anyList(), anyString());
    }

    @Test
    void deletePortfolio_presentInTrade_deletesLocalAndPublishesKafka() {
        String portfolioId = "upstox-id";
        PortfolioModel existing = PortfolioModel.builder()
                .portfolioId(portfolioId)
                .name("Upstox")
                .ownerId("owner-1")
                .build();
        when(portfolioPersistenceService.findByPortfolioId(portfolioId)).thenReturn(Optional.of(existing));
        when(tradeDetailsService.findModelsByPortfolioId(portfolioId)).thenReturn(new ArrayList<>());

        service.deletePortfolio(portfolioId, true);

        verify(amPortfolioDeleteClient, never()).deleteBrokerPortfolio(anyString(), anyString());
        verify(tradeApiService).publishBulkPortfolioSyncEvent(
                eq(portfolioId), eq("Upstox"), eq("owner-1"), anyList(), eq("DELETE_PORTFOLIO"));
        verify(portfolioPersistenceService).deleteByPortfolioId(portfolioId);
    }
}
