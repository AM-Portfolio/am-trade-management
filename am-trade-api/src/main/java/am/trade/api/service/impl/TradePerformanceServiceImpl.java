package am.trade.api.service.impl;

import am.trade.api.dto.summary.DailyPerformance;
import am.trade.api.dto.summary.DayOfWeekPerformance;
import am.trade.api.dto.summary.HourlyPerformance;
import am.trade.api.dto.summary.MonthlyPerformance;
import am.trade.api.dto.summary.PerformanceMetrics;
import am.trade.api.dto.summary.TimingAnalysis;
import am.trade.api.dto.summary.TradePerformanceSummary;
import am.trade.api.dto.summary.WeeklyPerformance;
import am.trade.api.dto.summary.YearlyPerformance;
import am.trade.api.service.TradeApiService;
import am.trade.api.service.TradePerformanceService;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradePerformanceServiceImpl implements TradePerformanceService {

        private final TradeApiService tradeApiService;

        @Override
        public TradePerformanceSummary getPerformanceSummary(String portfolioId, LocalDate startDate,
                        LocalDate endDate) {
                log.info("Calculating performance summary for portfolio: {} from {} to {}", portfolioId, startDate,
                                endDate);

                List<TradeDetails> allTrades = tradeApiService.getTradesByFilters(
                                Collections.singletonList(portfolioId),
                                null,
                                Arrays.asList(TradeStatus.WIN, TradeStatus.LOSS, TradeStatus.BREAK_EVEN),
                                startDate,
                                endDate,
                                null,
                                Pageable.unpaged()).getContent();

                if (allTrades.isEmpty()) {
                        return TradePerformanceSummary.builder()
                                        .totalTrades(0)
                                        .winPercentage(0.0)
                                        .totalProfitLoss(BigDecimal.ZERO)
                                        .averageProfitPerTrade(BigDecimal.ZERO)
                                        .maxDrawdown(BigDecimal.ZERO)
                                        .profitFactor(0.0)
                                        .largestWin(BigDecimal.ZERO)
                                        .largestLoss(BigDecimal.ZERO)
                                        .build();
                }

                PerformanceMetrics metrics = calculateMetrics(allTrades);

                // Map PerformanceMetrics to TradePerformanceSummary flat fields for backward
                // compatibility
                return TradePerformanceSummary.builder()
                                .totalTrades(allTrades.size())
                                .winningTrades((int) allTrades.stream().filter(t -> t.getStatus() == TradeStatus.WIN)
                                                .count())
                                .losingTrades((int) allTrades.stream().filter(t -> t.getStatus() == TradeStatus.LOSS)
                                                .count())
                                .breakEvenTrades((int) allTrades.stream()
                                                .filter(t -> t.getStatus() == TradeStatus.BREAK_EVEN).count())
                                .winPercentage(metrics.getWinPercentage())
                                .totalProfitLoss(metrics.getGrossPnL())
                                .averageProfitPerTrade(metrics.getAvgGrossTradePnL())
                                .averageWinAmount(metrics.getAvgWin())
                                .averageLossAmount(metrics.getAvgLoss())
                                .averageHoldingTimeWin(metrics.getAvgHoldTime()) // Note: Summary has separate win/loss
                                                                                 // hold time, using
                                                                                 // avg for now or need split
                                .averageHoldingTimeLoss(metrics.getAvgHoldTime()) // FIXME: Calculate separate hold
                                                                                  // times if needed
                                .maxDrawdown(metrics.getMaxWeeklyGrossDrawdown()) // Using weekly drawdown or global?
                                                                                  // Summary usually
                                                                                  // means global.
                                .profitFactor(metrics.getProfitFactor())
                                .largestWin(metrics.getLargestProfitableTrade())
                                .largestLoss(metrics.getLargestLosingTrade())
                                .metrics(metrics)
                                .build();
        }

        @Override
        public List<DailyPerformance> getDailyPerformance(String portfolioId, int limit) {
                log.info("Calculating daily performance for portfolio: {}, limit: {}", portfolioId, limit);

                List<TradeDetails> allTrades = fetchAllTrades(portfolioId);

                Map<LocalDate, List<TradeDetails>> tradesByDate = allTrades.stream()
                                .collect(Collectors.groupingBy(TradeDetails::getTradeDate));

                return tradesByDate.entrySet().stream()
                                .map(entry -> calculateDailyPerformance(entry.getKey(), entry.getValue()))
                                .sorted(Comparator.comparing(DailyPerformance::getTotalProfitLoss).reversed())
                                .limit(limit)
                                .collect(Collectors.toList());
        }

        @Override
        public TimingAnalysis getTimingAnalysis(String portfolioId) {
                log.info("Calculating timing analysis for portfolio: {}", portfolioId);

                List<TradeDetails> allTrades = fetchAllTrades(portfolioId);

                List<HourlyPerformance> hourlyPerformances = calculateHourlyPerformance(allTrades);
                List<DayOfWeekPerformance> dayOfWeekPerformances = calculateDayOfWeekPerformance(allTrades);
                List<MonthlyPerformance> monthlyPerformances = calculateMonthlyPerformance(allTrades);
                List<YearlyPerformance> yearlyPerformances = calculateYearlyPerformance(allTrades);
                List<WeeklyPerformance> weeklyPerformances = calculateWeeklyPerformance(allTrades);

                // Statistics helper
                Integer bestHour = hourlyPerformances.stream()
                                .max(Comparator.comparing(HourlyPerformance::getTotalProfitLoss))
                                .map(HourlyPerformance::getHour).orElse(null);
                Integer worstHour = hourlyPerformances.stream()
                                .min(Comparator.comparing(HourlyPerformance::getTotalProfitLoss))
                                .map(HourlyPerformance::getHour).orElse(null);
                String bestDay = dayOfWeekPerformances.stream()
                                .max(Comparator.comparing(DayOfWeekPerformance::getTotalProfitLoss))
                                .map(DayOfWeekPerformance::getDayOfWeek).orElse(null);
                String worstDay = dayOfWeekPerformances.stream()
                                .min(Comparator.comparing(DayOfWeekPerformance::getTotalProfitLoss))
                                .map(DayOfWeekPerformance::getDayOfWeek).orElse(null);
                String bestMonth = monthlyPerformances.stream()
                                .max(Comparator.comparing(MonthlyPerformance::getTotalProfitLoss))
                                .map(MonthlyPerformance::getMonth)
                                .orElse(null);
                String worstMonth = monthlyPerformances.stream()
                                .min(Comparator.comparing(MonthlyPerformance::getTotalProfitLoss))
                                .map(MonthlyPerformance::getMonth)
                                .orElse(null);

                return TimingAnalysis.builder()
                                .hourlyPerformance(hourlyPerformances)
                                .dayOfWeekPerformance(dayOfWeekPerformances)
                                .monthlyPerformance(monthlyPerformances)
                                .yearlyPerformance(yearlyPerformances)
                                .weeklyPerformance(weeklyPerformances)
                                .bestTradingHour(bestHour)
                                .worstTradingHour(worstHour)
                                .bestTradingDay(bestDay)
                                .worstTradingDay(worstDay)
                                .bestTradingMonth(bestMonth)
                                .worstTradingMonth(worstMonth)
                                .build();
        }

        private List<TradeDetails> fetchAllTrades(String portfolioId) {
                return tradeApiService.getTradesByFilters(
                                Collections.singletonList(portfolioId),
                                null,
                                Arrays.asList(TradeStatus.WIN, TradeStatus.LOSS, TradeStatus.BREAK_EVEN),
                                null, null, null,
                                Pageable.unpaged()).getContent();
        }

        // --- Timeframe Calculation Methods ---

        private List<HourlyPerformance> calculateHourlyPerformance(List<TradeDetails> allTrades) {
                Map<Integer, List<TradeDetails>> tradesByHour = allTrades.stream()
                                .filter(t -> t.getEntryInfo() != null && t.getEntryInfo().getTimestamp() != null)
                                .collect(Collectors.groupingBy(t -> t.getEntryInfo().getTimestamp().getHour()));

                List<HourlyPerformance> list = new ArrayList<>();
                for (int i = 0; i < 24; i++) {
                        List<TradeDetails> trades = tradesByHour.getOrDefault(i, Collections.emptyList());
                        if (!trades.isEmpty()) {
                                PerformanceMetrics metrics = calculateMetrics(trades);
                                list.add(HourlyPerformance.builder()
                                                .hour(i)
                                                .tradeCount(trades.size())
                                                .winCount((int) trades.stream()
                                                                .filter(t -> t.getStatus() == TradeStatus.WIN).count())
                                                .lossCount((int) trades.stream()
                                                                .filter(t -> t.getStatus() == TradeStatus.LOSS).count())
                                                .winRate(metrics.getWinPercentage())
                                                .totalProfitLoss(metrics.getGrossPnL())
                                                .averageWinAmount(metrics.getAvgWin())
                                                .averageLossAmount(metrics.getAvgLoss())
                                                .averageHoldingTime(metrics.getAvgHoldTime())
                                                .metrics(metrics)
                                                .build());
                        }
                }
                list.sort(Comparator.comparingInt(HourlyPerformance::getHour));
                return list;
        }

        private List<DayOfWeekPerformance> calculateDayOfWeekPerformance(List<TradeDetails> allTrades) {
                Map<java.time.DayOfWeek, List<TradeDetails>> tradesByDay = allTrades.stream()
                                .filter(t -> t.getTradeDate() != null)
                                .collect(Collectors.groupingBy(t -> t.getTradeDate().getDayOfWeek()));

                return Arrays.stream(java.time.DayOfWeek.values())
                                .map(day -> {
                                        List<TradeDetails> trades = tradesByDay.getOrDefault(day,
                                                        Collections.emptyList());
                                        if (trades.isEmpty())
                                                return null;
                                        PerformanceMetrics metrics = calculateMetrics(trades);
                                        return DayOfWeekPerformance.builder()
                                                        .dayOfWeek(day.name())
                                                        .dayOrder(day.getValue())
                                                        .tradeCount(trades.size())
                                                        .winCount((int) trades.stream()
                                                                        .filter(t -> t.getStatus() == TradeStatus.WIN)
                                                                        .count())
                                                        .lossCount((int) trades.stream()
                                                                        .filter(t -> t.getStatus() == TradeStatus.LOSS)
                                                                        .count())
                                                        .winRate(metrics.getWinPercentage())
                                                        .totalProfitLoss(metrics.getGrossPnL())
                                                        .averageWinAmount(metrics.getAvgWin())
                                                        .averageLossAmount(metrics.getAvgLoss())
                                                        .averageHoldingTime(metrics.getAvgHoldTime())
                                                        .metrics(metrics)
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
        }

        private List<WeeklyPerformance> calculateWeeklyPerformance(List<TradeDetails> allTrades) {
                // Group by ISO Week
                Map<String, List<TradeDetails>> tradesByWeek = allTrades.stream()
                                .filter(t -> t.getTradeDate() != null)
                                .collect(Collectors.groupingBy(t -> {
                                        WeekFields weekFields = WeekFields.of(Locale.getDefault());
                                        int weekNum = t.getTradeDate().get(weekFields.weekOfWeekBasedYear());
                                        int year = t.getTradeDate().get(weekFields.weekBasedYear());
                                        return year + "-W" + String.format("%02d", weekNum);
                                }));

                return tradesByWeek.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(entry -> {
                                        PerformanceMetrics metrics = calculateMetrics(entry.getValue());
                                        // Rough start/end date estimation or min/max from actual trades
                                        LocalDate minDate = entry.getValue().stream().map(TradeDetails::getTradeDate)
                                                        .min(LocalDate::compareTo).orElse(LocalDate.now());
                                        LocalDate maxDate = entry.getValue().stream().map(TradeDetails::getTradeDate)
                                                        .max(LocalDate::compareTo).orElse(LocalDate.now());

                                        return WeeklyPerformance.builder()
                                                        .weekId(entry.getKey())
                                                        .startDate(minDate)
                                                        .endDate(maxDate)
                                                        .metrics(metrics)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private List<MonthlyPerformance> calculateMonthlyPerformance(List<TradeDetails> allTrades) {
                Map<java.time.Month, List<TradeDetails>> tradesByMonth = allTrades.stream()
                                .filter(t -> t.getTradeDate() != null)
                                .collect(Collectors.groupingBy(t -> t.getTradeDate().getMonth()));

                return Arrays.stream(java.time.Month.values())
                                .map(month -> {
                                        List<TradeDetails> trades = tradesByMonth.getOrDefault(month,
                                                        Collections.emptyList());
                                        if (trades.isEmpty())
                                                return null;
                                        PerformanceMetrics metrics = calculateMetrics(trades);
                                        return MonthlyPerformance.builder()
                                                        .month(month.name())
                                                        .monthOrder(month.getValue())
                                                        .tradeCount(trades.size())
                                                        .winCount((int) trades.stream()
                                                                        .filter(t -> t.getStatus() == TradeStatus.WIN)
                                                                        .count())
                                                        .lossCount((int) trades.stream()
                                                                        .filter(t -> t.getStatus() == TradeStatus.LOSS)
                                                                        .count())
                                                        .winRate(metrics.getWinPercentage())
                                                        .totalProfitLoss(metrics.getGrossPnL())
                                                        .averageWinAmount(metrics.getAvgWin())
                                                        .averageLossAmount(metrics.getAvgLoss())
                                                        .averageHoldingTime(metrics.getAvgHoldTime())
                                                        .metrics(metrics)
                                                        .build();
                                })
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());
        }

        private List<YearlyPerformance> calculateYearlyPerformance(List<TradeDetails> allTrades) {
                Map<Integer, List<TradeDetails>> tradesByYear = allTrades.stream()
                                .filter(t -> t.getTradeDate() != null)
                                .collect(Collectors.groupingBy(t -> t.getTradeDate().getYear()));

                return tradesByYear.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .map(entry -> {
                                        PerformanceMetrics metrics = calculateMetrics(entry.getValue());
                                        return YearlyPerformance.builder()
                                                        .year(entry.getKey())
                                                        .tradeCount(entry.getValue().size())
                                                        .winCount((int) entry.getValue().stream()
                                                                        .filter(t -> t.getStatus() == TradeStatus.WIN)
                                                                        .count())
                                                        .lossCount((int) entry.getValue().stream()
                                                                        .filter(t -> t.getStatus() == TradeStatus.LOSS)
                                                                        .count())
                                                        .winRate(metrics.getWinPercentage())
                                                        .totalProfitLoss(metrics.getGrossPnL())
                                                        .averageWinAmount(metrics.getAvgWin())
                                                        .averageLossAmount(metrics.getAvgLoss())
                                                        .averageHoldingTime(metrics.getAvgHoldTime())
                                                        .metrics(metrics)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }

        private DailyPerformance calculateDailyPerformance(LocalDate date, List<TradeDetails> trades) {
                PerformanceMetrics metrics = calculateMetrics(trades);

                String bestSymbol = null;
                BigDecimal bestTradePnL = BigDecimal.valueOf(-Double.MAX_VALUE);

                for (TradeDetails trade : trades) {
                        BigDecimal pnl = trade.calculateTotalProfitLoss();
                        if (pnl.compareTo(bestTradePnL) > 0) {
                                bestTradePnL = pnl;
                                bestSymbol = trade.getSymbol();
                        }
                }
                if (bestSymbol == null)
                        bestTradePnL = BigDecimal.ZERO;

                return DailyPerformance.builder()
                                .date(date)
                                .tradeCount(trades.size())
                                .winCount((int) trades.stream().filter(t -> t.getStatus() == TradeStatus.WIN).count())
                                .lossCount((int) trades.stream().filter(t -> t.getStatus() == TradeStatus.LOSS).count())
                                .winRate(metrics.getWinPercentage())
                                .totalProfitLoss(metrics.getGrossPnL())
                                .bestTradeSymbol(bestSymbol)
                                .bestTradePnL(bestTradePnL)
                                .averageProfitPerTrade(metrics.getAvgGrossTradePnL())
                                .averageWinAmount(metrics.getAvgWin())
                                .averageLossAmount(metrics.getAvgLoss())
                                .metrics(metrics)
                                .build();
        }

        // --- Core Metric Calculation ---

        private PerformanceMetrics calculateMetrics(List<TradeDetails> trades) {
                if (trades == null || trades.isEmpty()) {
                        return new PerformanceMetrics();
                }

                int totalTrades = trades.size();
                int winCount = 0;
                int lossCount = 0;
                int breakEvenCount = 0;
                BigDecimal totalPnL = BigDecimal.ZERO;
                BigDecimal totalWinAmt = BigDecimal.ZERO;
                BigDecimal totalLossAmt = BigDecimal.ZERO;
                BigDecimal maxProfit = BigDecimal.ZERO;
                BigDecimal maxLoss = BigDecimal.ZERO; // e.g. -500
                long totalHoldTime = 0;

                List<BigDecimal> pnls = new ArrayList<>();

                // Sorting for streaks
                List<TradeDetails> sortedTrades = trades.stream()
                                .sorted(Comparator.comparing(TradeDetails::getTradeDate))
                                .collect(Collectors.toList());

                for (TradeDetails trade : sortedTrades) {
                        BigDecimal pnl = trade.calculateTotalProfitLoss();
                        pnls.add(pnl);
                        totalPnL = totalPnL.add(pnl);

                        Long hold = trade.calculateHoldingTimeHours();
                        if (hold != null)
                                totalHoldTime += hold;

                        if (trade.getStatus() == TradeStatus.WIN) {
                                winCount++;
                                totalWinAmt = totalWinAmt.add(pnl);
                                if (pnl.compareTo(maxProfit) > 0)
                                        maxProfit = pnl;
                        } else if (trade.getStatus() == TradeStatus.LOSS) {
                                lossCount++;
                                totalLossAmt = totalLossAmt.add(pnl.abs());
                                if (pnl.compareTo(maxLoss) < 0)
                                        maxLoss = pnl; // find most negative
                        } else {
                                breakEvenCount++;
                        }
                }

                // Averages
                BigDecimal avgGrossTradePnL = totalPnL.divide(BigDecimal.valueOf(totalTrades), 2, RoundingMode.HALF_UP);
                BigDecimal avgWin = winCount > 0
                                ? totalWinAmt.divide(BigDecimal.valueOf(winCount), 2, RoundingMode.HALF_UP)
                                : BigDecimal.ZERO;
                BigDecimal avgLoss = lossCount > 0
                                ? totalLossAmt.divide(BigDecimal.valueOf(lossCount), 2, RoundingMode.HALF_UP).negate()
                                : BigDecimal.ZERO;
                double winPercentage = totalTrades > 0 ? (double) winCount / totalTrades * 100 : 0.0;
                double avgHoldTime = totalTrades > 0 ? (double) totalHoldTime / totalTrades : 0.0;

                // Profit Factor
                double profitFactor = totalLossAmt.compareTo(BigDecimal.ZERO) > 0
                                ? totalWinAmt.divide(totalLossAmt, 2, RoundingMode.HALF_UP).doubleValue()
                                : (totalWinAmt.compareTo(BigDecimal.ZERO) > 0 ? Double.POSITIVE_INFINITY : 0.0);

                // Streaks
                int maxConsecutiveWins = 0;
                int maxConsecutiveLosses = 0;
                int currentConsecutiveWins = 0;
                int currentConsecutiveLosses = 0;

                for (TradeDetails trade : sortedTrades) {
                        if (trade.getStatus() == TradeStatus.WIN) {
                                currentConsecutiveWins++;
                                currentConsecutiveLosses = 0;
                                maxConsecutiveWins = Math.max(maxConsecutiveWins, currentConsecutiveWins);
                        } else if (trade.getStatus() == TradeStatus.LOSS) {
                                currentConsecutiveLosses++;
                                currentConsecutiveWins = 0;
                                maxConsecutiveLosses = Math.max(maxConsecutiveLosses, currentConsecutiveLosses);
                        } else {
                                currentConsecutiveWins = 0;
                                currentConsecutiveLosses = 0;
                        }
                }

                // Drawdown (simplified local drawdown for this set of trades)
                BigDecimal currentEquity = BigDecimal.ZERO;
                BigDecimal peakEquity = BigDecimal.ZERO;
                BigDecimal maxDrawdown = BigDecimal.ZERO;

                for (TradeDetails trade : sortedTrades) {
                        currentEquity = currentEquity.add(trade.calculateTotalProfitLoss());
                        if (currentEquity.compareTo(peakEquity) > 0)
                                peakEquity = currentEquity;
                        BigDecimal dd = peakEquity.subtract(currentEquity);
                        if (dd.compareTo(maxDrawdown) > 0)
                                maxDrawdown = dd;
                }

                // Populate DTO
                return PerformanceMetrics.builder()
                                .avgHoldTime(avgHoldTime)
                                // .longestTradeDuration() // Need logic
                                .avgGrossTradePnL(avgGrossTradePnL)
                                .avgLoss(avgLoss)
                                .avgWin(avgWin)
                                .avgTradeWinLossRatio(BigDecimal.ZERO) // TODO: impl
                                .grossPnL(totalPnL)
                                .largestLosingTrade(maxLoss)
                                .largestProfitableTrade(maxProfit)
                                .profitFactor(profitFactor)
                                .maxWeeklyGrossDrawdown(maxDrawdown.negate()) // Using generic max drawdown slot
                                .maxConsecutiveWins(maxConsecutiveWins)
                                .maxConsecutiveLosses(maxConsecutiveLosses)
                                .winPercentage(Math.round(winPercentage * 100.0) / 100.0)
                                .winningDays(0) // Need logic aggregating by day... complex for sub-lists
                                .losingDays(0)
                                .build();
        }
}
