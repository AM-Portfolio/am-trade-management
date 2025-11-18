package am.trade.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for bulk deletion of favorite filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkDeleteResponse {
    
    private int deletedCount;
    private int totalRequested;
    private String message;
}
