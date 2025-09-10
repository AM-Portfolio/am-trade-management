package am.trade.analytics.client.interceptor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A buffered HTTP response that allows the response body to be read multiple times
 */
public class BufferedClientHttpResponse implements ClientHttpResponse {

    private final ClientHttpResponse response;
    private final byte[] body;

    public BufferedClientHttpResponse(ClientHttpResponse response, byte[] body) {
        this.response = response;
        this.body = body;
    }

    @Override
    @NonNull
    public HttpStatusCode getStatusCode() throws IOException {
        return response.getStatusCode();
    }

    @Override
    @NonNull
    public String getStatusText() throws IOException {
        return response.getStatusText();
    }

    @Override
    public void close() {
        response.close();
    }

    @Override
    @NonNull
    public InputStream getBody() throws IOException {
        return new ByteArrayInputStream(body);
    }

    @Override
    @NonNull
    public HttpHeaders getHeaders() {
        return response.getHeaders();
    }
}
