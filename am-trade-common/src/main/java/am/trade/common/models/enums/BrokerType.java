package am.trade.common.models.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import com.fasterxml.jackson.annotation.JsonCreator;

@Schema(description = "Broker platform used for executing trades")
public enum BrokerType {
    @Schema(description = "Dhan trading platform")
    DHAN("Dhan"),
    
    @Schema(description = "Zerodha trading platform")
    ZERODHA("Zerodha"),
    
    @Schema(description = "Mirae Asset MStock trading platform")
    MSTOCK("MStock"),
    
    @Schema(description = "Grow trading platform")
    GROW("Grow"),
    
    @Schema(description = "Kotak Securities trading platform")
    KOTAK("Kotak"),
    
    @Schema(description = "Angel One trading platform")
    ANGEL_ONE("Angel One"),
    
    @Schema(description = "Manually entered trade")
    MANUAL("Manual"),
    
    @Schema(description = "Unknown or unsupported broker")
    OTHER("Other");
 
    private String brokerName;
 
    private BrokerType(String brokerName) {
       this.brokerName = brokerName;
    }
 
    /**
     * Tolerant Reader: Parse incoming broker strings.
     * If the string does not match any known enum, fallback to OTHER instead of crashing.
     */
    @JsonCreator
    public static BrokerType fromString(String value) {
        if (value == null) {
            return OTHER;
        }
        try {
            return BrokerType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }

    public static BrokerType fromCode(String code) {
       BrokerType[] var1 = values();
       int var2 = var1.length;
 
       for(int var3 = 0; var3 < var2; ++var3) {
          BrokerType type = var1[var3];
          if (type.getCode().equals(code)) {
             return type;
          }
       }
 
       return null;
    }
 
    public String getCode() {
       return this.brokerName;
    }
 
    public boolean isDhan() {
       return "Dhan".equals(this.brokerName);
    }
 
    public boolean isZerodha() {
       return "Zerodha".equals(this.brokerName);
    }
 
    public boolean isMStock() {
       return "MStock".equals(this.brokerName);
    }
 
    public boolean isGrow() {
       return "Grow".equals(this.brokerName);
    }
 
    public boolean isKotak() {
       return "Kotak".equals(this.brokerName);
    }

    public boolean isAngelOne() {
       return "Angel One".equals(this.brokerName);
    }

    public boolean isManual() {
       return "Manual".equals(this.brokerName);
    }
 }
 