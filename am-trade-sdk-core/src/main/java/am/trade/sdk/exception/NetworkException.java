package am.trade.sdk.exception;

/**
 * Exception for network/connection errors
 */
public class NetworkException extends SdkException {

    public NetworkException(String message) {
        super(message, "NETWORK_ERROR");
    }

    public NetworkException(String message, Throwable cause) {
        super(message, cause);
    }

    public NetworkException(String message, Object details) {
        super(message, "NETWORK_ERROR", details);
    }
}
