package am.trade.exceptions;

import am.trade.exceptions.model.ErrorDetail;
import org.springframework.http.HttpStatus;

import java.util.List;

/**
 * Exception thrown when validation errors occur in the application
 */
public class ValidationException extends TradeException {
    
    /**
     * Create a new ValidationException with a single error message
     * @param message Error message
     */
    public ValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Create a new ValidationException with a message and cause
     * @param message Error message
     * @param cause Root cause exception
     */
    public ValidationException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Create a new ValidationException with a message and list of errors
     * @param message Error message
     * @param errors List of validation errors
     */
    public ValidationException(String message, List<ErrorDetail> errors) {
        super(message, HttpStatus.BAD_REQUEST);
        addErrors(errors);
    }
    
    /**
     * Create a validation exception builder for fluent API usage
     * @param message General validation error message
     * @return A new ValidationExceptionBuilder
     */
    public static ValidationExceptionBuilder builder(String message) {
        return new ValidationExceptionBuilder(message);
    }
    
    /**
     * Builder class for creating ValidationExceptions with multiple errors
     */
    public static class ValidationExceptionBuilder {
        private final ValidationException exception;
        
        /**
         * Create a new builder with the specified message
         * @param message General validation error message
         */
        public ValidationExceptionBuilder(String message) {
            this.exception = new ValidationException(message);
        }
        
        /**
         * Add a validation error
         * @param field Field with error
         * @param message Error message
         * @param code Error code
         * @return This builder for chaining
         */
        public ValidationExceptionBuilder addError(String field, String message, String code) {
            exception.addError(field, message, code);
            return this;
        }
        
        /**
         * Add a validation error
         * @param error Error detail object
         * @return This builder for chaining
         */
        public ValidationExceptionBuilder addError(ErrorDetail error) {
            exception.addError(error);
            return this;
        }
        
        /**
         * Add multiple validation errors
         * @param errors List of error details
         * @return This builder for chaining
         */
        public ValidationExceptionBuilder addErrors(List<ErrorDetail> errors) {
            exception.addErrors(errors);
            return this;
        }
        
        /**
         * Build the ValidationException with all added errors
         * @return The built ValidationException
         */
        public ValidationException build() {
            return exception;
        }
    }
}
