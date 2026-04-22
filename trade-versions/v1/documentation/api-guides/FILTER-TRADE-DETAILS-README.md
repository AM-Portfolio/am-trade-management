# Filter Trade Details with Favorite Filter

## Example Payloads

### 1. Filter with Custom Criteria Only (No Saved Filter)
```json
{
  "userId": "user123",
  "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
  "symbols": ["NIFTY", "BANKNIFTY"],
  "statuses": ["WIN", "LOSS"],
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "strategies": ["Momentum Trading", "Scalping"],
  "minProfitLoss": -5000.0,
  "maxProfitLoss": 50000.0,
  "tradePositionTypes": ["LONG"],
  "tags": ["intraday", "futures"],
  "minHoldingTimeHours": 1,
  "maxHoldingTimeHours": 24
}
```

### 2. Filter Using Saved Favorite Filter
```json
{
  "userId": "user123",
  "favoriteFilterId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
}
```

### 3. Filter Using Saved Filter with Overrides
```json
{
  "userId": "user123",
  "favoriteFilterId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
  "symbols": ["NIFTY"],
  "startDate": "2025-11-01",
  "endDate": "2025-11-30"
}
```

### 4. Filter Winning Trades Only
```json
{
  "userId": "user123",
  "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
  "statuses": ["WIN"],
  "minProfitLoss": 0.01
}
```

### 5. Filter Intraday Trades
```json
{
  "userId": "user123",
  "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
  "maxHoldingTimeHours": 24,
  "tags": ["intraday"]
}
```

### 6. Filter Recent Losses
```json
{
  "userId": "user123",
  "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
  "statuses": ["LOSS"],
  "maxProfitLoss": -0.01,
  "startDate": "2025-11-12",
  "endDate": "2025-11-19"
}
```

## cURL Examples

### Filter with Custom Criteria
```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "symbols": ["NIFTY", "BANKNIFTY"],
    "statuses": ["WIN"],
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "minProfitLoss": 0.01
  }'
```

### Filter Using Saved Favorite Filter
```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "favoriteFilterId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
  }'
```

### Filter with Saved Filter + Overrides
```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "favoriteFilterId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "symbols": ["NIFTY"],
    "startDate": "2025-11-01",
    "endDate": "2025-11-30"
  }'
```

## PowerShell Examples

### Filter with Custom Criteria
```powershell
$body = @{
    userId = "user123"
    portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
    symbols = @("NIFTY", "BANKNIFTY")
    statuses = @("WIN")
    startDate = "2025-01-01"
    endDate = "2025-12-31"
    minProfitLoss = 0.01
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8050/api/v1/trades/details/filter" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

### Filter Using Saved Favorite Filter
```powershell
$body = @{
    userId = "user123"
    favoriteFilterId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8050/api/v1/trades/details/filter" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

### Filter Intraday Trades
```powershell
$body = @{
    userId = "user123"
    portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
    maxHoldingTimeHours = 24
    tags = @("intraday")
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8050/api/v1/trades/details/filter" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

## Response Example

```json
{
  "trades": [
    {
      "tradeId": "e4bc9a71-9716-4be6-a679-90ede072c084",
      "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
      "symbol": "NIFTY25JULFUT",
      "strategy": "Momentum Trading",
      "status": "WIN",
      "metrics": {
        "profitLoss": 22450.0,
        "profitLossPercentage": 1.0,
        "holdingTimeHours": 5
      }
    }
  ],
  "totalCount": 1,
  "appliedFilterName": "Winning Trades Only",
  "filterSummary": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "symbols": ["NIFTY"],
    "statuses": ["WIN"],
    "dateRange": "2025-01-01 to 2025-12-31",
    "strategies": null,
    "profitLossRange": "0.01 to any",
    "holdingTimeRange": null
  }
}
```

## Available Filter Fields

| Field | Type | Description | Example |
|-------|------|-------------|---------|
| `userId` | String | **Required**. User ID | `"user123"` |
| `favoriteFilterId` | String | Optional. Saved filter ID to apply | `"uuid"` |
| `portfolioIds` | List<String> | Portfolio IDs to filter | `["PORT-001"]` |
| `symbols` | List<String> | Trading symbols | `["NIFTY", "BANKNIFTY"]` |
| `statuses` | List<TradeStatus> | Trade statuses | `["WIN", "LOSS", "OPEN"]` |
| `startDate` | LocalDate | Start date (YYYY-MM-DD) | `"2025-01-01"` |
| `endDate` | LocalDate | End date (YYYY-MM-DD) | `"2025-12-31"` |
| `strategies` | List<String> | Trading strategies | `["Momentum Trading"]` |
| `minProfitLoss` | Double | Minimum P&L | `0.01` |
| `maxProfitLoss` | Double | Maximum P&L | `50000.0` |
| `tradePositionTypes` | List<String> | Position types | `["LONG", "SHORT"]` |
| `tags` | List<String> | Trade tags | `["intraday", "futures"]` |
| `minHoldingTimeHours` | Integer | Min holding time | `1` |
| `maxHoldingTimeHours` | Integer | Max holding time | `24` |

## Notes

- If `favoriteFilterId` is provided, the saved filter configuration will be loaded and merged with the request
- Request parameters override saved filter parameters
- At least `userId` is required
- If using a saved filter, `portfolioIds` from the saved filter will be used unless overridden
- All filters are applied with AND logic (all conditions must match)
- Tags filter uses OR logic (trade must have at least one matching tag)
