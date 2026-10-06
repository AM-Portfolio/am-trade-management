package am.trade.api.service.impl;

import am.trade.api.service.PortfolioSummaryService;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.PortfolioSummaryDTO;
import am.trade.common.models.AssetAllocation;
import am.trade.common.models.TradeDetails;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the Portfolio Summary Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSummaryServiceImpl implements PortfolioSummaryService {

    private final PortfolioService portfolioService;
    private final TradeDetailsService tradeDetailsService;
    private final ObjectMapper objectMapper;
    private final RestTemplateBuilder restTemplateBuilder;

    @org.springframework.beans.factory.annotation.Autowired(required = false)
    private StringRedisTemplate redisTemplate;

    @Value("${app.demo.portfolio-id:}")
    private String demoPortfolioId;

    @Value("${am.portfolio.service.url:http://am-portfolio-dev:8080}")
    private String portfolioServiceUrl;

    @Override
    public PortfolioModel getPortfolioSummary(String portfolioId) {
        log.debug("Getting portfolio summary for portfolioId: {}", portfolioId);
        
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        // Get the complete portfolio with all trades and metrics
        Optional<PortfolioModel> portfolio = portfolioService.findByPortfolioId(portfolioId);
        
        if (portfolio.isEmpty()) {
            log.warn("Portfolio not found with ID: {}", portfolioId);
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }
        
        return portfolio.get();
    }

    @Override
    public List<AssetAllocation> getAssetAllocation(String portfolioId) {
        log.debug("Getting asset allocation for portfolioId: {}", portfolioId);
        
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        // Get the portfolio model which contains asset allocations
        Optional<PortfolioModel> portfolio = portfolioService.findByPortfolioId(portfolioId);
        
        if (portfolio.isEmpty()) {
            log.warn("Portfolio not found with ID: {}", portfolioId);
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }
        
        return portfolio.get().getAssetAllocations();
    }

    @Override
    public Map<LocalDate, Double> getPortfolioPerformance(String portfolioId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting portfolio performance for portfolioId: {} from {} to {}", portfolioId, startDate, endDate);
        
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Get the portfolio with all trades
        Optional<PortfolioModel> portfolio = portfolioService.findByPortfolioId(portfolioId);
        
        if (portfolio.isEmpty()) {
            log.warn("Portfolio not found with ID: {}", portfolioId);
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }

        List<String> portfolioTradeIds = portfolio.get().getTradeIds();

        List<TradeDetails> trades = tradeDetailsService.findModelsByTradeIds(portfolioTradeIds);
        
        // Calculate daily performance based on trade data
        // This is a simplified version - actual implementation would depend on specific performance calculation logic
        Map<LocalDate, Double> performance = new HashMap<>();
        
        // Get trades in the date range
        List<TradeDetails> relevantTrades = trades.stream()
            .filter(trade -> {
                LocalDate tradeDate = trade.getTradeDate();
                return !tradeDate.isBefore(startDate) && !tradeDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
        
        // Group trades by date and calculate daily performance
        Map<LocalDate, List<TradeDetails>> tradesByDate = relevantTrades.stream()
            .collect(Collectors.groupingBy(TradeDetails::getTradeDate));
        
        // For each day in the range, calculate performance
        LocalDate currentDate = startDate;
        Double cumulativePerformance = 0.0;
        
        while (!currentDate.isAfter(endDate)) {
            // Add current day's profit/loss to cumulative performance
            List<TradeDetails> dailyTrades = tradesByDate.getOrDefault(currentDate, new ArrayList<>());
            Double dailyProfitLoss = dailyTrades.stream()
                .mapToDouble(trade -> trade.getMetrics().getProfitLoss().doubleValue())
                .sum();
            
            cumulativePerformance += dailyProfitLoss;
            performance.put(currentDate, cumulativePerformance);
            
            currentDate = currentDate.plusDays(1);
        }
        
        return performance;
    }

    @Override
    public Map<String, PortfolioModel> comparePortfolios(List<String> portfolioIds) {
        log.debug("Comparing portfolios: {}", portfolioIds);
        
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            throw new IllegalArgumentException("Portfolio IDs list cannot be null or empty");
        }
        
        Map<String, PortfolioModel> portfolioMap = new HashMap<>();
        
        for (String portfolioId : portfolioIds) {
            try {
                PortfolioModel portfolio = getPortfolioSummary(portfolioId);
                portfolioMap.put(portfolioId, portfolio);
            } catch (Exception e) {
                log.warn("Error getting portfolio with ID: {}", portfolioId, e);
                // Skip portfolios that cannot be retrieved
            }
        }
        
        if (portfolioMap.isEmpty()) {
            throw new IllegalArgumentException("None of the requested portfolios could be found");
        }
        
        return portfolioMap;
    }
    
    @Override
    // Never cache Demo / empty — otherwise a missed Kafka fan-out sticks until TTL.
    @Cacheable(
            value = "portfolioSummary",
            key = "#ownerId",
            unless = "#result == null || #result.isEmpty() "
                    + "|| (#result.size() == 1 && 'Demo Portfolio'.equals(#result.get(0).getName()))")
    public List<PortfolioModel> getPortfolioSummariesByOwnerId(String ownerId) {
        log.debug("Getting portfolio summaries for ownerId: {}", ownerId);
        
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID cannot be null or empty");
        }
        
        // Return the full PortfolioModel list so the frontend receives all metrics
        // (winRate, netProfitLoss, totalTrades, etc.) — not just portfolioId + name
        List<PortfolioModel> portfolios = portfolioService.findByOwnerId(ownerId);

        if (portfolios.isEmpty()) {
            // Missed Kafka am-portfolio-update → pull broker portfolios from am-portfolio before Demo.
            try {
                if (importMissingFromAmPortfolio(ownerId) > 0) {
                    portfolios = portfolioService.findByOwnerId(ownerId);
                }
            } catch (Exception e) {
                log.warn("am-portfolio catch-up failed for {}: {}", ownerId, e.getMessage());
            }
        }

        if (portfolios.isEmpty()) {
            if (demoPortfolioId != null && !demoPortfolioId.trim().isEmpty()) {
                try {
                    Optional<PortfolioModel> demoPortfolio = portfolioService.findByPortfolioId(demoPortfolioId);
                    if (demoPortfolio.isPresent()) {
                        PortfolioModel clonedDemo = cloneDemoPortfolio(demoPortfolio.get(), ownerId);
                        portfolios = new ArrayList<>();
                        portfolios.add(clonedDemo);
                        log.info("Injected demo portfolio {} for ownerId {}", demoPortfolioId, ownerId);
                    }
                } catch (Exception e) {
                    log.warn("Failed to load demo portfolio {}: {}", demoPortfolioId, e.getMessage());
                }
            }
        }

        return portfolios;
    }

    /**
     * Creates local trade portfolio rows from am-portfolio when Kafka fan-out was missed.
     * Portfolio shell is enough to hide Demo; holdings sync via later Kafka / trade flows.
     */
    private int importMissingFromAmPortfolio(String ownerId) throws Exception {
        String auth = currentAuthorizationHeader();
        if (auth == null || auth.isBlank()) {
            return 0;
        }

        String base = portfolioServiceUrl.endsWith("/")
                ? portfolioServiceUrl.substring(0, portfolioServiceUrl.length() - 1)
                : portfolioServiceUrl;
        RestTemplate rt = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(3))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, auth.startsWith("Bearer ") ? auth : "Bearer " + auth);
        ResponseEntity<String> response = rt.exchange(
                base + "/v1/portfolios",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);
        if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
            return 0;
        }

        JsonNode root = objectMapper.readTree(response.getBody());
        if (!root.isArray()) {
            return 0;
        }

        int created = 0;
        for (JsonNode node : root) {
            String kind = text(node, "portfolioKind");
            if (kind != null && !"BROKER".equalsIgnoreCase(kind)) {
                continue;
            }
            String id = node.hasNonNull("id") ? node.get("id").asText() : null;
            String owner = text(node, "owner");
            if (id == null || owner == null || !ownerId.equals(owner)) {
                continue;
            }
            if (portfolioService.findByPortfolioId(id).isPresent()) {
                continue;
            }
            String name = text(node, "name");
            if (name == null || name.isBlank()) {
                name = text(node.path("brokerType"), "code");
                if (name == null && node.has("brokerType") && node.get("brokerType").isTextual()) {
                    name = node.get("brokerType").asText();
                }
                if (name == null || name.isBlank()) {
                    name = "Imported Portfolio";
                }
            }
            portfolioService.savePortfolio(PortfolioModel.builder()
                    .portfolioId(id)
                    .ownerId(owner)
                    .name(name)
                    .active(true)
                    .build());
            created++;
            log.info("Imported missing trade portfolio {} ({}) from am-portfolio for {}", id, name, ownerId);
        }
        return created;
    }

    private static String currentAuthorizationHeader() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest request = attrs != null ? attrs.getRequest() : null;
            return request != null ? request.getHeader("Authorization") : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || !node.has(field) || node.get(field).isNull()) {
            return null;
        }
        String v = node.get(field).asText();
        return v != null && !v.isBlank() ? v : null;
    }

    private PortfolioModel cloneDemoPortfolio(PortfolioModel source, String newOwnerId) {
        return PortfolioModel.builder()
            .portfolioId(source.getPortfolioId())
            .name("Demo Portfolio")
            .description(source.getDescription())
            .ownerId(newOwnerId)
            .active(source.isActive())
            .currency(source.getCurrency())
            .initialCapital(source.getInitialCapital())
            .currentCapital(source.getCurrentCapital())
            .createdDate(source.getCreatedDate())
            .lastUpdatedDate(source.getLastUpdatedDate())
            .metrics(source.getMetrics())
            .tradeIds(source.getTradeIds())
            .build();
    }
}
