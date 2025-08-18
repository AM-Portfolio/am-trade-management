package am.trade.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.Import;

import am.trade.api.config.ApiAutoConfiguration;
import am.trade.analytics.config.TradeAnalyticsAutoConfiguration;
import am.trade.dashboard.config.DashboardAutoConfiguration;
import am.trade.exceptions.config.ExceptionsAutoConfiguration;
import am.trade.kafka.config.KafkaAutoConfiguration;
import am.trade.persistence.config.MongoConfig;
import am.trade.services.config.TradeServicesAutoConfiguration;

/**
 * Main application class for Trade Management System
 */
@SpringBootApplication(
    scanBasePackages = {"am.trade"},
    exclude = {
        // Exclude default MongoDB auto-configuration as we're using our custom one
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
    }
)
@Import({
    MongoConfig.class,
    TradeServicesAutoConfiguration.class,
    DashboardAutoConfiguration.class,
    ApiAutoConfiguration.class,
    KafkaAutoConfiguration.class,
    ExceptionsAutoConfiguration.class,
    TradeAnalyticsAutoConfiguration.class
}) 
public class TradeManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeManagementApplication.class, args);
    }
}
