package am.trade.sdk.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for API errors (4xx, 5xx responses)
 */
public class ApiException extends SdkException {

    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message, "API_ERROR");
        this.statusCode = statusCode;
    }

    public ApiException(String message, int statusCode, String errorCode) {
        super(message, errorCode);
        this.statusCode = statusCode;
    }

    public ApiException(String message, int statusCode, Object details) {
        super(message, "API_ERROR", details);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    private HttpStatus getHttpStatus() {
        try {
            return HttpStatus.valueOf(statusCode);
        } catch (IllegalArgumentException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
