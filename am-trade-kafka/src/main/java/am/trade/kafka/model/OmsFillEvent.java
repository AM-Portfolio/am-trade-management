package am.trade.kafka.model;

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
public class OmsFillEvent {
    private String eventId;
    private String source;
    private String dataVersion;
    private String orderId;
    private String walletId;
    private String ownerId;
    private String action;
    private String symbol;
    private String quantity;
    private String price;
    private String instrumentType;
    private String portfolioKind;
    private String id;
    private String portfolioId;
    private String timestamp;
}
