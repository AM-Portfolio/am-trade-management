package am.trade.common.models;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request model for profit/loss heatmap data filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeatmapRequest {
    
    private List<String> portfolioIds;
    
    private ProfitLossHeatmapData.GranularityType granularity;
    
    private Integer financialYear;
    private Integer month;
    
    private boolean includeTradeDetails;
}
