package am.trade.common.models;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Strongly typed DTO for tracking measurable trading goals within a notebook
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Strongly typed details for a measurable trading goal")
public class NotebookGoalDetails {

    @Schema(description = "The target date to achieve this goal", example = "2024-12-31T23:59:59Z")
    private LocalDateTime targetDate;

    @Schema(description = "The mathematical target value to hit (e.g. 50000 for a profit goal)", example = "50000.00")
    private BigDecimal targetValue;

    @Schema(description = "The current value towards the goal", example = "12500.50")
    private BigDecimal currentValue;

    @Schema(description = "Currency of the goal if applicable (ISO 4217 code)", example = "USD")
    private String currency;

    @Schema(description = "Status of the goal (e.g., IN_PROGRESS, ACHIEVED, FAILED)", example = "IN_PROGRESS")
    private String status;
}
