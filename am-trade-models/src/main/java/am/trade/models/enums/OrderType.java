package am.trade.models.enums;

/**
 * Enum representing the type of a trade order
 */
public enum OrderType {
    MARKET,
    LIMIT,
    STOP,
    STOP_LIMIT,
    STOP_LOSS,
    STOP_LOSS_MARKET,
    TRAILING_STOP,
    FILL_OR_KILL,
    GOOD_TILL_CANCEL,
    GOOD_TILL_DATE,
    AT_THE_OPEN,
    AT_THE_CLOSE;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static OrderType fromString(String value) {
        if (value == null) return null;
        String normalized = value.toUpperCase().replace("_", "");
        for (OrderType type : OrderType.values()) {
            if (type.name().replace("_", "").equals(normalized)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown order type: " + value);
    }
}
