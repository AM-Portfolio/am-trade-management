package am.trade.api.service.impl;

import am.trade.common.models.PortfolioMetrics;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeSummary;
import am.trade.models.enums.TradeStatus;
import am.trade.services.service.PortfolioService;
import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.impl.TradeProcessingServiceImpl;
import am.trade.api.service.TradeManagementService;
import am.trade.api.client.MarketDataApiClient;
import am.trade.common.util.ValidationUtils;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import am.trade.common.logger.AppLogger;
import am.trade.persistence.repository.PortfolioRepository;
import am.trade.persistence.entity.PortfolioEntity;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Implementation of TradeManagementService that provides calendar-based trade
 * analytics
 */

@Service
@RequiredArgsConstructor
public class TradeManagementServiceImpl implements TradeManagementService {

    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final TradeDetailsService tradeDetailsService;
    private final PortfolioRepository portfolioRepository;
    private final PortfolioService portfolioService;
    private final TradeDetailsMapper tradeDetailsMapper;
    private final AppLogger log;
    private final MarketDataApiClient marketDataApiClient;
    private final ValidationUtils validationUtils;
    private final am.trade.services.service.PortfolioSyncInstrumentResolver portfolioSyncInstrumentResolver;
    private final CacheManager cacheManager;

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByDay(LocalDate date, String portfolioId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);

