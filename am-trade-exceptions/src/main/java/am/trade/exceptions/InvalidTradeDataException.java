package am.trade.exceptions;

/**
 * Exception thrown when trade data is invalid or cannot be processed
 */
public class InvalidTradeDataException extends RuntimeException {
    
    public InvalidTradeDataException(String message) {
        super(message);
    }
    
    public InvalidTradeDataException(String message, Throwable cause) {
        super(message, cause);
    }
}
