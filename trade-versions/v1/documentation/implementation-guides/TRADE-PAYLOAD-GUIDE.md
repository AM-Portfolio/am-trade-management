# Trade Payload Correction Guide

## Original vs Corrected Payload

### Key Corrections Made

#### 1. **Removed `symbol` at root level**
- **Issue**: Your original payload had `"symbol": "NIFTY25JULFUT"` at the root level
- **Fix**: The `symbol` field should ONLY be inside `instrumentInfo` object
- **Reason**: `TradeDetails` model derives the symbol from `instrumentInfo.getSymbol()` via a `@JsonIgnore` getter method

#### 2. **Simplified `tradeExecutions` structure**
- **Original**: Complex array with `executionId`, `executionType`, etc.
- **Corrected**: Empty array `[]` or should use `TradeModel` structure
- **Reason**: `tradeExecutions` expects `List<TradeModel>` objects, not custom execution objects
- **Note**: For basic trade creation, you can leave this as an empty array

#### 3. **Renamed `tradeAnalysisImages` to `attachments`**
- **Original**: `"tradeAnalysisImages": ["url1", "url2"]`
- **Corrected**: `"attachments": []` (empty array or `List<Attachment>` objects)
- **Reason**: The model uses `List<Attachment>` with proper attachment structure

#### 4. **Fixed `metrics` field names**
- **Original**: Used `holdingPeriodHours`
- **Corrected**: Changed to `holdingTimeHours`
- **Reason**: The `TradeMetrics` model uses `holdingTimeHours`, not `holdingPeriodHours`

#### 5. **Removed invalid fields**
- Removed fields that don't exist in the `TradeDetails` model
- All fields now match the exact model structure

## Complete TradeDetails Model Structure

### Top-Level Fields
```java
{
  "tradeId": "string",              // Auto-generated if not provided
  "portfolioId": "string",           // REQUIRED
  "instrumentInfo": { ... },         // REQUIRED - see below
  "strategy": "string",              // Optional
  "status": "WIN|LOSS|OPEN|BREAK_EVEN",  // REQUIRED
  "tradePositionType": "LONG|SHORT", // REQUIRED
  "entryInfo": { ... },              // REQUIRED - see below
  "exitInfo": { ... },               // Optional for OPEN trades
  "metrics": { ... },                // Optional - see below
  "tradeExecutions": [ ... ],        // Optional - List<TradeModel>
  "notes": "string",                 // Optional
  "tags": ["string"],                // Optional
  "userId": "string",                // REQUIRED
  "attachments": [ ... ],            // Optional - List<Attachment>
  "psychologyData": { ... },         // Optional
  "entryReasoning": { ... },         // Optional
  "exitReasoning": { ... }           // Optional
}
```

### InstrumentInfo Structure
```java
{
  "symbol": "string",                // REQUIRED
  "isin": "string",                  // Optional
  "rawSymbol": "string",             // Optional
  "exchange": "NSE|BSE|MCX|...",     // REQUIRED
  "segment": "EQUITY|INDEX_FUTURES|INDEX_OPTIONS|...", // REQUIRED
  "series": "EQ|FUT|OPT|...",        // Optional
  "indexType": "NIFTY50|SENSEX|...", // Optional - for indices
  "derivativeInfo": { ... },         // Required for derivatives
  "description": "string",           // Optional
  "currency": "INR|USD|...",         // Optional
  "lotSize": "string"                // Optional
}
```

### DerivativeInfo Structure (for Futures/Options)
```java
{
  "expiryDate": "YYYY-MM-DD",        // LocalDate format
  "underlyingSymbol": "string",      // e.g., "NIFTY", "BANKNIFTY"
  "strikePrice": 22500.00,           // For options only
  "isCall": true,                    // For options: true=CALL, false=PUT
  "isEuropean": true,                // For options settlement type
  "futureType": "WEEKLY|MONTHLY|QUARTERLY", // For futures only
  "isCashSettled": true              // Settlement method
}
```

### EntryExitInfo Structure
```java
{
  "timestamp": "YYYY-MM-DDTHH:mm:ss", // LocalDateTime format
  "price": 22450.75,                  // BigDecimal
  "quantity": 2,                      // Integer
  "totalValue": 44901.50,             // Optional BigDecimal
  "fees": 125.50,                     // Optional BigDecimal
  "reason": "string"                  // Optional
}
```

