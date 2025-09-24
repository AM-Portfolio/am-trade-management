package am.trade.exceptions;

import am.trade.exceptions.model.ErrorDetail;
import am.trade.exceptions.model.ErrorResponse;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

/**
 * Base exception class for all trade-related exceptions
 */
@Getter
public class TradeException extends RuntimeException {
    private final HttpStatus status;
    private final List<ErrorDetail> errors = new ArrayList<>();
    private final String traceId;

    /**
     * Create a new TradeException with a single error message
     * @param message Error message
     * @param status HTTP status code
     */
    public TradeException(String message, HttpStatus status) {
        super(message);
        this.status = status;
        this.traceId = generateTraceId();
    }

    /**
     * Create a new TradeException with a message and cause
     * @param message Error message
     * @param cause Root cause exception
     * @param status HTTP status code
     */
    public TradeException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
        this.traceId = generateTraceId();
    }

    /**
     * Add a single error detail to this exception
     * @param field Field with error
     * @param message Error message
     * @param code Error code
     * @return This exception for chaining
     */
    public TradeException addError(String field, String message, String code) {
        this.errors.add(new ErrorDetail(field, message, code));
        return this;
    }

    /**
     * Add an error detail object to this exception
     * @param error Error detail object
     * @return This exception for chaining
     */
    public TradeException addError(ErrorDetail error) {
        this.errors.add(error);
        return this;
    }

    /**
     * Add multiple error details to this exception
     * @param errors List of error details
     * @return This exception for chaining
     */
    public TradeException addErrors(List<ErrorDetail> errors) {
        this.errors.addAll(errors);
        return this;
    }

    /**
     * Convert this exception to an ErrorResponse object
     * @param path Request path
     * @return ErrorResponse object
     */
    public ErrorResponse toErrorResponse(String path) {
        return ErrorResponse.builder()
                .timestamp(java.time.LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(getMessage())
                .path(path)
                .traceId(traceId)
                .errors(new ArrayList<>(errors))
                .build();
    }

    /**
     * Generate a unique trace ID for this exception
     * @return Trace ID string
     */
    private String generateTraceId() {
        return java.util.UUID.randomUUID().toString();
    }
}
