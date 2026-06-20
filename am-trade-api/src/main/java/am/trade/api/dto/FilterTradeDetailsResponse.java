package am.trade.api.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import am.trade.common.models.TradeDetails;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for filtered trade details
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Response containing filtered trade details")
public class FilterTradeDetailsResponse {
    
    @Schema(description = "List of filtered trade details")
    private List<TradeDetails> trades;
    
    @Schema(description = "Total number of trades matching the filter")
    private long totalCount;
    
    @Schema(description = "Applied filter name (if using favorite filter)")
    private String appliedFilterName;
    
    @Schema(description = "Filter criteria summary")
    private FilterSummary filterSummary;
    
    @Schema(description = "Current page number (0-based)")
    private Integer page;
    
    @Schema(description = "Page size")
    private Integer size;
    
    @Schema(description = "Total number of pages")
    private Integer totalPages;
    
    @Schema(description = "Whether this is the first page")
    private Boolean isFirst;
    
    @Schema(description = "Whether this is the last page")
    private Boolean isLast;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FilterSummary {
        private List<String> portfolioIds;
        private List<String> symbols;
        private List<String> statuses;
        private String dateRange;
        private List<String> strategies;
        private String profitLossRange;
        private String holdingTimeRange;
    }
}
