package am.trade.models.shared;

import java.math.BigDecimal;
import java.time.LocalDate;

import am.trade.models.shared.enums.FNOTradeType;
import am.trade.models.shared.enums.OptionType;
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
