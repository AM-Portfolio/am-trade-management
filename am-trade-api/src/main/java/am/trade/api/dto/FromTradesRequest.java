package am.trade.api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request to create journal entries from existing trades
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FromTradesRequest {

    @NotEmpty(message = "tradeIds are required")
    private List<String> tradeIds;

    @Builder.Default
    private String journalStatus = "COMPLETED";
}
