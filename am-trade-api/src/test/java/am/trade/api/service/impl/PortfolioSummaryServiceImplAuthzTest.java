package am.trade.api.service.impl;

import am.trade.common.models.PortfolioModel;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import com.am.security.context.UserContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PortfolioSummaryServiceImplAuthzTest {

    @Mock
    private PortfolioService portfolioService;
    @Mock
    private TradeDetailsService tradeDetailsService;

    private PortfolioSummaryServiceImpl service;
    private MockedStatic<UserContext> userContext;

    @BeforeEach
    void setUp() {
        service = new PortfolioSummaryServiceImpl(portfolioService, tradeDetailsService);
        userContext = mockStatic(UserContext.class);
    }

    @AfterEach
    void tearDown() {
        userContext.close();
    }

    @Test
    void getPortfolioSummary_deniesForeignOwner() {
        userContext.when(UserContext::getUserIdOrThrow).thenReturn("user-a");
        when(portfolioService.findByPortfolioId("pid-1")).thenReturn(Optional.of(
                PortfolioModel.builder().portfolioId("pid-1").ownerId("user-b").build()));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> service.getPortfolioSummary("pid-1"));
        assertEquals(HttpStatus.FORBIDDEN, ex.getStatusCode());
    }

    @Test
    void getPortfolioSummary_allowsOwner() {
        userContext.when(UserContext::getUserIdOrThrow).thenReturn("user-a");
        PortfolioModel owned = PortfolioModel.builder().portfolioId("pid-1").ownerId("user-a").build();
        when(portfolioService.findByPortfolioId("pid-1")).thenReturn(Optional.of(owned));

        assertEquals("pid-1", service.getPortfolioSummary("pid-1").getPortfolioId());
    }
}
