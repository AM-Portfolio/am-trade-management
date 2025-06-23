package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * Class for trade count statistics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeCounts {
    @Field("total")
    private Integer total;
    
    @Field("winning")
    private Integer winning;
    
    @Field("losing")
    private Integer losing;
    
    @Field("break_even")
    private Integer breakEven;
}
