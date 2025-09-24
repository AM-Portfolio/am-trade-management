package am.trade.dashboard.exception;

/**
 * Exception thrown when there is an error generating trading feedback
 */
public class FeedbackGenerationException extends RuntimeException {
    
    public FeedbackGenerationException(String message) {
        super(message);
    }
    
    public FeedbackGenerationException(String message, Throwable cause) {
        super(message, cause);
    }
}
