# Filter Trade Details API - Pagination Guide

## Endpoint
`POST /api/v1/trades/details/filter`

## Pagination Support

The API supports optional pagination using Spring Data's `Pageable` interface through query parameters.

### Pagination Parameters (Optional)

- `page`: Page number (0-based, default: 0)
- `size`: Number of items per page (default: 20)
- `sort`: Sort field and direction (format: `field,direction`)

### Available Sort Fields

- `entryDate` / `tradeDate` / `entryTimestamp` - Entry date/time
- `exitDate` / `exitTimestamp` - Exit date/time
- `profitLoss` / `pnl` - Profit/Loss amount
- `symbol` - Trading symbol
- `status` - Trade status (WIN, LOSS, OPEN)
- `strategy` - Trading strategy name
- `holdingTimeHours` / `holdingTime` - Holding duration

### Sort Directions

- `asc` - Ascending order
- `desc` - Descending order

---

## Example cURL Commands

### 1. Without Pagination (All Results)

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
      "tradeCharacteristics": {
        "statuses": ["WIN"]
      }
    }
  }'
```

### 2. With Pagination - First Page (20 items)

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=0&size=20" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"]
    }
  }'
```

### 3. With Pagination - Second Page

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=1&size=20" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"]
    }
  }'
```

### 4. With Sorting - By Profit/Loss (Descending)

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?sort=profitLoss,desc" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
      "tradeCharacteristics": {
        "statuses": ["WIN"]
      }
    }
  }'
```

### 5. With Sorting - By Entry Date (Ascending)

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?sort=entryDate,asc" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"]
    }
  }'
```

### 6. With Pagination AND Sorting

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=0&size=10&sort=profitLoss,desc" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
      "tradeCharacteristics": {
        "statuses": ["WIN", "LOSS"]
      }
    }
  }'
```

### 7. Multiple Sort Fields

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?sort=status,asc&sort=profitLoss,desc" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"]
    }
  }'
```

### 8. Large Page Size for Export

```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=0&size=1000&sort=entryDate,desc" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user123",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
      "dateRange": {
        "startDate": "2025-01-01",
        "endDate": "2025-12-31"
      }
    }
  }'
```

---

## PowerShell Examples

### Basic Pagination

```powershell
$body = @{
    userId = "user123"
    metricsConfig = @{
        portfolioIds = @("163d0143-4fcb-480c-ac20-622f14e0e293")
        tradeCharacteristics = @{
            statuses = @("WIN")
        }
    }
} | ConvertTo-Json -Depth 10

Invoke-RestMethod -Uri "http://localhost:8050/api/v1/trades/details/filter?page=0&size=20&sort=profitLoss,desc" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

### With Favorite Filter and Pagination

```powershell
$body = @{
    userId = "user123"
    favoriteFilterId = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8050/api/v1/trades/details/filter?page=0&size=50&sort=entryDate,desc" `
  -Method Post `
  -ContentType "application/json" `
  -Body $body
```

---

## Response Structure (With Pagination)

```json
{
  "trades": [
    {
      "tradeId": "trade-001",
      "symbol": "NIFTY25JULFUT",
      "status": "WIN",
      "metrics": {
        "profitLoss": 5000.0
      }
    }
  ],
  "totalCount": 150,
  "page": 0,
  "size": 20,
  "totalPages": 8,
  "isFirst": true,
  "isLast": false,
  "appliedFilterName": "My Favorite Filter",
  "filterSummary": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "symbols": ["NIFTY"],
    "statuses": ["WIN"],
    "dateRange": "2025-01-01 to 2025-12-31"
  }
}
```

## Response Structure (Without Pagination)

```json
{
  "trades": [ /* all matching trades */ ],
  "totalCount": 150,
  "appliedFilterName": "My Favorite Filter",
  "filterSummary": { /* filter details */ }
}
```

---

## Notes

1. **Pagination is Optional**: If no pagination parameters are provided, all results are returned.

2. **Default Values**: 
   - Default page: 0 (first page)
   - Default size: 20 items per page

3. **Sorting Priority**: When multiple sort fields are provided, they are applied in order.

4. **Performance**: Use pagination for large result sets to improve performance.

5. **Total Count**: The `totalCount` field always shows the total number of matching trades, regardless of pagination.

6. **Page Navigation**: Use `isFirst` and `isLast` flags to determine if you're on the first or last page.
