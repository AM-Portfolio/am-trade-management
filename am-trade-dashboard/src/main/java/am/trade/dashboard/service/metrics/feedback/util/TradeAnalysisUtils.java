package am.trade.dashboard.service.metrics.feedback.util;

import am.trade.common.models.TradeDetails;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Utility class with common methods for trade analysis
 */
@UtilityClass
@Slf4j
public class TradeAnalysisUtils {
    
    /**
     * Check if a list of trades is profitable overall
     * 
     * @param trades List of trades to analyze
     * @return true if the trades are profitable overall, false otherwise
     */
    public boolean isProfitable(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return false;
        }
        
        double totalProfitLoss = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
            .sum();
            
        return totalProfitLoss > 0;
    }
    
    /**
     * Calculate win rate for a list of trades
     * 
     * @param trades List of trades to analyze
     * @return Win rate as a percentage (0-100)
     */
    public double calculateWinRate(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        long winCount = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
            .count();
            
        return Math.round((double) winCount / trades.size() * 100);
    }
    
    /**
     * Calculate average profit/loss per trade
     * 
     * @param trades List of trades to analyze
     * @return Average profit/loss amount
     */
    public double calculateAverageProfitLoss(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        return trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calculate average risk-reward ratio
     * 
     * @param trades List of trades to analyze
     * @return Average risk-reward ratio
     */
    public double calculateAverageRiskRewardRatio(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        return trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getRiskRewardRatio() != null)
            .mapToDouble(t -> t.getMetrics().getRiskRewardRatio().doubleValue())
            .average()
            .orElse(0.0);
    }
    
    /**
     * Calculate profit factor (gross profit / gross loss)
     * 
     * @param trades List of trades to analyze
     * @return Profit factor, or 0 if there are no trades
     */
    public double calculateProfitFactor(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        double grossProfit = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) > 0)
            .mapToDouble(t -> t.getMetrics().getProfitLoss().doubleValue())
            .sum();
            
        double grossLoss = trades.stream()
            .filter(t -> t.getMetrics() != null && t.getMetrics().getProfitLoss() != null)
            .filter(t -> t.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0)
            .mapToDouble(t -> Math.abs(t.getMetrics().getProfitLoss().doubleValue()))
            .sum();
            
        return grossLoss > 0 ? grossProfit / grossLoss : grossProfit > 0 ? Double.POSITIVE_INFINITY : 0.0;
    }
    
    /**
     * Calculate max drawdown from a list of trades
     * 
     * @param trades List of trades to analyze
     * @return Maximum drawdown as a percentage
     */
    public double calculateMaxDrawdown(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0.0;
        }
        
        double maxDrawdown = 0.0;
        double peak = 0.0;
        double balance = 0.0;
        
        // Sort trades chronologically
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                balance += trade.getMetrics().getProfitLoss().doubleValue();
                
                if (balance > peak) {
                    peak = balance;
                }
                
                double drawdown = peak > 0 ? (peak - balance) / peak * 100 : 0;
                if (drawdown > maxDrawdown) {
                    maxDrawdown = drawdown;
                }
            }
        }
        
        return maxDrawdown;
    }
    
    /**
     * Calculate maximum consecutive losses
     * 
     * @param trades List of trades to analyze
     * @return Maximum number of consecutive losing trades
     */
    public int calculateMaxConsecutiveLosses(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return 0;
        }
        
        // Sort trades chronologically
        List<TradeDetails> sortedTrades = new ArrayList<>(trades);
        sortedTrades.sort(Comparator.comparing(t -> t.getEntryInfo().getTimestamp()));
        
        int currentStreak = 0;
        int maxStreak = 0;
        
        for (TradeDetails trade : sortedTrades) {
            if (trade.getMetrics() != null && trade.getMetrics().getProfitLoss() != null) {
                if (trade.getMetrics().getProfitLoss().compareTo(BigDecimal.ZERO) < 0) {
                    // Loss
                    currentStreak++;
                    maxStreak = Math.max(maxStreak, currentStreak);
                } else {
                    // Win
                    currentStreak = 0;
                }
            }
        }
        
        return maxStreak;
    }
    
    /**
     * Group trades by a specific enum property
     * 
     * @param trades List of trades to group
     * @param propertyExtractor Function to extract the enum property from a trade
     * @param <T> The enum type
     * @return Map of enum values to lists of trades
     */
    public <T extends Enum<?>> Map<T, List<TradeDetails>> groupTradesByEnum(
            List<TradeDetails> trades, 
            java.util.function.Function<TradeDetails, T> propertyExtractor) {
        
        Map<T, List<TradeDetails>> result = new HashMap<>();
        
        for (TradeDetails trade : trades) {
            T value = propertyExtractor.apply(trade);
            if (value != null) {
                result.computeIfAbsent(value, k -> new ArrayList<>()).add(trade);
            }
        }
        
        return result;
    }
    
    /**
     * Format an enum value as a readable string
     * 
     * @param enumValue The enum value to format
     * @return Formatted string with spaces instead of underscores and proper capitalization
     */
    public String formatEnumValue(Enum<?> enumValue) {
        if (enumValue == null) {
            return "";
        }
        
        return capitalizeWords(enumValue.toString().toLowerCase().replace('_', ' '));
    }
    
    /**
     * Capitalize each word in a string
     * 
     * @param input The input string
     * @return String with each word capitalized
     */
    public String capitalizeWords(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        StringBuilder result = new StringBuilder();
        String[] words = input.split("\\s");
        
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        
        return result.toString().trim();
    }
}
