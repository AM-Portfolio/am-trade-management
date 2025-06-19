package am.trade.common.exception;

import lombok.Getter;

/**
 * Base exception class for all application exceptions
 */
@Getter
public class BaseException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private final String errorCode;
    private final String errorMessage;
    private final int statusCode;
    
    public BaseException(String errorCode, String errorMessage, int statusCode) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }
    
    public BaseException(String errorCode, String errorMessage, int statusCode, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.statusCode = statusCode;
    }
}
