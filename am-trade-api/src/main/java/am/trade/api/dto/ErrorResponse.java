package am.trade.api.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error response for API errors
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ErrorResponse {
    private String status;
    private int code;
    private String message;
    private LocalDateTime timestamp;
    
    @Builder.Default
    private List<String> details = new ArrayList<>();
    
    private String path;
    
    /**
     * Create a standard bad request error response
     * 
     * @param message The error message
     * @param path The API path that generated the error
     * @return ErrorResponse object
     */
    public static ErrorResponse badRequest(String message, String path) {
        return ErrorResponse.builder()
                .status("BAD_REQUEST")
                .code(400)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
    
    /**
     * Create a standard not found error response
     * 
     * @param message The error message
     * @param path The API path that generated the error
     * @return ErrorResponse object
     */
    public static ErrorResponse notFound(String message, String path) {
        return ErrorResponse.builder()
                .status("NOT_FOUND")
                .code(404)
                .message(message)
                .timestamp(LocalDateTime.now())
                .path(path)
                .build();
    }
    
    /**
     * Add a detail to the error response
     * 
     * @param detail The detail to add
     * @return This error response for chaining
     */
    public ErrorResponse addDetail(String detail) {
        this.details.add(detail);
        return this;
    }
}
