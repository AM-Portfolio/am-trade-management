package am.trade.api.service.impl;

import am.trade.api.client.MarketDataApiClient;
import am.trade.common.models.InstrumentInfo;
import am.trade.common.models.TradeDetails;
import am.trade.common.util.ValidationUtils;
import am.trade.services.service.PortfolioSyncInstrumentResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Uses the same securities batch-search path as {@code enrichWithLivePrices}.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSyncInstrumentResolverImpl implements PortfolioSyncInstrumentResolver {

    private final MarketDataApiClient marketDataApiClient;
    private final ValidationUtils validationUtils;

    @Override
    public void resolveForSync(TradeDetails trade) {
        if (trade == null) {
            return;
        }
        resolveForSync(List.of(trade));
    }

    @Override
    public void resolveForSync(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return;
        }

        List<String> isinsToResolve = trades.stream()
                .map(this::extractIsinKey)
                .filter(s -> s != null && validationUtils.isValidIsin(s))
                .distinct()
                .collect(Collectors.toList());

        Map<String, Map<String, String>> resolved = Map.of();
        if (!isinsToResolve.isEmpty()) {
            try {
                Map<String, Map<String, String>> api = marketDataApiClient.resolveTickersByIsins(isinsToResolve);
                resolved = api != null ? api : Map.of();
            } catch (Exception ex) {
                log.warn("ISIN resolve for portfolio sync failed: {}", ex.getMessage());
                resolved = Map.of();
            }
        }

        Set<TradeDetails> isinResolved = new HashSet<>();
        for (TradeDetails trade : trades) {
            if (applyResolved(trade, resolved)) {
                isinResolved.add(trade);
            }
        }

        // SYMBOL/NAME canonicalize for broker aliases (IDEA→VODAFONEIDEA) and ISIN misses only.
        applySymbolNameFallback(trades, isinResolved);
    }

    private void applySymbolNameFallback(List<TradeDetails> trades, Set<TradeDetails> skipTrades) {
        List<String> queries = new java.util.ArrayList<>();
        for (TradeDetails trade : trades) {
            if (skipTrades.contains(trade)) {
                continue;
            }
            String sym = trade.getSymbol();
            if (sym != null && !sym.isBlank() && !validationUtils.isValidIsin(sym.trim())) {
                queries.add(stripBrokerSeriesSuffix(sym.trim().toUpperCase(Locale.ROOT)));
            }
            InstrumentInfo info = trade.getInstrumentInfo();
            if (info != null && info.getDescription() != null && !info.getDescription().isBlank()) {
                String desc = stripBrokerSeriesSuffix(info.getDescription().trim());
                if (!desc.isBlank()) {
                    queries.add(desc);
                }
            }
        }
        queries = queries.stream().distinct().collect(Collectors.toList());
        if (queries.isEmpty()) {
            return;
        }
        try {
            Map<String, Map<String, String>> byQuery = marketDataApiClient.resolveTickersByQueries(
                    queries, java.util.Arrays.asList("SYMBOL", "NAME"));
            if (byQuery == null || byQuery.isEmpty()) {
                return;
            }
            for (TradeDetails trade : trades) {
                if (skipTrades.contains(trade)) {
                    continue;
                }
                String sym = trade.getSymbol();
                String originalSym = sym != null ? stripBrokerSeriesSuffix(sym.trim().toUpperCase(Locale.ROOT)) : null;

                Map<String, String> symbolHit = originalSym != null ? byQuery.get(originalSym) : null;
                Map<String, String> nameHit = null;
                if (trade.getInstrumentInfo() != null
                        && trade.getInstrumentInfo().getDescription() != null) {
                    String desc = stripBrokerSeriesSuffix(trade.getInstrumentInfo().getDescription().trim());
                    nameHit = byQuery.get(desc.toUpperCase(Locale.ROOT));
                    if (nameHit == null) {
                        nameHit = byQuery.get(desc);
                    }
                }

                Map<String, String> hit = pickBestQueryHit(symbolHit, nameHit, originalSym);
                if (hit == null) {
                    continue;
                }
                String ticker = hit.get("symbol");
                if (ticker != null && !ticker.isBlank() && !validationUtils.isValidIsin(ticker)) {
                    String cleanTicker = ticker.trim().toUpperCase(Locale.ROOT);
                    if (originalSym == null || !cleanTicker.equals(originalSym)) {
                        trade.setSymbol(cleanTicker);
                        ensureInstrumentInfo(trade).setSymbol(cleanTicker);
                    }
                }
                String description = hit.get("description");
                if (description != null && !description.isBlank()) {
                    ensureInstrumentInfo(trade).setDescription(description.trim());
                }
            }
        } catch (Exception ex) {
            log.warn("SYMBOL/NAME fallback resolve failed: {}", ex.getMessage());
        }
    }

    /**
     * Prefer a SYMBOL hit that actually changes the ticker; otherwise use NAME
     * (e.g. IDEA→IDEA no-op should not block IDEA→VODAFONEIDEA via company name).
     */
    private Map<String, String> pickBestQueryHit(
            Map<String, String> symbolHit, Map<String, String> nameHit, String originalSym) {
        if (symbolHit != null) {
            String ticker = symbolHit.get("symbol");
            if (ticker != null && !ticker.isBlank() && !validationUtils.isValidIsin(ticker)) {
                String clean = ticker.trim().toUpperCase(Locale.ROOT);
                if (originalSym == null || !clean.equals(originalSym)) {
                    return symbolHit;
                }
            }
        }
        if (nameHit != null) {
            return nameHit;
        }
        return symbolHit;
    }

    /** Strip broker series suffixes ({@code -EQ}, {@code -BE}) before NAME/SYMBOL search. */
    static String stripBrokerSeriesSuffix(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return value.replaceAll("(?i)\\s*-\\s*(EQ|BE)\\s*$", "").trim();
    }

    /** @return true when ISIN lookup produced a non-ISIN ticker (do not overwrite with NAME/SYMBOL). */
    private boolean applyResolved(TradeDetails trade, Map<String, Map<String, String>> resolved) {
        String isinKey = extractIsinKey(trade);
        String currentSymbol = trade.getSymbol();
        boolean symbolIsIsin = currentSymbol != null && validationUtils.isValidIsin(currentSymbol.trim());

        if (isinKey != null && resolved.containsKey(isinKey)) {
            Map<String, String> info = resolved.get(isinKey);
            String ticker = info.get("symbol");
            String description = info.get("description");
            boolean tickerResolved = false;

            if (ticker != null && !ticker.isBlank() && !validationUtils.isValidIsin(ticker.trim())) {
                String cleanTicker = ticker.trim().toUpperCase(Locale.ROOT);
                trade.setSymbol(cleanTicker);
                ensureInstrumentInfo(trade).setSymbol(cleanTicker);
                tickerResolved = true;
            } else if (symbolIsIsin) {
                log.warn("Portfolio sync: could not resolve ticker for ISIN {} (tradeId={})",
                        isinKey, trade.getTradeId());
            }

            if (description != null && !description.isBlank()) {
                ensureInstrumentInfo(trade).setDescription(description.trim());
            }

            InstrumentInfo ii = ensureInstrumentInfo(trade);
            if (ii.getIsin() == null || ii.getIsin().isBlank()) {
                ii.setIsin(isinKey);
            }
            return tickerResolved;
        }

        if (symbolIsIsin) {
            log.warn("Portfolio sync: no market-data match for ISIN {} (tradeId={}) — emitting as-is",
                    currentSymbol, trade.getTradeId());
        }
        return false;
    }

    private String extractIsinKey(TradeDetails trade) {
        if (trade == null) {
            return null;
        }
        InstrumentInfo info = trade.getInstrumentInfo();
        if (info != null && info.getIsin() != null && !info.getIsin().isBlank()) {
            return info.getIsin().trim().toUpperCase();
        }
        String sym = trade.getSymbol();
        if (sym != null && validationUtils.isValidIsin(sym.trim())) {
            return sym.trim().toUpperCase();
        }
        return null;
    }

    private InstrumentInfo ensureInstrumentInfo(TradeDetails trade) {
        if (trade.getInstrumentInfo() == null) {
            trade.setInstrumentInfo(new InstrumentInfo());
        }
        return trade.getInstrumentInfo();
    }
}
