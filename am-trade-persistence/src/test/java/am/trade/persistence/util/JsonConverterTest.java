// package am.trade.persistence.util;

// import am.trade.common.models.TradeDetails;
// import am.trade.common.util.JsonConverter;
// import org.junit.jupiter.api.Test;

// import java.util.List;

// import static org.junit.jupiter.api.Assertions.*;

// /**
//  * Test class for JsonConverter utility
//  */
// public class JsonConverterTest {

//     private static final String TEST_RESOURCE_PATH = "tradedetails.json";

//     @Test
//     void testLoadTradeDetailsFromResource() {
//         // Load a single TradeDetails object from the resource file
//         TradeDetails tradeDetails = JsonConverter.fromJsonResource(TEST_RESOURCE_PATH, TradeDetails.class);
        
//         // Verify the loaded object
//         assertNotNull(tradeDetails);
//         assertEquals("MKU257_TES29T1", tradeDetails.getPortfolioId());
//         assertEquals("HDFC", tradeDetails.getSymbol());
//         assertEquals("NASDAQ", tradeDetails.getInstrumentInfo().getExchange());
//         assertEquals("SWING_TRADE", tradeDetails.getStrategy());
//         assertEquals("WIN", tradeDetails.getStatus());
//         assertEquals("LONG", tradeDetails.getTradePositionType());
        
//         // Verify entry info
//         assertNotNull(tradeDetails.getEntryInfo());
//         assertEquals(198.75, tradeDetails.getEntryInfo().getPrice());
//         assertEquals(100, tradeDetails.getEntryInfo().getQuantity());
        
//         // Verify exit info
//         assertNotNull(tradeDetails.getExitInfo());
//         assertEquals(205.50, tradeDetails.getExitInfo().getPrice());
        
//         // Verify metrics
//         assertNotNull(tradeDetails.getMetrics());
//         assertEquals(675.00, tradeDetails.getMetrics().getProfitLoss());
//         assertEquals(3.40, tradeDetails.getMetrics().getProfitLossPercentage());
//     }

//     @Test
//     void testConvertTradeDetailsToJson() {
//         // Load a TradeDetails object from the resource file
//         TradeDetails tradeDetails = JsonConverter.fromJsonResource(TEST_RESOURCE_PATH, TradeDetails.class);
        
//         // Convert the object to JSON
//         String json = JsonConverter.toJson(tradeDetails);
        
//         // Verify the JSON contains expected values
//         assertTrue(json.contains("\"portfolioId\" : \"MKU257_TES29T1\""));
//         assertTrue(json.contains("\"symbol\" : \"HDFC\""));
//         assertTrue(json.contains("\"exchange\" : \"NASDAQ\""));
//         assertTrue(json.contains("\"price\" : 198.75"));
        
//         // Convert the JSON back to an object
//         TradeDetails convertedTradeDetails = JsonConverter.fromJson(json, TradeDetails.class);
        
//         // Verify the converted object
//         assertEquals(tradeDetails.getPortfolioId(), convertedTradeDetails.getPortfolioId());
//         assertEquals(tradeDetails.getSymbol(), convertedTradeDetails.getSymbol());
//         assertEquals(tradeDetails.getEntryInfo().getPrice(), convertedTradeDetails.getEntryInfo().getPrice());
//     }

//     @Test
//     void testHandleNullValues() {
//         // Create a TradeDetails object with some null values
//         TradeDetails tradeDetails = new TradeDetails();
//         tradeDetails.setPortfolioId("TEST_PORTFOLIO");
//         tradeDetails.setSymbol("TEST");
//         // Leave other fields as null
        
//         // Convert to JSON and back
//         String json = JsonConverter.toJson(tradeDetails);
//         TradeDetails convertedTradeDetails = JsonConverter.fromJson(json, TradeDetails.class);
        
//         // Verify the conversion worked correctly
//         assertEquals("TEST_PORTFOLIO", convertedTradeDetails.getPortfolioId());
//         assertEquals("TEST", convertedTradeDetails.getSymbol());
//         assertNull(convertedTradeDetails.getEntryInfo());
//         assertNull(convertedTradeDetails.getExitInfo());
//     }
// }
