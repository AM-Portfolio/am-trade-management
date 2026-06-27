package am.trade.api.controller;

import am.trade.api.dto.performance.DailyPerformanceResponse;
import am.trade.api.dto.performance.PerformanceSummaryResponse;
import am.trade.api.dto.performance.TimingAnalysisResponse;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import am.trade.services.service.TradeDetailsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

/**
 * REST controller for performance analytics.
 *
 * <p>Provides three endpoints consumed by the Flutter {@code trade_report_remote_datasource.dart}:
 * <ul>
 *   <li>GET /v1/performance/summary  – overall KPIs</li>
 *   <li>GET /v1/performance/daily    – day-by-day P&amp;L</li>
 *   <li>GET /v1/performance/timing   – breakdown by hour / DOW / month / year / week</li>
 * </ul>
 *
 * <p><b>Design note:</b> All analytics are computed in-memory from MongoDB trade documents.
 * For large portfolios this is fine at typical scale; if needed in future, pre-aggregated
 * collections can replace the in-memory pass without changing the API contract.
 */
@RestController
@RequestMapping("/v1/performance")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Performance", description = "Trade performance analytics endpoints")
public class PerformanceController {

    private final TradeDetailsService tradeDetailsService;

    private static final Map<String, Object> EMPTY_METRICS = Collections.emptyMap();

