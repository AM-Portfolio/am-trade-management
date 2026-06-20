package am.trade.sdk.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception for validation errors
 */
public class ValidationException extends SdkException {

    private final String fieldName;

    public ValidationException(String message) {
        super(message, "VALIDATION_ERROR");
        this.fieldName = null;
    }

    public ValidationException(String message, String fieldName) {
        super(message, "VALIDATION_ERROR");
        this.fieldName = fieldName;
    }

    public ValidationException(String message, String fieldName, Object details) {
        super(message, "VALIDATION_ERROR", details);
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
