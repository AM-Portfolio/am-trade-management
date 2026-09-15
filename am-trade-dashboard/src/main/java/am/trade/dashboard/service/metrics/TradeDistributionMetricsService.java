package am.trade.dashboard.service.metrics;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeDistributionMetrics;
import am.trade.common.models.TradingStyleHint;
import am.trade.models.enums.AssetClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.math.RoundingMode.HALF_UP;

/**
 * Service for calculating trade distribution metrics from trade data.
 * Timing session buckets use NSE cash windows (entry time as stored).
 */
@Service
@Slf4j
public class TradeDistributionMetricsService {

    private static final java.math.RoundingMode ROUNDING_MODE = HALF_UP;
    private static final int STYLE_MIN_SAMPLE = 10;
    private static final double STYLE_DOMINANCE = 0.55;

    public static final String SESSION_0915_1100 = "SESSION_0915_1100";
    public static final String SESSION_1100_1300 = "SESSION_1100_1300";
    public static final String SESSION_1300_1500 = "SESSION_1300_1500";
    public static final String SESSION_1500_1530 = "SESSION_1500_1530";
    public static final String SESSION_OTHER = "OTHER";

    /** Stable display order for Timing session charts. */
    public static final List<String> SESSION_KEYS = List.of(
            SESSION_0915_1100,
            SESSION_1100_1300,
            SESSION_1300_1500,
            SESSION_1500_1530,
            SESSION_OTHER
    );

    /**
     * Half-open [start, end) minutes-of-day for NSE session entry windows.
     */
    public static String resolveSessionKey(LocalDateTime timestamp) {
        if (timestamp == null) {
            return SESSION_OTHER;
        }
        int m = timestamp.getHour() * 60 + timestamp.getMinute();
        if (m >= 9 * 60 + 15 && m < 11 * 60) {
            return SESSION_0915_1100;
        }
        if (m >= 11 * 60 && m < 13 * 60) {
            return SESSION_1100_1300;
        }
        if (m >= 13 * 60 && m < 15 * 60) {
            return SESSION_1300_1500;
        }
        if (m >= 15 * 60 && m < 15 * 60 + 30) {
            return SESSION_1500_1530;
        }
        return SESSION_OTHER;
    }

    public TradeDistributionMetrics calculateMetrics(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return emptyMetricsSkeleton();
        }

        TradeDistributionMetrics metrics = new TradeDistributionMetrics();

        Map<String, List<TradeDetails>> tradesByDay = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByHour = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByMonth = new HashMap<>();
        Map<String, List<TradeDetails>> tradesBySession = emptySessionBuckets();
        Map<String, List<TradeDetails>> tradesByAssetClass = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByStrategy = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByDuration = new HashMap<>();
        Map<String, List<TradeDetails>> tradesByPositionSize = new HashMap<>();

        int skippedMissingEntry = 0;
        int openOrMissingPnl = 0;
        int badTimestamp = 0;

        for (TradeDetails trade : trades) {
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null) {
                openOrMissingPnl++;
            }

            if (trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null) {
                skippedMissingEntry++;
                continue;
            }

            LocalDateTime entryTs = trade.getEntryInfo().getTimestamp();

            String dayOfWeek = entryTs.getDayOfWeek().toString();
            tradesByDay.computeIfAbsent(dayOfWeek, k -> new ArrayList<>()).add(trade);

            String hour = String.valueOf(entryTs.getHour());
            tradesByHour.computeIfAbsent(hour, k -> new ArrayList<>()).add(trade);

            String month = entryTs.getMonth().toString();
            tradesByMonth.computeIfAbsent(month, k -> new ArrayList<>()).add(trade);

            String session = resolveSessionKey(entryTs);
            tradesBySession.get(session).add(trade);

            if (trade.getInstrumentInfo() != null && trade.getInstrumentInfo().getSegment() != null) {
                String assetClass = trade.getInstrumentInfo().getSegment().toString();
                tradesByAssetClass.computeIfAbsent(assetClass, k -> new ArrayList<>()).add(trade);
            }

