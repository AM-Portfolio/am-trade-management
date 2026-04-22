package am.trade.sdk.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * User-facing DTOs for Portfolio operations.
 */
public class PortfolioDTO {

    /**
     * DTO for creating a portfolio.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioCreateRequest {
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("initial_capital")
        private Double initialCapital;

        public boolean isValid() {
            return name != null && !name.isEmpty()
                    && initialCapital != null && initialCapital > 0;
        }
    }

    /**
     * DTO for updating a portfolio.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioUpdateRequest {
        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;
    }

    /**
     * DTO for portfolio responses.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PortfolioResponse {
        @SerializedName("id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("description")
        private String description;

        @SerializedName("initial_capital")
        private Double initialCapital;

        @SerializedName("current_value")
        private Double currentValue;

        @SerializedName("total_pnl")
        private Double totalPnl;

        @SerializedName("total_pnl_percentage")
        private Double totalPnlPercentage;

        @SerializedName("total_trades")
        private Integer totalTrades;

        @SerializedName("active_trades")
        private Integer activeTrades;
    }
}
