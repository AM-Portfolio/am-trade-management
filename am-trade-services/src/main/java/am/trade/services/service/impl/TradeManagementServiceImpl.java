package am.trade.services.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.persistence.service.TradeDetailsService;
import am.trade.services.model.TradeSummary;
import am.trade.services.service.TradeManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation of TradeManagementService that provides calendar-based trade analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TradeManagementServiceImpl implements TradeManagementService {

    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final TradeDetailsService tradeDetailsService;

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByDay(LocalDate date, String portfolioId) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.plusDays(1).atStartOfDay().minusNanos(1);
        
        return getTradeDetailsByDateTimeRange(startOfDay, endOfDay, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByMonth(int year, int month, String portfolioId) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByQuarter(int year, int quarter, String portfolioId) {
        // Calculate the start month of the quarter (1->1, 2->4, 3->7, 4->10)
        int startMonth = (quarter - 1) * 3 + 1;
        
        LocalDate startDate = LocalDate.of(year, startMonth, 1);
        LocalDate endDate = startDate.plusMonths(3).minusDays(1);
        
        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByFinancialYear(int financialYear, String portfolioId) {
        // Financial year is from April 1 to March 31
        // For FY 2024-2025, financialYear parameter would be 2025
        int startYear = financialYear - 1;
        
        LocalDate startDate = LocalDate.of(startYear, Month.APRIL, 1);
        LocalDate endDate = LocalDate.of(financialYear, Month.MARCH, 31);
        
        return getTradeDetailsByDateRange(startDate, endDate, portfolioId);
    }

    @Override
    public Map<String, List<TradeDetails>> getTradeDetailsByDateRange(LocalDate startDate, LocalDate endDate, String portfolioId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);
        
        return getTradeDetailsByDateTimeRange(startDateTime, endDateTime, portfolioId);
    }
    
    private Map<String, List<TradeDetails>> getTradeDetailsByDateTimeRange(LocalDateTime startDateTime, LocalDateTime endDateTime, String portfolioId) {
        List<TradeDetails> trades;
        
        // If portfolio ID is provided, filter by it
        if (portfolioId != null && !portfolioId.isEmpty()) {
            // Get all trades for the portfolio
            trades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
            
            // Filter by date range
            trades = trades.stream()
                .filter(trade -> {
                    LocalDateTime tradeDate = trade.getEntryInfo() != null ? 
                        trade.getEntryInfo().getTimestamp() : null;
                    
                    return tradeDate != null && 
                        !tradeDate.isBefore(startDateTime) && 
                        !tradeDate.isAfter(endDateTime);
                })
                .collect(Collectors.toList());
        } else {
            // Get all trades in the date range
            trades = tradeDetailsService.findModelsByEntryDateBetween(startDateTime, endDateTime);
        }
        
        // Group trades by portfolio ID
        return trades.stream()
            .collect(Collectors.groupingBy(
                TradeDetails::getPortfolioId,
                Collectors.toList()
            ));
    }

    @Override
    public Page<TradeDetails> getTradeDetailsByPortfolio(String portfolioId, Pageable pageable) {
        return tradeDetailsService.findModelsByPortfolioId(portfolioId, pageable);
    }

    @Override
    public TradeSummary getTradeSummary(String portfolioId, LocalDate startDate, LocalDate endDate) {
        // Get all trades for the portfolio in the date range
        Map<String, List<TradeDetails>> tradesByPortfolio = getTradeDetailsByDateRange(startDate, endDate, portfolioId);
        
        // If no trades found or portfolio doesn't exist
        if (!tradesByPortfolio.containsKey(portfolioId)) {
            return TradeSummary.builder()
                .portfolioId(portfolioId)
                .startDate(startDate)
                .endDate(endDate)
                .totalTrades(0)
                .build();
        }
        
        List<TradeDetails> trades = tradesByPortfolio.get(portfolioId);
        
        // Initialize counters and accumulators
        int totalTrades = trades.size();
        int winningTrades = 0;
        int losingTrades = 0;
        int breakEvenTrades = 0;
        int openPositions = 0;
        
        BigDecimal totalProfit = BigDecimal.ZERO;
        BigDecimal totalLoss = BigDecimal.ZERO;
        BigDecimal largestWin = BigDecimal.ZERO;
        BigDecimal largestLoss = BigDecimal.ZERO;
        
        double totalWinningDays = 0;
        double totalLosingDays = 0;
        
        // Process each trade to calculate summary metrics
        for (TradeDetails trade : trades) {
            // Count trades by status
            switch (trade.getStatus()) {
                case WIN:
                    winningTrades++;
                    if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                        BigDecimal profit = trade.getMetrics().getProfitLoss();
                        totalProfit = totalProfit.add(profit);
                        
                        // Track largest win
                        if (profit.compareTo(largestWin) > 0) {
                            largestWin = profit;
                        }
                        
                        // Track holding time for winning trades
                        if (trade.getEntryInfo() != null && trade.getExitInfo() != null) {
                            LocalDateTime entryDate = trade.getEntryInfo().getTimestamp();
                            LocalDateTime exitDate = trade.getExitInfo().getTimestamp();
                            if (entryDate != null && exitDate != null) {
                                totalWinningDays += ChronoUnit.DAYS.between(entryDate, exitDate);
                            }
                        }
                    }
                    break;
                case LOSS:
                    losingTrades++;
                    if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                        BigDecimal loss = trade.getMetrics().getProfitLoss();
                        totalLoss = totalLoss.add(loss);
                        
                        // Track largest loss (loss is negative, so we want the most negative)
                        if (loss.compareTo(largestLoss) < 0) {
                            largestLoss = loss;
                        }
                        
                        // Track holding time for losing trades
                        if (trade.getEntryInfo() != null && trade.getExitInfo() != null) {
                            LocalDateTime entryDate = trade.getEntryInfo().getTimestamp();
                            LocalDateTime exitDate = trade.getExitInfo().getTimestamp();
                            if (entryDate != null && exitDate != null) {
                                totalLosingDays += ChronoUnit.DAYS.between(entryDate, exitDate);
                            }
                        }
                    }
                    break;
                case BREAK_EVEN:
                    breakEvenTrades++;
                    break;
                case OPEN:
                    openPositions++;
                    break;
            }
        }
        
        // Calculate derived metrics
        BigDecimal netProfitLoss = totalProfit.add(totalLoss);
        
        // Win rate (percentage of winning trades)
        BigDecimal winRate = totalTrades > 0 ? 
            new BigDecimal(winningTrades).multiply(new BigDecimal(100))
                .divide(new BigDecimal(totalTrades), DECIMAL_SCALE, ROUNDING_MODE) : 
            BigDecimal.ZERO;
        
        // Profit factor (total profit / total loss)
        BigDecimal profitFactor = totalLoss.compareTo(BigDecimal.ZERO) != 0 ? 
            totalProfit.abs().divide(totalLoss.abs(), DECIMAL_SCALE, ROUNDING_MODE) : 
            BigDecimal.ZERO;
        
        // Average win and loss
        BigDecimal averageWin = winningTrades > 0 ? 
            totalProfit.divide(new BigDecimal(winningTrades), DECIMAL_SCALE, ROUNDING_MODE) : 
            BigDecimal.ZERO;
        
        BigDecimal averageLoss = losingTrades > 0 ? 
            totalLoss.divide(new BigDecimal(losingTrades), DECIMAL_SCALE, ROUNDING_MODE) : 
            BigDecimal.ZERO;
        
        // Average holding times
        double averageHoldingTimeDays = totalTrades > 0 ? 
            (totalWinningDays + totalLosingDays) / totalTrades : 0;
        
        double averageWinningTradeDurationDays = winningTrades > 0 ? 
            totalWinningDays / winningTrades : 0;
        
        double averageLosingTradeDurationDays = losingTrades > 0 ? 
            totalLosingDays / losingTrades : 0;
        
        // Build and return the summary
        return TradeSummary.builder()
            .portfolioId(portfolioId)
            .startDate(startDate)
            .endDate(endDate)
            .totalTrades(totalTrades)
            .winningTrades(winningTrades)
            .losingTrades(losingTrades)
            .breakEvenTrades(breakEvenTrades)
            .openPositions(openPositions)
            .totalProfit(totalProfit)
            .totalLoss(totalLoss)
            .netProfitLoss(netProfitLoss)
            .winRate(winRate)
            .profitFactor(profitFactor)
            .averageWin(averageWin)
            .averageLoss(averageLoss)
            .largestWin(largestWin)
            .largestLoss(largestLoss)
            .averageHoldingTimeDays(averageHoldingTimeDays)
            .averageWinningTradeDurationDays(averageWinningTradeDurationDays)
            .averageLosingTradeDurationDays(averageLosingTradeDurationDays)
            .build();
    }
}
