package am.trade.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Result of journal CSV import.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalImportResponse {

    private int created;

    @Builder.Default
    private List<JournalImportError> errors = new ArrayList<>();
}
