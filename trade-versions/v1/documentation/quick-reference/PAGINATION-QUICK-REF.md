# Quick Reference - Filter Trade Details with Pagination

## Basic Usage (No Pagination)
```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","metricsConfig":{"portfolioIds":["163d0143-4fcb-480c-ac20-622f14e0e293"]}}'
```

## With Pagination
```bash
# Page 1 (20 items)
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=0&size=20" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","metricsConfig":{"portfolioIds":["163d0143-4fcb-480c-ac20-622f14e0e293"]}}'

# Page 2 (20 items)
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=1&size=20" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","metricsConfig":{"portfolioIds":["163d0143-4fcb-480c-ac20-622f14e0e293"]}}'
```

## With Sorting
```bash
# Sort by profit/loss (highest first)
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?sort=profitLoss,desc" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","metricsConfig":{"portfolioIds":["163d0143-4fcb-480c-ac20-622f14e0e293"]}}'

# Sort by date (oldest first)
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?sort=entryDate,asc" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","metricsConfig":{"portfolioIds":["163d0143-4fcb-480c-ac20-622f14e0e293"]}}'
```

## Combined: Pagination + Sorting
```bash
curl -X POST "http://localhost:8050/api/v1/trades/details/filter?page=0&size=10&sort=profitLoss,desc" \
  -H "Content-Type: application/json" \
  -d '{"userId":"user123","metricsConfig":{"portfolioIds":["163d0143-4fcb-480c-ac20-622f14e0e293"]}}'
```

## Sort Fields
- `entryDate` - Entry date
- `profitLoss` - P&L amount
- `symbol` - Symbol name
- `status` - Trade status
- `strategy` - Strategy name
- `holdingTimeHours` - Holding duration

## PowerShell
```powershell
$body = @{userId="user123";metricsConfig=@{portfolioIds=@("163d0143-4fcb-480c-ac20-622f14e0e293")}} | ConvertTo-Json -Depth 10
Invoke-RestMethod -Uri "http://localhost:8050/api/v1/trades/details/filter?page=0&size=20&sort=profitLoss,desc" -Method Post -ContentType "application/json" -Body $body
```
