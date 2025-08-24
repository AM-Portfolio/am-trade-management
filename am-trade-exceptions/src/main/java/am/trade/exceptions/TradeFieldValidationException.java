package am.trade.exceptions;

import am.trade.exceptions.model.ErrorDetail;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Exception thrown when trade field validation errors occur
 * Specifically designed for trade update validation failures
 */
public class TradeFieldValidationException extends ValidationException {
    
    /**
     * Create a new TradeFieldValidationException with a single error message
     * @param message Error message
     */
    public TradeFieldValidationException(String message) {
        super(message);
    }
    
    /**
     * Create a new TradeFieldValidationException with a message and cause
     * @param message Error message
     * @param cause Root cause exception
     */
    public TradeFieldValidationException(String message, Throwable cause) {
        super(message, cause);
    }
    
    /**
     * Create a new TradeFieldValidationException with a message and list of errors
     * @param message Error message
     * @param errors List of validation errors
     */
    public TradeFieldValidationException(String message, List<ErrorDetail> errors) {
        super(message);
        addErrors(errors);
    }
    
    /**
     * Create a trade field validation exception builder for fluent API usage
     * @param message General validation error message
     * @return A new TradeFieldValidationExceptionBuilder
     */
    public static TradeFieldValidationExceptionBuilder fieldBuilder(String message) {
        return new TradeFieldValidationExceptionBuilder(message);
    }
    
    /**
     * Builder class for creating TradeFieldValidationExceptions with multiple errors
     */
    public static class TradeFieldValidationExceptionBuilder {
        private final TradeFieldValidationException exception;
        
        /**
         * Create a new builder with the specified message
         * @param message General validation error message
         */
        public TradeFieldValidationExceptionBuilder(String message) {
            this.exception = new TradeFieldValidationException(message);
        }
        
        /**
         * Add a validation error
         * @param field Field with error
         * @param message Error message
         * @return This builder for chaining
         */
        public TradeFieldValidationExceptionBuilder addFieldError(String field, String message) {
            exception.addError(field, message, "FIELD_IMMUTABLE");
            return this;
        }
        
        /**
         * Add a validation error with custom code
         * @param field Field with error
         * @param message Error message
         * @param code Error code
         * @return This builder for chaining
         */
        public TradeFieldValidationExceptionBuilder addError(String field, String message, String code) {
            exception.addError(field, message, code);
            return this;
        }
        
        /**
         * Add multiple validation errors
         * @param errors List of error details
         * @return This builder for chaining
         */
        public TradeFieldValidationExceptionBuilder addErrors(List<ErrorDetail> errors) {
            exception.addErrors(errors);
            return this;
        }
        
        /**
         * Build the TradeFieldValidationException with all added errors
         * @return The built TradeFieldValidationException
         */
        public TradeFieldValidationException build() {
            return exception;
        }
    }
}
