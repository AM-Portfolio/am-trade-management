package am.trade.common.util;

import am.trade.common.models.TradeDetails;

import java.time.Duration;

/**
 * Shared hold-duration style buckets for Timing Analysis filter and style hint.
 * SCALPER: ≥0 and &lt;15 minutes; INTRADAY: ≥15m and &lt;24h; SWING: ≥24h.
 */
public final class HoldingStyleClassifier {

    public static final long SCALPER_MAX_MINUTES_EXCLUSIVE = 15;
    public static final long INTRADAY_MAX_MINUTES_EXCLUSIVE = 24 * 60;

    private HoldingStyleClassifier() {}

    /**
     * @return hold duration, or null if entry/exit timestamps missing
     */
    public static Duration holdDurationOrNull(TradeDetails trade) {
        if (trade == null
                || trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null
                || trade.getExitInfo() == null || trade.getExitInfo().getTimestamp() == null) {
            return null;
        }
        return Duration.between(
                trade.getEntryInfo().getTimestamp(),
                trade.getExitInfo().getTimestamp());
    }

    /**
     * Style bucket for a single trade, or null when hold cannot be classified
     * (missing timestamps or negative duration).
     */
    public static String classifyHold(TradeDetails trade) {
        Duration hold = holdDurationOrNull(trade);
        if (hold == null || hold.isNegative()) {
            return null;
        }
        long minutes = hold.toMinutes();
        if (minutes < SCALPER_MAX_MINUTES_EXCLUSIVE) {
            return "SCALPER";
        }
        if (minutes < INTRADAY_MAX_MINUTES_EXCLUSIVE) {
            return "INTRADAY";
        }
        return "SWING";
    }

    /**
     * Whether the trade matches a filter style (SCALPER / INTRADAY / SWING).
     * Unknown style strings match all classifiable trades (return true when hold is valid).
     */
    public static boolean matchesStyle(TradeDetails trade, String style) {
        if (style == null || style.isBlank()) {
            return true;
        }
        String classified = classifyHold(trade);
        if (classified == null) {
            return false;
        }
        String normalized = style.trim().toUpperCase();
        return switch (normalized) {
            case "SCALPER", "INTRADAY", "SWING" -> classified.equals(normalized);
            default -> true;
        };
    }
}
