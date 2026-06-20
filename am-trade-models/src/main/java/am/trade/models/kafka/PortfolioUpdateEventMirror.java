package am.trade.models.kafka;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Mirror of Portfolio's {@code PortfolioUpdateEvent}.
 * <p>
 * Trade consumes this event from topic {@code am-portfolio-update} to stay
 * informed about recalculated portfolio data (holdings, P&L, summary).
 * <p>
 * <b>Why a mirror class?</b> In a microservices architecture, each service owns
 * its own models. Trade must never import Portfolio's classes directly — that
 * would create a compile-time dependency between two independent deployable
 * services. Instead, we define a local class with <em>identical JSON field
 * names</em>. When Kafka serializes/deserializes, the JSON matches perfectly.
 * <p>
 * <b>@JsonIgnoreProperties(ignoreUnknown = true)</b> is critical: if Portfolio
 * adds a new field in the future, Trade won't crash — it will simply ignore the
 * unknown field. This is a key resilience pattern for event-driven systems.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortfolioUpdateEventMirror {

    private UUID id;
    private String userId;
    private String portfolioId;

    // ─── Holdings Data ───────────────────────────────────────────────────
    /**
     * Each equity mirrors Portfolio's {@code EquityModel}. We use a generic
     * inner class instead of importing Portfolio's EquityModel to avoid
     * cross-service compile dependencies.
     */
    private List<EquitySnapshot> equities;

    // ─── Summary / Calculation Data ──────────────────────────────────────
    private Double totalValue;
    private Double totalInvestment;
    private Double totalGainLoss;
    private Double totalGainLossPercentage;
    private Double todayGainLoss;
    private Double todayGainLossPercentage;

    private LocalDateTime timestamp;

    // ─── Inner class for individual equity data ──────────────────────────
    /**
     * A lightweight snapshot of a single equity holding as published by
     * Portfolio. Only the fields Trade cares about are included; unknown
     * fields are safely ignored via {@code @JsonIgnoreProperties}.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EquitySnapshot {
        private String symbol;
        private String name;
        private String isin;
        private Double quantity;
        private Double avgBuyingPrice;
        private Double currentPrice;
        private Double currentValue;
        private Double investmentValue;
        private Double profitLoss;
        private Double profitLossPercentage;
        private Double todayProfitLoss;
        private Double todayProfitLossPercentage;
    }
}
