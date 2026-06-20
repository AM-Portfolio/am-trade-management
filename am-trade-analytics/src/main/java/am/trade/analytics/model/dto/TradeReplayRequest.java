package am.trade.analytics.model.dto;

import am.trade.models.enums.OrderSide;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for trade replay requests
 * Contains all necessary parameters to initiate a trade replay analysis
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeReplayRequest {
    
    @NotBlank(message = "Symbol is required")
    private String symbol;
    
    @NotNull(message = "Entry date is required")
    private LocalDateTime entryDate;
    
    @NotNull(message = "Exit date is required")
    private LocalDateTime exitDate;
    
    @NotNull(message = "Entry price is required")
    private BigDecimal entryPrice;
    
    @NotNull(message = "Exit price is required")
    private BigDecimal exitPrice;
    
    @NotNull(message = "Order side is required")
    private OrderSide side;
    
    @NotNull(message = "Position size is required")
    @Positive(message = "Position size must be positive")
    private Integer positionSize;
    
    private String originalTradeId;
    
    private String strategyId;
    
    private String portfolioId;
}
