package am.trade.services.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import am.trade.services.service.TradeDetailsService;
import am.trade.services.service.impl.TradeDetailsServiceImpl;
import am.trade.persistence.repository.TradeDetailsRepository;
import am.trade.persistence.mapper.TradeDetailsMapper;
import am.trade.persistence.config.PersistenceAutoConfiguration;

/**
 * Auto-configuration for the services module
 */
@AutoConfiguration
@Import(PersistenceAutoConfiguration.class)
@ComponentScan(basePackages = {"am.trade.services.service", "am.trade.services.service.impl"})
public class ServicesAutoConfiguration {
    
    /**
     * Creates the TradeDetailsService bean if not already defined
     * 
     * @param tradeDetailsRepository The trade details repository
     * @param tradeDetailsMapper The trade details mapper
     * @return TradeDetailsService instance
     */
    @Bean
    @ConditionalOnMissingBean
    public TradeDetailsService tradeDetailsService(
            TradeDetailsRepository tradeDetailsRepository,
            TradeDetailsMapper tradeDetailsMapper) {
        return new TradeDetailsServiceImpl(tradeDetailsRepository, tradeDetailsMapper);
    }
}
