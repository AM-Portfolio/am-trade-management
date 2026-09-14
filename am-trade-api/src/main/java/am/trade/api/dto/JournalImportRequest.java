package am.trade.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * CSV import request for journal stubs from trade IDs.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalImportRequest {

    /** Raw CSV text with a header row including tradeId (symbol optional). */
    private String csv;

    /** When true (default), create journal stubs for each tradeId row. */
    @Builder.Default
    private Boolean createJournalStubs = true;
}