        return getTradeDetailsByDateTimeRange(startOfDay, endOfDay, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByMonth(int year, int month, String portfolioId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);

        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByQuarter(int year, int quarter, String portfolioId) {
        // Calculate the start month of the quarter (1->1, 2->4, 3->7, 4->10)
        int startMonth = (quarter - 1) * 3 + 1;

        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);

        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByFinancialYear(int financialYear, String portfolioId) {
        // Financial year is from April 1 to March 31
        // For FY 2024-2025, financialYear parameter would be 2025
        int startYear = financialYear - 1;

        LocalDate startDate = LocalDate.of(startYear, Month.APRIL, 1);
        LocalDate endDate = LocalDate.of(financialYear, Month.MARCH, 31);

        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByDateRange(LocalDate startDate, LocalDate endDate,
            String portfolioId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return getTradeDetailsByDateTimeRange(startDateTime, endDateTime, portfolioId);
    }

    private Map<String, List<TradeDetails>> getTradeDetailsByDateTimeRange(LocalDateTime startDateTime,
            LocalDateTime endDateTime, String portfolioId) {
        List<TradeDetails> trades;

        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        log.info("Filtering trades for user {} between {} and {}", userId, startDateTime, endDateTime);

        // If portfolio ID is provided, filter by it
        if (portfolioId != null && !portfolioId.isEmpty()) {
            trades = tradeDetailsService.findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(userId, portfolioId, startDateTime, endDateTime);
            if (trades.isEmpty()) {
                healOrphanedImportedTrades(portfolioId, userId);
                trades = tradeDetailsService.findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(userId, portfolioId, startDateTime, endDateTime);
            }
        } else {
            trades = tradeDetailsService.findModelsByUserIdAndEntryInfoTimestampBetween(userId, startDateTime, endDateTime);
            java.util.Set<String> paperIds = portfolioRepository.findByOwnerId(userId).stream()
                    .filter(p -> "PAPER".equalsIgnoreCase(p.getKind()))
                    .map(PortfolioEntity::getPortfolioId)
                    .collect(Collectors.toSet());
            trades = trades.stream().filter(t -> !paperIds.contains(t.getPortfolioId())).collect(Collectors.toList());
        }

        log.info("Retained {} trades after date filtering", trades.size());
        
        enrichWithLivePrices(trades);

        // Group trades by portfolio ID
        return trades.stream()
                .collect(Collectors.groupingBy(
                        TradeDetails::getPortfolioId,
                        Collectors.toList()));
    }

    @Override
    public List<TradeDetails> getAllTradesByTradePortfolioId(String portfolioId) {
        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        List<TradeDetails> trades = tradeDetailsService.findModelsByUserIdAndPortfolioId(userId, portfolioId);
        if (trades.isEmpty()) {
            healOrphanedImportedTrades(portfolioId, userId);
            trades = tradeDetailsService.findModelsByUserIdAndPortfolioId(userId, portfolioId);
        }
        enrichWithLivePrices(trades);
        return trades;
    }

    @Override
    public Page<TradeDetails> getTradeDetailsByPortfolio(String portfolioId, Pageable pageable) {
        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        Page<TradeDetails> page = tradeDetailsService.findModelsByUserIdAndPortfolioId(userId, portfolioId, pageable);
        if (page.isEmpty()) {
            healOrphanedImportedTrades(portfolioId, userId);
            page = tradeDetailsService.findModelsByUserIdAndPortfolioId(userId, portfolioId, pageable);
        }
        enrichWithLivePrices(page.getContent());
        return page;
    }

    /**
     * Doc-parser Kafka imports historically saved trades without {@code userId}.
     * Holdings queries filter by userId+portfolioId, so those rows look missing.
     * Only heal when the authenticated user owns the portfolio.
     */
    private void healOrphanedImportedTrades(String portfolioId, String userId) {
        Optional<PortfolioEntity> portfolio = portfolioRepository.findByPortfolioId(portfolioId);
        if (portfolio.isEmpty() || !userId.equals(portfolio.get().getOwnerId())) {
            return;
        }
        tradeDetailsService.backfillMissingUserId(portfolioId, userId);
    }

    @Override
    public List<TradeDetails> getTradesByDateRange(String portfolioId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        List<TradeDetails> filteredTrades = tradeDetailsService.findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(userId, portfolioId, startDateTime, endDateTime);
        if (filteredTrades.isEmpty()) {
            healOrphanedImportedTrades(portfolioId, userId);
            filteredTrades = tradeDetailsService.findByUserIdAndPortfolioIdAndEntryInfoTimestampBetween(userId, portfolioId, startDateTime, endDateTime);
        }
                
        enrichWithLivePrices(filteredTrades);
        return filteredTrades;
    }

    @Override
    public List<TradeDetails> getTradesBySymbols(String portfolioId, List<String> symbols) {
        log.info("Fetching trades for portfolio: {} filtered by symbols: {}", portfolioId, symbols);

        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }

        if (symbols == null || symbols.isEmpty()) {
            // If no symbols provided, return all trades for the portfolio
            return getAllTradesByTradePortfolioId(portfolioId);
        }

        // Convert input symbols to upper case for case-insensitive matching in MongoDB
        List<String> upperCaseSymbols = symbols.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        // Use database-side filtering
        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        List<TradeDetails> filteredTrades = tradeDetailsService.findModelsByUserIdAndPortfolioIdAndSymbolInIgnoreCase(userId, portfolioId, upperCaseSymbols);
                
        enrichWithLivePrices(filteredTrades);
        return filteredTrades;
    }

    @Override
    public Page<TradeDetails> getTradesBySymbolsPage(String portfolioId, List<String> symbols, Pageable pageable) {
        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        log.info("Fetching paginated trades for user: {} portfolio: {} filtered by symbols: {}", userId, portfolioId, symbols);

        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }

        List<String> cleanSymbols = symbols == null ? java.util.Collections.emptyList() : symbols.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(String::trim)
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        org.springframework.data.domain.Pageable effectivePageable = (pageable != null && pageable.getSort().isSorted()) 
                ? pageable 
                : org.springframework.data.domain.PageRequest.of(
                        pageable != null ? pageable.getPageNumber() : 0, 
                        pageable != null ? pageable.getPageSize() : 20, 
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "entryInfo.timestamp")
                                .and(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.ASC, "tradeId")));

        Page<TradeDetails> page;
        if (cleanSymbols.isEmpty()) {
            page = tradeDetailsService.findModelsByUserIdAndPortfolioId(userId, portfolioId, effectivePageable);
        } else {
            page = tradeDetailsService.findModelsByUserIdAndPortfolioIdAndSymbolInIgnoreCase(userId, portfolioId, cleanSymbols, effectivePageable);
        }
        enrichWithLivePrices(page.getContent());
        return page;
    }

    @Override
    public Page<TradeDetails> getTradesByFilters(
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            List<String> strategies,
            Pageable pageable) {

        log.info(
                "Fetching trades with filters - portfolioIds: {}, symbols: {}, statuses: {}, startDate: {}, endDate: {}, strategies: {}",
                portfolioIds, symbols, statuses, startDate, endDate, strategies);

        if (portfolioIds == null || portfolioIds.isEmpty()) {
            throw new IllegalArgumentException("At least one portfolio ID must be provided");
        }

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay().minusNanos(1) : null;

        String userId = com.am.security.context.UserContext.getUserIdOrThrow();
        Page<TradeDetails> pagedTrades = tradeDetailsService.findByFilters(
                userId, portfolioIds, symbols, statuses, strategies, startDateTime, endDateTime, pageable);

        List<TradeDetails> content = pagedTrades.getContent();
        if (!content.isEmpty()) {
            enrichWithLivePrices(content);
        }

        return pagedTrades;
    }
    
    private void enrichWithLivePrices(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return;
        }

        Map<String, String> symbolBefore = new java.util.HashMap<>();
        for (TradeDetails trade : trades) {
            if (trade != null && trade.getTradeId() != null) {
                symbolBefore.put(trade.getTradeId(), trade.getSymbol());
            }
        }

        // ISIN + SYMBOL/NAME canonicalize for every open/closed trade on the read path.
        try {
            if (portfolioSyncInstrumentResolver != null) {
                portfolioSyncInstrumentResolver.resolveForSync(trades);
            }
        } catch (Exception e) {
            log.warn("Symbol resolve during enrich failed: {}", e.getMessage());
        }

        List<TradeDetails> toPersist = trades.stream()
                .filter(t -> t != null && t.getTradeId() != null)
                .filter(t -> {
                    String before = symbolBefore.get(t.getTradeId());
                    String after = t.getSymbol();
                    return before == null ? after != null : !before.equalsIgnoreCase(after);
                })
                .collect(Collectors.toList());
        if (!toPersist.isEmpty()) {
            try {
                tradeDetailsService.saveAllTradeDetails(toPersist);
                log.info("Persisted {} ISIN/SYMBOL-resolved tickers after enrich", toPersist.size());
            } catch (Exception e) {
                log.warn("Failed to persist resolved tickers: {}", e.getMessage());
            }
        }

        List<TradeDetails> openTrades = trades.stream()
                .filter(t -> TradeStatus.OPEN.equals(t.getStatus()) && t.getSymbol() != null)
                .collect(Collectors.toList());

        if (openTrades.isEmpty()) {
            return;
        }

        List<String> symbols = openTrades.stream()
                .map(trade -> trade.getSymbol() != null ? trade.getSymbol().trim() : null)
                .filter(s -> s != null && !s.isEmpty())
                .filter(s -> !validationUtils.isValidIsin(s))
                .distinct()
                .collect(Collectors.toList());

        if (symbols.isEmpty()) {
            return;
        }

        try {
            Map<String, am.trade.common.models.market.MarketDataResponse> quotes =
                    marketDataApiClient.getQuotes(symbols);
            log.info("[enrichWithLivePrices] Live quotes received from API for {} symbols",
                    quotes != null ? quotes.size() : 0);

            if (quotes == null || quotes.isEmpty()) {
                return;
            }

            List<TradeDetails> healZeroCost = new ArrayList<>();
            Map<String, Double> previousCloseCache = new HashMap<>();
            openTrades.forEach(trade -> {
                String cleanSymbol = trade.getSymbol() != null ? trade.getSymbol().trim() : null;
                if (cleanSymbol != null && cleanSymbol.contains(":")) {
                    cleanSymbol = cleanSymbol.substring(cleanSymbol.lastIndexOf(":") + 1);
                }
                am.trade.common.models.market.MarketDataResponse quote =
                        cleanSymbol != null ? quotes.get(cleanSymbol) : null;
                if (quote == null && trade.getSymbol() != null) {
                    quote = quotes.get(trade.getSymbol().trim());
                }
                Double price = quote != null ? quote.getLastPrice() : null;
                // Market-data often returns 0.0 for unknown tickers — treat as miss.
                if (price == null || price <= 0) {
                    return;
                }
                trade.setCurrentPrice(BigDecimal.valueOf(price));

                if (trade.getTradePositionType() == null) {
                    trade.setTradePositionType(am.trade.models.enums.TradePositionType.LONG);
                }

                if (trade.getMetrics() == null) {
                    trade.setMetrics(new am.trade.common.models.TradeMetrics());
                }
                // Clear stale metrics so unknown-cost rows cannot keep inflated P&L.
                trade.getMetrics().setProfitLoss(null);
                trade.getMetrics().setProfitLossPercentage(null);
                trade.getMetrics().setTodayProfitLoss(null);
                trade.getMetrics().setTodayProfitLossPercentage(null);
                trade.getMetrics().setUnrealizedBasis(null);

                Integer qty = trade.getEntryInfo() != null ? trade.getEntryInfo().getQuantity() : null;
                if (qty == null || qty <= 0) {
                    return;
                }
                BigDecimal quantity = BigDecimal.valueOf(qty);
                BigDecimal currentPrc = trade.getCurrentPrice();

                // Heal legacy email imports that persisted entry price as 0 — never use live LTP as mark.
                BigDecimal rawEntry = trade.getEntryInfo() != null ? trade.getEntryInfo().getPrice() : null;
                boolean healed = false;
                if (rawEntry != null && rawEntry.compareTo(BigDecimal.ZERO) <= 0) {
                    trade.getEntryInfo().setPrice(null);
                    if (trade.getEntryInfo().getTotalValue() != null
                            && trade.getEntryInfo().getTotalValue().compareTo(BigDecimal.ZERO) <= 0) {
                        trade.getEntryInfo().setTotalValue(null);
                    }
                    healed = true;
                }
                // Durable import-mark backfill from hist close on entry/asOf date (not live LTP).
                if ((trade.getImportMarkPrice() == null
                        || trade.getImportMarkPrice().compareTo(BigDecimal.ZERO) <= 0)
                        && (trade.getEntryInfo() == null
                            || trade.getEntryInfo().getPrice() == null
                            || trade.getEntryInfo().getPrice().compareTo(BigDecimal.ZERO) <= 0)) {
                    LocalDate asOf = trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null
                            ? trade.getEntryInfo().getTimestamp().toLocalDate()
                            : null;
                    if (asOf != null && cleanSymbol != null) {
                        Double histClose = marketDataApiClient.getCloseOnOrBefore(cleanSymbol, asOf);
                        if (histClose != null && histClose > 0) {
                            trade.setImportMarkPrice(BigDecimal.valueOf(histClose));
                            healed = true;
                        }
                    }
                }
                if (healed) {
                    healZeroCost.add(trade);
                }

                BigDecimal entryPrice = trade.getEntryInfo() != null ? trade.getEntryInfo().getPrice() : null;
                boolean hasCost = entryPrice != null && entryPrice.compareTo(BigDecimal.ZERO) > 0;
                BigDecimal importMark = trade.getImportMarkPrice();
                boolean hasImportMark = importMark != null && importMark.compareTo(BigDecimal.ZERO) > 0;

                // 1D P&L — same formula as Portfolio (LTP − previousClose) × qty.
                // Live OHLC often omits/collapses previousClose; hist close is Trade's
                // equivalent of Portfolio's Mongo previousClose fallback.
                Double previousClose = resolvePreviousCloseForDayPnl(
                        quote, cleanSymbol, currentPrc, previousCloseCache);
                if (previousClose != null && previousClose > 0) {
                    BigDecimal prev = BigDecimal.valueOf(previousClose);
                    BigDecimal dayPerUnit = currentPrc.subtract(prev);
                    if (am.trade.models.enums.TradePositionType.SHORT.equals(trade.getTradePositionType())) {
                        dayPerUnit = prev.subtract(currentPrc);
                    }
                    BigDecimal dayPnl = dayPerUnit.multiply(quantity);
                    trade.getMetrics().setTodayProfitLoss(dayPnl);
                    BigDecimal dayPct = dayPerUnit
                            .divide(prev, DECIMAL_SCALE, ROUNDING_MODE)
                            .multiply(BigDecimal.valueOf(100));
                    trade.getMetrics().setTodayProfitLossPercentage(dayPct);
                }

                // Overall unrealized: cost OR import mark only — never copy 1D into overall.
                BigDecimal baseline = null;
                String basis = null;
                if (hasCost) {
                    baseline = entryPrice;
                    basis = "COST";
                } else if (hasImportMark) {
                    baseline = importMark;
                    basis = "IMPORT_MARK";
                }

                if (baseline != null) {
                    BigDecimal pnlPerUnit;
                    if (am.trade.models.enums.TradePositionType.SHORT.equals(trade.getTradePositionType())) {
                        pnlPerUnit = baseline.subtract(currentPrc);
                    } else {
                        pnlPerUnit = currentPrc.subtract(baseline);
                    }
                    trade.getMetrics().setProfitLoss(pnlPerUnit.multiply(quantity));
                    trade.getMetrics().setProfitLossPercentage(
                            pnlPerUnit.divide(baseline, DECIMAL_SCALE, ROUNDING_MODE)
                                    .multiply(BigDecimal.valueOf(100)));
                    trade.getMetrics().setUnrealizedBasis(basis);
                }
            });

            if (!healZeroCost.isEmpty()) {
                try {
                    tradeDetailsService.saveAllTradeDetails(healZeroCost);
                    log.info("Healed {} zero-cost imported holdings to null entry price", healZeroCost.size());
                } catch (Exception e) {
                    log.warn("Failed to persist zero-cost entry heal: {}", e.getMessage());
                }
            }

            // Persist portfolio card totalValue from live/import marks (LTP itself stays in-memory).
            refreshPortfolioTotalValues(openTrades);
        } catch (Exception e) {
            log.error("Error enriching trades with live prices", e);
        }
    }

    /**
     * Prefer quote previousClose; if missing/collapsed, hist close on/before yesterday (IST).
     * Rejects a prior that equals LTP (same collapsed-after-hours guard as Portfolio).
     */
    private Double resolvePreviousCloseForDayPnl(
            am.trade.common.models.market.MarketDataResponse quote,
            String cleanSymbol,
            BigDecimal lastPrice,
            Map<String, Double> cache) {
        Double previousClose = quote != null ? quote.resolvePreviousClose() : null;
        if (isUsablePrior(previousClose, lastPrice)) {
            return previousClose;
        }
        if (cleanSymbol == null || cleanSymbol.isBlank()) {
            return null;
        }
        if (cache.containsKey(cleanSymbol)) {
            return cache.get(cleanSymbol);
        }
        LocalDate asOf = LocalDate.now(ZoneId.of("Asia/Kolkata")).minusDays(1);
        Double hist = null;
        try {
            hist = marketDataApiClient.getCloseOnOrBefore(cleanSymbol, asOf);
        } catch (Exception e) {
            log.warn("Hist previousClose fallback failed for {}: {}", cleanSymbol, e.getMessage());
        }
        Double resolved = isUsablePrior(hist, lastPrice) ? hist : null;
        cache.put(cleanSymbol, resolved);
        return resolved;
    }

    private static boolean isUsablePrior(Double prior, BigDecimal lastPrice) {
        if (prior == null || prior <= 0) {
            return false;
        }
        if (lastPrice == null) {
            return true;
        }
        return Math.abs(prior - lastPrice.doubleValue()) > 1e-6;
    }

    /**
     * After live enrich, rewrite {@code metrics.totalValue} so Trade portfolio cards
     * show market value for unknown-cost demat books instead of ₹0.
     */
    private void refreshPortfolioTotalValues(List<TradeDetails> openTrades) {
        if (openTrades == null || openTrades.isEmpty()) {
            return;
        }
        Map<String, BigDecimal> totals = new HashMap<>();
        for (TradeDetails trade : openTrades) {
            if (trade == null || trade.getPortfolioId() == null || trade.getPortfolioId().isBlank()) {
                continue;
            }
            BigDecimal piece = TradeProcessingServiceImpl.resolvePositionMarketOrCostValue(trade);
            totals.merge(trade.getPortfolioId(), piece, BigDecimal::add);
        }
        for (Map.Entry<String, BigDecimal> entry : totals.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }
            try {
                Optional<PortfolioModel> opt = portfolioService.findByPortfolioId(entry.getKey());
                if (opt.isEmpty()) {
                    continue;
                }
                PortfolioModel portfolio = opt.get();
                PortfolioMetrics metrics = portfolio.getMetrics();
                if (metrics == null) {
                    metrics = PortfolioMetrics.builder().build();
                    portfolio.setMetrics(metrics);
                }
                BigDecimal existing = metrics.getTotalValue();
                if (existing != null
                        && existing.compareTo(BigDecimal.ZERO) > 0
                        && existing.subtract(entry.getValue()).abs().compareTo(BigDecimal.ONE) <= 0) {
                    continue;
                }
                metrics.setTotalValue(entry.getValue());
                portfolioService.savePortfolio(portfolio);
                evictPortfolioSummaryCache();
                log.info("Refreshed portfolio {} totalValue to {}", entry.getKey(), entry.getValue());
            } catch (Exception e) {
                log.warn("Failed to refresh totalValue for portfolio {}: {}", entry.getKey(), e.getMessage());
            }
        }
    }

    private void evictPortfolioSummaryCache() {
        try {
            var cache = cacheManager.getCache("portfolioSummary");
            if (cache != null) {
                cache.clear();
            }
        } catch (Exception e) {
            log.warn("Could not evict portfolioSummary cache: {}", e.getMessage());
        }
    }

}
