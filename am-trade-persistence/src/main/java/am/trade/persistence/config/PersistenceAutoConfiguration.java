package am.trade.persistence.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import am.trade.persistence.mapper.PortfolioMapper;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.repository.PortfolioRepository;
import am.trade.persistence.service.PortfolioPersistenceService;

/**
 * Auto-configuration class for the persistence module
 * Automatically configures all necessary beans and components
 */
@AutoConfiguration
@ConditionalOnProperty(name = "am.trade.persistence.enabled", havingValue = "true", matchIfMissing = true)
@ComponentScan(basePackages = "am.trade.persistence")
@EntityScan(basePackages = "am.trade.persistence.entity")
//@EnableMongoRepositories(basePackages = "am.trade.persistence.repository")
@Import(MongoConfig.class)
public class PersistenceAutoConfiguration {

    /**
     * Creates the PortfolioMapper bean if not already defined
     * @return PortfolioMapper instance
     */
    @Bean
    @ConditionalOnProperty(name = "am.trade.persistence.portfolio.enabled", havingValue = "true", matchIfMissing = true)
    public PortfolioMapper portfolioMapper() {
        return new PortfolioMapper();
    }

    /**
     * Creates the PortfolioPersistenceService bean if not already defined
     * @param portfolioRepository The portfolio repository
     * @param portfolioMapper The portfolio mapper
     * @return PortfolioPersistenceService instance
     */
    @Bean
    @ConditionalOnProperty(name = "am.trade.persistence.portfolio.enabled", havingValue = "true", matchIfMissing = true)
    public PortfolioPersistenceService portfolioPersistenceService(
            PortfolioRepository portfolioRepository,
            PortfolioMapper portfolioMapper,
            TradeDetailsMapper tradeDetailsMapper) {
        return new PortfolioPersistenceService(portfolioRepository, portfolioMapper, tradeDetailsMapper);
    }
}
