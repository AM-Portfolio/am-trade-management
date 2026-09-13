package am.trade.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-row error from journal CSV import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalImportError {

    private int row;
    private String message;
}
