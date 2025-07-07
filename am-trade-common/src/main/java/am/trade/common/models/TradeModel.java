package am.trade.common.models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import am.trade.common.models.enums.BrokerType;
import am.trade.common.models.enums.TradeType;
import am.trade.common.util.TradeModelDeserializer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

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
@JsonDeserialize(using = TradeModelDeserializer.class)
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
