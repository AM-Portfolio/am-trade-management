package am.trade.analytics.util;

import am.trade.analytics.client.MarketDataClient;
import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import am.trade.analytics.client.util.MarketDataConverter;
import am.trade.analytics.model.PriceDataPoint;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for trade analytics operations
 * Provides methods for fetching historical price data and performing calculations
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeAnalyticsUtils {

    private final MarketDataClient marketDataClient;

    /**
     * Fetch historical price data for a symbol between start and end dates
     * 
     * @param symbol The stock symbol
     * @param startDate The start date
     * @param endDate The end date
     * @return List of price data points
     */
    public List<PriceDataPoint> fetchHistoricalPriceData(String symbol, LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Fetching historical price data for {}, from {} to {}", symbol, startDate, endDate);
        
        try {
            // Use the market data client to fetch real data from the API
            // Default to 60-minute intervals, can be made configurable if needed
            HistoricalMarketDataResponse response = marketDataClient.fetchHistoricalData(
                    symbol, 
                    startDate, 
                    endDate, 
                    "60minute", 
                    false);
            
            // Convert the response to our internal PriceDataPoint model
            List<PriceDataPoint> priceDataPoints = MarketDataConverter.toPriceDataPoints(response);
            
            log.info("Successfully fetched {} price data points for {}", priceDataPoints.size(), symbol);
            return priceDataPoints;
        } catch (Exception e) {
            log.error("Failed to fetch historical price data: {}", e.getMessage(), e);
            // Return empty list on error
            return List.of();
        }
    }
    
    /**
     * Calculate the average daily price movement during a trade
     * 
     * @param priceDataPoints List of price data points
     * @return Average daily price movement as a percentage
     */
    public BigDecimal calculateAverageDailyMovement(List<PriceDataPoint> priceDataPoints) {
        if (priceDataPoints == null || priceDataPoints.isEmpty() || priceDataPoints.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal totalDailyMovement = BigDecimal.ZERO;
        
        for (int i = 1; i < priceDataPoints.size(); i++) {
            BigDecimal previousClose = priceDataPoints.get(i - 1).getClose();
            BigDecimal currentClose = priceDataPoints.get(i).getClose();
            
            BigDecimal dailyMovement = currentClose.subtract(previousClose)
                    .divide(previousClose, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .abs(); // We're interested in the magnitude of movement, not direction
            
            totalDailyMovement = totalDailyMovement.add(dailyMovement);
        }
        
        return totalDailyMovement.divide(BigDecimal.valueOf(priceDataPoints.size() - 1), 4, RoundingMode.HALF_UP);
    }
    
    /**
     * Calculate the volatility (standard deviation) of daily returns
     * 
     * @param priceDataPoints List of price data points
     * @return Volatility as a percentage
     */
    public BigDecimal calculateVolatility(List<PriceDataPoint> priceDataPoints) {
        if (priceDataPoints == null || priceDataPoints.isEmpty() || priceDataPoints.size() < 2) {
            return BigDecimal.ZERO;
        }
        
        // Calculate daily returns
        List<BigDecimal> dailyReturns = new ArrayList<>();
        
        for (int i = 1; i < priceDataPoints.size(); i++) {
            BigDecimal previousClose = priceDataPoints.get(i - 1).getClose();
            BigDecimal currentClose = priceDataPoints.get(i).getClose();
            
            BigDecimal dailyReturn = currentClose.subtract(previousClose)
                    .divide(previousClose, 6, RoundingMode.HALF_UP);
            
            dailyReturns.add(dailyReturn);
        }
        
        // Calculate mean of daily returns
        BigDecimal sum = BigDecimal.ZERO;
        for (BigDecimal dailyReturn : dailyReturns) {
            sum = sum.add(dailyReturn);
        }
        BigDecimal mean = sum.divide(BigDecimal.valueOf(dailyReturns.size()), 6, RoundingMode.HALF_UP);
        
        // Calculate sum of squared differences from mean
        BigDecimal sumSquaredDiff = BigDecimal.ZERO;
        for (BigDecimal dailyReturn : dailyReturns) {
            BigDecimal diff = dailyReturn.subtract(mean);
            sumSquaredDiff = sumSquaredDiff.add(diff.multiply(diff));
        }
        
        // Calculate variance and standard deviation
        BigDecimal variance = sumSquaredDiff.divide(BigDecimal.valueOf(dailyReturns.size()), 6, RoundingMode.HALF_UP);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
        
        // Convert to annualized volatility (assuming 252 trading days in a year)
        BigDecimal annualizedVolatility = stdDev.multiply(BigDecimal.valueOf(Math.sqrt(252)))
                .multiply(BigDecimal.valueOf(100)); // Convert to percentage
        
        return annualizedVolatility.setScale(2, RoundingMode.HALF_UP);
    }
}
