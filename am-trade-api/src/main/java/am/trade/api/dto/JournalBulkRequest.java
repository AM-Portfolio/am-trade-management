package am.trade.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Bulk operations request for journal entries
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalBulkRequest {

    @NotEmpty(message = "entryIds are required")
    private List<String> entryIds;

    /** Optional — used by bulkAddTags */
    private List<String> tagIds;
}
