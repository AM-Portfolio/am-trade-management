# Filter Payload Examples

This directory contains comprehensive examples of filter payloads for the AM Trade Management System's Favorite Filters API.

## Available Filter Examples

### 1. **comprehensive-filter-payload.json**
Complete example showing ALL available filter options and configurations.

**Use case**: Understanding all available filter parameters
- All metric types
- All instrument filters
- All trade characteristics
- Complete date range and time period options
- Profit/loss filters
- Group by options

### 2. **filter-winning-trades.json**
Filter to show only profitable/winning trades.

**Use case**: Analyzing successful trades
- Only closed trades with positive P&L
- Metrics: Profit/Loss, Win Rate, Risk/Reward

**curl example**:
```bash
curl -X POST "http://localhost:8050/api/v1/filters?userId=user123" \
  -H "Content-Type: application/json" \
  -d @filter-winning-trades.json
```

### 3. **filter-intraday-trades.json**
Filter for intraday trades (holding time less than 1 day).

**Use case**: Intraday trading performance analysis
- Last 30 days
- Holding time < 24 hours
- Tagged as "intraday"
- Metrics: Performance, Timing

### 4. **filter-nifty-options.json**
NIFTY options trades from current month.

**Use case**: Index options analysis
- Current month only
- NIFTY index options
- Group by Strategy and Day of Week
- Metrics: Performance, Risk, Distribution

### 5. **filter-high-risk-trades.json**
Trades with position size greater than 50,000.

**Use case**: High-value trade analysis
- Position size > 50,000
- Metrics: Risk, Profit/Loss
- Grouped by Symbol and Strategy

### 6. **filter-momentum-strategy.json**
Analysis of momentum trading strategy.

**Use case**: Strategy-specific performance
- Last 90 days
- Momentum trading strategy only
- Group by Symbol, Day of Week, Direction
- Metrics: Performance, Pattern, Win Rate

### 7. **filter-recent-losses.json**
Losing trades from last 7 days for review.

**Use case**: Recent loss analysis and learning
- Last 7 days
- Only negative P&L
- Closed trades
- Metrics: Profit/Loss, Risk

## Filter Configuration Structure

### Main Filter Object
```json
{
  "name": "Filter Name",
  "description": "Filter Description",
  "isDefault": false,
  "filterConfig": { ... }
}
```

### FilterConfig Properties

#### portfolioIds (Required)
```json
"portfolioIds": ["PORT-001", "PORT-002"]
```

#### dateRange
```json
"dateRange": {
  "startDate": "2025-01-01",
  "endDate": "2025-12-31"
}
```

#### timePeriod
Predefined time periods for quick selection:
- `TODAY`
- `YESTERDAY`
- `THIS_WEEK`
- `LAST_WEEK`
- `THIS_MONTH`
- `LAST_MONTH`
- `LAST_7_DAYS`
- `LAST_30_DAYS`
- `LAST_90_DAYS`
- `THIS_QUARTER`
- `LAST_QUARTER`
- `THIS_YEAR`
- `LAST_YEAR`

```json
"timePeriod": {
  "period": "LAST_30_DAYS"
}
```

#### metricTypes
Available metric types:
- `PERFORMANCE` - Overall performance metrics
- `RISK` - Risk-related metrics
- `DISTRIBUTION` - Distribution analysis
- `TIMING` - Timing-related metrics
- `PATTERN` - Pattern analysis
- `PROFIT_LOSS` - P&L metrics
- `WIN_RATE` - Win/loss rate
- `RISK_REWARD` - Risk/reward ratios
- `DRAWDOWN` - Drawdown analysis
- `SHARPE_RATIO` - Sharpe ratio

```json
"metricTypes": ["PROFIT_LOSS", "WIN_RATE", "RISK_REWARD"]
```

#### instruments
Filter by specific trading symbols:
```json
"instruments": ["NIFTY", "BANKNIFTY", "RELIANCE"]
```

#### instrumentFilters
Detailed instrument filtering:
```json
"instrumentFilters": {
  "marketSegments": ["EQUITY", "INDEX_FUTURES", "INDEX_OPTIONS"],
  "baseSymbols": ["NIFTY", "BANKNIFTY"],
  "indexTypes": ["NIFTY", "BANKNIFTY"],
  "derivativeTypes": ["FUTURES", "OPTIONS"]
}
```

**Market Segments**:
- `EQUITY`
- `INDEX`
- `EQUITY_FUTURES`
- `INDEX_FUTURES`
- `EQUITY_OPTIONS`
- `INDEX_OPTIONS`

