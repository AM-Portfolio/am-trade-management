package am.trade.common.models.enums;

/**
 * Enum representing the status of a trade
 */
public enum TradeStatus {
    WIN,    // Trade resulted in profit
    LOSS,   // Trade resulted in loss
    OPEN,   // Position is still open
    BREAK_EVEN  // Trade resulted in no profit or loss
}
