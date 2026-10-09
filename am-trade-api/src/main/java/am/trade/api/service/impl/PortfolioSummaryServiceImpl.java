package am.trade.api.service.impl;

import am.trade.api.service.PortfolioSummaryService;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.AssetAllocation;
import am.trade.common.models.TradeDetails;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the Portfolio Summary Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSummaryServiceImpl implements PortfolioSummaryService {

    private static final String DEMO_DISPLAY_NAME = "Demo Portfolio";

    private final PortfolioService portfolioService;
    private final TradeDetailsService tradeDetailsService;

    @Value("${app.demo.portfolio-id:}")
    private String demoPortfolioId;

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
    // Never cache Demo / empty — otherwise a late Kafka upsert stays invisible until TTL.
    @Cacheable(
            value = "portfolioSummary",
            key = "#ownerId",
            unless = "#result == null || #result.isEmpty() "
                    + "|| #result.?[portfolioId == @environment.getProperty('app.demo.portfolio-id', '')].size() > 0")
    public List<PortfolioModel> getPortfolioSummariesByOwnerId(String ownerId) {
        log.debug("Getting portfolio summaries for ownerId: {}", ownerId);
        
        if (ownerId == null || ownerId.trim().isEmpty()) {
            throw new IllegalArgumentException("Owner ID cannot be null or empty");
        }
        
        List<PortfolioModel> portfolios = portfolioService.findByOwnerId(ownerId);
        if (portfolios == null) {
            portfolios = List.of();
        }

        portfolios = normalizeOwnerPortfolios(portfolios, ownerId);

        if (portfolios.isEmpty()) {
            PortfolioModel injected = tryInjectDemo(ownerId);
            if (injected != null) {
                portfolios = List.of(injected);
                log.info("Injected demo portfolio {} for ownerId {}", demoPortfolioId, ownerId);
            }
        }

        return portfolios;
    }

    /**
     * Dedup + demo naming so Trade UI never shows two identical broker cards
     * while the sidebar says Demo Portfolio.
     */
    List<PortfolioModel> normalizeOwnerPortfolios(List<PortfolioModel> raw, String ownerId) {
        if (raw == null || raw.isEmpty()) {
            return new ArrayList<>();
        }

        // Collapse same display name BEFORE renaming the shared demo UUID, otherwise
        // "Upstox" + demo-id-"Upstox" become "Upstox" + "Demo Portfolio" and both stay.
        List<PortfolioModel> byId = dedupeByPortfolioId(raw);
        List<PortfolioModel> collapsed = collapseSameNameDuplicates(byId);
        return collapsed.stream()
                .map(p -> isDemoPortfolioId(p.getPortfolioId()) ? cloneDemoPortfolio(p, ownerId) : p)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private PortfolioModel tryInjectDemo(String ownerId) {
        if (!isDemoConfigured()) {
            return null;
        }
        try {
            Optional<PortfolioModel> demoPortfolio = portfolioService.findByPortfolioId(demoPortfolioId.trim());
            if (demoPortfolio.isPresent()) {
                return cloneDemoPortfolio(demoPortfolio.get(), ownerId);
            }
        } catch (Exception e) {
            log.warn("Failed to load demo portfolio {}: {}", demoPortfolioId, e.getMessage());
        }
        return null;
    }

    private List<PortfolioModel> dedupeByPortfolioId(List<PortfolioModel> raw) {
        Map<String, PortfolioModel> unique = new LinkedHashMap<>();
        for (PortfolioModel p : raw) {
            if (p == null) {
                continue;
            }
            String id = p.getPortfolioId();
            if (id == null || id.isBlank()) {
                // Keep nameless rows under a synthetic key so we do not drop them silently.
                unique.put("missing-" + unique.size() + "-" + Objects.toString(p.getName(), ""), p);
                continue;
            }
            String key = id.trim().toLowerCase(Locale.ROOT);
            PortfolioModel existing = unique.get(key);
            if (existing == null || prefer(p, existing) == p) {
                unique.put(key, p);
            }
        }
        return new ArrayList<>(unique.values());
    }

    /**
     * Drop shared-demo UUID rows that collide with a real broker name.
     * Distinct non-demo portfolios with the same display name are kept (display name ≠ identity).
     * Kafka consumer remaps same-name upserts to avoid creating new UUID duplicates.
     */
    private List<PortfolioModel> collapseSameNameDuplicates(List<PortfolioModel> portfolios) {
        java.util.Set<String> realNameKeys = new java.util.HashSet<>();
        for (PortfolioModel p : portfolios) {
            if (p == null || isDemoPortfolioId(p.getPortfolioId())) {
                continue;
            }
            if (p.getName() != null && !p.getName().isBlank()) {
                realNameKeys.add(p.getName().trim().toLowerCase(Locale.ROOT));
            }
        }

        List<PortfolioModel> out = new ArrayList<>();
        for (PortfolioModel p : portfolios) {
            if (p == null) {
                continue;
            }
            if (isDemoPortfolioId(p.getPortfolioId())
                    && p.getName() != null
                    && !p.getName().isBlank()
                    && realNameKeys.contains(p.getName().trim().toLowerCase(Locale.ROOT))) {
                log.info("Dropping demo-id portfolio colliding with real name='{}' id={}",
                        p.getName(), p.getPortfolioId());
                continue;
            }
            out.add(p);
        }
        return out;
    }

    private PortfolioModel prefer(PortfolioModel a, PortfolioModel b) {
        // Prefer real broker rows over the shared demo UUID when names collide.
        if (isDemoPortfolioId(a.getPortfolioId()) && !isDemoPortfolioId(b.getPortfolioId())) {
            return b;
        }
        if (isDemoPortfolioId(b.getPortfolioId()) && !isDemoPortfolioId(a.getPortfolioId())) {
            return a;
        }
        if (a.getLastUpdatedDate() != null && b.getLastUpdatedDate() != null
                && !a.getLastUpdatedDate().equals(b.getLastUpdatedDate())) {
            return a.getLastUpdatedDate().isAfter(b.getLastUpdatedDate()) ? a : b;
        }
        int tradesA = a.getTradeIds() == null ? 0 : a.getTradeIds().size();
        int tradesB = b.getTradeIds() == null ? 0 : b.getTradeIds().size();
        if (tradesA != tradesB) {
            return tradesA > tradesB ? a : b;
        }
        return a;
    }

    private boolean isDemoConfigured() {
        return demoPortfolioId != null && !demoPortfolioId.isBlank();
    }

    private boolean isDemoPortfolioId(String portfolioId) {
        return isDemoConfigured()
                && portfolioId != null
                && demoPortfolioId.trim().equalsIgnoreCase(portfolioId.trim());
    }

    private PortfolioModel cloneDemoPortfolio(PortfolioModel source, String newOwnerId) {
        return PortfolioModel.builder()
            .portfolioId(source.getPortfolioId())
            .name(DEMO_DISPLAY_NAME)
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
            .assetAllocations(source.getAssetAllocations())
            .build();
    }
}
