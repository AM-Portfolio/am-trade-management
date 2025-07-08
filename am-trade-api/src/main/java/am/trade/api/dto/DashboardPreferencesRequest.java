package am.trade.api.dto;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for user dashboard preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DashboardPreferencesRequest {
    
    @NotNull(message = "Default metric types must be specified")
    private List<String> defaultMetricTypes;
    
    private List<String> favoritePortfolioIds;
    
    private String defaultTimeRange; // DAILY, WEEKLY, MONTHLY, QUARTERLY, YEARLY, CUSTOM
    
    private Integer customDaysRange; // Used when defaultTimeRange is CUSTOM
    
    @Builder.Default
    private boolean showProfitLossChart = true;
    
    @Builder.Default
    private boolean showWinRateChart = true;
    
    @Builder.Default
    private boolean showTradeFrequencyChart = true;
    
    @Builder.Default
    private boolean showInstrumentDistributionChart = true;
    
    private WidgetLayout widgetLayout;
    
    private Map<String, Boolean> additionalCharts;
    
    /**
     * Widget layout configuration for dashboard
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WidgetLayout {
        private List<Widget> widgets;
        
        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Widget {
            private String id;
            private String type;
            private int row;
            private int col;
            private int sizeX;
            private int sizeY;
            private Map<String, Object> config;
        }
    }
}
