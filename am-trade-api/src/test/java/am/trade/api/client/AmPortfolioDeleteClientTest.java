package am.trade.api.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class AmPortfolioDeleteClientTest {

    private MockRestServiceServer server;
    private AmPortfolioDeleteClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new AmPortfolioDeleteClient(restTemplate, "http://am-portfolio:8080", "test-internal-secret");
    }

    @Test
    void deleteBrokerPortfolio_204_ok() {
        server.expect(requestTo("http://am-portfolio:8080/v1/portfolios/internal/pid-1?ownerId=owner-1"))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header("X-Internal-Secret", "test-internal-secret"))
                .andRespond(withStatus(HttpStatus.NO_CONTENT));

        assertTrue(client.deleteBrokerPortfolio("pid-1", "owner-1"));
        server.verify();
    }

    @Test
    void deleteBrokerPortfolio_404_treatedAsSuccess() {
        server.expect(requestTo("http://am-portfolio:8080/v1/portfolios/internal/pid-1?ownerId=owner-1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        assertTrue(client.deleteBrokerPortfolio("pid-1", "owner-1"));
        server.verify();
    }

    @Test
    void deleteBrokerPortfolio_500_throws() {
        server.expect(requestTo("http://am-portfolio:8080/v1/portfolios/internal/pid-1?ownerId=owner-1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                        .contentType(MediaType.TEXT_PLAIN)
                        .body("boom"));

        assertThrows(IllegalStateException.class,
                () -> client.deleteBrokerPortfolio("pid-1", "owner-1"));
        server.verify();
    }

    @Test
    void deleteBrokerPortfolio_blankSecret_throws() {
        AmPortfolioDeleteClient noSecret = new AmPortfolioDeleteClient(
                new RestTemplate(), "http://am-portfolio:8080", "");
        assertThrows(IllegalStateException.class,
                () -> noSecret.deleteBrokerPortfolio("pid-1", "owner-1"));
    }
}
