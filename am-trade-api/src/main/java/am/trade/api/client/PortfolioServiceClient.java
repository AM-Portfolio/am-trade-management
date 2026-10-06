package am.trade.api.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads broker portfolios from am-portfolio (JWT forwarded) so trade can
 * self-heal when Kafka {@code am-portfolio-update} fan-out was missed.
 */
@Component
@Slf4j
public class PortfolioServiceClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final String baseUrl;

    public PortfolioServiceClient(
            RestTemplateBuilder restTemplateBuilder,
            ObjectMapper objectMapper,
            @Value("${am.portfolio.service.url:http://am-portfolio-dev:8080}") String baseUrl) {
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    public List<RemotePortfolio> listPortfoliosForUser(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            return Collections.emptyList();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, bearerToken.startsWith("Bearer ")
                    ? bearerToken
                    : "Bearer " + bearerToken);
            ResponseEntity<String> response = restTemplate.exchange(
                    baseUrl + "/v1/portfolios",
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    String.class);
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                log.warn("am-portfolio list returned {}", response.getStatusCode());
                return Collections.emptyList();
            }
            JsonNode root = objectMapper.readTree(response.getBody());
            if (!root.isArray()) {
                return Collections.emptyList();
            }
            List<RemotePortfolio> out = new ArrayList<>();
            for (JsonNode node : root) {
                RemotePortfolio p = parse(node);
                if (p != null) {
                    out.add(p);
                }
            }
            return out;
        } catch (Exception e) {
            log.warn("Failed to list portfolios from am-portfolio at {}: {}", baseUrl, e.getMessage());
            return Collections.emptyList();
        }
    }

    private RemotePortfolio parse(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String kind = text(node, "portfolioKind");
        if (kind != null && !"BROKER".equalsIgnoreCase(kind)) {
            return null;
        }
        String id = node.hasNonNull("id") ? node.get("id").asText() : null;
        String owner = text(node, "owner");
        if (id == null || id.isBlank() || owner == null || owner.isBlank()) {
            return null;
        }
        String name = text(node, "name");
        String broker = null;
        if (node.has("brokerType") && !node.get("brokerType").isNull()) {
            JsonNode bt = node.get("brokerType");
            broker = bt.isTextual() ? bt.asText() : text(bt, "code");
            if (broker == null) {
                broker = bt.asText();
            }
        }
        List<RemoteEquity> equities = new ArrayList<>();
        JsonNode eq = node.get("equityModels");
        if (eq != null && eq.isArray()) {
            for (JsonNode e : eq) {
                String symbol = text(e, "symbol");
                Double qty = e.has("quantity") && !e.get("quantity").isNull() ? e.get("quantity").asDouble() : null;
                if (symbol != null && qty != null && qty > 0) {
                    equities.add(new RemoteEquity(
                            symbol,
                            text(e, "isin"),
                            qty,
                            e.has("avgBuyingPrice") && !e.get("avgBuyingPrice").isNull()
                                    ? e.get("avgBuyingPrice").asDouble() : null,
                            e.has("investmentValue") && !e.get("investmentValue").isNull()
                                    ? e.get("investmentValue").asDouble() : null,
                            e.has("currentPrice") && !e.get("currentPrice").isNull()
                                    ? e.get("currentPrice").asDouble() : null));
                }
            }
        }
        return new RemotePortfolio(id, owner, name, broker, equities);
    }

    private static String text(JsonNode node, String field) {
        if (node == null || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String v = node.get(field).asText();
        return v != null && !v.isBlank() ? v : null;
    }

    public record RemotePortfolio(
            String portfolioId,
            String ownerId,
            String name,
            String brokerType,
            List<RemoteEquity> equities) {}

    public record RemoteEquity(
            String symbol,
            String isin,
            Double quantity,
            Double avgBuyingPrice,
            Double investmentValue,
            Double currentPrice) {}
}