            String strategy = trade.getStrategy() != null ? trade.getStrategy() : "UNKNOWN";
            tradesByStrategy.computeIfAbsent(strategy, k -> new ArrayList<>()).add(trade);

            String durationCategory = calculateDurationCategory(trade);
            tradesByDuration.computeIfAbsent(durationCategory, k -> new ArrayList<>()).add(trade);

            String positionSizeCategory = calculatePositionSizeCategory(trade);
            tradesByPositionSize.computeIfAbsent(positionSizeCategory, k -> new ArrayList<>()).add(trade);

            if (hasBadHoldTimestamps(trade)) {
                badTimestamp++;
            }
        }

        applyDayMetrics(metrics, tradesByDay);
        applyHourMetrics(metrics, tradesByHour);
        applyMonthMetrics(metrics, tradesByMonth);
        applySessionMetrics(metrics, tradesBySession);

        Map<String, BigDecimal> profitByAssetClass = calculateProfitByCategory(tradesByAssetClass);
        Map<String, BigDecimal> winRateByAssetClass = calculateWinRateByCategory(tradesByAssetClass);

        Map<AssetClass, Integer> tradeCountByAssetClass = new HashMap<>();
        Map<AssetClass, BigDecimal> profitByAssetClassEnum = new HashMap<>();
        Map<AssetClass, BigDecimal> winRateByAssetClassEnum = new HashMap<>();

        tradesByAssetClass.forEach((key, value) -> {
            try {
                AssetClass assetClass = AssetClass.fromCode(key, null);
                tradeCountByAssetClass.put(assetClass, value.size());
                profitByAssetClassEnum.put(assetClass, profitByAssetClass.getOrDefault(key, BigDecimal.ZERO));
                winRateByAssetClassEnum.put(assetClass, winRateByAssetClass.getOrDefault(key, BigDecimal.ZERO));
            } catch (IllegalArgumentException e) {
                log.warn("Could not convert {} to AssetClass enum", key);
            }
        });

        metrics.setTradeCountByAssetClass(tradeCountByAssetClass);
        metrics.setProfitByAssetClass(profitByAssetClassEnum);
        metrics.setWinRateByAssetClass(winRateByAssetClassEnum);

        metrics.setTradeCountByStrategy(convertToTradeCount(tradesByStrategy));
        metrics.setProfitByStrategy(calculateProfitByCategory(tradesByStrategy));
        metrics.setWinRateByStrategy(calculateWinRateByCategory(tradesByStrategy));

        metrics.setTradesByDuration(convertToTradeCount(tradesByDuration));
        metrics.setProfitByDuration(calculateProfitByCategory(tradesByDuration));
        metrics.setWinRateByDuration(calculateWinRateByCategory(tradesByDuration));

        metrics.setTradesByPositionSize(convertToTradeCount(tradesByPositionSize));
        metrics.setProfitByPositionSize(calculateProfitByCategory(tradesByPositionSize));
        metrics.setWinRateByPositionSize(calculateWinRateByCategory(tradesByPositionSize));

        metrics.setSkippedMissingEntryCount(skippedMissingEntry);
        metrics.setOpenOrMissingPnlCount(openOrMissingPnl);
        metrics.setBadTimestampCount(badTimestamp);
        metrics.setTimezoneNote("entry_local_as_stored");
        metrics.setTradingStyleHint(inferTradingStyleHint(trades));

        return metrics;
    }

    private void applyDayMetrics(
            TradeDistributionMetrics metrics,
            Map<String, List<TradeDetails>> tradesByDay) {
        metrics.setTradesByDay(convertToTradeCount(tradesByDay));
        metrics.setProfitByDay(calculateProfitByCategory(tradesByDay));
        metrics.setWinRateByDay(calculateWinRateByCategory(tradesByDay));
        metrics.setAvgPnlByDay(calculateAvgPnlByCategory(tradesByDay));
        metrics.setEligibleTradesByDay(calculateEligibleCountByCategory(tradesByDay));
        metrics.setAvgHoldMinutesByDay(calculateAvgHoldMinutesByCategory(tradesByDay));
        metrics.setRiskRewardByDay(calculateRiskRewardByCategory(tradesByDay));
    }

    private void applyHourMetrics(
            TradeDistributionMetrics metrics,
            Map<String, List<TradeDetails>> tradesByHour) {
        metrics.setTradesByHour(convertToTradeCount(tradesByHour));
        metrics.setProfitByHour(calculateProfitByCategory(tradesByHour));
        metrics.setWinRateByHour(calculateWinRateByCategory(tradesByHour));
        metrics.setAvgPnlByHour(calculateAvgPnlByCategory(tradesByHour));
        metrics.setEligibleTradesByHour(calculateEligibleCountByCategory(tradesByHour));
        metrics.setAvgHoldMinutesByHour(calculateAvgHoldMinutesByCategory(tradesByHour));
        metrics.setRiskRewardByHour(calculateRiskRewardByCategory(tradesByHour));
    }

    private void applyMonthMetrics(
            TradeDistributionMetrics metrics,
            Map<String, List<TradeDetails>> tradesByMonth) {
        metrics.setTradesByMonth(convertToTradeCount(tradesByMonth));
        metrics.setProfitByMonth(calculateProfitByCategory(tradesByMonth));
        metrics.setWinRateByMonth(calculateWinRateByCategory(tradesByMonth));
        metrics.setAvgPnlByMonth(calculateAvgPnlByCategory(tradesByMonth));
        metrics.setEligibleTradesByMonth(calculateEligibleCountByCategory(tradesByMonth));
        metrics.setAvgHoldMinutesByMonth(calculateAvgHoldMinutesByCategory(tradesByMonth));
        metrics.setRiskRewardByMonth(calculateRiskRewardByCategory(tradesByMonth));
    }

    private void applySessionMetrics(
            TradeDistributionMetrics metrics,
            Map<String, List<TradeDetails>> tradesBySession) {
        metrics.setTradesBySession(convertToTradeCountPreservingKeys(tradesBySession));
        metrics.setProfitBySession(calculateProfitByCategoryPreservingKeys(tradesBySession));
        metrics.setWinRateBySession(calculateWinRateByCategoryPreservingKeys(tradesBySession));
        metrics.setAvgPnlBySession(calculateAvgPnlByCategoryPreservingKeys(tradesBySession));
        metrics.setEligibleTradesBySession(calculateEligibleCountByCategoryPreservingKeys(tradesBySession));
        metrics.setAvgHoldMinutesBySession(
                calculateAvgHoldMinutesByCategoryPreservingKeys(tradesBySession));
        metrics.setRiskRewardBySession(
                calculateRiskRewardByCategoryPreservingKeys(tradesBySession));
        applyBestSession(metrics);
    }

    private static final int BEST_SESSION_MIN_ELIGIBLE = 3;

    private void applyBestSession(TradeDistributionMetrics metrics) {
        Map<String, BigDecimal> avgPnl = metrics.getAvgPnlBySession();
        Map<String, Integer> eligible = metrics.getEligibleTradesBySession();
        if (avgPnl == null || eligible == null) {
            return;
        }
        String bestKey = null;
        BigDecimal bestAvg = null;
        for (String key : SESSION_KEYS) {
            int e = eligible.getOrDefault(key, 0);
            if (e < BEST_SESSION_MIN_ELIGIBLE) {
                continue;
            }
            BigDecimal avg = avgPnl.get(key);
            if (avg == null) {
                continue;
            }
            if (bestAvg == null || avg.compareTo(bestAvg) > 0) {
                bestAvg = avg;
                bestKey = key;
            }
        }
        metrics.setBestSessionKey(bestKey);
        metrics.setBestSessionAvgPnl(bestAvg);
    }

    static TradingStyleHint inferTradingStyleHint(List<TradeDetails> trades) {
        int scalper = 0;
        int intraday = 0;
        int swing = 0;
        int sample = 0;

        for (TradeDetails trade : trades) {
            Duration hold = holdDurationOrNull(trade);
            if (hold == null) {
                continue;
            }
            if (hold.isNegative()) {
                continue;
            }
            sample++;
            long minutes = hold.toMinutes();
            if (minutes < 15) {
                scalper++;
            } else if (minutes < 24 * 60) {
                intraday++;
            } else {
                swing++;
            }
        }

        if (sample < STYLE_MIN_SAMPLE) {
            return TradingStyleHint.builder()
                    .style("UNKNOWN")
                    .confidencePercent(BigDecimal.ZERO.setScale(2, ROUNDING_MODE))
                    .basis("holding_duration")
                    .sampleSize(sample)
                    .build();
        }

        double scalperShare = scalper / (double) sample;
        double intradayShare = (scalper + intraday) / (double) sample;
        // Intraday rule: not Scalper and ≥55% hold < 24h (scalper+intraday buckets)
        double swingShare = swing / (double) sample;

        String style;
        double confidence;
        if (scalperShare >= STYLE_DOMINANCE) {
            style = "SCALPER";
            confidence = scalperShare;
        } else if ((scalper + intraday) / (double) sample >= STYLE_DOMINANCE) {
            style = "INTRADAY";
            confidence = intradayShare;
        } else if (swingShare >= STYLE_DOMINANCE) {
            style = "SWING";
            confidence = swingShare;
        } else {
            style = "MIXED";
            int max = Math.max(scalper, Math.max(intraday, swing));
            confidence = max / (double) sample;
        }

        return TradingStyleHint.builder()
                .style(style)
                .confidencePercent(BigDecimal.valueOf(confidence * 100.0).setScale(2, ROUNDING_MODE))
                .basis("holding_duration")
                .sampleSize(sample)
                .build();
    }

    private static Duration holdDurationOrNull(TradeDetails trade) {
        if (trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null
                || trade.getExitInfo() == null || trade.getExitInfo().getTimestamp() == null) {
            return null;
        }
        return Duration.between(
                trade.getEntryInfo().getTimestamp(),
                trade.getExitInfo().getTimestamp());
    }

    private static boolean hasBadHoldTimestamps(TradeDetails trade) {
        Duration hold = holdDurationOrNull(trade);
        return hold != null && hold.isNegative();
    }

    private TradeDistributionMetrics emptyMetricsSkeleton() {
        Map<String, List<TradeDetails>> emptySession = emptySessionBuckets();
        TradeDistributionMetrics metrics = new TradeDistributionMetrics();
        applySessionMetrics(metrics, emptySession);
        metrics.setSkippedMissingEntryCount(0);
        metrics.setOpenOrMissingPnlCount(0);
        metrics.setBadTimestampCount(0);
        metrics.setTimezoneNote("entry_local_as_stored");
        metrics.setTradingStyleHint(TradingStyleHint.builder()
                .style("UNKNOWN")
                .confidencePercent(BigDecimal.ZERO.setScale(2, ROUNDING_MODE))
                .basis("holding_duration")
                .sampleSize(0)
                .build());
        return metrics;
    }

    private static Map<String, List<TradeDetails>> emptySessionBuckets() {
        Map<String, List<TradeDetails>> map = new LinkedHashMap<>();
        for (String key : SESSION_KEYS) {
            map.put(key, new ArrayList<>());
        }
        return map;
    }

    private String calculateDurationCategory(TradeDetails trade) {
        if (trade.getEntryInfo() == null || trade.getEntryInfo().getTimestamp() == null ||
            trade.getExitInfo() == null || trade.getExitInfo().getTimestamp() == null) {
            return "UNKNOWN";
        }

        Duration duration = Duration.between(
            trade.getEntryInfo().getTimestamp(),
            trade.getExitInfo().getTimestamp()
        );

        long hours = duration.toHours();

        if (hours < 1) {
            return "INTRADAY_SHORT";
        } else if (hours < 8) {
            return "INTRADAY_LONG";
        } else if (hours < 24) {
            return "SINGLE_DAY";
        } else if (hours < 24 * 7) {
            return "LESS_THAN_WEEK";
        } else if (hours < 24 * 30) {
            return "LESS_THAN_MONTH";
        } else {
            return "LONG_TERM";
        }
    }

    private String calculatePositionSizeCategory(TradeDetails trade) {
        if (trade.getEntryInfo() == null || trade.getEntryInfo().getPrice() == null ||
            trade.getEntryInfo().getQuantity() == null) {
            return "UNKNOWN";
        }

        BigDecimal positionSize = trade.getEntryInfo().getPrice()
            .multiply(BigDecimal.valueOf(trade.getEntryInfo().getQuantity().doubleValue()));

        if (positionSize.compareTo(BigDecimal.valueOf(1000)) < 0) {
            return "MICRO";
        } else if (positionSize.compareTo(BigDecimal.valueOf(5000)) < 0) {
            return "SMALL";
        } else if (positionSize.compareTo(BigDecimal.valueOf(20000)) < 0) {
            return "MEDIUM";
        } else if (positionSize.compareTo(BigDecimal.valueOf(50000)) < 0) {
            return "LARGE";
        } else {
            return "EXTRA_LARGE";
        }
    }

    private Map<String, BigDecimal> calculateProfitByCategory(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> profitByCategory = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            profitByCategory.put(entry.getKey(), sumProfit(entry.getValue()));
        }
        return profitByCategory;
    }

    private Map<String, BigDecimal> calculateProfitByCategoryPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> profitByCategory = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            profitByCategory.put(entry.getKey(), sumProfit(entry.getValue()));
        }
        return profitByCategory;
    }

    private BigDecimal sumProfit(List<TradeDetails> categoryTrades) {
        return categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .map(t -> t.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Win% = wins (pnl &gt; 0) / eligible (non-null pnl). Null when eligible=0.
     */
    private Map<String, BigDecimal> calculateWinRateByCategory(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> winRateByCategory = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            winRateByCategory.put(entry.getKey(), winRateFor(entry.getValue()));
        }
        return winRateByCategory;
    }

    private Map<String, BigDecimal> calculateWinRateByCategoryPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> winRateByCategory = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            winRateByCategory.put(entry.getKey(), winRateFor(entry.getValue()));
        }
        return winRateByCategory;
    }

    private BigDecimal winRateFor(List<TradeDetails> categoryTrades) {
        long eligible = categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .count();
        if (eligible == 0) {
            return null;
        }
        long winCount = categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null
                        && t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
                .count();
        return BigDecimal.valueOf(winCount * 100.0 / eligible).setScale(2, ROUNDING_MODE);
    }

    private Map<String, BigDecimal> calculateAvgPnlByCategory(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> avg = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            avg.put(entry.getKey(), avgPnlFor(entry.getValue()));
        }
        return avg;
    }

    private Map<String, BigDecimal> calculateAvgPnlByCategoryPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> avg = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            avg.put(entry.getKey(), avgPnlFor(entry.getValue()));
        }
        return avg;
    }

    private BigDecimal avgPnlFor(List<TradeDetails> categoryTrades) {
        List<BigDecimal> pnls = categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .map(t -> t.getMetrics().getProfitLoss())
                .toList();
        if (pnls.isEmpty()) {
            return null;
        }
        BigDecimal sum = pnls.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(pnls.size()), 4, ROUNDING_MODE);
    }

    private Map<String, BigDecimal> calculateAvgHoldMinutesByCategory(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> out = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            out.put(entry.getKey(), avgHoldMinutesFor(entry.getValue()));
        }
        return out;
    }

    private Map<String, BigDecimal> calculateAvgHoldMinutesByCategoryPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            out.put(entry.getKey(), avgHoldMinutesFor(entry.getValue()));
        }
        return out;
    }

    private BigDecimal avgHoldMinutesFor(List<TradeDetails> categoryTrades) {
        BigDecimal totalMinutes = BigDecimal.ZERO;
        int sample = 0;
        for (TradeDetails trade : categoryTrades) {
            Duration hold = holdDurationOrNull(trade);
            if (hold == null || hold.isNegative()) {
                continue;
            }
            totalMinutes = totalMinutes.add(
                    BigDecimal.valueOf(hold.toMillis())
                            .divide(BigDecimal.valueOf(60_000L), 4, ROUNDING_MODE));
            sample++;
        }
        if (sample == 0) {
            return null;
        }
        return totalMinutes.divide(BigDecimal.valueOf(sample), 4, ROUNDING_MODE);
    }

    private Map<String, BigDecimal> calculateRiskRewardByCategory(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> out = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            out.put(entry.getKey(), riskRewardFor(entry.getValue()));
        }
        return out;
    }

    private Map<String, BigDecimal> calculateRiskRewardByCategoryPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, BigDecimal> out = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            out.put(entry.getKey(), riskRewardFor(entry.getValue()));
        }
        return out;
    }

    /**
     * Avg win ÷ |avg loss|. Null when no wins or no losses (not a stop-based R-multiple).
     */
    private BigDecimal riskRewardFor(List<TradeDetails> categoryTrades) {
        BigDecimal totalWin = BigDecimal.ZERO;
        BigDecimal totalLossAbs = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        for (TradeDetails trade : categoryTrades) {
            if (trade.getMetrics() == null || trade.getMetrics().getProfitLoss() == null) {
                continue;
            }
            BigDecimal pnl = trade.getMetrics().getProfitLoss();
            int cmp = pnl.compareTo(BigDecimal.ZERO);
            if (cmp > 0) {
                totalWin = totalWin.add(pnl);
                winCount++;
            } else if (cmp < 0) {
                totalLossAbs = totalLossAbs.add(pnl.abs());
                lossCount++;
            }
        }
        if (winCount == 0 || lossCount == 0) {
            return null;
        }
        BigDecimal avgWin = totalWin.divide(BigDecimal.valueOf(winCount), 4, ROUNDING_MODE);
        BigDecimal avgLoss = totalLossAbs.divide(BigDecimal.valueOf(lossCount), 4, ROUNDING_MODE);
        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return avgWin.divide(avgLoss, 4, ROUNDING_MODE);
    }

    private Map<String, Integer> calculateEligibleCountByCategory(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, Integer> counts = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            counts.put(entry.getKey(), eligibleCount(entry.getValue()));
        }
        return counts;
    }

    private Map<String, Integer> calculateEligibleCountByCategoryPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            counts.put(entry.getKey(), eligibleCount(entry.getValue()));
        }
        return counts;
    }

    private int eligibleCount(List<TradeDetails> categoryTrades) {
        return (int) categoryTrades.stream()
                .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
                .count();
    }

    private Map<String, Integer> convertToTradeCount(Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, Integer> tradeCountByCategory = new HashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            tradeCountByCategory.put(entry.getKey(), entry.getValue().size());
        }
        return tradeCountByCategory;
    }

    private Map<String, Integer> convertToTradeCountPreservingKeys(
            Map<String, List<TradeDetails>> tradesByCategory) {
        Map<String, Integer> tradeCountByCategory = new LinkedHashMap<>();
        for (Map.Entry<String, List<TradeDetails>> entry : tradesByCategory.entrySet()) {
            tradeCountByCategory.put(entry.getKey(), entry.getValue().size());
        }
        return tradeCountByCategory;
    }
}
