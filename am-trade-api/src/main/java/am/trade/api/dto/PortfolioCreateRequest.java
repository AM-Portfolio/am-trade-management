package am.trade.api.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioCreateRequest {
    
    @NotBlank(message = "Portfolio name is required")
    private String name;
    
    private String description;
    
    @NotBlank(message = "Currency is required")
    private String currency;
    
    @NotNull(message = "Initial capital is required")
    private BigDecimal initialCapital;
}
