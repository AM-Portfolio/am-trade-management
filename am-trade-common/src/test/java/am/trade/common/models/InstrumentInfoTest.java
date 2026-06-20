package am.trade.common.models;

import am.trade.common.models.enums.MarketSegment;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class InstrumentInfoTest {

    @Test
    public void testOptionSymbolParsing() {
        // Test the new option symbol formats
        
        // Month-day format: BANKNIFTY20O0121000CE (Oct 1)
        InstrumentInfo info1 = InstrumentInfo.fromRawSymbol("BANKNIFTY20O0121000CE");
        assertNotNull(info1);
        assertEquals("BANKNIFTY", info1.getSymbol());
        assertEquals(MarketSegment.INDEX_OPTIONS, info1.getSegment());
        assertTrue(info1.isDerivative());
        assertTrue(info1.getDerivativeInfo().getIsCall());
        assertEquals(21000, info1.getDerivativeInfo().getStrikePrice().intValue());
        
        // Month-week format: BANKNIFTY20N1929100CE (Nov Week 19)
        InstrumentInfo info2 = InstrumentInfo.fromRawSymbol("BANKNIFTY20N1929100CE");
        assertNotNull(info2);
        assertEquals("BANKNIFTY", info2.getSymbol());
        assertEquals(MarketSegment.INDEX_OPTIONS, info2.getSegment());
        assertTrue(info2.isDerivative());
        assertTrue(info2.getDerivativeInfo().getIsCall());
        assertEquals(29100, info2.getDerivativeInfo().getStrikePrice().intValue());
        
        // December put option: NIFTY20D0312800PE
        InstrumentInfo info3 = InstrumentInfo.fromRawSymbol("NIFTY20D0312800PE");
        assertNotNull(info3);
        assertEquals("NIFTY", info3.getSymbol());
        assertEquals(MarketSegment.INDEX_OPTIONS, info3.getSegment());
        assertTrue(info3.isDerivative());
        assertFalse(info3.getDerivativeInfo().getIsCall());
        assertEquals(12800, info3.getDerivativeInfo().getStrikePrice().intValue());
    }
}
