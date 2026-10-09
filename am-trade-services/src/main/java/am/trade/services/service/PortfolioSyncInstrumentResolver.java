package am.trade.services.service;

import am.trade.common.models.TradeDetails;

import java.util.List;

/**
 * Resolves ISIN → trading ticker (+ company name) before portfolio Kafka sync
 * so am-portfolio receives tickers instead of raw ISINs.
 */
public interface PortfolioSyncInstrumentResolver {

    /**
     * Mutates {@code trade} in place: sets ticker on symbol / instrumentInfo.symbol
     * and company name on instrumentInfo.description when resolvable.
     * Persisting the trade is the caller's responsibility when a write is desired.
     */
    void resolveForSync(TradeDetails trade);

    /** Batch variant — one market-data call for all ISINs. */
    void resolveForSync(List<TradeDetails> trades);
}
