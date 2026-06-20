package am.trade.sdk.dto;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * User-facing DTOs for Trade operations.
 * These are what SDK users interact with - NOT internal models.
 */
public class TradeDTO {

    /**
     * DTO for creating a trade - User provides this.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeCreateRequest {
        @SerializedName("portfolio_id")
        private String portfolioId;

        @SerializedName("symbol")
        private String symbol;

        @SerializedName("trade_type")
        private String tradeType; // BUY, SELL, SHORT, COVER

        @SerializedName("quantity")
        private Double quantity;

        @SerializedName("entry_price")
        private Double entryPrice;

        @SerializedName("entry_date")
        private LocalDateTime entryDate;

        @SerializedName("notes")
        private String notes;

        // Validation
        public boolean isValid() {
            return portfolioId != null && !portfolioId.isEmpty()
                    && symbol != null && !symbol.isEmpty()
                    && tradeType != null
                    && quantity != null && quantity > 0
                    && entryPrice != null && entryPrice > 0
                    && entryDate != null;
        }
    }

    /**
     * DTO for updating a trade - User provides this.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeUpdateRequest {
        @SerializedName("exit_price")
        private Double exitPrice;

        @SerializedName("exit_date")
        private LocalDateTime exitDate;

        @SerializedName("status")
        private String status;

        @SerializedName("notes")
        private String notes;
    }

    /**
     * DTO for trade responses - Backend returns this.
     * Contains only non-sensitive fields.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeResponse {
        @SerializedName("id")
        private String id;

        @SerializedName("portfolio_id")
        private String portfolioId;

        @SerializedName("symbol")
        private String symbol;

        @SerializedName("trade_type")
        private String tradeType;

        @SerializedName("quantity")
        private Double quantity;

        @SerializedName("entry_price")
        private Double entryPrice;

        @SerializedName("entry_date")
        private LocalDateTime entryDate;

        @SerializedName("exit_price")
        private Double exitPrice;

        @SerializedName("exit_date")
        private LocalDateTime exitDate;

        @SerializedName("status")
        private String status;

        @SerializedName("notes")
        private String notes;

        @SerializedName("pnl")
        private Double pnl;

        @SerializedName("pnl_percentage")
        private Double pnlPercentage;

        // NEVER expose: internal_id, transaction_cost, fee_paid, tax_impact, margin_used
    }

    /**
     * DTO for filtering trades.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TradeFilterRequest {
        @SerializedName("portfolio_id")
        private String portfolioId;

        @SerializedName("symbol")
        private String symbol;

        @SerializedName("status")
        private String status;

        @SerializedName("trade_type")
        private String tradeType;

        @SerializedName("min_pnl")
        private Double minPnl;

        @SerializedName("max_pnl")
        private Double maxPnl;

        @SerializedName("start_date")
        private LocalDateTime startDate;

        @SerializedName("end_date")
        private LocalDateTime endDate;

        @SerializedName("page")
        private Integer page = 0;

        @SerializedName("page_size")
        private Integer pageSize = 20;
    }
}
