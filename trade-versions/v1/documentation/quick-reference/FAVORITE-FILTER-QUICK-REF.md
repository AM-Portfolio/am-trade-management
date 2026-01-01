# Favorite Filter API - Quick Reference Card

## 🚀 Quick Start

### Schema Location
```
/postman/favorite-filter-api-schema.json
```

### Base URL
```
http://localhost:8073/api/v1/filters
```

---

## 📋 Core Models

### FavoriteFilterRequest
```json
{
  "name": "string (required, max 100 chars)",
  "description": "string (optional, max 500 chars)",
  "isDefault": "boolean (default: false)",
  "filterConfig": "MetricsFilterConfig (required)"
}
```

### MetricsFilterConfig ⚠️ IMPORTANT STRUCTURE
```json
{
  "portfolioIds": ["uuid"],
  "instruments": ["string"],  // NOT "symbols"!
  "tradeCharacteristics": {   // statuses, strategies, tags go HERE
    "statuses": ["LOSS", "WIN", "CLOSED"],
    "strategies": ["Momentum Trading"],
    "tags": ["intraday"],
    "directions": ["LONG", "SHORT"],
    "minHoldingTimeHours": 1,
    "maxHoldingTimeHours": 24
  },
  "profitLossFilters": {
    "minProfitLoss": -10000.00,
    "maxProfitLoss": 50000.00
  },
  "dateRange": {
    "startDate": "2025-01-01",
    "endDate": "2025-12-31"
  }
}
```

### FavoriteFilterResponse
```json
{
  "id": "uuid",
  "name": "string",
  "description": "string",
  "createdAt": "ISO 8601 datetime",
  "updatedAt": "ISO 8601 datetime",
  "isDefault": "boolean",
  "filterConfig": "MetricsFilterConfig"
}
```

---

## 🔧 Common API Calls

### 1️⃣ Create Filter
```bash
POST /api/v1/filters?userId=ssd2658
Content-Type: application/json

{
  "name": "High Risk Trades",
  "description": "Filters for high-risk intraday trades",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "instruments": ["NIFTY", "BANKNIFTY"],
    "tradeCharacteristics": {
      "statuses": ["LOSS"],
      "strategies": ["Momentum Trading"],
      "tags": ["intraday", "high-risk"],
      "maxHoldingTimeHours": 24
    },
    "profitLossFilters": {
      "minProfitLoss": -10000.00
    }
  }
}
```

### 2️⃣ Get All Filters
```bash
GET /api/v1/filters?userId=ssd2658
```

### 3️⃣ Get Filter by ID
```bash
GET /api/v1/filters/{filterId}?userId=ssd2658
```

### 4️⃣ Update Filter
```bash
PUT /api/v1/filters/{filterId}?userId=ssd2658
Content-Type: application/json

{
  "name": "Updated Filter Name",
  "filterConfig": { ... }
}
```

### 5️⃣ Delete Single Filter
```bash
DELETE /api/v1/filters/{filterId}?userId=ssd2658
```

### 6️⃣ Bulk Delete Filters
```bash
DELETE /api/v1/filters/bulk
Content-Type: application/json

{
  "userId": "ssd2658",
  "filterIds": [
    "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "a12bc34d-56ef-7890-b123-4567890abcde"
  ]
}
```

### 7️⃣ Get Default Filter
```bash
GET /api/v1/filters/default?userId=ssd2658
```

### 8️⃣ Set as Default
```bash
PUT /api/v1/filters/{filterId}/default?userId=ssd2658
```

### 9️⃣ Apply Filter (Get Metrics)
```bash
GET /api/v1/filters/apply/{filterId}?userId=ssd2658&portfolioIds=uuid1,uuid2
```

---

## 🎯 Code Generation Commands

### Java (Maven)
```bash
mvn jsonschema2pojo:generate
# or
mvn openapi-generator:generate
```

### Python (Pydantic)
```bash
datamodel-codegen \
  --input postman/favorite-filter-api-schema.json \
  --output generated/models.py \
  --output-model-type pydantic.BaseModel
```

