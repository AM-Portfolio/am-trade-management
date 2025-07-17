package am.trade.analytics.client.util;

import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import am.trade.analytics.client.model.MarketDataPoint;
import am.trade.analytics.model.PriceDataPoint;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Utility class for converting between market data client models and internal models
 */
@UtilityClass
public class MarketDataConverter {

    /**
     * Convert a market data response to a list of price data points
     *
     * @param response The historical market data response
     * @return List of price data points
     */
    public List<PriceDataPoint> toPriceDataPoints(HistoricalMarketDataResponse response) {
        if (response == null || response.getData() == null || response.getData().getDataPoints() == null) {
            return List.of();
        }

        return response.getData().getDataPoints().stream()
                .map(MarketDataConverter::toPriceDataPoint)
                .collect(Collectors.toList());
    }

    /**
     * Convert a single market data point to a price data point
     *
     * @param marketDataPoint The market data point
     * @return Price data point
     */
    private PriceDataPoint toPriceDataPoint(MarketDataPoint marketDataPoint) {
        List<Integer> timeComponents = marketDataPoint.getTime();
        LocalDateTime timestamp = createTimestampFromComponents(timeComponents);

        // Calculate profit/loss metrics (placeholder - would need a reference price)
        BigDecimal entryPrice = BigDecimal.valueOf(100); // This would be replaced with actual entry price
        BigDecimal profitLossAtPoint = marketDataPoint.getClose().subtract(entryPrice);
        BigDecimal profitLossPercentageAtPoint = profitLossAtPoint
                .divide(entryPrice, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return PriceDataPoint.builder()
                .timestamp(timestamp)
                .open(marketDataPoint.getOpen())
                .high(marketDataPoint.getHigh())
                .low(marketDataPoint.getLow())
                .close(marketDataPoint.getClose())
                .volume(marketDataPoint.getVolume())
                .profitLossAtPoint(profitLossAtPoint)
                .profitLossPercentageAtPoint(profitLossPercentageAtPoint)
                .build();
    }

    /**
     * Create a LocalDateTime from time components [year, month, day, hour, minute]
     *
     * @param timeComponents List of time components
     * @return LocalDateTime
     */
    private LocalDateTime createTimestampFromComponents(List<Integer> timeComponents) {
        if (timeComponents == null || timeComponents.size() < 5) {
            return LocalDateTime.now(); // Default fallback
        }

        return LocalDateTime.of(
                timeComponents.get(0), // year
                timeComponents.get(1), // month
                timeComponents.get(2), // day
                timeComponents.get(3), // hour
                timeComponents.get(4)  // minute
        );
    }
}