### TradeMetrics Structure
```java
{
  "profitLoss": 22450.0,              // BigDecimal
  "profitLossPercentage": 1.0,        // BigDecimal
  "returnOnEquity": 0.5,              // Optional BigDecimal
  "riskAmount": 1000.0,               // Optional BigDecimal
  "rewardAmount": 2000.0,             // Optional BigDecimal
  "riskRewardRatio": 2.0,             // Optional BigDecimal
  "holdingTimeDays": 5,               // Optional Long
  "holdingTimeHours": 120,            // Optional Long
  "holdingTimeMinutes": 7200,         // Optional Long
  "maxAdverseExcursion": -500.0,      // Optional BigDecimal
  "maxFavorableExcursion": 3000.0     // Optional BigDecimal
}
```

### Attachment Structure
```java
{
  "id": "string",
  "name": "string",
  "url": "string",
  "type": "IMAGE|PDF|...",
  "size": 1024,
  "uploadDate": "YYYY-MM-DDTHH:mm:ss"
}
```

## Controller Endpoint Details

### POST /api/v1/trades/details - Add New Trade

**Endpoint**: `POST http://localhost:8073/api/v1/trades/details`

**Headers**:
```
Content-Type: application/json
Accept: application/json
```

**Required Fields**:
- `userId` - User identification
- `portfolioId` - Portfolio to associate the trade with
- `instrumentInfo.symbol` - Trading instrument symbol
- `instrumentInfo.exchange` - Exchange where traded
- `instrumentInfo.segment` - Market segment
- `status` - Trade status (WIN, LOSS, OPEN, BREAK_EVEN)
- `tradePositionType` - Position type (LONG, SHORT)
- `entryInfo.timestamp` - Entry timestamp
- `entryInfo.price` - Entry price
- `entryInfo.quantity` - Entry quantity

**Optional but Recommended**:
- `exitInfo` - Exit details (required for closed trades)
- `metrics` - Trade performance metrics
- `strategy` - Trading strategy name
- `notes` - Trade notes
- `tags` - Trade tags for categorization

**Response**:
- **201 Created**: Trade successfully created
- **400 Bad Request**: Invalid trade data
- **500 Internal Server Error**: Server error

## Validation Rules

### Controller Validation
1. **Trade ID Consistency**: When updating, path `tradeId` must match request body `tradeId`
2. **User ID Required**: Cannot update trade without valid `userId`

### Service Layer Validation
1. **Required Fields**: `userId`, `portfolioId` must be present
2. **Non-Editable Fields**: When updating, these fields cannot be changed:
   - `symbol`
   - `portfolioId`
   - `userId`
   - `tradeId`
   - Entry price/quantity
   - Entry timestamp

### Business Logic
1. **Trade ID Generation**: Auto-generated UUID if not provided
2. **Calculated Fields**: The system auto-calculates:
   - Profit/Loss (from entry/exit prices and position type)
   - Holding time (from entry/exit timestamps)
   - P/L percentage
   - Symbol (derived from instrumentInfo)

## Sample Payloads

### 1. Nifty Futures Trade (WIN - LONG)
```json
{
  "portfolioId": "{{portfolioId}}",
  "instrumentInfo": {
    "symbol": "NIFTY25JULFUT",
    "exchange": "NSE",
    "segment": "INDEX_FUTURES",
    "series": "FUT",
    "derivativeInfo": {
      "expiryDate": "2025-07-31",
      "underlyingSymbol": "NIFTY",
      "futureType": "MONTHLY",
      "isCashSettled": true
    },
    "currency": "INR",
    "lotSize": "50"
  },
  "strategy": "Momentum Trading",
  "status": "WIN",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-07-01T09:30:00",
    "price": 22450.75,
    "quantity": 2,
    "fees": 125.50
  },
  "exitInfo": {
    "timestamp": "2025-07-01T15:15:00",
    "price": 22675.25,
    "quantity": 2,
    "fees": 127.75
  },
  "notes": "Bought on market momentum, sold at resistance",
  "tags": ["futures", "momentum", "intraday", "win"],
  "userId": "{{userId}}"
}
```

