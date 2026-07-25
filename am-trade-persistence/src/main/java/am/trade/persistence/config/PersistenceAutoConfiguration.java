package am.trade.persistence.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import am.trade.persistence.mapper.PortfolioMapper;
import am.trade.persistence.mapper.TradeDetailsMapper;
import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import java.util.concurrent.TimeUnit;

/**
 * Auto-configuration class for the persistence module
 * Automatically configures all necessary beans and components
 */
@AutoConfiguration
@ConditionalOnProperty(name = "am.trade.persistence.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = {"am.trade.persistence", "am.trade.common.models"})
@EntityScan(basePackages = "am.trade.persistence.entity")
@EnableMongoRepositories(basePackages = "am.trade.persistence.repository")
//@Import(MongoConfig.class)
public class PersistenceAutoConfiguration {

    /**
     * Creates the PortfolioMapper bean if not already defined
     * @return PortfolioMapper instance
     */
    @Bean
    @ConditionalOnProperty(name = "am.trade.persistence.portfolio.enabled", havingValue = "true", matchIfMissing = true)
    public PortfolioMapper portfolioMapper() {
        return new PortfolioMapper(new TradeDetailsMapper());
    }

    /**
     * Customize MongoDB client settings for high concurrency
     */
    @Bean
    public MongoClientSettingsBuilderCustomizer customMongoClientSettings() {
        return builder -> {
            builder.applyToConnectionPoolSettings(pool -> 
                pool.maxSize(1000)
                    .minSize(50)
                    .maxWaitTime(10, TimeUnit.SECONDS)
            );
        };
    }
}
