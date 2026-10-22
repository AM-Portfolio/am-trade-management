package am.trade.api.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Duration;

/**
 * Synchronous broker-portfolio delete against am-portfolio (any broker / any portfolioId).
 * Used when Trade has no local {@code portfolio_trades} row so delete cannot rely on
 * fire-and-forget Kafka alone.
 */
@Slf4j
@Component
public class AmPortfolioDeleteClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final String internalSecret;

    public AmPortfolioDeleteClient(
            RestTemplateBuilder restTemplateBuilder,
            @Value("${am.portfolio.service.url:http://am-portfolio:8080}") String baseUrl,
            @Value("${app.jwt.internal-secret:}") String internalSecret) {
        this(restTemplateBuilder
                        .setConnectTimeout(Duration.ofSeconds(3))
                        .setReadTimeout(Duration.ofSeconds(15))
                        .build(),
                baseUrl,
                internalSecret);
    }

    /** Test / custom RestTemplate entry point. */
    AmPortfolioDeleteClient(RestTemplate restTemplate, String baseUrl, String internalSecret) {
        this.baseUrl = baseUrl != null && baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.internalSecret = internalSecret != null ? internalSecret : "";
        this.restTemplate = restTemplate;
    }

    /**
     * @return true if am-portfolio deleted the book (204) or it was already gone (404)
     * @throws IllegalStateException if the call fails for any other reason
     */
    public boolean deleteBrokerPortfolio(String portfolioId, String ownerId) {
        if (portfolioId == null || portfolioId.isBlank()) {
            throw new IllegalArgumentException("portfolioId required");
        }
        if (ownerId == null || ownerId.isBlank()) {
            throw new IllegalArgumentException("ownerId required");
        }
        if (internalSecret.isBlank()) {
            throw new IllegalStateException(
                    "app.jwt.internal-secret is unset — cannot sync-delete portfolio-only book on am-portfolio");
        }

        String url = UriComponentsBuilder
                .fromUriString(baseUrl + "/v1/portfolios/internal/" + portfolioId)
                .queryParam("ownerId", ownerId)
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", internalSecret);

        try {
            ResponseEntity<Void> response = restTemplate.exchange(
                    url, HttpMethod.DELETE, new HttpEntity<>(headers), Void.class);
            int code = response.getStatusCode().value();
            log.info("am-portfolio internal delete portfolioId={} ownerId={} status={}",
                    portfolioId, ownerId, code);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpStatusCodeException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND
                    || e.getStatusCode().value() == 404) {
                log.info("am-portfolio already missing portfolioId={} ownerId={}", portfolioId, ownerId);
                return true;
            }
            log.error("am-portfolio internal delete failed portfolioId={} status={} body={}",
                    portfolioId, e.getStatusCode().value(), e.getResponseBodyAsString());
            throw new IllegalStateException(
                    "Failed to delete portfolio on am-portfolio: HTTP " + e.getStatusCode().value(), e);
        } catch (RestClientException e) {
            log.error("am-portfolio internal delete transport error portfolioId={}: {}",
                    portfolioId, e.getMessage(), e);
            throw new IllegalStateException(
                    "Failed to reach am-portfolio to delete portfolio " + portfolioId, e);
        }
    }
}
