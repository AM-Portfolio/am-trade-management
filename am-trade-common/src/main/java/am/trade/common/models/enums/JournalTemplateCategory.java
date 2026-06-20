package am.trade.common.models.enums;

/**
 * Categories for journal templates
 */
public enum JournalTemplateCategory {
    DAILY_CHECKIN("Daily Check-in"),
    PRE_MARKET("Pre-Market Prep"),
    POST_MARKET("Post-Market Review"),
    TRADE_RECAP("Trade Recap"),
    WEEKLY_REVIEW("Weekly Review"),
    MONTHLY_REVIEW("Monthly Review"),
    QUARTERLY_REVIEW("Quarterly Review"),
    CUSTOM("Custom");

    private final String displayName;

    JournalTemplateCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
