package am.trade.models.document.statistics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

/**
 * Class for additional metrics that provide deeper insights into trading performance
 * and portfolio health beyond standard metrics
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalMetrics {
    @Field("expectancy")
    private BigDecimal expectancy;
    
    @Field("system_quality_number")
    private BigDecimal systemQualityNumber;
    
    @Field("average_roe")
    private BigDecimal averageRoe;
    
    @Field("average_winning_roe")
    private BigDecimal averageWinningRoe;
    
    @Field("average_losing_roe")
    private BigDecimal averageLosingRoe;
    
    @Field("max_consecutive_wins")
    private Integer maxConsecutiveWins;
    
    @Field("max_consecutive_losses")
    private Integer maxConsecutiveLosses;
    
    @Field("current_consecutive_wins")
    private Integer currentConsecutiveWins;
    
    @Field("current_consecutive_losses")
    private Integer currentConsecutiveLosses;
    
    @Field("profit_loss_ratio")
    private BigDecimal profitLossRatio;
    
    @Field("calmar_ratio")
    private BigDecimal calmarRatio;
    
    @Field("sortino_ratio")
    private BigDecimal sortinoRatio;
    
    @Field("recovery_factor")
    private BigDecimal recoveryFactor;
    
    @Field("profit_factor")
    private BigDecimal profitFactor;
    
    @Field("kelly_criterion")
    private BigDecimal kellyCriterion;
    
    @Field("ulcer_index")
    private BigDecimal ulcerIndex;
    
    @Field("max_adverse_excursion")
    private BigDecimal maxAdverseExcursion;
    
    @Field("max_favorable_excursion")
    private BigDecimal maxFavorableExcursion;
    
    @Field("trading_frequency_score")
    private BigDecimal tradingFrequencyScore;
    
    @Field("consistency_score")
    private BigDecimal consistencyScore;
}
