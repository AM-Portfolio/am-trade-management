package am.trade.services.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for caching using Redis.
 * This can be toggled on or off via am.trade.cache.enabled
 */
@Configuration
@EnableCaching
@ConditionalOnProperty(name = "am.trade.cache.enabled", havingValue = "true")
public class CacheConfig {

    @Value("${cache.trade-details.expiry-minutes:10}")
    private long tradeDetailsExpiryMinutes;

    @Value("${cache.portfolio-summary.expiry-minutes:5}")
    private long portfolioSummaryExpiryMinutes;

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(10));

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        cacheConfigurations.put("tradeDetails", defaultConfig.entryTtl(Duration.ofMinutes(tradeDetailsExpiryMinutes)));
        cacheConfigurations.put("portfolioSummary", defaultConfig.entryTtl(Duration.ofMinutes(portfolioSummaryExpiryMinutes)));
        cacheConfigurations.put("tradeDomainCache", defaultConfig.entryTtl(Duration.ofMinutes(10)));

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
