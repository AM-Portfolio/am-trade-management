package am.trade.analytics.client.util;

import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import am.trade.analytics.client.model.MarketDataPoint;
import am.trade.analytics.model.PriceDataPoint;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Utility class for converting between market data client models and internal models
 */
@UtilityClass
@Slf4j
public class MarketDataConverter {
    
    // Using ObjectMapper for type conversion when needed

    /**
     * Convert a market data response to a list of price data points
     *
     * @param response The historical market data response
     * @return List of price data points
     */
    public List<PriceDataPoint> toPriceDataPoints(HistoricalMarketDataResponse response) {
        if (response == null || response.getData() == null) {
            log.warn("Received null response or null data from market data client");
            return List.of();
        }

        Object dataPointsObj = response.getData().getDataPoints();
        if (dataPointsObj == null) {
            log.warn("Received null dataPoints from market data client");
            return List.of();
        }

        List<PriceDataPoint> result;
        
        if (dataPointsObj instanceof List<?>) {
            // No need for unchecked warning with wildcard
            List<?> dataPointsList = (List<?>) dataPointsObj;
            
            // Check if the list contains MarketDataPoint objects or Maps
            if (!dataPointsList.isEmpty()) {
                if (dataPointsList.get(0) instanceof MarketDataPoint) {
                    log.info("Processing list of MarketDataPoint objects");
                    // Process each item individually to avoid class cast exceptions
                    List<MarketDataPoint> safeList = new ArrayList<>();
                    for (Object item : dataPointsList) {
                        if (item instanceof MarketDataPoint) {
                            safeList.add((MarketDataPoint) item);
                        } else if (item instanceof Map) {
                            safeList.add(convertToMarketDataPoint(item));
                        }
                    }
                    result = toPriceDataPoints(safeList);
                } else if (dataPointsList.get(0) instanceof Map<?, ?>) {
                    // Convert each Map to MarketDataPoint
                    log.info("Converting list of Maps to MarketDataPoint objects");
                    List<MarketDataPoint> convertedList = new ArrayList<>();
                    
                    for (Object item : dataPointsList) {
                        if (item instanceof Map<?, ?>) {
                            MarketDataPoint point = convertToMarketDataPoint(item);
                            convertedList.add(point);
                        } else {
                            log.warn("Skipping non-Map item in dataPointsList: {}", 
                                item != null ? item.getClass().getName() : "null");
                        }
                    }
                    result = toPriceDataPoints(convertedList);
                } else {
                    log.error("Unexpected data type in list: {}", dataPointsList.get(0).getClass().getName());
                    result = List.of();
                }
            } else {
                log.warn("Empty data points list");
                result = List.of();
            }
        } else if (dataPointsObj instanceof Map<?, ?>) {
            log.warn("Received dataPoints as Map instead of List, converting");
            // Convert Map entries to MarketDataPoint objects
            log.info("Converting Map to list of MarketDataPoint objects");
            @SuppressWarnings("unchecked")
            Map<String, Object> dataMap = (Map<String, Object>) dataPointsObj;
            
            List<MarketDataPoint> convertedList = new ArrayList<>();
            for (Object value : dataMap.values()) {
                MarketDataPoint point = convertToMarketDataPoint(value);
                if (point != null) {
                    convertedList.add(point);
                }
            }
            result = toPriceDataPoints(convertedList);
        } else {
            log.error("Unexpected dataPoints type: {}", dataPointsObj != null ? dataPointsObj.getClass().getName() : "null");
            result = List.of();
        }
        
        return result;
    }

    /**
     * Convert a list of market data points to price data points
     *
     * @param marketDataPointList The list of market data points
     * @return List of price data points
     */
    private List<PriceDataPoint> toPriceDataPoints(List<?> marketDataPointList) {
        if (marketDataPointList == null) {
            log.warn("Received null marketDataPointList");
            return List.of();
        }
        
        // Avoid stream operations that might cause ClassCastException
        List<PriceDataPoint> result = new ArrayList<>();
        for (Object item : marketDataPointList) {
            if (item == null) {
                continue;
            }
            
            try {
                if (item instanceof Map) {
                    // Handle Map objects directly
                    MarketDataPoint convertedPoint = convertToMarketDataPoint(item);
                    if (convertedPoint != null) {
                        PriceDataPoint pricePoint = toPriceDataPoint(convertedPoint);
                        if (pricePoint != null) {
                            result.add(pricePoint);
                        }
                    }
                } else if (item instanceof MarketDataPoint) {
                    // Handle MarketDataPoint objects
                    MarketDataPoint marketDataPoint = (MarketDataPoint) item;
                    PriceDataPoint pricePoint = toPriceDataPoint(marketDataPoint);
                    if (pricePoint != null) {
                        result.add(pricePoint);
                    }
                } else {
                    log.warn("Unexpected item type in marketDataPointList: {}", 
                            item != null ? item.getClass().getName() : "null");
                }
            } catch (Exception e) {
                log.error("Error processing market data point: {}", e.getMessage(), e);
            }
        }
        return result;
    }


    private PriceDataPoint toPriceDataPoint(MarketDataPoint marketDataPoint) {
        if (marketDataPoint instanceof Map) {
            log.warn("Received Map instead of MarketDataPoint, converting");
            return toPriceDataPoint(convertToMarketDataPoint(marketDataPoint));
        }
        
        try {
            return PriceDataPoint.builder()
                    .timestamp(createTimestampFromComponents(marketDataPoint.getTime()))
                    .open(marketDataPoint.getOpen())
                    .high(marketDataPoint.getHigh())
                    .low(marketDataPoint.getLow())
                    .close(marketDataPoint.getClose())
                    .volume(marketDataPoint.getVolume())
                    .build();
        } catch (Exception e) {
            log.error("Error creating PriceDataPoint: {}", e.getMessage());
            return null;
        }
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
    
    /**
     * Convert a generic object to MarketDataPoint
     * 
     * @param obj The object to convert
     * @return MarketDataPoint
     */
    private MarketDataPoint convertToMarketDataPoint(Object obj) {
        try {
            if (obj instanceof Map<?, ?>) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                MarketDataPoint point = new MarketDataPoint();
                
                // Extract and set time
                if (map.get("time") instanceof List<?>) {
                    @SuppressWarnings("unchecked")
                    List<Integer> timeList = (List<Integer>) map.get("time");
                    point.setTime(timeList);
                }
                
                // Extract and set numeric values
                if (map.get("open") != null) {
                    point.setOpen(new BigDecimal(map.get("open").toString()));
                }
                if (map.get("high") != null) {
                    point.setHigh(new BigDecimal(map.get("high").toString()));
                }
                if (map.get("low") != null) {
                    point.setLow(new BigDecimal(map.get("low").toString()));
                }
                if (map.get("close") != null) {
                    point.setClose(new BigDecimal(map.get("close").toString()));
                }
                if (map.get("volume") != null) {
                    point.setVolume(Long.valueOf(map.get("volume").toString()));
                }
                
                return point;
            } else {
                // Try to convert using Jackson's conversion
                if (obj instanceof MarketDataPoint) {
                    return (MarketDataPoint) obj;
                }
                // If direct conversion isn't possible, create an empty object
                log.warn("Unable to convert object of type {} to MarketDataPoint", obj.getClass().getName());
                return new MarketDataPoint();
            }
        } catch (Exception e) {
            log.error("Error converting to MarketDataPoint: {}", e.getMessage());
            return new MarketDataPoint(); // Return empty object as fallback
        }
    }
}
