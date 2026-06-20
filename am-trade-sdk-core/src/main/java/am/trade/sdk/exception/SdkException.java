package am.trade.sdk.exception;

import am.trade.exceptions.TradeException;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base exception for AM Trade SDK.
 *
 * All SDK-specific exceptions extend this class.
 */
@Getter
public class SdkException extends TradeException {

    private final String errorCode;
    private final Object details;

    /**
     * Create SDK exception
     *
     * @param message Error message
     */
    public SdkException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        this.errorCode = "SDK_ERROR";
        this.details = null;
    }

    /**
     * Create SDK exception with error code
     *
     * @param message Error message
     * @param errorCode Error code
     */
    public SdkException(String message, String errorCode) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        this.errorCode = errorCode;
        this.details = null;
    }

    /**
     * Create SDK exception with details
     *
     * @param message Error message
     * @param errorCode Error code
     * @param details Additional details
     */
    public SdkException(String message, String errorCode, Object details) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
        this.errorCode = errorCode;
        this.details = details;
    }

    /**
     * Create SDK exception with cause
     *
     * @param message Error message
     * @param cause Root cause
     */
    public SdkException(String message, Throwable cause) {
        super(message, cause, HttpStatus.INTERNAL_SERVER_ERROR);
        this.errorCode = "SDK_ERROR";
        this.details = null;
    }

    /**
     * Create SDK exception with HTTP status
     *
     * @param message Error message
     * @param status HTTP status
     */
    public SdkException(String message, HttpStatus status) {
        super(message, status);
        this.errorCode = "SDK_ERROR";
        this.details = null;
    }
}
