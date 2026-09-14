package am.trade.common.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Actual trade execution details captured in a journal entry
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TradeExecution {

    private LocalDateTime entryDateTime;
    private Double actualEntryPrice;
    private Double quantity;
    private String broker;
    private String orderType;
    private String externalOrderId;
    private String notes;
}
