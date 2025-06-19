package am.trade.models.enums;

/**
 * Enum representing the status of a trade order
 */
public enum OrderStatus {
    PENDING,
    OPEN,
    PARTIALLY_FILLED,
    FILLED,
    CANCELLED,
    REJECTED,
    EXPIRED,
    SETTLED
}
