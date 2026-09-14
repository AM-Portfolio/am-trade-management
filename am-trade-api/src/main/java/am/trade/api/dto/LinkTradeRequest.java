package am.trade.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to link a journal entry to an existing trade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkTradeRequest {

    @NotBlank(message = "tradeId is required")
    private String tradeId;
}
