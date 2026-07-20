package am.trade.services.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration for caching using Caffeine cache provider
 * Sets up caches for trade summaries and metrics with configurable expiry and size
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${cache.trade-summary.expiry-minutes:60}")
    private long tradeSummaryCacheExpiryMinutes;

    @Value("${cache.trade-summary.max-size:1000}")
    private long tradeSummaryCacheMaxSize;

    /**
     * Configure the cache manager with Caffeine cache provider
     *
     * @return Configured cache manager
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("tradeSummaryCache");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Configure Caffeine cache with expiry and maximum size
     *
     * @return Caffeine cache builder
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .expireAfterWrite(tradeSummaryCacheExpiryMinutes, TimeUnit.MINUTES)
                .maximumSize(tradeSummaryCacheMaxSize)
                .recordStats();
    }
}
