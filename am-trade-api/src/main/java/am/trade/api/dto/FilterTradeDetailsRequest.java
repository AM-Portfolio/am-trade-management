package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import am.trade.common.models.MetricsFilterConfig;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for filtering trade details using favorite filter configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Request for filtering trade details")
public class FilterTradeDetailsRequest {
    
    @Schema(description = "Favorite filter ID to apply (optional - if provided, merges with this request)")
    private String favoriteFilterId;
    
    @Valid
    @Schema(description = "Metrics filter configuration for filtering trades")
    private MetricsFilterConfig metricsConfig;
}
