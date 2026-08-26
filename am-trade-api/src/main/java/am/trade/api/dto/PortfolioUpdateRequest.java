package am.trade.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdateRequest {
    
    @NotBlank(message = "Portfolio name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    private BigDecimal initialCapital;
}
