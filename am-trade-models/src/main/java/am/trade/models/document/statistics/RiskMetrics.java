package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * Class for risk-related metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RiskMetrics {
    @Field("win_loss_ratio")
    private BigDecimal winLossRatio;
    
    @Field("profit_factor")
    private BigDecimal profitFactor;
    
    @Field("risk_reward_ratio")
    private BigDecimal riskRewardRatio;
    
    @Field("sharpe_ratio")
    private BigDecimal sharpeRatio;
    
    @Field("max_drawdown")
    private BigDecimal maxDrawdown;
    
    @Field("max_drawdown_percentage")
    private BigDecimal maxDrawdownPercentage;
}
