package am.trade.analytics.client.config;

import am.trade.analytics.client.interceptor.RequestResponseLoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for the Market Data Client
 */
@Configuration
public class MarketDataClientConfig {

    @Value("${market-data.api.connection-timeout:5000}")
    private int connectionTimeout;
    
    @Value("${market-data.api.read-timeout:10000}")
    private int readTimeout;
    
    @Bean
    public RestTemplate marketDataRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        // Create a logging interceptor
        RequestResponseLoggingInterceptor loggingInterceptor = new RequestResponseLoggingInterceptor();
        
        // Use BufferingClientHttpRequestFactory to allow reading the response multiple times
        return restTemplateBuilder
                .requestFactory(() -> new BufferingClientHttpRequestFactory(clientHttpRequestFactory()))
                .interceptors(loggingInterceptor)
                .build();
    }

    private ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectionTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
