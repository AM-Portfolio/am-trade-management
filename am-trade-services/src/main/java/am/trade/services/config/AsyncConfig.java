package am.trade.services.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Dedicated thread pool for processing trade aggregations/deltas.
     * Uses a bounded queue and CallerRunsPolicy to provide graceful backpressure
     * when the system is under extreme load (e.g., 500+ concurrent requests).
     */
    @Bean(name = "tradeProcessingExecutor")
    public Executor tradeProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("TradeProc-");
        // If the queue is full, the calling thread (Tomcat HTTP thread) will execute the task directly.
        // This acts as a natural backpressure mechanism to prevent OutOfMemory errors and queue unbounded growth.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
