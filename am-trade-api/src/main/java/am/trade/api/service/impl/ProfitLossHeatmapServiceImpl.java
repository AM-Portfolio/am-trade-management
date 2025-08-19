package am.trade.api.service.impl;

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

import am.trade.common.models.enums.TradeStatus;
import am.trade.common.models.HeatmapRequest;
import am.trade.common.models.PeriodProfitLossData;
import am.trade.common.models.ProfitLossHeatmapData;
import am.trade.common.models.TradeDetails;
import am.trade.api.service.ProfitLossHeatmapService;
import am.trade.api.service.TradeManagementService;
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
        
        // Calculate detailed profit/loss data for each year
        List<PeriodProfitLossData> periodDataList = new ArrayList<>();
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        
        for (Map.Entry<Integer, List<TradeDetails>> entry : tradesByYear.entrySet()) {
            List<TradeDetails> yearTrades = entry.getValue();
            String periodId = String.valueOf(entry.getKey());
            
            // Create detailed period data
            PeriodProfitLossData periodData = createPeriodProfitLossData(periodId, yearTrades);
            periodDataList.add(periodData);
            
            // Update totals
            totalProfitLoss = totalProfitLoss.add(periodData.getProfitLoss());
            winCount += periodData.getWinCount();
            lossCount += periodData.getLossCount();
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
        
        ProfitLossHeatmapData heatmapData = ProfitLossHeatmapData.builder()
                .granularityType(ProfitLossHeatmapData.GranularityType.YEARLY)
                .periodData(periodDataList)
                .totalProfitLoss(totalProfitLoss)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(winRate)
                .tradeDetails(includeTradeDetails ? allTrades : null)
                .winTrades(includeTradeDetails ? winningTrades : null)
                .lossTrades(includeTradeDetails ? losingTrades : null)
                .build();
         // Calculate summary metrics from period data
         return heatmapData.calculateSummaryMetricsFromPeriods();
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
        
        // Calculate detailed profit/loss data for each month
        List<PeriodProfitLossData> periodDataList = new ArrayList<>();
        BigDecimal totalProfitLoss = BigDecimal.ZERO;
        int winCount = 0;
        int lossCount = 0;
        
        // Format for the period ID: "YYYY-MM"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
        
        for (Map.Entry<YearMonth, List<TradeDetails>> entry : tradesByMonth.entrySet()) {
            List<TradeDetails> monthTrades = entry.getValue();
            String periodId = entry.getKey().format(formatter);
            
            // Create detailed period data
            PeriodProfitLossData periodData = createPeriodProfitLossData(periodId, monthTrades);
            periodDataList.add(periodData);
            
            // Update totals
            totalProfitLoss = totalProfitLoss.add(periodData.getProfitLoss());
            winCount += periodData.getWinCount();
            lossCount += periodData.getLossCount();
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
        
        ProfitLossHeatmapData heatmapData = ProfitLossHeatmapData.builder()
                .granularityType(ProfitLossHeatmapData.GranularityType.MONTHLY)
                .periodData(periodDataList)
                .totalProfitLoss(totalProfitLoss)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(winRate)
                .tradeDetails(includeTradeDetails ? fyTrades : null)
                .winTrades(includeTradeDetails ? winningTrades : null)
                .lossTrades(includeTradeDetails ? losingTrades : null)
                .build();
        // Calculate summary metrics from period data
        return heatmapData.calculateSummaryMetricsFromPeriods();
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
        
        // Calculate detailed profit/loss data for each day
        List<PeriodProfitLossData> periodDataList = new ArrayList<>();
        
        // Format for the period ID: "YYYY-MM-DD"
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map.Entry<LocalDate, List<TradeDetails>> entry : tradesByDay.entrySet()) {
            List<TradeDetails> dayTrades = entry.getValue();
            String periodId = entry.getKey().format(formatter);
            
            // Create detailed period data
            PeriodProfitLossData periodData = createPeriodProfitLossData(periodId, dayTrades);
            periodDataList.add(periodData);
        }
        
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
        
        // Build the heatmap data with period data and trade details
        ProfitLossHeatmapData heatmapData = ProfitLossHeatmapData.builder()
                .granularityType(ProfitLossHeatmapData.GranularityType.DAILY)
                .periodData(periodDataList)
                .tradeDetails(includeTradeDetails ? monthlyTrades : null)
                .winTrades(includeTradeDetails ? winningTrades : null)
                .lossTrades(includeTradeDetails ? losingTrades : null)
                .build();
                
        // Calculate summary metrics from period data
        return heatmapData.calculateSummaryMetricsFromPeriods();
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
    
    /**
     * Create a PeriodProfitLossData object for a specific period
     */
    private PeriodProfitLossData createPeriodProfitLossData(String periodId, List<TradeDetails> trades) {
        // Calculate profit/loss
        BigDecimal profitLoss = calculateTotalProfitLoss(trades);
        
        // Count wins and losses
        int winCount = countTradesByStatus(trades, TradeStatus.WIN);
        int lossCount = countTradesByStatus(trades, TradeStatus.LOSS);
        
        // Calculate win rate
        BigDecimal winRate = calculateWinRate(winCount, lossCount);
        
        // Get winning and losing trades
        List<TradeDetails> winningTrades = trades.stream()
                .filter(trade -> trade.getStatus() == TradeStatus.WIN)
                .collect(Collectors.toList());
                
        List<TradeDetails> losingTrades = trades.stream()
                .filter(trade -> trade.getStatus() == TradeStatus.LOSS)
                .collect(Collectors.toList());
        
        // Calculate average win and loss amounts
        BigDecimal avgWinAmount = calculateAverageAmount(winningTrades);
        BigDecimal avgLossAmount = calculateAverageAmount(losingTrades);
        
        // Calculate max win and loss amounts
        BigDecimal maxWinAmount = calculateMaxAmount(winningTrades);
        BigDecimal maxLossAmount = calculateMaxAmount(losingTrades);
        
        return PeriodProfitLossData.builder()
                .periodId(periodId)
                .profitLoss(profitLoss)
                .winCount(winCount)
                .lossCount(lossCount)
                .winRate(winRate)
                .avgWinAmount(avgWinAmount)
                .avgLossAmount(avgLossAmount)
                .maxWinAmount(maxWinAmount)
                .maxLossAmount(maxLossAmount)
                .build();
    }
    
    /**
     * Calculate average profit/loss amount for a list of trades
     */
    private BigDecimal calculateAverageAmount(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal total = calculateTotalProfitLoss(trades);
        return total.divide(new BigDecimal(trades.size()), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate maximum profit/loss amount for a list of trades
     */
    private BigDecimal calculateMaxAmount(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        return trades.stream()
                .filter(trade -> trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null)
                .map(trade -> trade.getMetrics().getProfitLoss().abs())
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    
    @Override
    public ProfitLossHeatmapData getHeatmapData(HeatmapRequest request) {
        log.info("Generating heatmap with granularity: {} for portfolios: {}", request.getGranularity(), request.getPortfolioIds());
        
        if (request.getPortfolioIds() == null || request.getPortfolioIds().isEmpty()) {
            throw new IllegalArgumentException("No portfolio IDs provided");
        }
        
        // For now, we'll use the first portfolio ID for backward compatibility
        // In the future, this method should be enhanced to aggregate data from multiple portfolios
        String portfolioId = request.getPortfolioIds().get(0);
        
        switch (request.getGranularity()) {
            case YEARLY:
                return getYearlyHeatmap(portfolioId, request.isIncludeTradeDetails());
                
            case MONTHLY:
                if (request.getFinancialYear() == null) {
                    throw new IllegalArgumentException("Financial year is required for MONTHLY granularity");
                }
                return getMonthlyHeatmap(portfolioId, request.getFinancialYear(), request.isIncludeTradeDetails());
                
            case DAILY:
                if (request.getFinancialYear() == null || request.getMonth() == null) {
                    throw new IllegalArgumentException("Financial year and month are required for DAILY granularity");
                }
                return getDailyHeatmap(portfolioId, request.getFinancialYear(), request.getMonth(), request.isIncludeTradeDetails());
                
            default:
                throw new IllegalArgumentException("Invalid granularity: " + request.getGranularity());
        }
    }
}
