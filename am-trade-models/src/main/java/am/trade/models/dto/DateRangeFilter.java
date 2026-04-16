package am.trade.models.dto;

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

    private LocalDate startDate;
    
    private LocalDate endDate;
}
