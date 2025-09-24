package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import am.trade.common.models.MetricsFilterConfig;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a favorite filter
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class FavoriteFilterRequest {
    @NotBlank(message = "Filter name is required")
    private String name;
    
    private String description;
    
    @Builder.Default
    private boolean isDefault = false;
    
    @NotNull(message = "Filter configuration is required")
    private MetricsFilterConfig filterConfig;
}
