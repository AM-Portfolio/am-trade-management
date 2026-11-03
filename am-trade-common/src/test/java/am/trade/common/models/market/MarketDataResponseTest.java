package am.trade.common.models.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MarketDataResponseTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void resolvePreviousClose_prefersExplicitField() throws Exception {
        MarketDataResponse quote = mapper.readValue(
                "{\"lastPrice\":100.0,\"previousClose\":95.5}",
                MarketDataResponse.class);
        assertEquals(95.5, quote.resolvePreviousClose());
    }

    @Test
    void resolvePreviousClose_fallsBackToDistinctOhlcClose() throws Exception {
        MarketDataResponse quote = mapper.readValue(
                "{\"lastPrice\":100.0,\"ohlc\":{\"close\":94.0}}",
                MarketDataResponse.class);
        assertEquals(94.0, quote.resolvePreviousClose());
    }

    @Test
    void resolvePreviousClose_ignoresCollapsedOhlcCloseEqualToLast() throws Exception {
        MarketDataResponse quote = mapper.readValue(
                "{\"lastPrice\":100.0,\"ohlc\":{\"close\":100.0}}",
                MarketDataResponse.class);
        assertNull(quote.resolvePreviousClose());
    }
}
