package am.trade.dashboard.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import am.trade.dashboard.model.TradeMetrics;
import am.trade.dashboard.service.TradeMetricsService;
import am.trade.models.dto.TradeDTO;
import am.trade.persistence.repository.TradeRepository;
import am.trade.services.service.TradeService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Implementation of TradeMetricsService
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TradeMetricsServiceImpl implements TradeMetricsService {
    
    private final TradeService tradeService;
    private final TradeRepository tradeRepository;
    
    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public TradeMetrics getTradeMetrics(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating trade metrics from {} to {}", startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        return calculateMetrics(trades);
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public TradeMetrics getPortfolioMetrics(String portfolioId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating trade metrics for portfolio {} from {} to {}", portfolioId, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByPortfolioIdAndTradeDateBetween(portfolioId, startDate, endDate, null)
                .getContent();
        return calculateMetrics(trades);
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public TradeMetrics getTraderMetrics(String traderId, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating trade metrics for trader {} from {} to {}", traderId, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByTraderId(traderId).stream()
                .filter(trade -> trade.getTradeDate().isAfter(startDate) && trade.getTradeDate().isBefore(endDate))
                .collect(Collectors.toList());
        return calculateMetrics(trades);
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public TradeMetrics getSymbolMetrics(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Calculating trade metrics for symbol {} from {} to {}", symbol, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findBySymbolAndTradeDateBetween(symbol, startDate, endDate);
        return calculateMetrics(trades);
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public Map<LocalDateTime, Long> getTradeVolumeTrend(LocalDateTime startDate, LocalDateTime endDate, String interval) {
        log.info("Calculating trade volume trend from {} to {} with interval {}", startDate, endDate, interval);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        Map<LocalDateTime, Long> volumeTrend = new HashMap<>();
        
        // Group trades by interval
        trades.forEach(trade -> {
            LocalDateTime bucketTime = truncateToInterval(trade.getTradeDate(), interval);
            volumeTrend.merge(bucketTime, 1L, Long::sum);
        });
        
        return volumeTrend;
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public Map<LocalDateTime, Double> getTradeValueTrend(LocalDateTime startDate, LocalDateTime endDate, String interval) {
        log.info("Calculating trade value trend from {} to {} with interval {}", startDate, endDate, interval);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        Map<LocalDateTime, Double> valueTrend = new HashMap<>();
        
        // Group trades by interval
        trades.forEach(trade -> {
            LocalDateTime bucketTime = truncateToInterval(trade.getTradeDate(), interval);
            valueTrend.merge(bucketTime, trade.getTotalValue().doubleValue(), Double::sum);
        });
        
        return valueTrend;
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public List<Map.Entry<String, Long>> getTopTradedSymbolsByVolume(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Finding top {} traded symbols by volume from {} to {}", limit, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        
        // Group by symbol and count
        Map<String, Long> symbolCounts = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getSymbol, Collectors.counting()));
        
        // Sort and limit
        return symbolCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public List<Map.Entry<String, Double>> getTopTradedSymbolsByValue(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Finding top {} traded symbols by value from {} to {}", limit, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        
        // Group by symbol and sum values
        Map<String, Double> symbolValues = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getSymbol, 
                        Collectors.summingDouble(trade -> trade.getTotalValue().doubleValue())));
        
        // Sort and limit
        return symbolValues.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public List<Map.Entry<String, Double>> getTopPerformingTraders(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Finding top {} performing traders from {} to {}", limit, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        
        // Group by trader and sum values
        Map<String, Double> traderValues = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getTraderId, 
                        Collectors.summingDouble(trade -> trade.getTotalValue().doubleValue())));
        
        // Sort and limit
        return traderValues.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public List<Map.Entry<String, Double>> getTopPerformingPortfolios(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Finding top {} performing portfolios from {} to {}", limit, startDate, endDate);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startDate, endDate);
        
        // Group by portfolio and sum values
        Map<String, Double> portfolioValues = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getPortfolioId, 
                        Collectors.summingDouble(trade -> trade.getTotalValue().doubleValue())));
        
        // Sort and limit
        return portfolioValues.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public byte[] generateDailyTradeSummaryReport(LocalDateTime date) {
        log.info("Generating daily trade summary report for {}", date);
        
        LocalDateTime startOfDay = date.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startOfDay, endOfDay);
        TradeMetrics metrics = calculateMetrics(trades);
        
        return generateExcelReport(metrics, "Daily Trade Summary - " + date.format(DateTimeFormatter.ISO_DATE));
    }

    @Override
    @CircuitBreaker(name = "dashboardMetrics")
    @Retry(name = "dashboardMetrics")
    public byte[] generateMonthlyTradeSummaryReport(int year, int month) {
        log.info("Generating monthly trade summary report for {}-{}", year, month);
        
        LocalDateTime startOfMonth = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1).minusNanos(1);
        
        List<TradeDTO> trades = tradeService.findByTradeDateBetween(startOfMonth, endOfMonth);
        TradeMetrics metrics = calculateMetrics(trades);
        
        return generateExcelReport(metrics, "Monthly Trade Summary - " + year + "-" + month);
    }
    
    /**
     * Calculate metrics from a list of trades
     */
    private TradeMetrics calculateMetrics(List<TradeDTO> trades) {
        if (trades == null || trades.isEmpty()) {
            return TradeMetrics.builder()
                    .timestamp(LocalDateTime.now())
                    .totalTradeCount(0)
                    .totalTradeValue(BigDecimal.ZERO)
                    .build();
        }
        
        // Calculate basic metrics
        long tradeCount = trades.size();
        BigDecimal totalValue = trades.stream()
                .map(TradeDTO::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal avgTradeSize = tradeCount > 0 
                ? totalValue.divide(BigDecimal.valueOf(tradeCount), 2, RoundingMode.HALF_UP) 
                : BigDecimal.ZERO;
        
        BigDecimal largestTrade = trades.stream()
                .map(TradeDTO::getTotalValue)
                .max(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        BigDecimal smallestTrade = trades.stream()
                .map(TradeDTO::getTotalValue)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        // Group by type
        Map<String, Long> countByType = trades.stream()
                .collect(Collectors.groupingBy(t -> t.getType().toString(), Collectors.counting()));
        
        Map<String, BigDecimal> valueByType = trades.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getType().toString(),
                        Collectors.reducing(BigDecimal.ZERO, TradeDTO::getTotalValue, BigDecimal::add)));
        
        // Group by symbol
        Map<String, Long> countBySymbol = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getSymbol, Collectors.counting()));
        
        Map<String, BigDecimal> valueBySymbol = trades.stream()
                .collect(Collectors.groupingBy(
                        TradeDTO::getSymbol,
                        Collectors.reducing(BigDecimal.ZERO, TradeDTO::getTotalValue, BigDecimal::add)));
        
        String mostTradedSymbol = countBySymbol.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        // Group by portfolio
        Map<String, Long> countByPortfolio = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getPortfolioId, Collectors.counting()));
        
        Map<String, BigDecimal> valueByPortfolio = trades.stream()
                .collect(Collectors.groupingBy(
                        TradeDTO::getPortfolioId,
                        Collectors.reducing(BigDecimal.ZERO, TradeDTO::getTotalValue, BigDecimal::add)));
        
        String topPortfolio = valueByPortfolio.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        // Group by trader
        Map<String, Long> countByTrader = trades.stream()
                .collect(Collectors.groupingBy(TradeDTO::getTraderId, Collectors.counting()));
        
        Map<String, BigDecimal> valueByTrader = trades.stream()
                .collect(Collectors.groupingBy(
                        TradeDTO::getTraderId,
                        Collectors.reducing(BigDecimal.ZERO, TradeDTO::getTotalValue, BigDecimal::add)));
        
        String mostActiveTrader = countByTrader.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        // Group by hour
        Map<String, Long> countByHour = trades.stream()
                .collect(Collectors.groupingBy(
                        t -> String.format("%02d:00", t.getTradeDate().getHour()),
                        Collectors.counting()));
        
        Map<String, BigDecimal> valueByHour = trades.stream()
                .collect(Collectors.groupingBy(
                        t -> String.format("%02d:00", t.getTradeDate().getHour()),
                        Collectors.reducing(BigDecimal.ZERO, TradeDTO::getTotalValue, BigDecimal::add)));
        
        String peakHour = countByHour.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");
        
        // Calculate fee metrics
        BigDecimal totalCommissions = trades.stream()
                .map(TradeDTO::getCommissionFee)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalFees = trades.stream()
                .map(TradeDTO::getOtherFees)
                .filter(fee -> fee != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal netValue = totalValue.subtract(totalCommissions).subtract(totalFees);
        
        BigDecimal profitLossPercentage = totalValue.compareTo(BigDecimal.ZERO) > 0
                ? netValue.divide(totalValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;
        
        return TradeMetrics.builder()
                .timestamp(LocalDateTime.now())
                .totalTradeCount(tradeCount)
                .totalTradeValue(totalValue)
                .averageTradeSize(avgTradeSize)
                .largestTradeValue(largestTrade)
                .smallestTradeValue(smallestTrade)
                .tradeCountByType(countByType)
                .tradeValueByType(valueByType)
                .tradeCountBySymbol(countBySymbol)
                .tradeValueBySymbol(valueBySymbol)
                .mostTradedSymbol(mostTradedSymbol)
                .tradeCountByPortfolio(countByPortfolio)
                .tradeValueByPortfolio(valueByPortfolio)
                .topPerformingPortfolio(topPortfolio)
                .tradeCountByTrader(countByTrader)
                .tradeValueByTrader(valueByTrader)
                .mostActiveTrader(mostActiveTrader)
                .tradeCountByHour(countByHour)
                .tradeValueByHour(valueByHour)
                .peakTradingHour(peakHour)
                .totalCommissions(totalCommissions)
                .totalFees(totalFees)
                .netTradeValue(netValue)
                .profitLossPercentage(profitLossPercentage)
                .build();
    }
    
    /**
     * Truncate a datetime to a specific interval
     */
    private LocalDateTime truncateToInterval(LocalDateTime dateTime, String interval) {
        switch (interval.toLowerCase()) {
            case "hour":
                return dateTime.truncatedTo(ChronoUnit.HOURS);
            case "day":
                return dateTime.truncatedTo(ChronoUnit.DAYS);
            case "week":
                return dateTime.minusDays(dateTime.getDayOfWeek().getValue() - 1).truncatedTo(ChronoUnit.DAYS);
            case "month":
                return dateTime.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
            default:
                return dateTime;
        }
    }
    
    /**
     * Generate Excel report from metrics
     */
    private byte[] generateExcelReport(TradeMetrics metrics, String reportName) {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Trade Summary");
            
            int rowNum = 0;
            
            // Report header
            Row headerRow = sheet.createRow(rowNum++);
            headerRow.createCell(0).setCellValue(reportName);
            headerRow.createCell(1).setCellValue("Generated: " + 
                    metrics.getTimestamp().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            
            rowNum++; // Empty row
            
            // Summary section
            Row summaryHeaderRow = sheet.createRow(rowNum++);
            summaryHeaderRow.createCell(0).setCellValue("Summary");
            
            Row totalTradesRow = sheet.createRow(rowNum++);
            totalTradesRow.createCell(0).setCellValue("Total Trades");
            totalTradesRow.createCell(1).setCellValue(metrics.getTotalTradeCount());
            
            Row totalValueRow = sheet.createRow(rowNum++);
            totalValueRow.createCell(0).setCellValue("Total Value");
            totalValueRow.createCell(1).setCellValue(metrics.getTotalTradeValue().doubleValue());
            
            Row avgSizeRow = sheet.createRow(rowNum++);
            avgSizeRow.createCell(0).setCellValue("Average Trade Size");
            avgSizeRow.createCell(1).setCellValue(metrics.getAverageTradeSize().doubleValue());
            
            Row largestTradeRow = sheet.createRow(rowNum++);
            largestTradeRow.createCell(0).setCellValue("Largest Trade");
            largestTradeRow.createCell(1).setCellValue(metrics.getLargestTradeValue().doubleValue());
            
            Row smallestTradeRow = sheet.createRow(rowNum++);
            smallestTradeRow.createCell(0).setCellValue("Smallest Trade");
            smallestTradeRow.createCell(1).setCellValue(metrics.getSmallestTradeValue().doubleValue());
            
            rowNum++; // Empty row
            
            // Symbol section
            Row symbolHeaderRow = sheet.createRow(rowNum++);
            symbolHeaderRow.createCell(0).setCellValue("Symbol Metrics");
            
            Row mostTradedSymbolRow = sheet.createRow(rowNum++);
            mostTradedSymbolRow.createCell(0).setCellValue("Most Traded Symbol");
            mostTradedSymbolRow.createCell(1).setCellValue(metrics.getMostTradedSymbol());
            
            // Add more sections for other metrics...
            
            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
            
        } catch (IOException e) {
            log.error("Error generating Excel report", e);
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
