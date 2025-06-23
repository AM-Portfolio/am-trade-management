package am.trade.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import am.trade.kafka.service.metrics.TradeService;
import am.trade.kafka.service.metrics.impl.TradeServiceImpl;
import am.trade.persistence.service.TradeDetailsService;

/**
 * Configuration for Trade Service beans
 */
@Configuration
public class TradeServiceConfig {
    
    @Bean
    public TradeService tradeService(TradeDetailsService tradeDetailsService) {
        return new TradeServiceImpl(tradeDetailsService);
    }
}
