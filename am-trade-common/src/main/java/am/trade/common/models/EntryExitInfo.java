package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Model representing entry or exit information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EntryExitInfo {
    private LocalDateTime timestamp;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalValue;
    private BigDecimal fees;
    private String reason;
}
