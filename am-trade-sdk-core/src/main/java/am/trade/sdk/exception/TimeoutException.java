package am.trade.sdk.exception;

/**
 * Exception for request timeout errors
 */
public class TimeoutException extends SdkException {

    private final int timeoutSeconds;

    public TimeoutException(String message, int timeoutSeconds) {
        super(message, "REQUEST_TIMEOUT");
        this.timeoutSeconds = timeoutSeconds;
    }

    public TimeoutException(String message, int timeoutSeconds, Throwable cause) {
        super(message, cause);
        this.timeoutSeconds = timeoutSeconds;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
