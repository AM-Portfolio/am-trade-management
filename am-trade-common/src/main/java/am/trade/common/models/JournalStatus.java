package am.trade.common.models;

/**
 * Lifecycle status of a trade journal entry
 */
public enum JournalStatus {
    DRAFT,
    PLANNED,
    OPEN,
    COMPLETED,
    ARCHIVED,
    MISSED
}
