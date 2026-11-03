package am.trade.common.models.market;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarketDataResponse {
    private Double lastPrice;
    /** Prior session close — used for 1D P&L when cost basis is unknown. */
    @JsonAlias({"previous_close", "prevClose", "prev_close", "closePrice"})
    private Double previousClose;
    /** Some dig OHLC payloads nest prior/session close here. */
    private Ohlc ohlc;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Ohlc {
        private Double open;
        private Double high;
        private Double low;
        private Double close;
    }

    /** Prefer explicit previousClose; fall back to ohlc.close when it is a distinct prior. */
    public Double resolvePreviousClose() {
        if (previousClose != null && previousClose > 0) {
            return previousClose;
        }
        if (ohlc != null && ohlc.getClose() != null && ohlc.getClose() > 0) {
            if (lastPrice == null || Math.abs(ohlc.getClose() - lastPrice) > 1e-6) {
                return ohlc.getClose();
            }
        }
        return previousClose;
    }
}
