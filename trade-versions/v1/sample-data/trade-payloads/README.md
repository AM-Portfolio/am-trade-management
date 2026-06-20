# Trade Sample Payloads

This directory contains sample JSON payloads for creating trades via the AM Trade Management API.

## Available Sample Payloads

### 1. **create-trade-nifty-futures.json**
- **Type**: Index Futures (NIFTY)
- **Status**: WIN
- **Position**: LONG
- **Strategy**: Momentum Trading
- **Features**: 
  - Complete psychology data with emotional state tracking
  - Detailed entry and exit reasoning
  - Technical and fundamental analysis
  - Risk assessment
- **Use Case**: Standard winning trade with full documentation

### 2. **create-trade-nifty-futures-complete.json**
- **Type**: Index Futures (NIFTY)
- **Status**: WIN
- **Position**: LONG
- **Strategy**: Momentum Trading
- **Features**:
  - **MOST COMPREHENSIVE** - All fields populated
  - Multiple technical indicators (RSI, MACD, EMA, Volume)
  - Chart patterns (Bullish Flag)
  - Support/Resistance levels
  - News impact and economic indicators
  - Complete risk assessment with mitigation strategies
  - Market context (time, volatility, conditions)
  - Attachments (charts, analysis documents)
  - Detailed lessons learned
- **Use Case**: Reference example showing ALL possible fields

### 3. **create-trade-banknifty-options-loss.json**
- **Type**: Index Options (BANK NIFTY Call)
- **Status**: LOSS
- **Position**: SHORT
- **Strategy**: Option Selling
- **Features**:
  - Psychology of a losing trade
  - FOMO and emotional decision making
  - Rule violations documented
  - Mistakes made and lessons learned
  - Shows what NOT to do
- **Use Case**: Learning from mistakes, emotional discipline

### 4. **create-trade-equity-open.json**
- **Type**: Equity (RELIANCE)
- **Status**: OPEN
- **Position**: LONG
- **Strategy**: Swing Trading
- **Features**:
  - Open trade (no exit info)
  - Swing trading setup
  - Support zone entry
  - Fundamental catalyst (Q3 results)
- **Use Case**: Creating open trades for monitoring

## How to Use These Payloads

### Method 1: Using Postman

1. **Import the TradeController Collection**
   ```
   File > Import > am-trade-managment/postman/TradeController.postman_collection.json
   ```

2. **Set Environment Variables**
   ```
   baseUrl: http://localhost:8073
   userId: your-user-id
   portfolioId: your-portfolio-id
   ```

3. **Select "Add New Trade" Request**

4. **Copy Payload**
   - Open the desired sample JSON file
   - Copy the entire contents
   - Paste into the request body

5. **Update Variables**
   - Replace `{{portfolioId}}` with actual portfolio ID
   - Replace `{{userId}}` with actual user ID
   - Or use Postman environment variables

6. **Send Request**
   - Click "Send"
   - Expect **201 Created** response

### Method 2: Using cURL

```bash
curl -X POST http://localhost:8073/api/v1/trades/details \
  -H "Content-Type: application/json" \
  -d @create-trade-nifty-futures.json
```

### Method 3: Direct API Call

```bash
# Read the file and post
cat create-trade-nifty-futures.json | \
  sed 's/{{portfolioId}}/actual-portfolio-id/g' | \
  sed 's/{{userId}}/actual-user-id/g' | \
  curl -X POST http://localhost:8073/api/v1/trades/details \
    -H "Content-Type: application/json" \
    -d @-
```

## Customizing Payloads

### Required Fields (Minimum)

```json
{
  "portfolioId": "required",
  "userId": "required",
  "instrumentInfo": {
    "symbol": "required",
    "exchange": "required",
    "segment": "required"
  },
  "status": "required (WIN|LOSS|OPEN|BREAK_EVEN)",
  "tradePositionType": "required (LONG|SHORT)",
  "entryInfo": {
    "timestamp": "required",
    "price": "required",
    "quantity": "required"
  }
}
```

### Optional but Recommended

