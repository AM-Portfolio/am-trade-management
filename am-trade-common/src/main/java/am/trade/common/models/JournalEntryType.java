package am.trade.common.models;

/**
 * Types of trade journal entries.
 * Legacy values PRE_PLAN, POST_REVIEW, TRADE_NOTE, GENERAL are retained for compatibility.
 */
public enum JournalEntryType {
    PRE_PLAN,
    TRADE_JOURNAL,
    DAILY,
    SESSION,
    MISSED,
    WEEKLY_REVIEW,
    POST_REVIEW,
    TRADE_NOTE,
    GENERAL
}
