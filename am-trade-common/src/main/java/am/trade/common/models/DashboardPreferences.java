package am.trade.common.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Domain model for user dashboard preferences
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardPreferences {
    
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
