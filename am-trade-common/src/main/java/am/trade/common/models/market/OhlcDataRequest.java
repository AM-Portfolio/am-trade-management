package am.trade.common.models.market;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class OhlcDataRequest {
    private String symbols;
    private String timeFrame;
    private boolean refresh;
    @JsonProperty("isIndexSymbol")
    private boolean indexSymbol;
}
