package am.trade.models.enums;

public enum BrokerType {
    DHAN("Dhan"),
    ZERODHA("Zerodha"),
    MSTOCK("MStock"),
    GROW("Grow"),
    KOTAK("Kotak"),
    ANGEL_ONE("Angel One"),
    UPSTOX("Upstox"),
    ICICI("ICICI Direct"),
    HDFC("HDFC Securities"),
    MANUAL("Manual"),
    OTHER("Other");
 
    private String brokerName;
 
    private BrokerType(String brokerName) {
       this.brokerName = brokerName;
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

    public boolean isUpstox() {
       return "Upstox".equals(this.brokerName);
    }

    public boolean isIcici() {
       return "ICICI Direct".equals(this.brokerName);
    }

    public boolean isHdfc() {
       return "HDFC Securities".equals(this.brokerName);
    }

    public boolean isManual() {
       return "Manual".equals(this.brokerName);
    }
}
