package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * Class for portfolio-level trade counts
 * Tracks the number of trades per portfolio
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PortfolioTradeCounts {
    @Field("portfolio_id")
    private String portfolioId;
    
    @Field("portfolio_name")
    private String portfolioName;
    
    @Field("total_trades")
    private Integer totalTrades;
    
    @Field("winning_trades")
    private Integer winningTrades;
    
    @Field("losing_trades")
    private Integer losingTrades;
    
    @Field("break_even_trades")
    private Integer breakEvenTrades;
    
    @Field("total_value")
    private BigDecimal totalValue;
    
    @Field("net_profit_loss")
    private BigDecimal netProfitLoss;
}
