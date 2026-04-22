package am.trade.sdk.http;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

/**
 * Retry interceptor for OkHttpClient with exponential backoff
 */
@Slf4j
public class RetryInterceptor implements Interceptor {

    private final int maxRetries;

    public RetryInterceptor(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Response response = null;
        IOException lastException = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                response = chain.proceed(request);

                // Retry on specific status codes
                if (shouldRetry(response.code()) && attempt < maxRetries) {
                    response.close();
                    long backoffMs = (long) Math.pow(2, attempt) * 1000;
                    log.debug("Retrying request, attempt {}/{}, backoff {}ms", 
                            attempt + 1, maxRetries, backoffMs);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Request interrupted", e);
                    }
                    continue;
                }

                return response;
            } catch (IOException e) {
                lastException = e;
                if (attempt < maxRetries) {
                    long backoffMs = (long) Math.pow(2, attempt) * 1000;
                    log.debug("Retrying after IOException, attempt {}/{}, backoff {}ms", 
                            attempt + 1, maxRetries, backoffMs);
                    try {
                        Thread.sleep(backoffMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw e;
                    }
                }
            }
        }

        if (lastException != null) {
            throw lastException;
        }

        return response;
    }

    private boolean shouldRetry(int statusCode) {
        return statusCode == 429 ||  // Rate limited
                statusCode == 500 ||  // Internal server error
                statusCode == 502 ||  // Bad gateway
                statusCode == 503 ||  // Service unavailable
                statusCode == 504;    // Gateway timeout
    }
}
