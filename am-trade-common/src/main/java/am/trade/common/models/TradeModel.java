package am.trade.common.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import am.trade.common.models.enums.BrokerType;
import am.trade.common.models.enums.TradeType;
import am.trade.common.models.enums.FNOTradeType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Model class representing a trade based on Zerodha's F&O trade book structure
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeModel {
    private BasicInfo basicInfo;
    private InstrumentInfo instrumentInfo;
    private ExecutionInfo executionInfo;
    private FnOInfo fnoInfo;
    private Charges charges;
    private Financials financials;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicInfo {
        private String tradeId;
        private String orderId;
        private LocalDate tradeDate;
        private LocalDateTime orderExecutionTime;
        private BrokerType brokerType;
        private TradeType tradeType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstrumentInfo {
        private String symbol;
        private String isin;
        private String exchange;
        private String segment;
        private String series;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutionInfo {
        private String auction;
        private Integer quantity;
        private BigDecimal price;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FnOInfo {
        private FNOTradeType instrumentType; // FUTIDX, OPTIDX, FUTEQ, OPTEQ
        private LocalDate expiryDate;
        private BigDecimal strikePrice;
        private String optionType; // CE, PE, null for futures
        private BigDecimal lotSize;
        private BigDecimal premiumValue;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Charges {
        private BigDecimal brokerage;
        private BigDecimal stt;
        private BigDecimal transactionCharges;
        private BigDecimal stampDuty;
        private BigDecimal sebiCharges;
        private BigDecimal gst;
        private BigDecimal totalTaxes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Financials {
        private BigDecimal turnover;
        private BigDecimal netAmount;
    }
}
