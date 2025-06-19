package am.trade.models.enums;

/**
 * Enum representing the type of a trade order
 */
public enum OrderType {
    MARKET,
    LIMIT,
    STOP,
    STOP_LIMIT,
    TRAILING_STOP,
    FILL_OR_KILL,
    GOOD_TILL_CANCEL,
    GOOD_TILL_DATE,
    AT_THE_OPEN,
    AT_THE_CLOSE
}
