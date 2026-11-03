package am.trade.api.config;

import am.trade.api.dto.TradeJournalEntryRequest;
import am.trade.common.models.BehaviorPatternSummary;
import am.trade.common.models.JournalStatus;
import am.trade.common.models.PostTradeReview;
import am.trade.common.models.PreTradePlan;
import am.trade.common.models.TradeExecution;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Builds educational demo journal entries for first-time users.
 * Content lives here (backend source of truth) — not in the Flutter client.
 */
public final class DemoJournalEntryFactory {

    public static final String CUSTOM_DEMO = "demo";
    public static final String CUSTOM_DEMO_KIND = "demoKind";
    public static final String DEMO_KIND_LOG = "log";
    public static final String DEMO_KIND_TRADE = "trade";

    private DemoJournalEntryFactory() {
    }

    public static TradeJournalEntryRequest logDay(LocalDateTime entryDate) {
        return TradeJournalEntryRequest.builder()
                .title("Sample Log Day — how daily notes work")
                .entryType("DAILY")
                .journalStatus(JournalStatus.COMPLETED.name())
                .entryDate(entryDate)
                .content("""
                        This is a sample Log Day entry so you can see how daily journaling works.

                        Use Log Day for session notes that are not tied to one trade:
                        • How you felt before / after the market
                        • Process wins and slips
                        • Tomorrow’s focus

                        Tip: delete this demo anytime — your real Log Days replace it.
                        """)
                .behaviorPatternSummaries(List.of(
                        BehaviorPatternSummary.builder()
                                .summary("Demo session: stayed flat after lunch, sized down on uncertain setups.")
                                .mood("FOCUSED")
                                .marketSentiment(6)
                                .tags(List.of("demo", "process", "sizing"))
                                .build()))
                .customFields(Map.of(
                        CUSTOM_DEMO, true,
                        CUSTOM_DEMO_KIND, DEMO_KIND_LOG))
                .build();
    }

    public static TradeJournalEntryRequest tradeJournal(LocalDateTime entryDate) {
        LocalDateTime entryDt = entryDate.withHour(10).withMinute(15).withSecond(0).withNano(0);
        LocalDateTime exitDt = entryDate.withHour(11).withMinute(5).withSecond(0).withNano(0);

        PreTradePlan pre = PreTradePlan.builder()
                .setupDescription("Opening-range breakout above prior day high with volume confirmation.")
                .entryRationale("Demo: clear level, defined risk under OR low, 2R target.")
                .marketContext("Index opening firm; sector leaders already in trend.")
                .strategy("Breakout")
                .setup("ORB")
                .plannedEntryPrice(100.0)
                .plannedStopLoss(98.5)
                .plannedTarget(103.0)
                .plannedQuantity(50.0)
                .plannedRiskAmount(75.0)
                .plannedRiskPercent(0.5)
                .plannedRRRatio(2.0)
                .setupChecklist(List.of(
                        "Level identified on daily/15m",
                        "Risk sized before entry",
                        "Invalidation written down"))
                .confirmedChecklistItems(List.of(
                        "Level identified on daily/15m",
                        "Risk sized before entry",
                        "Invalidation written down"))
                .build();

        TradeExecution execution = TradeExecution.builder()
                .entryDateTime(entryDt)
                .actualEntryPrice(100.2)
                .quantity(50.0)
                .orderType("MARKET")
                .notes("Demo fill — slightly worse than plan (slippage).")
                .build();

        PostTradeReview post = PostTradeReview.builder()
                .exitDateTime(exitDt)
                .actualExitPrice(102.8)
                .actualPnl(130.0)
                .actualRMultiple(1.73)
                .followedStopLoss("YES")
                .followedTarget("PARTIAL")
                .tradeOutcome("WIN")
                .whatWentWell("Waited for volume; cut size when first push stalled.")
                .whatCouldBeImproved("Took profit early — could have trailed to full 2R.")
                .lessonLearned("Demo lesson: write the trail rule in Pre so Execute stays mechanical.")
                .mistakeCategory("EARLY_EXIT")
                .executionScore(8)
                .emotionalState("CALM")
                .postChecklist(List.of(
                        "Logged actual vs plan",
                        "Tagged mistake category",
                        "Wrote one reusable lesson"))
                .completedChecklistItems(List.of(
                        "Logged actual vs plan",
                        "Tagged mistake category",
                        "Wrote one reusable lesson"))
                .build();

        return TradeJournalEntryRequest.builder()
                .title("Sample Trade Journal — Pre → Execute → Post")
                .entryType("TRADE_JOURNAL")
                .journalStatus(JournalStatus.COMPLETED.name())
                .entryDate(entryDate)
                .symbol("DEMO")
                .setup("ORB")
                .tradeDirection("LONG")
                .content("""
                        This is a completed sample Trade Journal so you can explore the
                        Pre-Trade → Execute → Post-Trade workflow without a live position.

                        Open it to see how each step captures plan, fill, and review.
                        Delete this demo when you start journaling real trades.
                        """)
                .preTradePlan(pre)
                .tradeExecution(execution)
                .postTradeReview(post)
                .planAdherenceScore(85.0)
                .checklistCompletionPct(100.0)
                .customFields(Map.of(
                        CUSTOM_DEMO, true,
                        CUSTOM_DEMO_KIND, DEMO_KIND_TRADE))
                .build();
    }
}
