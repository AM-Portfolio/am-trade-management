package am.trade.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import am.trade.api.config.ApiAutoConfiguration;
//import am.trade.analytics.config.TradeAnalyticsAutoConfiguration;
import am.trade.dashboard.config.DashboardAutoConfiguration;
import am.trade.exceptions.config.ExceptionsAutoConfiguration;
import am.trade.kafka.config.KafkaConfig;
import am.trade.services.config.TradeServicesAutoConfiguration;

/**
 * Main application class for Trade Management System
 */
@SpringBootApplication(scanBasePackages = {"am.trade"})
@Import({
    KafkaConfig.class,
    TradeServicesAutoConfiguration.class,
    DashboardAutoConfiguration.class,
    ApiAutoConfiguration.class,
    ExceptionsAutoConfiguration.class,  
    //TradeAnalyticsAutoConfiguration.class
})
@EnableMongoRepositories(basePackages = "am.trade.models.repository")
public class TradeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeManagementApplication.class, args);
    }
}