### 2. Equity Trade (OPEN - LONG)
```json
{
  "portfolioId": "{{portfolioId}}",
  "instrumentInfo": {
    "symbol": "RELIANCE",
    "isin": "INE002A01018",
    "exchange": "NSE",
    "segment": "EQUITY",
    "series": "EQ",
    "description": "Reliance Industries Limited",
    "currency": "INR",
    "lotSize": "1"
  },
  "strategy": "Swing Trading",
  "status": "OPEN",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-11-01T10:15:00",
    "price": 2850.50,
    "quantity": 10,
    "fees": 50.00
  },
  "notes": "Entry on support breakout",
  "tags": ["equity", "swing", "open"],
  "userId": "{{userId}}"
}
```

### 3. Bank Nifty Options (LOSS - SHORT)
```json
{
  "portfolioId": "{{portfolioId}}",
  "instrumentInfo": {
    "symbol": "BANKNIFTY25JUL45000CE",
    "exchange": "NSE",
    "segment": "INDEX_OPTIONS",
    "series": "OPT",
    "derivativeInfo": {
      "expiryDate": "2025-07-31",
      "underlyingSymbol": "BANKNIFTY",
      "strikePrice": 45000,
      "isCall": true,
      "isEuropean": true,
      "isCashSettled": true
    },
    "currency": "INR",
    "lotSize": "15"
  },
  "strategy": "Option Selling",
  "status": "LOSS",
  "tradePositionType": "SHORT",
  "entryInfo": {
    "timestamp": "2025-07-01T09:30:00",
    "price": 150.00,
    "quantity": 15,
    "fees": 75.00
  },
  "exitInfo": {
    "timestamp": "2025-07-01T14:00:00",
    "price": 185.00,
    "quantity": 15,
    "fees": 75.00
  },
  "metrics": {
    "profitLoss": -525.00,
    "profitLossPercentage": -23.33,
    "holdingTimeHours": 4.5
  },
  "notes": "Stop loss hit due to unexpected market rally",
  "tags": ["options", "banknifty", "loss", "intraday"],
  "userId": "{{userId}}"
}
```

## Common Mistakes to Avoid

1. ❌ **Don't add `symbol` at root level** - it's auto-derived from `instrumentInfo`
2. ❌ **Don't use custom `tradeExecutions` format** - use proper `TradeModel` structure or leave empty
3. ❌ **Don't use `tradeAnalysisImages`** - use `attachments` with proper `Attachment` objects
4. ❌ **Don't use wrong field names** - `holdingPeriodHours` → `holdingTimeHours`
5. ❌ **Don't omit required fields** - `userId`, `portfolioId`, `status`, `tradePositionType`, `entryInfo`
6. ❌ **Don't mix date formats** - use `LocalDateTime`: `YYYY-MM-DDTHH:mm:ss`
7. ❌ **Don't forget derivativeInfo for derivatives** - futures/options need this

## Testing Your Payload

### Using Postman
1. Import the corrected collection
2. Set environment variables:
   - `baseUrl`: `http://localhost:8073`
   - `userId`: Your user ID
   - `portfolioId`: Your portfolio ID
3. Use the "Add New Trade" request
4. Paste the corrected payload
5. Send the request

### Expected Response (201 Created)
```json
{
  "tradeId": "generated-uuid",
  "portfolioId": "your-portfolio-id",
  "instrumentInfo": { ... },
  "status": "WIN",
  "tradePositionType": "LONG",
  "entryInfo": { ... },
  "exitInfo": { ... },
  "metrics": { ... },
  "userId": "your-user-id",
  ...
}
```

## Additional Resources

- **Controller**: `TradeController.java` - `/api/v1/trades/details`
- **Model**: `TradeDetails.java` - Complete trade model
- **Validation**: `TradeValidator.java` - Field validation logic
- **Service**: `TradeApiService.java` - Business logic

## Postman Variables Reference

```
{{baseUrl}}       = http://localhost:8073
{{userId}}        = sample-user-id or your-user-id
{{portfolioId}}   = 8a57024c-05c2-475b-a2c4-0545865efa4a
{{tradeId}}       = generated after trade creation
```