### Flutter/Dart
```bash
quicktype \
  --src postman/favorite-filter-api-schema.json \
  --lang dart \
  --out lib/models/favorite_filter_models.dart \
  --null-safety
```

### TypeScript
```bash
quicktype \
  --src postman/favorite-filter-api-schema.json \
  --lang typescript \
  --out src/models/FavoriteFilterModels.ts \
  --just-types
```

---

## ⚠️ Common Mistakes

### ❌ WRONG: Statuses at wrong level
```json
{
  "metricsConfig": {
    "portfolioIds": ["uuid"],
    "statuses": ["LOSS"]  // ❌ WRONG LEVEL!
  }
}
```

### ✅ CORRECT: Statuses inside tradeCharacteristics
```json
{
  "metricsConfig": {
    "portfolioIds": ["uuid"],
    "tradeCharacteristics": {
      "statuses": ["LOSS"]  // ✅ CORRECT!
    }
  }
}
```

### ❌ WRONG: Using "symbols" instead of "instruments"
```json
{
  "metricsConfig": {
    "symbols": ["NIFTY"]  // ❌ Wrong field name
  }
}
```

### ✅ CORRECT: Using "instruments"
```json
{
  "metricsConfig": {
    "instruments": ["NIFTY"]  // ✅ Correct field name
  }
}
```

---

## 📊 Filter Configuration Fields

### Metric Types (metricTypes)
Available values:
- `PERFORMANCE` - Overall performance metrics
- `RISK` - Risk analysis metrics
- `DISTRIBUTION` - Trade distribution analysis
- `TIMING` - Entry/exit timing analysis
- `PATTERN` - Pattern recognition metrics
- `PROFIT_LOSS` - P&L specific metrics
- `WIN_RATE` - Win rate calculations
- `RISK_REWARD` - Risk/reward ratio analysis
- `DRAWDOWN` - Drawdown analysis
- `SHARPE_RATIO` - Sharpe ratio calculations

### Group By Dimensions (groupBy)
Available values:
- `STRATEGY` - Group by trading strategy
- `SYMBOL` - Group by instrument symbol
- `DAY_OF_WEEK` - Group by day of week
- `MONTH` - Group by month
- `PORTFOLIO` - Group by portfolio
- `TRADE_TYPE` - Group by trade type
- `INSTRUMENT_TYPE` - Group by instrument type
- `DIRECTION` - Group by long/short direction

### Trade Characteristics (tradeCharacteristics)
| Field | Type | Possible Values | Example |
|-------|------|-----------------|---------|
| statuses | string[] | OPEN, CLOSED, WIN, LOSS, BREAKEVEN, CANCELLED | ["LOSS", "WIN", "CLOSED"] |
| strategies | string[] | Any custom strategy names | ["Momentum Trading", "Scalping"] |
| tags | string[] | Any custom tags | ["intraday", "high-risk"] |
| directions | string[] | LONG, SHORT | ["LONG", "SHORT"] |
| minHoldingTimeHours | integer | 0+ | 1 |
| maxHoldingTimeHours | integer | 0+ | 168 (7 days) |

### Profit/Loss Filters (profitLossFilters)
| Field | Type | Example |
|-------|------|---------|
| minProfitLoss | number | -10000.00 |
| maxProfitLoss | number | 50000.00 |
| minPositionSize | number | 1000.00 |
| maxPositionSize | number | 100000.00 |

### Instrument Filters (instrumentFilters)
| Field | Type | Example |
|-------|------|---------|
| marketSegments | string[] | ["INDEX_OPTIONS", "EQUITY_OPTIONS"] |
| baseSymbols | string[] | ["NIFTY", "BANKNIFTY"] |
| indexTypes | string[] | ["NIFTY", "BANKNIFTY"] |
| derivativeTypes | string[] | ["FUTURES", "OPTIONS"] |

### Date Range (dateRange)
| Field | Type | Example |
|-------|------|---------|
| startDate | date | "2025-01-01" |
| endDate | date | "2025-12-31" |

---

## 🔄 Response Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success |
| 201 | Created |
| 204 | No Content (Delete Success) |
| 400 | Bad Request |
| 404 | Not Found |
| 500 | Internal Server Error |

---

## 📚 Documentation Files

