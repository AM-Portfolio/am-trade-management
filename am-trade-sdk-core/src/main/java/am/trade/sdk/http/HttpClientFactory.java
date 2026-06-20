package am.trade.sdk.http;

import am.trade.sdk.config.SdkConfiguration;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * Factory for creating OkHttpClient instances with SDK configuration
 */
@Slf4j
public class HttpClientFactory {

    /**
     * Create configured OkHttpClient
     *
     * @param config SDK configuration
     * @return Configured OkHttpClient
     */
    public static OkHttpClient createHttpClient(SdkConfiguration config) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        // Configure timeouts
        builder.connectTimeout(config.getConnectionTimeout(), TimeUnit.SECONDS);
        builder.readTimeout(config.getTimeout(), TimeUnit.SECONDS);
        builder.writeTimeout(config.getTimeout(), TimeUnit.SECONDS);

        // Configure logging if enabled
        if (config.isEnableLogging()) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(
                    message -> log.debug(message)
            );
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addInterceptor(loggingInterceptor);
        }

        // Configure SSL verification
        if (!config.isVerifySsl()) {
            try {
                javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                sslContext.init(
                        null,
                        new javax.net.ssl.TrustManager[]{new TrustAllCertificates()},
                        new java.security.SecureRandom()
                );
                builder.sslSocketFactory(
                        sslContext.getSocketFactory(),
                        new TrustAllCertificates()
                );
                builder.hostnameVerifier((hostname, session) -> true);
            } catch (Exception e) {
                log.warn("Failed to disable SSL verification", e);
            }
        }

        // Add retry interceptor
        builder.addInterceptor(new RetryInterceptor(config.getMaxRetries()));

        return builder.build();
    }

    /**
     * Trust all certificates (for SSL verification disabled)
     */
    private static class TrustAllCertificates implements javax.net.ssl.X509TrustManager {
        @Override
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType) {
        }

        @Override
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] chain,
                String authType) {
        }

        @Override
        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[]{};
        }
    }
}
