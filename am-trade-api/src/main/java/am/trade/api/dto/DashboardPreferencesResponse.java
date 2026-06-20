package am.trade.api.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for user dashboard preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class DashboardPreferencesResponse {
    
    private String id;
    private String userId;
    private List<String> defaultMetricTypes;
    private List<String> favoritePortfolioIds;
    private String defaultTimeRange;
    private Integer customDaysRange;
    private boolean showProfitLossChart;
    private boolean showWinRateChart;
    private boolean showTradeFrequencyChart;
    private boolean showInstrumentDistributionChart;
    private WidgetLayout widgetLayout;
    private Map<String, Boolean> additionalCharts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
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
