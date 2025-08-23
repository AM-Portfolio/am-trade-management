package am.trade.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Filter criteria for date ranges
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateRangeFilter {

    @Schema(description = "Start date for the metrics calculation period", required = true)
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @Schema(description = "End date for the metrics calculation period", required = true)
    @NotNull(message = "End date is required")
    private LocalDate endDate;
}
