package am.trade.api.service.impl;

import am.trade.common.models.TradeDetails;
import am.trade.common.models.TradeSummary;
import am.trade.models.enums.TradeStatus;
import am.trade.services.service.TradeDetailsService;
import am.trade.api.service.TradeManagementService;
import am.trade.api.client.MarketDataApiClient;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

import am.trade.common.logger.AppLogger;
import am.trade.persistence.repository.PortfolioRepository;
import am.trade.persistence.entity.PortfolioEntity;
import am.trade.persistence.entity.TradeDetailsEntity;
import am.trade.persistence.mapper.TradeDetailsMapper;
import java.util.Optional;
import java.util.ArrayList;

/**
 * Implementation of TradeManagementService that provides calendar-based trade
 * analytics
 */

@Service
@RequiredArgsConstructor
public class TradeManagementServiceImpl implements TradeManagementService {

    private static final int DECIMAL_SCALE = 4;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final TradeDetailsService tradeDetailsService;
    private final PortfolioRepository portfolioRepository;
    private final TradeDetailsMapper tradeDetailsMapper;
    private final AppLogger log;
    private final MarketDataApiClient marketDataApiClient;

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
    public Map<String, List<TradeDetails>> getTradeDetailsByDateRange(LocalDate startDate, LocalDate endDate,
            String portfolioId) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        return getTradeDetailsByDateTimeRange(startDateTime, endDateTime, portfolioId);
    }

    private Map<String, List<TradeDetails>> getTradeDetailsByDateTimeRange(LocalDateTime startDateTime,
            LocalDateTime endDateTime, String portfolioId) {
        List<TradeDetails> trades;

        log.info("Filtering trades between {} and {}", startDateTime, endDateTime);

        // If portfolio ID is provided, filter by it
        if (portfolioId != null && !portfolioId.isEmpty()) {
            trades = tradeDetailsService.findByPortfolioIdAndEntryInfoTimestampBetween(portfolioId, startDateTime, endDateTime);
        } else {
            trades = tradeDetailsService.findModelsByEntryDateBetween(startDateTime, endDateTime);
        }

        log.info("Retained {} trades after date filtering", trades.size());
        
        enrichWithLivePrices(trades);

        // Group trades by portfolio ID
        return trades.stream()
                .collect(Collectors.groupingBy(
                        TradeDetails::getPortfolioId,
                        Collectors.toList()));
    }

    @Override
    public Page<TradeDetails> getTradeDetailsByPortfolio(String portfolioId, Pageable pageable) {
        Page<TradeDetails> page = tradeDetailsService.findModelsByPortfolioId(portfolioId, pageable);
        enrichWithLivePrices(page.getContent());
        return page;
    }

    @Override
    public List<TradeDetails> getAllTradesByTradePortfolioId(String portfolioId) {
        List<TradeDetails> trades = tradeDetailsService.findModelsByPortfolioId(portfolioId);
        enrichWithLivePrices(trades);
        return trades;
    }

    @Override
    public List<TradeDetails> getTradesByDateRange(String portfolioId, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay().minusNanos(1);

        List<TradeDetails> filteredTrades = tradeDetailsService.findByPortfolioIdAndEntryInfoTimestampBetween(portfolioId, startDateTime, endDateTime);
                
        enrichWithLivePrices(filteredTrades);
        return filteredTrades;
    }

    @Override
    public List<TradeDetails> getTradesBySymbols(String portfolioId, List<String> symbols) {
        log.info("Fetching trades for portfolio: {} filtered by symbols: {}", portfolioId, symbols);

        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }

        if (symbols == null || symbols.isEmpty()) {
            // If no symbols provided, return all trades for the portfolio
            return getAllTradesByTradePortfolioId(portfolioId);
        }

        // Convert input symbols to upper case for case-insensitive matching in MongoDB
        List<String> upperCaseSymbols = symbols.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        // Use database-side filtering
        List<TradeDetails> filteredTrades = tradeDetailsService.findModelsByPortfolioIdAndSymbolInIgnoreCase(portfolioId, upperCaseSymbols);
                
        enrichWithLivePrices(filteredTrades);
        return filteredTrades;
    }

    @Override
    public Page<TradeDetails> getTradesBySymbolsPage(String portfolioId, List<String> symbols, Pageable pageable) {
        log.info("Fetching paginated trades for portfolio: {} filtered by symbols: {}", portfolioId, symbols);

        if (portfolioId == null || portfolioId.isEmpty()) {
            throw new IllegalArgumentException("Portfolio ID cannot be null or empty");
        }

        if (symbols == null || symbols.isEmpty()) {
            return getTradeDetailsByPortfolio(portfolioId, pageable);
        }

        List<String> upperCaseSymbols = symbols.stream()
                .map(String::toUpperCase)
                .collect(Collectors.toList());

        Page<TradeDetails> page = tradeDetailsService.findModelsByPortfolioIdAndSymbolInIgnoreCase(portfolioId, upperCaseSymbols, pageable);
        enrichWithLivePrices(page.getContent());
        return page;
    }

    @Override
    public Page<TradeDetails> getTradesByFilters(
            List<String> portfolioIds,
            List<String> symbols,
            List<TradeStatus> statuses,
            LocalDate startDate,
            LocalDate endDate,
            List<String> strategies,
            Pageable pageable) {

        log.info(
                "Fetching trades with filters - portfolioIds: {}, symbols: {}, statuses: {}, startDate: {}, endDate: {}, strategies: {}",
                portfolioIds, symbols, statuses, startDate, endDate, strategies);

        if (portfolioIds == null || portfolioIds.isEmpty()) {
            throw new IllegalArgumentException("At least one portfolio ID must be provided");
        }

        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay().minusNanos(1) : null;

        Page<TradeDetails> pagedTrades = tradeDetailsService.findByFilters(
                portfolioIds, symbols, statuses, strategies, startDateTime, endDateTime, pageable);

        List<TradeDetails> content = pagedTrades.getContent();
        if (!content.isEmpty()) {
            enrichWithLivePrices(content);
        }

        return pagedTrades;
    }
    
    private void enrichWithLivePrices(List<TradeDetails> trades) {
        if (trades == null || trades.isEmpty()) {
            return;
        }

        List<TradeDetails> openTrades = trades.stream()
                .filter(t -> TradeStatus.OPEN.equals(t.getStatus()) && t.getSymbol() != null)
                .collect(Collectors.toList());

        if (openTrades.isEmpty()) {
            return;
        }

        List<String> symbols = openTrades.stream()
                .map(trade -> trade.getSymbol() != null ? trade.getSymbol().trim() : null)
                .filter(s -> s != null && !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());

        try {
            Map<String, Double> livePrices = marketDataApiClient.getCurrentPrices(symbols);
            log.info("Live prices received from API: {}", livePrices);
            
            if (livePrices != null && !livePrices.isEmpty()) {
                openTrades.forEach(trade -> {
                    String cleanSymbol = trade.getSymbol() != null ? trade.getSymbol().trim() : null;
                    if (cleanSymbol != null && cleanSymbol.contains(":")) {
                        cleanSymbol = cleanSymbol.substring(cleanSymbol.lastIndexOf(":") + 1);
                    }
                    Double price = cleanSymbol != null ? livePrices.get(cleanSymbol) : null;
                    if (price == null && trade.getSymbol() != null) {
                        // Fallback in case MarketDataApiClient didn't strip it
                        price = livePrices.get(trade.getSymbol().trim());
                    }
                    if (price != null) {
                        trade.setCurrentPrice(java.math.BigDecimal.valueOf(price));
                        
                        // Default to LONG if tradePositionType is null
                        if (trade.getTradePositionType() == null) {
                            trade.setTradePositionType(am.trade.models.enums.TradePositionType.LONG);
                        }
                        
                        // Recalculate profit/loss with live price
                        java.math.BigDecimal entryPrice = trade.getEntryInfo() != null ? trade.getEntryInfo().getPrice() : null;
                        if (entryPrice != null && trade.getEntryInfo().getQuantity() != null) {
                            java.math.BigDecimal currentPrc = trade.getCurrentPrice();
                            java.math.BigDecimal profitLossPerUnit = java.math.BigDecimal.ZERO;
                            
                            if (am.trade.models.enums.TradePositionType.LONG.equals(trade.getTradePositionType())) {
                                profitLossPerUnit = currentPrc.subtract(entryPrice);
                            } else if (am.trade.models.enums.TradePositionType.SHORT.equals(trade.getTradePositionType())) {
                                profitLossPerUnit = entryPrice.subtract(currentPrc);
                            }
                            
                            java.math.BigDecimal profitLoss = profitLossPerUnit.multiply(new java.math.BigDecimal(trade.getEntryInfo().getQuantity()));
                            
                            if (trade.getMetrics() == null) {
                                trade.setMetrics(new am.trade.common.models.TradeMetrics());
                            }
                            trade.getMetrics().setProfitLoss(profitLoss);
                            
                            if (entryPrice.compareTo(java.math.BigDecimal.ZERO) > 0) {
                                java.math.BigDecimal percentage = profitLossPerUnit
                                        .divide(entryPrice, 4, java.math.RoundingMode.HALF_UP)
                                        .multiply(new java.math.BigDecimal("100"));
                                trade.getMetrics().setProfitLossPercentage(percentage);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            log.error("Error enriching trades with live prices", e);
        }
    }

}