#### tradeCharacteristics
Filter by trade attributes:
```json
"tradeCharacteristics": {
  "strategies": ["Momentum Trading", "Scalping"],
  "tags": ["intraday", "win"],
  "directions": ["LONG", "SHORT"],
  "statuses": ["OPEN", "CLOSED"],
  "minHoldingTimeHours": 1,
  "maxHoldingTimeHours": 24
}
```

**Trade Directions**:
- `LONG`
- `SHORT`

**Trade Statuses**:
- `OPEN`
- `CLOSED`
- `CANCELLED`

#### profitLossFilters
Filter by P&L and position size:
```json
"profitLossFilters": {
  "minProfitLoss": -10000.0,
  "maxProfitLoss": 50000.0,
  "minPositionSize": 1000.0,
  "maxPositionSize": 100000.0
}
```

#### groupBy
Group results by dimensions:
- `STRATEGY`
- `SYMBOL`
- `DAY_OF_WEEK`
- `MONTH`
- `TRADE_TYPE`
- `INSTRUMENT_TYPE`
- `DIRECTION`

```json
"groupBy": ["STRATEGY", "SYMBOL", "DAY_OF_WEEK"]
```

## API Endpoints

### Create Filter
```bash
POST /api/v1/filters?userId={userId}
Content-Type: application/json

Body: {filter payload}
```

### Update Filter
```bash
PUT /api/v1/filters/{filterId}?userId={userId}
Content-Type: application/json

Body: {filter payload}
```

### Get All Filters
```bash
GET /api/v1/filters?userId={userId}
```

### Get Specific Filter
```bash
GET /api/v1/filters/{filterId}?userId={userId}
```

### Delete Filter
```bash
DELETE /api/v1/filters/{filterId}?userId={userId}
```

### Get Default Filter
```bash
GET /api/v1/filters/default?userId={userId}
```

### Set Filter as Default
```bash
PUT /api/v1/filters/{filterId}/default?userId={userId}
```

### Apply Filter to Get Metrics
```bash
GET /api/v1/filters/apply/{filterId}?userId={userId}&portfolioIds={portfolioId}
```

## Complete Example Workflow

### 1. Create a new filter for winning trades
```bash
curl -X POST "http://localhost:8050/api/v1/filters?userId=user123" \
  -H "Content-Type: application/json" \
  -d @filter-winning-trades.json
```

Response:
```json
{
  "id": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "name": "Winning Trades Only",
  "description": "Filter to show only profitable/winning trades",
  "isDefault": false,
  "createdAt": "2025-11-19T10:30:00",
  "updatedAt": "2025-11-19T10:30:00",
  "filterConfig": { ... }
}
```

### 2. Set it as default
```bash
curl -X PUT "http://localhost:8050/api/v1/filters/a1b2c3d4-e5f6-7890-abcd-ef1234567890/default?userId=user123" \
  -H "Content-Type: application/json"
```

### 3. Apply filter to get metrics
```bash
curl -X GET "http://localhost:8050/api/v1/filters/apply/a1b2c3d4-e5f6-7890-abcd-ef1234567890?userId=user123" \
  -H "Content-Type: application/json"
```

### 4. Update filter
```bash
curl -X PUT "http://localhost:8050/api/v1/filters/a1b2c3d4-e5f6-7890-abcd-ef1234567890?userId=user123" \
  -H "Content-Type: application/json" \
  -d @filter-winning-trades.json
```

### 5. Delete filter
```bash
curl -X DELETE "http://localhost:8050/api/v1/filters/a1b2c3d4-e5f6-7890-abcd-ef1234567890?userId=user123" \
  -H "Content-Type: application/json"
```

## PowerShell Examples

### Create Filter
```powershell
$body = Get-Content -Path "filter-winning-trades.json" -Raw
Invoke-RestMethod -Uri "http://localhost:8050/api/v1/filters?userId=user123" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

### Get All Filters
```powershell
Invoke-RestMethod -Uri "http://localhost:8050/api/v1/filters?userId=user123" `
  -Method Get `
  -ContentType "application/json"
```

### Apply Filter
```powershell
$filterId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
Invoke-RestMethod -Uri "http://localhost:8050/api/v1/filters/apply/$filterId?userId=user123" `
  -Method Get `
  -ContentType "application/json"
```

## Notes

- All date fields use ISO 8601 format: `YYYY-MM-DD`
- All datetime fields use ISO 8601 format: `YYYY-MM-DDTHH:mm:ss`
- Filter IDs are UUIDs generated automatically on creation
- User ID is required for all filter operations
- Setting a filter as default will clear any existing default filter
- Filters are user-specific and isolated by userId
