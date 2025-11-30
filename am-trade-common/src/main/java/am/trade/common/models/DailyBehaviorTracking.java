package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB document for daily behavior tracking
 */
@Document(collection = "daily_behavior_tracking")
@CompoundIndex(name = "idx_user_date", def = "{'user_id': 1, 'date': 1}", unique = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DailyBehaviorTracking {
    
    @Id
    private String id;
    
    @Field("user_id")
    private String userId;
    
    @Field("date")
    private LocalDate date;
    
    @Field("behavior_pattern_summary")
    private BehaviorPatternSummary behaviorPatternSummary;
    
    @Field("trade_count")
    private Integer tradeCount;
    
    @Field("winning_trades")
    private Integer winningTrades;
    
    @Field("losing_trades")
    private Integer losingTrades;
    
    @Field("total_pnl")
    private Double totalPnl;
    
    @Field("notes")
    private String notes;
    
    @Field("created_at")
    private LocalDateTime createdAt;
    
    @Field("updated_at")
    private LocalDateTime updatedAt;
}
