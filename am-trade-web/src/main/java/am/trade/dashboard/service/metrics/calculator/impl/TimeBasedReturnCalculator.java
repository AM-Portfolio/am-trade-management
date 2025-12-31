package am.trade.dashboard.service.metrics.calculator.impl;

import am.trade.common.models.PerformanceMetrics;
import am.trade.common.models.TradeDetails;
import am.trade.dashboard.service.metrics.calculator.MetricsCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced calculator for time-based return metrics (monthly, quarterly, annual)
 * This calculator computes multiple metrics at once and applies them directly to the metrics object.
 */
@Component
@Slf4j
public class TimeBasedReturnCalculator implements MetricsCalculator {

    private static final int SCALE = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    @Override
    public BigDecimal calculate(List<TradeDetails> trades) {
        // This method returns the annualized return as the primary metric
        // Other metrics are calculated in calculateTimeBasedReturns
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Filter trades with valid timestamps and sort chronologically
        List<TradeDetails> validTrades = trades.stream()
                .filter(trade -> trade.getEntryInfo() != null && 
                                trade.getEntryInfo().getTimestamp() != null &&
                                trade.getMetrics() != null && 
                                trade.getMetrics().getProfitLoss() != null)
                .sorted(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()))
                .collect(Collectors.toList());
                
        if (validTrades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        // Get first and last trade dates
        LocalDateTime firstTradeDate = validTrades.get(0).getEntryInfo().getTimestamp();
        LocalDateTime lastTradeDate = validTrades.get(validTrades.size() - 1).getEntryInfo().getTimestamp();
        
        // Calculate total profit/loss
        BigDecimal totalProfitLoss = validTrades.stream()
                .map(trade -> trade.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calculate trading period in years
        double tradingYears = Math.max(0.1, ChronoUnit.DAYS.between(firstTradeDate, lastTradeDate) / 365.0);
        
        // Calculate annualized return (simple method)
        return BigDecimal.valueOf(Math.pow(1 + totalProfitLoss.doubleValue(), 1 / tradingYears) - 1)
                .multiply(new BigDecimal("100"))
                .setScale(SCALE, ROUNDING_MODE);
    }
    
    /**
     * Calculate time-based returns and apply them to the metrics object
     * 
     * @param trades List of trade details
     * @param metrics PerformanceMetrics object to update
     */
    public void calculateTimeBasedReturns(List<TradeDetails> trades, PerformanceMetrics metrics) {
        if (trades == null || trades.isEmpty() || metrics == null) {
            return;
        }
        
        try {
            // Filter and sort trades
            List<TradeDetails> validTrades = trades.stream()
                    .filter(trade -> trade.getEntryInfo() != null && 
                                    trade.getEntryInfo().getTimestamp() != null &&
                                    trade.getMetrics() != null && 
                                    trade.getMetrics().getProfitLoss() != null)
                    .sorted(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()))
                    .collect(Collectors.toList());
                    
            if (validTrades.isEmpty()) {
                return;
            }
            
            // Group trades by time periods
            Map<Integer, List<TradeDetails>> tradesByYear = new HashMap<>();
            Map<Integer, List<TradeDetails>> tradesByQuarter = new HashMap<>();
            Map<Integer, List<TradeDetails>> tradesByMonth = new HashMap<>();
            
            for (TradeDetails trade : validTrades) {
                LocalDateTime timestamp = trade.getEntryInfo().getTimestamp();
                int year = timestamp.getYear();
                int quarter = (timestamp.getMonthValue() - 1) / 3 + 1;
                int month = timestamp.getMonthValue();
                
                // Group by year
                tradesByYear.computeIfAbsent(year, k -> new ArrayList<>()).add(trade);
                
                // Group by quarter (year * 10 + quarter gives a unique key)
                int quarterKey = year * 10 + quarter;
                tradesByQuarter.computeIfAbsent(quarterKey, k -> new ArrayList<>()).add(trade);
                
                // Group by month (year * 100 + month gives a unique key)
                int monthKey = year * 100 + month;
                tradesByMonth.computeIfAbsent(monthKey, k -> new ArrayList<>()).add(trade);
            }
            
            // Calculate year-to-date return
            int currentYear = LocalDate.now().getYear();
            List<TradeDetails> ytdTrades = tradesByYear.get(currentYear);
            if (ytdTrades != null && !ytdTrades.isEmpty()) {
                BigDecimal ytdReturn = calculateReturnForTrades(ytdTrades);
                metrics.setYearToDateReturn(ytdReturn);
            }
            
            // Calculate quarterly return (most recent quarter)
            int currentQuarter = (LocalDate.now().getMonthValue() - 1) / 3 + 1;
            int currentQuarterKey = currentYear * 10 + currentQuarter;
            List<TradeDetails> quarterTrades = tradesByQuarter.get(currentQuarterKey);
            if (quarterTrades != null && !quarterTrades.isEmpty()) {
                BigDecimal quarterlyReturn = calculateReturnForTrades(quarterTrades);
                metrics.setQuarterlyReturn(quarterlyReturn);
            }
            
            // Calculate monthly return (most recent month)
            int currentMonth = LocalDate.now().getMonthValue();
            int currentMonthKey = currentYear * 100 + currentMonth;
            List<TradeDetails> monthTrades = tradesByMonth.get(currentMonthKey);
            if (monthTrades != null && !monthTrades.isEmpty()) {
                BigDecimal monthlyReturn = calculateReturnForTrades(monthTrades);
                metrics.setMonthlyReturn(monthlyReturn);
            }
            
            // Calculate annualized return
            BigDecimal annualizedReturn = calculate(validTrades);
            metrics.setAnnualizedReturn(annualizedReturn);
            
        } catch (Exception e) {
            log.error("Error calculating time-based returns", e);
        }
    }
    
    /**
     * Calculate return percentage for a list of trades
     * 
     * @param trades List of trades
     * @return Return percentage
     */
    private BigDecimal calculateReturnForTrades(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalProfitLoss = trades.stream()
                .map(trade -> trade.getMetrics().getProfitLoss())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        BigDecimal totalInvested = trades.stream()
                .filter(trade -> trade.getEntryInfo() != null && 
                               trade.getEntryInfo().getTotalValue() != null)
                .map(trade -> trade.getEntryInfo().getTotalValue())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        if (totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            return totalProfitLoss.divide(totalInvested, SCALE, ROUNDING_MODE)
                    .multiply(new BigDecimal("100"));
        }
        
        return BigDecimal.ZERO;
    }

    @Override
    public String getMetricName() {
        return "annualizedReturn";
    }
}
