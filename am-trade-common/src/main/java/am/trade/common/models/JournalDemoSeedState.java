package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * One-shot marker so demo journal entries are not recreated after the user deletes them.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "journal_demo_seed")
public class JournalDemoSeedState {

    /** Same as the authenticated user id. */
    @Id
    private String userId;

    private LocalDateTime seededAt;

    private int entriesCreated;
}
