package am.trade.api.service.impl;

import am.trade.api.service.PortfolioSummaryService;
import am.trade.common.models.PortfolioModel;
import am.trade.common.models.AssetAllocation;
import am.trade.common.models.TradeDetails;
import am.trade.services.service.PortfolioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the Portfolio Summary Service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PortfolioSummaryServiceImpl implements PortfolioSummaryService {

    private final PortfolioService portfolioService;

    @Override
    public PortfolioModel getPortfolioSummary(String portfolioId) {
        log.debug("Getting portfolio summary for portfolioId: {}", portfolioId);
        
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        // Get the complete portfolio with all trades and metrics
        Optional<PortfolioModel> portfolio = portfolioService.findByPortfolioId(portfolioId);
        
        if (portfolio.isEmpty()) {
            log.warn("Portfolio not found with ID: {}", portfolioId);
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }
        
        return portfolio.get();
    }

    @Override
    public List<AssetAllocation> getAssetAllocation(String portfolioId) {
        log.debug("Getting asset allocation for portfolioId: {}", portfolioId);
        
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        // Get the portfolio model which contains asset allocations
        Optional<PortfolioModel> portfolio = portfolioService.findByPortfolioId(portfolioId);
        
        if (portfolio.isEmpty()) {
            log.warn("Portfolio not found with ID: {}", portfolioId);
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }
        
        return portfolio.get().getAssetAllocations();
    }

    @Override
    public Map<LocalDate, Double> getPortfolioPerformance(String portfolioId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting portfolio performance for portfolioId: {} from {} to {}", portfolioId, startDate, endDate);
        
        if (portfolioId == null || portfolioId.trim().isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }
        
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Start date cannot be after end date");
        }
        
        // Get the portfolio with all trades
        Optional<PortfolioModel> portfolio = portfolioService.findByPortfolioId(portfolioId);
        
        if (portfolio.isEmpty()) {
            log.warn("Portfolio not found with ID: {}", portfolioId);
            throw new IllegalArgumentException("Portfolio not found with ID: " + portfolioId);
        }
        
        // Calculate daily performance based on trade data
        // This is a simplified version - actual implementation would depend on specific performance calculation logic
        Map<LocalDate, Double> performance = new HashMap<>();
        
        // Get trades in the date range
        List<TradeDetails> relevantTrades = portfolio.get().getTrades().stream()
            .filter(trade -> {
                LocalDate tradeDate = trade.getTradeDate();
                return !tradeDate.isBefore(startDate) && !tradeDate.isAfter(endDate);
            })
            .collect(Collectors.toList());
        
        // Group trades by date and calculate daily performance
        Map<LocalDate, List<TradeDetails>> tradesByDate = relevantTrades.stream()
            .collect(Collectors.groupingBy(TradeDetails::getTradeDate));
        
        // For each day in the range, calculate performance
        LocalDate currentDate = startDate;
        Double cumulativePerformance = 0.0;
        
        while (!currentDate.isAfter(endDate)) {
            // Add current day's profit/loss to cumulative performance
            List<TradeDetails> dailyTrades = tradesByDate.getOrDefault(currentDate, new ArrayList<>());
            Double dailyProfitLoss = dailyTrades.stream()
                .mapToDouble(trade -> trade.getMetrics().getProfitLoss().doubleValue())
                .sum();
            
            cumulativePerformance += dailyProfitLoss;
            performance.put(currentDate, cumulativePerformance);
            
            currentDate = currentDate.plusDays(1);
        }
        
        return performance;
    }

    @Override
    public Map<String, PortfolioModel> comparePortfolios(List<String> portfolioIds) {
        log.debug("Comparing portfolios: {}", portfolioIds);
        
        if (portfolioIds == null || portfolioIds.isEmpty()) {
            throw new IllegalArgumentException("Portfolio IDs list cannot be null or empty");
        }
        
        Map<String, PortfolioModel> portfolioMap = new HashMap<>();
        
        for (String portfolioId : portfolioIds) {
            try {
                PortfolioModel portfolio = getPortfolioSummary(portfolioId);
                portfolioMap.put(portfolioId, portfolio);
            } catch (Exception e) {
                log.warn("Error getting portfolio with ID: {}", portfolioId, e);
                // Skip portfolios that cannot be retrieved
            }
        }
        
        if (portfolioMap.isEmpty()) {
            throw new IllegalArgumentException("None of the requested portfolios could be found");
        }
        
        return portfolioMap;
    }
}
