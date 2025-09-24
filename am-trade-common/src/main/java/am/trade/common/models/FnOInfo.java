package am.trade.common.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import am.trade.common.models.enums.FNOTradeType;
import am.trade.common.models.enums.OptionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FnOInfo {
    private FNOTradeType instrumentType; // FUTIDX, OPTIDX, FUTEQ, OPTEQ
    private LocalDate expiryDate;
    private BigDecimal strikePrice;
    private OptionType optionType; // CALL, PUT, NONE for futures
    private BigDecimal lotSize;
    private BigDecimal premiumValue;
}