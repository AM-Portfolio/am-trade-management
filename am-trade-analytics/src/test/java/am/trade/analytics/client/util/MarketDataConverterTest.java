package am.trade.analytics.client.util;

import am.trade.analytics.client.model.HistoricalMarketDataResponse;
import am.trade.common.models.PriceDataPoint;
import lombok.SneakyThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MarketDataConverterTest {

    private ObjectMapper objectMapper;
    private String sampleJson;

    @BeforeEach
    @SneakyThrows
    void setUp() {
        objectMapper = new ObjectMapper();
        sampleJson = new String(Files.readAllBytes(Paths.get("src/test/resources/historicaldata.json")));
    }

    @Test
    void testToPriceDataPointsWithValidResponse() throws Exception {
        // Create HistoricalMarketDataResponse from JSON
        HistoricalMarketDataResponse response = objectMapper.readValue(sampleJson, HistoricalMarketDataResponse.class);
        
        // Convert to price data points
        List<PriceDataPoint> priceDataPoints = MarketDataConverter.toPriceDataPoints(response);
        
        // Verify results
        assertNotNull(priceDataPoints);
        assertEquals(1, priceDataPoints.size());
        
        PriceDataPoint point = priceDataPoints.get(0);
        assertEquals(LocalDateTime.of(2023, 11, 30, 9, 15), point.getTimestamp());
        assertEquals(new BigDecimal("20108.5"), point.getOpen());
        assertEquals(new BigDecimal("20136.15"), point.getHigh());
        assertEquals(new BigDecimal("20015.85"), point.getLow());
        assertEquals(new BigDecimal("20047.35"), point.getClose());
        assertEquals(0L, point.getVolume());
    }
}
