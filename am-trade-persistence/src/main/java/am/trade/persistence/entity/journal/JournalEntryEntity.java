package am.trade.persistence.entity.journal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import am.trade.common.models.journal.BehaviorPatternSummary;
import am.trade.common.models.journal.JournalAttachment;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "trade_journal_entries")
public class JournalEntryEntity {
    
    @Id
    private String id;
    
    @Indexed
    private String userId;
    
    @Indexed
    private String tradeId;
    
    private String title;
    private String content;
    
    private List<BehaviorPatternSummary> behaviorPatternSummaries;
    private Map<String, Object> customFields;
    
    @Indexed
    private String entryDate;
    
    private List<String> imageUrls;
    private List<JournalAttachment> attachments;
    
    private List<String> relatedTradeIds;
    private List<String> tagIds;
    
    private String createdAt;
    private String updatedAt;
}
