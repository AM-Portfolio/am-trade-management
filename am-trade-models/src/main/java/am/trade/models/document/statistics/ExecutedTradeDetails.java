package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Class for tracking individual trade executions
 * Contains details about each execution within a trade
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExecutedTradeDetails {
    @Field("execution_id")
    private String executionId;
    
    @Field("trade_id")
    private String tradeId;
    
    @Field("execution_time")
    private LocalDateTime executionTime;
    
    @Field("execution_price")
    private BigDecimal executionPrice;
    
    @Field("execution_quantity")
    private Integer executionQuantity;
    
    @Field("execution_venue")
    private String executionVenue;
    
    @Field("execution_fee")
    private BigDecimal executionFee;
    
    @Field("execution_status")
    private String executionStatus;
    
    @Field("execution_notes")
    private String executionNotes;
}
