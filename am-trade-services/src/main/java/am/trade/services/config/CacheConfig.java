package am.trade.services.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching using Caffeine cache provider.
 * Provides separate cache specifications for different data domains to allow fine‑grained TTLs and sizes.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    // ----- Trade Details By Day Cache -----
    @Value("${cache.trade-details-by-day.expiry-minutes:10}")
    private long tradeDetailsByDayExpiryMinutes;
    @Value("${cache.trade-details-by-day.max-size:2000}")
    private long tradeDetailsByDayMaxSize;

    // ----- Trade Details By Month Cache -----
    @Value("${cache.trade-details-by-month.expiry-minutes:60}")
    private long tradeDetailsByMonthExpiryMinutes;
    @Value("${cache.trade-details-by-month.max-size:2000}")
    private long tradeDetailsByMonthMaxSize;

    // ----- Portfolio Trades Cache (short‑lived) -----
    @Value("${cache.portfolio-trades.expiry-minutes:5}")
    private long portfolioTradesExpiryMinutes;
    @Value("${cache.portfolio-trades.max-size:1000}")
    private long portfolioTradesMaxSize;

    // ----- Trades By Date Range Cache -----
    @Value("${cache.trades-by-date-range.expiry-minutes:5}")
    private long tradesByDateRangeExpiryMinutes;
    @Value("${cache.trades-by-date-range.max-size:2000}")
    private long tradesByDateRangeMaxSize;

    // ----- Trades By Symbols Cache -----
    @Value("${cache.trades-by-symbols.expiry-minutes:10}")
    private long tradesBySymbolsExpiryMinutes;
    @Value("${cache.trades-by-symbols.max-size:2000}")
    private long tradesBySymbolsMaxSize;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(Arrays.asList(
                caffeineCache("tradeDetailsByDay", tradeDetailsByDayExpiryMinutes, tradeDetailsByDayMaxSize),
                caffeineCache("tradeDetailsByMonth", tradeDetailsByMonthExpiryMinutes, tradeDetailsByMonthMaxSize),
                caffeineCache("portfolioTrades", portfolioTradesExpiryMinutes, portfolioTradesMaxSize),
                caffeineCache("tradesByDateRange", tradesByDateRangeExpiryMinutes, tradesByDateRangeMaxSize),
                caffeineCache("tradesBySymbols", tradesBySymbolsExpiryMinutes, tradesBySymbolsMaxSize)
        ));
        return manager;
    }

    private CaffeineCache caffeineCache(String name, long expiryMinutes, long maxSize) {
        return new CaffeineCache(name,
                Caffeine.newBuilder()
                        .expireAfterWrite(expiryMinutes, TimeUnit.MINUTES)
                        .maximumSize(maxSize)
                        .recordStats()
                        .build());
    }
}
