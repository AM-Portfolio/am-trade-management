package am.trade.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for trade performance comparisons
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ComparisonResponse {
    
    private String comparisonId;
    private String userId;
    private String comparisonType;
    private List<ComparisonDimension> dimensions;
    private List<ComparisonMetric> metrics;
    private Map<String, Object> additionalData;
    private String generatedAt;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonDimension {
        private String id;
        private String name;
        private String description;
        private String startDate;
        private String endDate;
        private int tradeCount;
        private Map<String, Object> metadata;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComparisonMetric {
        private String name;
        private String displayName;
        private String unit;
        private Map<String, Double> values; // dimension id -> value
        private Map<String, Double> percentChanges; // dimension id -> percent change from baseline
        private String bestDimensionId;
        private String worstDimensionId;
        private Double average;
        private Double median;
    }
}
