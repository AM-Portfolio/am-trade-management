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
     * Shared thread pool for processing asynchronous tasks (e.g., trade aggregations/deltas).
     * Uses a bounded queue and CallerRunsPolicy to provide graceful backpressure
     * when the system is under extreme load.
     */
    @Bean(name = "taskExecutor")
    @org.springframework.context.annotation.Primary
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("AsyncProc-");
        // If the queue is full, the calling thread (Tomcat HTTP thread) will execute the task directly.
        // This acts as a natural backpressure mechanism to prevent OutOfMemory errors and queue unbounded growth.
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
