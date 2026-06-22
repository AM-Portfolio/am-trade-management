package am.trade.app;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import am.trade.api.config.ApiAutoConfiguration;
import am.trade.analytics.config.TradeAnalyticsAutoConfiguration;
import am.trade.dashboard.config.DashboardAutoConfiguration;
import am.trade.exceptions.config.ExceptionsAutoConfiguration;
import am.trade.kafka.config.KafkaConfig;
import am.trade.services.config.TradeServicesAutoConfiguration;

/**
 * Main application class for Trade Management System
 */
@SpringBootApplication(scanBasePackages = { "am.trade" })
@Import({
        KafkaConfig.class,
        am.trade.kafka.config.LocalKafkaConfig.class,
        TradeServicesAutoConfiguration.class,
        DashboardAutoConfiguration.class,
        ApiAutoConfiguration.class,
        ExceptionsAutoConfiguration.class,
        TradeAnalyticsAutoConfiguration.class
})

public class TradeManagementApplication {

    private static void loadVaultSecrets() {
        String[] secretFiles = {
            "/vault/secrets/app", "/vault/secrets/auth", "/vault/secrets/identity-oidc",
            "/vault/secrets/infra", "/vault/secrets/kafka", "/vault/secrets/market-data",
            "/vault/secrets/mongo", "/vault/secrets/redis", "/vault/secrets/service-oauth"
        };

        for (String filePath : secretFiles) {
            Path path = Paths.get(filePath);
            
            // Try to read for up to 10 seconds to handle the Vault Agent race condition
            int retries = 10;
            while (retries > 0) {
                if (Files.exists(path)) {
                    try {
                        List<String> lines = Files.readAllLines(path);
                        for (String line : lines) {
                            if (line.startsWith("export ")) {
                                String[] parts = line.substring(7).split("=", 2);
                                if (parts.length == 2) {
                                    String key = parts[0];
                                    String value = parts[1];
                                    // Remove surrounding quotes if present
                                    if (value.startsWith("\"") && value.endsWith("\"")) {
                                        value = value.substring(1, value.length() - 1);
                                    }
                                    System.setProperty(key, value);
                                    System.out.println("Loaded vault secret for key: " + key);
                                }
                            }
                        }
                        break; // Successfully read, break out of retry loop
                    } catch (IOException e) {
                        System.err.println("Failed to read " + filePath + ": " + e.getMessage());
                    }
                }
                
                try {
                    Thread.sleep(1000); // Wait 1 second before retrying
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                retries--;
            }
        }
    }

    public static void main(String[] args) {
        // Load secrets and fix the Vault Agent race condition & Bash syntax issue
        loadVaultSecrets();
        SpringApplication.run(TradeManagementApplication.class, args);
    }
}

