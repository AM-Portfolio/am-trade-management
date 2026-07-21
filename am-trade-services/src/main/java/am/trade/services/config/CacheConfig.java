package am.trade.services.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.tracing.MicrometerTracing;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for caching using Redis.
 * This can be toggled on or off via am.trade.cache.enabled
 *
 * Redis tracing is enabled by injecting MicrometerTracing into the Lettuce
 * client resources, exactly mirroring am-portfolio's approach. This is
 * required because the Spring Boot YAML property
 * spring.data.redis.client.observation.enabled only works for the
 * auto-configured connection factory. Since we declare a custom bean here,
 * we must wire tracing manually.
 */
@Configuration
@EnableCaching
@ConditionalOnExpression("false") // FORCED OFF BY USER REQUEST
public class CacheConfig {

    @Value("${cache.trade-details.expiry-minutes:10}")
    private long tradeDetailsExpiryMinutes;

    @Value("${cache.portfolio-summary.expiry-minutes:5}")
    private long portfolioSummaryExpiryMinutes;

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    /**
     * Custom Lettuce connection factory with MicrometerTracing injected.
     * This is what makes "redis.get", "redis.set" spans appear in Grafana Tempo.
     * Mirrors portfolio-redis RedisConfig exactly.
     */
    @Bean
    @Primary
    public RedisConnectionFactory redisConnectionFactory(ObservationRegistry observationRegistry) {
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        redisConfig.setPort(redisPort);

        if (redisPassword != null && !redisPassword.isBlank()) {
            redisConfig.setPassword(redisPassword);
        }

        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofSeconds(5))
                .clientResources(DefaultClientResources.builder()
                        .tracing(new MicrometerTracing(observationRegistry, "redis"))
                        .build())
                .build();

        return new LettuceConnectionFactory(redisConfig, clientConfig);
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        ObjectMapper cacheObjectMapper = objectMapper.copy();

        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType(Object.class)
                .build();

        cacheObjectMapper.activateDefaultTyping(ptv, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(cacheObjectMapper);

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer))
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
