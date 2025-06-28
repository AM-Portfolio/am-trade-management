package am.trade.services.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import am.trade.common.models.ProfitLossHeatmapData;
import am.trade.common.models.TradeDetails;
import am.trade.common.models.enums.TradeStatus;
import am.trade.services.service.ProfitLossHeatmapService;
import am.trade.services.service.TradeManagementService;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ProfitLossHeatmapServiceImpl implements ProfitLossHeatmapService {

    private final TradeManagementService tradeManagementService;
    
    public ProfitLossHeatmapServiceImpl(TradeManagementService tradeManagementService) {
        this.tradeManagementService = tradeManagementService;
    }
    
    @Override
    public ProfitLossHeatmapData getYearlyHeatmap(String portfolioId, boolean includeTradeDetails) {
        log.info("Generating yearly profit/loss heatmap for portfolio: {}", portfolioId);
        
        // Get all trades for the portfolio
        List<TradeDetails> allTrades = tradeManagementService.getAllTradesByTradePortfolioId(portfolioId);
        
        // Group trades by year
        Map<Integer, List<TradeDetails>> tradesByYear = allTrades.stream()
                .filter(trade -> trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null)
                .collect(Collectors.groupingBy(
                        trade -> trade.getEntryInfo().getTimestamp().getYear()
                ));
        
        // Calculate profit/loss for each year
        Map<String, BigDecimal> profitLossMap = new HashMap<>();
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        
        for (Map.Entry<Integer, List<TradeDetails>> entry : tradesByYear.entrySet()) {
            BigDecimal yearlyProfitLoss = calculateTotalProfitLoss(entry.getValue());
            profitLossMap.put(String.valueOf(entry.getKey()), yearlyProfitLoss);
            totalProfitLoss = totalProfitLoss.add(yearlyProfitLoss);
            
            // Count wins and losses
            winCount += countTradesByStatus(entry.getValue(), TradeStatus.WIN);
            lossCount += countTradesByStatus(entry.getValue(), TradeStatus.LOSS);
        }
        
        // Calculate win rate
        BigDecimal winRate = calculateWinRate(winCount, lossCount);
        
        // If includeTradeDetails is true, separate win and loss trades
        List<TradeDetails> winningTrades = null;
        List<TradeDetails> losingTrades = null;
        
        if (includeTradeDetails) {
            winningTrades = allTrades.stream()
                    .filter(trade -> trade.getStatus() == TradeStatus.WIN)
                    .collect(Collectors.toList());
                    
            losingTrades = allTrades.stream()
                    .filter(trade -> trade.getStatus() == TradeStatus.LOSS)
                    .collect(Collectors.toList());
        }
        
        return ProfitLossHeatmapData.builder()
                .granularityType(ProfitLossHeatmapData.GranularityType.YEARLY)
                .profitLossMap(profitLossMap)
                .totalProfitLoss(totalProfitLoss)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(winRate)
                .tradeDetails(includeTradeDetails ? allTrades : null)
                .winTrades(includeTradeDetails ? winningTrades : null)
                .lossTrades(includeTradeDetails ? losingTrades : null)
                .build();
    }
    
    @Override
    public ProfitLossHeatmapData getMonthlyHeatmap(String portfolioId, int financialYear, boolean includeTradeDetails) {
        log.info("Generating monthly profit/loss heatmap for portfolio: {} for financial year: {}", portfolioId, financialYear);
        
        // Financial year in India runs from April to March
        // So FY 2025-26 means April 2025 to March 2026
        LocalDate startDate = LocalDate.of(financialYear, Month.APRIL, 1);
        LocalDate endDate = LocalDate.of(financialYear + 1, Month.MARCH, 31);
        
        // Get trades for the financial year
        List<TradeDetails> fyTrades = tradeManagementService.getTradesByDateRange(portfolioId, startDate, endDate);
        
        // Group trades by month
        Map<YearMonth, List<TradeDetails>> tradesByMonth = fyTrades.stream()
                .filter(trade -> trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null)
                .collect(Collectors.groupingBy(
                        trade -> YearMonth.from(trade.getEntryInfo().getTimestamp())
                ));
        
        // Calculate profit/loss for each month
        Map<String, BigDecimal> profitLossMap = new HashMap<>();
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        
        // Format for the map key: "YYYY-MM"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (Map.Entry<YearMonth, List<TradeDetails>> entry : tradesByMonth.entrySet()) {
            BigDecimal monthlyProfitLoss = calculateTotalProfitLoss(entry.getValue());
            profitLossMap.put(entry.getKey().format(formatter), monthlyProfitLoss);
            totalProfitLoss = totalProfitLoss.add(monthlyProfitLoss);
            
            // Count wins and losses
            winCount += countTradesByStatus(entry.getValue(), TradeStatus.WIN);
            lossCount += countTradesByStatus(entry.getValue(), TradeStatus.LOSS);
        }
        
        // Calculate win rate
        BigDecimal winRate = calculateWinRate(winCount, lossCount);
        
        // If includeTradeDetails is true, separate win and loss trades
        List<TradeDetails> winningTrades = null;
        List<TradeDetails> losingTrades = null;
        
        if (includeTradeDetails) {
            winningTrades = fyTrades.stream()
                    .filter(trade -> trade.getStatus() == TradeStatus.WIN)
                    .collect(Collectors.toList());
                    
            losingTrades = fyTrades.stream()
                    .filter(trade -> trade.getStatus() == TradeStatus.LOSS)
                    .collect(Collectors.toList());
        }
        
        return ProfitLossHeatmapData.builder()
                .granularityType(ProfitLossHeatmapData.GranularityType.MONTHLY)
                .profitLossMap(profitLossMap)
                .totalProfitLoss(totalProfitLoss)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(winRate)
                .tradeDetails(includeTradeDetails ? fyTrades : null)
                .winTrades(includeTradeDetails ? winningTrades : null)
                .lossTrades(includeTradeDetails ? losingTrades : null)
                .build();
    }
    
    @Override
    public ProfitLossHeatmapData getDailyHeatmap(String portfolioId, int year, int month, boolean includeTradeDetails) {
        log.info("Generating daily profit/loss heatmap for portfolio: {} for {}-{}", portfolioId, year, month);
        
        // Validate month
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        
        // Get start and end date for the month
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        // Get trades for the month
        List<TradeDetails> monthlyTrades = tradeManagementService.getTradesByDateRange(portfolioId, startDate, endDate);
        
        // Group trades by day
        Map<LocalDate, List<TradeDetails>> tradesByDay = monthlyTrades.stream()
                .filter(trade -> trade.getEntryInfo() != null && trade.getEntryInfo().getTimestamp() != null)
                .collect(Collectors.groupingBy(
                        trade -> trade.getEntryInfo().getTimestamp().toLocalDate()
                ));
        
        // Calculate profit/loss for each day
        Map<String, BigDecimal> profitLossMap = new HashMap<>();
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        
        // Format for the map key: "YYYY-MM-DD"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map.Entry<LocalDate, List<TradeDetails>> entry : tradesByDay.entrySet()) {
            BigDecimal dailyProfitLoss = calculateTotalProfitLoss(entry.getValue());
            profitLossMap.put(entry.getKey().format(formatter), dailyProfitLoss);
            totalProfitLoss = totalProfitLoss.add(dailyProfitLoss);
            
            // Count wins and losses
            winCount += countTradesByStatus(entry.getValue(), TradeStatus.WIN);
            lossCount += countTradesByStatus(entry.getValue(), TradeStatus.LOSS);
        }
        
        // Calculate win rate
        BigDecimal winRate = calculateWinRate(winCount, lossCount);
        
        // If includeTradeDetails is true, separate win and loss trades
        List<TradeDetails> winningTrades = null;
        List<TradeDetails> losingTrades = null;
        
        if (includeTradeDetails) {
            winningTrades = monthlyTrades.stream()
                    .filter(trade -> trade.getStatus() == TradeStatus.WIN)
                    .collect(Collectors.toList());
                    
            losingTrades = monthlyTrades.stream()
                    .filter(trade -> trade.getStatus() == TradeStatus.LOSS)
                    .collect(Collectors.toList());
        }
        
        return ProfitLossHeatmapData.builder()
                .granularityType(ProfitLossHeatmapData.GranularityType.DAILY)
                .profitLossMap(profitLossMap)
                .totalProfitLoss(totalProfitLoss)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(winRate)
                .tradeDetails(includeTradeDetails ? monthlyTrades : null)
                .winTrades(includeTradeDetails ? winningTrades : null)
                .lossTrades(includeTradeDetails ? losingTrades : null)
                .build();
    }
    
    /**
     * Calculate total profit/loss for a list of trades
     */
    private BigDecimal calculateTotalProfitLoss(List<TradeDetails> trades) {
        return trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null)
                .map(trade -> trade.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    /**
     * Count trades with a specific status
     */
    private int countTradesByStatus(List<TradeDetails> trades, TradeStatus status) {
        return (int) trades.stream()
                .filter(trade -> trade.getStatus() == status)
                .count();
    }
    
    /**
     * Calculate win rate percentage
     */
    private BigDecimal calculateWinRate(int winCount, int lossCount) {
        int totalTrades = winCount + lossCount;
        if (totalTrades == 0) {
            return BigDecimal.ZERO;
        }
        
        return new BigDecimal(winCount)
                .multiply(new BigDecimal(100))
                .divide(new BigDecimal(totalTrades), 2, RoundingMode.HALF_UP);
    }
}