- `exitInfo` - For closed trades (WIN/LOSS/BREAK_EVEN)
- `metrics` - Trade performance metrics
- `strategy` - Trading strategy name
- `notes` - Trade notes
- `tags` - Categorization tags

### Advanced (Optional)

- `psychologyData` - Emotional state and discipline tracking
- `entryReasoning` - Detailed entry analysis
- `exitReasoning` - Detailed exit analysis
- `attachments` - Charts and analysis documents

## Field-Level Documentation

### Psychology Data
See: `../TRADE-PAYLOAD-GUIDE.md` - Section on Psychology Data

### Technical Analysis
- **Indicators**: RSI, MACD, EMA, SMA, Bollinger Bands, Volume
- **Patterns**: Flags, Triangles, Head & Shoulders, Double Tops/Bottoms
- **Support/Resistance**: Key levels for entry/exit decisions

### Fundamental Analysis
- **News Impact**: Market-moving news and events
- **Economic Indicators**: GDP, Inflation, Interest Rates
- **Sector Analysis**: Sector trends and rotations

## Validation

All payloads are validated against:
1. **TradeDetails Model** - Java model structure
2. **Controller Validation** - `@Validated` annotations
3. **Service Layer** - `TradeValidator` business rules

## Common Mistakes to Avoid

❌ **Don't include `symbol` at root level**
```json
{
  "symbol": "NIFTY25JULFUT",  // ❌ WRONG
  "instrumentInfo": {
    "symbol": "NIFTY25JULFUT"  // ✅ CORRECT
  }
}
```

❌ **Don't use `tradeAnalysisImages`**
```json
{
  "tradeAnalysisImages": [...],  // ❌ WRONG - old field
  "attachments": [...]           // ✅ CORRECT - use this
}
```

❌ **Don't use wrong field names in metrics**
```json
{
  "metrics": {
    "holdingPeriodHours": 5  // ❌ WRONG
    "holdingTimeHours": 5    // ✅ CORRECT
  }
}
```

## Testing Scenarios

### Scenario 1: Complete Documentation
**File**: `create-trade-nifty-futures-complete.json`
**Test**: All fields properly accepted and stored

### Scenario 2: Emotional Discipline
**File**: `create-trade-banknifty-options-loss.json`
**Test**: Psychology data captures mistakes and lessons

### Scenario 3: Open Trade Monitoring
**File**: `create-trade-equity-open.json`
**Test**: Open trades without exit info

### Scenario 4: Minimal Data
**Test**: Create trade with only required fields
```json
{
  "portfolioId": "test-portfolio",
  "userId": "test-user",
  "instrumentInfo": {
    "symbol": "TESTSYM",
    "exchange": "NSE",
    "segment": "EQUITY"
  },
  "status": "OPEN",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-11-11T10:00:00",
    "price": 100.00,
    "quantity": 10
  }
}
```

## Expected Responses

### Success (201 Created)
```json
{
  "tradeId": "generated-uuid",
  "portfolioId": "your-portfolio-id",
  "userId": "your-user-id",
  "instrumentInfo": { ... },
  "status": "WIN",
  ...
}
```

### Validation Error (400 Bad Request)
```json
{
  "timestamp": "2025-11-11T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed: userId is required",
  "path": "/api/v1/trades/details"
}
```

## Integration with Postman Collection

These payloads are designed to work seamlessly with:
- `TradeController.postman_collection.json`
- All controller-specific collections
- The consolidated collection (when created)

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2025-11-11 | Initial creation with 4 sample payloads |
| 1.1 | 2025-11-11 | Added psychology and reasoning fields |

## Related Documentation

- **Main Guide**: `../TRADE-PAYLOAD-GUIDE.md`
- **Controller Docs**: `../../am-trade-api/src/main/java/am/trade/api/controller/TradeController.java`
- **Model Docs**: `../../am-trade-common/src/main/java/am/trade/common/models/TradeDetails.java`

## Support

For issues or questions:
1. Check `../TRADE-PAYLOAD-GUIDE.md` for detailed field documentation
2. Review controller validation rules
3. Check application logs for detailed error messages
4. Ensure all required environment variables are set
