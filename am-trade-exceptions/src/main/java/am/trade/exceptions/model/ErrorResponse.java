package am.trade.exceptions.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standard error response model that will be returned to API clients
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    private int status;
    private String error;
    private String message;
    private String path;
    private String traceId;
    
    @Builder.Default
    private List<ErrorDetail> errors = new ArrayList<>();
    
    /**
     * Add a single error detail to the error list
     * @param field Field with error
     * @param message Error message
     * @param code Error code
     * @return This ErrorResponse instance for chaining
     */
    public ErrorResponse addError(String field, String message, String code) {
        this.errors.add(new ErrorDetail(field, message, code));
        return this;
    }
    
    /**
     * Add a single error detail to the error list
     * @param errorDetail Error detail object
     * @return This ErrorResponse instance for chaining
     */
    public ErrorResponse addError(ErrorDetail errorDetail) {
        this.errors.add(errorDetail);
        return this;
    }
    
    /**
     * Add multiple error details to the error list
     * @param errorDetails List of error details
     * @return This ErrorResponse instance for chaining
     */
    public ErrorResponse addErrors(List<ErrorDetail> errorDetails) {
        this.errors.addAll(errorDetails);
        return this;
    }
}
