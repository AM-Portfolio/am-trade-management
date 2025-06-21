package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Class for storing trading notes and observations
 * Allows traders to document insights, decisions, and market conditions
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradingNote {
    @Field("note_id")
    private String noteId;
    
    @Field("note_date")
    private LocalDateTime noteDate;
    
    @Field("author_id")
    private String authorId;
    
    @Field("note_title")
    private String noteTitle;
    
    @Field("note_content")
    private String noteContent;
    
    @Field("note_category")
    private String noteCategory;
    
    @Field("note_tags")
    private List<String> noteTags;
    
    @Field("related_trade_ids")
    private List<String> relatedTradeIds;
    
    @Field("importance_level")
    private Integer importanceLevel;
}