    // ──────────────────────────────────────────────────────────────────────────
    //  1.  SUMMARY
    // ──────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get overall performance summary for a portfolio and date range")
    @GetMapping("/summary")
    public ResponseEntity<PerformanceSummaryResponse> getSummary(
            @RequestParam(required = false) String portfolioId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        log.info("Performance summary request: portfolioId={}, start={}, end={}", portfolioId, startDate, endDate);

        List<TradeDetails> trades = fetchTrades(portfolioId, startDate, endDate);

        // Only closed trades contribute to P&L statistics
        List<TradeDetails> closedTrades = trades.stream()
                .filter(t -> t.getExitInfo() != null && t.getExitInfo().getTimestamp() != null)
                .collect(Collectors.toList());

        int total   = closedTrades.size();
        int wins    = (int) closedTrades.stream().filter(t -> TradeStatus.WIN.equals(t.getStatus())).count();
        int losses  = (int) closedTrades.stream().filter(t -> TradeStatus.LOSS.equals(t.getStatus())).count();
        int be      = total - wins - losses;

        double winPct = total == 0 ? 0 : round((double) wins / total * 100);

        // P&L numbers
        List<Double> pnls = closedTrades.stream()
                .map(t -> t.calculateTotalProfitLoss().doubleValue())
                .collect(Collectors.toList());

        double totalPnl     = pnls.stream().mapToDouble(Double::doubleValue).sum();
        double avgPnl       = total == 0 ? 0 : totalPnl / total;

        List<Double> winPnls  = pnls.stream().filter(p -> p > 0).collect(Collectors.toList());
        List<Double> lossPnls = pnls.stream().filter(p -> p < 0).collect(Collectors.toList());

        double avgWin  = winPnls.isEmpty()  ? 0 : winPnls.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double avgLoss = lossPnls.isEmpty() ? 0 : lossPnls.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double largestWin  = winPnls.isEmpty()  ? 0 : winPnls.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double largestLoss = lossPnls.isEmpty() ? 0 : lossPnls.stream().mapToDouble(Double::doubleValue).min().orElse(0);

        // Profit factor = gross profit / |gross loss|
        double grossProfit = winPnls.stream().mapToDouble(Double::doubleValue).sum();
        double grossLoss   = Math.abs(lossPnls.stream().mapToDouble(Double::doubleValue).sum());
        double profitFactor = grossLoss == 0 ? (grossProfit > 0 ? Double.MAX_VALUE : 0) : grossProfit / grossLoss;

        // Average holding time split by win/loss (in hours)
        double avgHoldWin  = holdingTime(closedTrades, TradeStatus.WIN);
        double avgHoldLoss = holdingTime(closedTrades, TradeStatus.LOSS);

        // Max drawdown (simple peak-to-trough on cumulative P&L series)
        double maxDrawdown = computeMaxDrawdown(pnls);

        PerformanceSummaryResponse resp = PerformanceSummaryResponse.builder()
                .totalTrades(total)
                .winningTrades(wins)
                .losingTrades(losses)
                .breakEvenTrades(be)
                .winPercentage(round(winPct))
                .totalProfitLoss(round(totalPnl))
                .averageProfitPerTrade(round(avgPnl))
                .averageWinAmount(round(avgWin))
                .averageLossAmount(round(avgLoss))
                .averageHoldingTimeWin(round(avgHoldWin))
                .averageHoldingTimeLoss(round(avgHoldLoss))
                .maxDrawdown(round(maxDrawdown))
                .profitFactor(round(profitFactor))
                .largestWin(round(largestWin))
                .largestLoss(round(largestLoss))
                .metrics(EMPTY_METRICS)
                .build();

        return ResponseEntity.ok(resp);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  2.  DAILY
    // ──────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get day-by-day performance breakdown")
    @GetMapping("/daily")
    public ResponseEntity<List<DailyPerformanceResponse>> getDaily(
            @RequestParam(required = false) String portfolioId,
            @RequestParam(defaultValue = "1000") int limit) {

        log.info("Daily performance request: portfolioId={}, limit={}", portfolioId, limit);

        // Fetch all trades for portfolio (no date filter — frontend paginates via limit param)
        List<TradeDetails> trades;
        if (portfolioId != null && !portfolioId.isBlank()) {
            trades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        } else {
            trades = Collections.emptyList();
        }

        // Group closed trades by exit date (day of trade closing)
        Map<LocalDate, List<TradeDetails>> byDate = trades.stream()
                .filter(t -> t.getExitInfo() != null && t.getExitInfo().getTimestamp() != null)
                .collect(Collectors.groupingBy(t -> t.getExitInfo().getTimestamp().toLocalDate()));

        List<DailyPerformanceResponse> result = byDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .limit(limit)
                .map(entry -> buildDailyEntry(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    private DailyPerformanceResponse buildDailyEntry(LocalDate date, List<TradeDetails> dayTrades) {
        int wins   = (int) dayTrades.stream().filter(t -> TradeStatus.WIN.equals(t.getStatus())).count();
        int losses = (int) dayTrades.stream().filter(t -> TradeStatus.LOSS.equals(t.getStatus())).count();
        int total  = dayTrades.size();
        double totalPnl = dayTrades.stream().mapToDouble(t -> t.calculateTotalProfitLoss().doubleValue()).sum();
        double winRate  = total == 0 ? 0 : round((double) wins / total * 100);

        // Best trade = highest PnL on this day
        Optional<TradeDetails> best = dayTrades.stream()
                .max(Comparator.comparingDouble(t -> t.calculateTotalProfitLoss().doubleValue()));

        return DailyPerformanceResponse.builder()
                .date(date.toString())
                .totalProfitLoss(round(totalPnl))
                .tradeCount(total)
                .winCount(wins)
                .lossCount(losses)
                .winRate(winRate)
                .bestTradeSymbol(best.map(TradeDetails::getSymbol).orElse(null))
                .bestTradePnL(best.map(t -> round(t.calculateTotalProfitLoss().doubleValue())).orElse(null))
                .metrics(EMPTY_METRICS)
                .build();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  3.  TIMING
    // ──────────────────────────────────────────────────────────────────────────

    @Operation(summary = "Get timing analysis broken down by hour, day-of-week, month, year, week")
    @GetMapping("/timing")
    public ResponseEntity<TimingAnalysisResponse> getTiming(
            @RequestParam(required = false) String portfolioId) {

        log.info("Timing analysis request: portfolioId={}", portfolioId);

        List<TradeDetails> trades;
        if (portfolioId != null && !portfolioId.isBlank()) {
            trades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        } else {
            trades = Collections.emptyList();
        }

        // Only closed trades for exit-time analysis
        List<TradeDetails> closed = trades.stream()
                .filter(t -> t.getExitInfo() != null && t.getExitInfo().getTimestamp() != null)
                .collect(Collectors.toList());

        List<TimingAnalysisResponse.HourlyPerformance>     hourly    = buildHourly(closed);
        List<TimingAnalysisResponse.DayOfWeekPerformance>  dowList   = buildDayOfWeek(closed);
        List<TimingAnalysisResponse.MonthlyPerformance>    monthly   = buildMonthly(closed);
        List<TimingAnalysisResponse.YearlyPerformance>     yearly    = buildYearly(closed);
        List<TimingAnalysisResponse.WeeklyPerformance>     weekly    = buildWeekly(closed);

        // Best / worst labels
        String bestDay  = dowList.stream().max(Comparator.comparingDouble(TimingAnalysisResponse.DayOfWeekPerformance::getTotalProfitLoss)).map(TimingAnalysisResponse.DayOfWeekPerformance::getDayOfWeek).orElse(null);
        String worstDay = dowList.stream().min(Comparator.comparingDouble(TimingAnalysisResponse.DayOfWeekPerformance::getTotalProfitLoss)).map(TimingAnalysisResponse.DayOfWeekPerformance::getDayOfWeek).orElse(null);
        Integer bestHour  = hourly.stream().max(Comparator.comparingDouble(TimingAnalysisResponse.HourlyPerformance::getTotalProfitLoss)).map(TimingAnalysisResponse.HourlyPerformance::getHour).orElse(null);
        Integer worstHour = hourly.stream().min(Comparator.comparingDouble(TimingAnalysisResponse.HourlyPerformance::getTotalProfitLoss)).map(TimingAnalysisResponse.HourlyPerformance::getHour).orElse(null);
        String bestMonth  = monthly.stream().max(Comparator.comparingDouble(TimingAnalysisResponse.MonthlyPerformance::getTotalProfitLoss)).map(TimingAnalysisResponse.MonthlyPerformance::getMonth).orElse(null);
        String worstMonth = monthly.stream().min(Comparator.comparingDouble(TimingAnalysisResponse.MonthlyPerformance::getTotalProfitLoss)).map(TimingAnalysisResponse.MonthlyPerformance::getMonth).orElse(null);

        TimingAnalysisResponse resp = TimingAnalysisResponse.builder()
                .hourlyPerformance(hourly)
                .dayOfWeekPerformance(dowList)
                .monthlyPerformance(monthly)
                .yearlyPerformance(yearly)
                .weeklyPerformance(weekly)
                .bestTradingHour(bestHour)
                .worstTradingHour(worstHour)
                .bestTradingDay(bestDay)
                .worstTradingDay(worstDay)
                .bestTradingMonth(bestMonth)
                .worstTradingMonth(worstMonth)
                .build();

        return ResponseEntity.ok(resp);
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Private helpers
    // ──────────────────────────────────────────────────────────────────────────

    private List<TradeDetails> fetchTrades(String portfolioId, LocalDate startDate, LocalDate endDate) {
        if (portfolioId == null || portfolioId.isBlank()) {
            return Collections.emptyList();
        }
        if (startDate != null && endDate != null) {
            LocalDateTime from = startDate.atStartOfDay();
            LocalDateTime to   = endDate.atTime(LocalTime.MAX);
            return tradeDetailsService.findByPortfolioIdInAndEntryInfoTimestampBetween(
                    List.of(portfolioId), from, to);
        }
        return tradeDetailsService.findModelsByPortfolioId(portfolioId);
    }

    private double holdingTime(List<TradeDetails> trades, TradeStatus status) {
        return trades.stream()
                .filter(t -> status.equals(t.getStatus()))
                .mapToLong(t -> {
                    Long h = t.calculateHoldingTimeHours();
                    return h != null ? h : 0L;
                })
                .average()
                .orElse(0);
    }

    /** Simple peak-to-trough max drawdown on a series of per-trade P&Ls. */
    private double computeMaxDrawdown(List<Double> pnls) {
        double peak = 0, equity = 0, maxDD = 0;
        for (double pnl : pnls) {
            equity += pnl;
            if (equity > peak) peak = equity;
            double dd = peak - equity;
            if (dd > maxDD) maxDD = dd;
        }
        return maxDD;
    }

    /* ── Timing builders ──────────────────────────────────────────────────── */

    private List<TimingAnalysisResponse.HourlyPerformance> buildHourly(List<TradeDetails> trades) {
        // Group by the hour of trade exit
        Map<Integer, List<TradeDetails>> byHour = trades.stream()
                .collect(Collectors.groupingBy(t -> t.getExitInfo().getTimestamp().getHour()));

        return byHour.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    int hour = e.getKey();
                    List<TradeDetails> g = e.getValue();
                    GroupStats s = groupStats(g);
                    return TimingAnalysisResponse.HourlyPerformance.builder()
                            .hour(hour)
                            .tradeCount(s.total)
                            .winCount(s.wins)
                            .lossCount(s.losses)
                            .winRate(s.winRate)
                            .totalProfitLoss(s.totalPnl)
                            .averageWinAmount(s.avgWin)
                            .averageLossAmount(s.avgLoss)
                            .averageHoldingTime(s.avgHoldHours)
                            .metrics(EMPTY_METRICS)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static final List<String> DOW_NAMES = List.of(
            "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY");

    private List<TimingAnalysisResponse.DayOfWeekPerformance> buildDayOfWeek(List<TradeDetails> trades) {
        Map<DayOfWeek, List<TradeDetails>> byDow = trades.stream()
                .collect(Collectors.groupingBy(t -> t.getExitInfo().getTimestamp().getDayOfWeek()));

        return byDow.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getValue()))
                .map(e -> {
                    DayOfWeek dow = e.getKey();
                    List<TradeDetails> g = e.getValue();
                    GroupStats s = groupStats(g);
                    return TimingAnalysisResponse.DayOfWeekPerformance.builder()
                            .dayOfWeek(dow.name())
                            .dayOrder(dow.getValue())
                            .tradeCount(s.total)
                            .winCount(s.wins)
                            .lossCount(s.losses)
                            .winRate(s.winRate)
                            .totalProfitLoss(s.totalPnl)
                            .averageWinAmount(s.avgWin)
                            .averageLossAmount(s.avgLoss)
                            .averageHoldingTime(s.avgHoldHours)
                            .metrics(EMPTY_METRICS)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static final String[] MONTH_NAMES = {
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private List<TimingAnalysisResponse.MonthlyPerformance> buildMonthly(List<TradeDetails> trades) {
        Map<Month, List<TradeDetails>> byMonth = trades.stream()
                .collect(Collectors.groupingBy(t -> t.getExitInfo().getTimestamp().getMonth()));

        return byMonth.entrySet().stream()
                .sorted(Comparator.comparingInt(e -> e.getKey().getValue()))
                .map(e -> {
                    Month m = e.getKey();
                    GroupStats s = groupStats(e.getValue());
                    return TimingAnalysisResponse.MonthlyPerformance.builder()
                            .month(MONTH_NAMES[m.getValue() - 1])
                            .monthOrder(m.getValue())
                            .tradeCount(s.total)
                            .winCount(s.wins)
                            .lossCount(s.losses)
                            .winRate(s.winRate)
                            .totalProfitLoss(s.totalPnl)
                            .averageWinAmount(s.avgWin)
                            .averageLossAmount(s.avgLoss)
                            .averageHoldingTime(s.avgHoldHours)
                            .metrics(EMPTY_METRICS)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<TimingAnalysisResponse.YearlyPerformance> buildYearly(List<TradeDetails> trades) {
        Map<Integer, List<TradeDetails>> byYear = trades.stream()
                .collect(Collectors.groupingBy(t -> t.getExitInfo().getTimestamp().getYear()));

        return byYear.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    GroupStats s = groupStats(e.getValue());
                    return TimingAnalysisResponse.YearlyPerformance.builder()
                            .year(e.getKey())
                            .tradeCount(s.total)
                            .winCount(s.wins)
                            .lossCount(s.losses)
                            .winRate(s.winRate)
                            .totalProfitLoss(s.totalPnl)
                            .averageWinAmount(s.avgWin)
                            .averageLossAmount(s.avgLoss)
                            .averageHoldingTime(s.avgHoldHours)
                            .metrics(EMPTY_METRICS)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private static final WeekFields WEEK_FIELDS = WeekFields.ISO;

    private List<TimingAnalysisResponse.WeeklyPerformance> buildWeekly(List<TradeDetails> trades) {
        Map<String, List<TradeDetails>> byWeek = trades.stream()
                .collect(Collectors.groupingBy(t -> {
                    LocalDateTime ts = t.getExitInfo().getTimestamp();
                    int year = ts.get(WEEK_FIELDS.weekBasedYear());
                    int week = ts.get(WEEK_FIELDS.weekOfWeekBasedYear());
                    return String.format("%d-W%02d", year, week);
                }));

        return byWeek.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> {
                    GroupStats s = groupStats(e.getValue());
                    return TimingAnalysisResponse.WeeklyPerformance.builder()
                            .weekId(e.getKey())
                            .tradeCount(s.total)
                            .winCount(s.wins)
                            .lossCount(s.losses)
                            .winRate(s.winRate)
                            .totalProfitLoss(s.totalPnl)
                            .metrics(EMPTY_METRICS)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /* ── Micro-helpers ────────────────────────────────────────────────────── */

    private static class GroupStats {
        int total, wins, losses;
        double winRate, totalPnl, avgWin, avgLoss, avgHoldHours;
    }

    private GroupStats groupStats(List<TradeDetails> g) {
        GroupStats s = new GroupStats();
        s.total  = g.size();
        s.wins   = (int) g.stream().filter(t -> TradeStatus.WIN.equals(t.getStatus())).count();
        s.losses = (int) g.stream().filter(t -> TradeStatus.LOSS.equals(t.getStatus())).count();
        s.winRate = s.total == 0 ? 0 : round((double) s.wins / s.total * 100);

        List<Double> pnls = g.stream().map(t -> t.calculateTotalProfitLoss().doubleValue()).collect(Collectors.toList());
        s.totalPnl = round(pnls.stream().mapToDouble(Double::doubleValue).sum());

        List<Double> winPnls  = pnls.stream().filter(p -> p > 0).collect(Collectors.toList());
        List<Double> lossPnls = pnls.stream().filter(p -> p < 0).collect(Collectors.toList());
        s.avgWin  = round(winPnls.isEmpty()  ? 0 : winPnls.stream().mapToDouble(Double::doubleValue).average().orElse(0));
        s.avgLoss = round(lossPnls.isEmpty() ? 0 : lossPnls.stream().mapToDouble(Double::doubleValue).average().orElse(0));

        s.avgHoldHours = round(g.stream()
                .mapToLong(t -> { Long h = t.calculateHoldingTimeHours(); return h != null ? h : 0L; })
                .average().orElse(0));
        return s;
    }

    private static double round(double v) {
        if (Double.isInfinite(v) || Double.isNaN(v)) return 0;
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