| File | Description |
|------|-------------|
| `favorite-filter-api-schema.json` | Complete JSON Schema |
| `SCHEMA-CODE-GENERATION-GUIDE.md` | Detailed code generation guide |
| `FavoriteFilterController.postman_collection.json` | Postman collection |
| `FILTER-IMPLEMENTATION-SUMMARY.md` | Implementation summary |
| `QUICK-REFERENCE.md` | This file |

---

## 🎨 Sample Payloads

### Minimal Create Request
```json
{
  "name": "My Filter",
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"]
  }
}
```

### Complete Create Request
```json
{
  "name": "Comprehensive Filter Example",
  "description": "Filter with all possible configuration options",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "dateRange": {
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    },
    "timePeriod": {
      "period": "LAST_30_DAYS",
      "customStartDate": null,
      "customEndDate": null
    },
    "metricTypes": [
      "PERFORMANCE",
      "RISK",
      "DISTRIBUTION",
      "TIMING",
      "PATTERN",
      "PROFIT_LOSS",
      "WIN_RATE",
      "RISK_REWARD",
      "DRAWDOWN",
      "SHARPE_RATIO"
    ],
    "instruments": [
      "NIFTY",
      "BANKNIFTY",
      "FINNIFTY",
      "NIFTY25JULFUT",
      "RELIANCE",
      "TCS",
      "INFY"
    ],
    "instrumentFilters": {
      "marketSegments": [
        "EQUITY",
        "INDEX",
        "EQUITY_FUTURES",
        "INDEX_FUTURES",
        "EQUITY_OPTIONS",
        "INDEX_OPTIONS"
      ],
      "baseSymbols": ["NIFTY", "BANKNIFTY", "RELIANCE", "TCS"],
      "indexTypes": ["NIFTY", "BANKNIFTY", "FINNIFTY"],
      "derivativeTypes": ["FUTURES", "OPTIONS"]
    },
    "tradeCharacteristics": {
      "strategies": [
        "Momentum Trading",
        "Scalping",
        "Swing Trading",
        "Options Strategy",
        "Breakout Strategy",
        "Mean Reversion"
      ],
      "tags": [
        "futures",
        "momentum",
        "intraday",
        "win",
        "loss",
        "nifty",
        "index-futures",
        "technical-setup",
        "bullish-flag"
      ],
      "directions": ["LONG", "SHORT"],
      "statuses": ["OPEN", "CLOSED", "CANCELLED"],
      "minHoldingTimeHours": 1,
      "maxHoldingTimeHours": 168
    },
    "profitLossFilters": {
      "minProfitLoss": -10000.0,
      "maxProfitLoss": 50000.0,
      "minPositionSize": 1000.0,
      "maxPositionSize": 100000.0
    },
    "groupBy": [
      "STRATEGY",
      "SYMBOL",
      "DAY_OF_WEEK",
      "MONTH",
      "TRADE_TYPE",
      "INSTRUMENT_TYPE",
      "DIRECTION"
    ]
  }
}
```

---

## 🔗 Related Endpoints

- **Trade Details Filter**: `POST /api/v1/trades/details/filter`
- **Trade Summary**: `POST /api/v1/trade-summary`
- **Portfolio Summary**: `POST /api/v1/portfolio-summary`
- **Trade Metrics**: `POST /api/v1/metrics`

---

## 💡 Pro Tips

1. **Always use `instruments`**, never `symbols`
2. **Nest filter criteria properly**: statuses/strategies/tags go inside `tradeCharacteristics`
3. **Use UUIDs** for portfolioIds and filterIds
4. **Date format**: ISO 8601 (YYYY-MM-DD)
5. **DateTime format**: ISO 8601 with timezone (2025-01-15T10:30:00Z)
6. **Test with Postman** before integrating into code
7. **Generate models** from schema to ensure type safety

---

## 📞 Support

For detailed documentation, see:
- `SCHEMA-CODE-GENERATION-GUIDE.md`
- `FILTER-IMPLEMENTATION-SUMMARY.md`
- Spring Boot application logs

---

**Last Updated**: 2025-01-19
**API Version**: 1.0.0
**Schema Version**: 1.0.0
