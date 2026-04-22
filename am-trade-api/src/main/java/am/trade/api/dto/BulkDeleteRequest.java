package am.trade.api.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for bulk deletion of favorite filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteRequest {
    
    @NotNull(message = "User ID cannot be null")
    @NotEmpty(message = "User ID cannot be empty")
    private String userId;
    
    @NotNull(message = "Filter IDs list cannot be null")
    @NotEmpty(message = "Filter IDs list cannot be empty")
    private List<String> filterIds;
}
