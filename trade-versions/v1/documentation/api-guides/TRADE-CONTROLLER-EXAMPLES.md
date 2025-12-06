# Trade Controller API - Examples Library

## Table of Contents
- [Create Trade Request Examples](#create-trade-request-examples)
- [Update Trade Request Examples](#update-trade-request-examples)
- [Filter Trade Request Examples](#filter-trade-request-examples)
- [Response Examples](#response-examples)
- [cURL Command Examples](#curl-command-examples)

---

## Create Trade Request Examples

### Example 1: Minimal Trade (Required Fields Only)

```json
{
  "tradeId": "TRD-2025-001",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "NIFTY",
    "rawSymbol": "NIFTY",
    "exchange": "NSE",
    "segment": "INDEX_OPTIONS"
  },
  "status": "OPEN",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-15T10:30:00Z",
    "price": 22500.00,
    "quantity": 25
  },
  "userId": "ssd2658"
}
```

### Example 2: Options Trade - Bank Nifty Call

```json
{
  "tradeId": "TRD-2025-002",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "BANKNIFTY",
    "rawSymbol": "BANKNIFTY2091722500CE",
    "exchange": "NSE",
    "segment": "INDEX_OPTIONS",
    "indexType": "BANKNIFTY",
    "derivativeInfo": {
      "derivativeType": "OPTIONS",
      "strikePrice": 22500.00,
      "expiryDate": "2025-09-17",
      "optionType": "CALL",
      "underlyingSymbol": "BANKNIFTY"
    },
    "lotSize": "15"
  },
  "symbol": "BANKNIFTY",
  "strategy": "Momentum Trading",
  "status": "CLOSED",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-15T10:30:00Z",
    "price": 125.50,
    "quantity": 15,
    "totalValue": 1882.50,
    "fees": 25.00,
    "reason": "Breakout above resistance with volume confirmation"
  },
  "exitInfo": {
    "timestamp": "2025-01-15T14:45:00Z",
    "price": 185.75,
    "quantity": 15,
    "totalValue": 2786.25,
    "fees": 25.00,
    "reason": "Target achieved at 2:1 risk-reward"
  },
  "metrics": {
    "profitLoss": 853.75,
    "profitLossPercentage": 45.32,
    "returnOnEquity": 42.5,
    "riskAmount": 375.00,
    "rewardAmount": 903.75,
    "riskRewardRatio": 2.41,
    "holdingTimeHours": 4,
    "holdingTimeMinutes": 255,
    "maxAdverseExcursion": -50.00,
    "maxFavorableExcursion": 920.00
  },
  "notes": "Excellent trade with disciplined execution",
  "tags": ["intraday", "options", "momentum", "high-probability"],
  "userId": "ssd2658"
}
```

### Example 3: Complete Trade with Psychology & Reasoning

```json
{
  "tradeId": "TRD-2025-003",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "NIFTY",
    "rawSymbol": "NIFTY25JAN23000CE",
    "exchange": "NSE",
    "segment": "INDEX_OPTIONS",
    "indexType": "NIFTY",
    "derivativeInfo": {
      "derivativeType": "OPTIONS",
      "strikePrice": 23000.00,
      "expiryDate": "2025-01-30",
      "optionType": "CALL",
      "underlyingSymbol": "NIFTY"
    },
    "description": "Nifty 50 Index Call Option",
    "currency": "INR",
    "lotSize": "50"
  },
  "symbol": "NIFTY",
  "strategy": "Scalping",
  "status": "WIN",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-15T09:30:00Z",
    "price": 95.00,
    "quantity": 50,
    "totalValue": 4750.00,
    "fees": 35.00,
    "reason": "Strong bullish momentum with RSI divergence"
  },
  "exitInfo": {
    "timestamp": "2025-01-15T15:30:00Z",
    "price": 125.00,
    "quantity": 50,
    "totalValue": 6250.00,
    "fees": 35.00,
    "reason": "Profit target reached, resistance at 125 level"
  },
  "metrics": {
    "profitLoss": 1430.00,
    "profitLossPercentage": 30.11,
    "returnOnEquity": 28.5,
    "riskAmount": 500.00,
    "rewardAmount": 1500.00,
    "riskRewardRatio": 3.0,
    "holdingTimeHours": 6,
    "holdingTimeMinutes": 360,
    "maxAdverseExcursion": -150.00,
    "maxFavorableExcursion": 1550.00
  },
  "notes": "Perfect execution of scalping strategy with proper risk management",
  "tags": ["intraday", "scalping", "nifty-options", "winning-trade"],
  "userId": "ssd2658",
  "attachments": [
    {
      "fileName": "entry_chart.png",
      "fileUrl": "https://storage.example.com/trades/TRD-2025-003/entry_chart.png",
      "fileType": "image/png",
      "uploadedAt": "2025-01-15T09:35:00Z",
      "description": "Entry point chart with technical indicators"
    },
    {
      "fileName": "exit_chart.png",
      "fileUrl": "https://storage.example.com/trades/TRD-2025-003/exit_chart.png",
      "fileType": "image/png",
      "uploadedAt": "2025-01-15T15:35:00Z",
      "description": "Exit point chart showing target achievement"
    }
  ],
  "psychologyData": {
    "entryPsychologyFactors": ["CALM_ANALYSIS", "DISCIPLINE", "PATIENCE"],
    "exitPsychologyFactors": ["TARGET_ACHIEVED", "PLAN_FOLLOWED", "RATIONAL_EXIT"],
    "behaviorPatterns": ["DISCIPLINED_TRADING", "PLAN_ADHERENCE", "RULE_FOLLOWING"],
    "categorizedTags": {
      "STRATEGY": ["scalping", "momentum"],
      "RISK": ["calculated", "managed"],
      "PERFORMANCE": ["winning", "profitable"],
      "TIMING": ["intraday", "quick-exit"]
    },
    "psychologyNotes": "Maintained discipline throughout. Followed the plan without emotional interference."
  },
  "entryReasoning": {
    "technicalReasons": ["BREAKOUT", "MOVING_AVERAGE_CROSS", "RSI_DIVERGENCE"],
    "fundamentalReasons": ["MARKET_SENTIMENT", "SECTOR_STRENGTH"],
    "primaryReason": "Breakout above 22,950 resistance with increasing volume",
    "reasoningSummary": "Clear technical setup with multiple confirmations. RSI showing bullish divergence while price broke above key resistance. Volume spike confirmed the breakout.",
    "confidenceLevel": 8,
    "supportingIndicators": [
      "RSI > 60",
      "MACD bullish crossover",
      "Volume 1.5x average",
      "Price above 20 EMA"
    ],
    "conflictingIndicators": [
      "Slightly overbought on hourly chart"
    ],
    "streategy": "Scalping"
  },
  "exitReasoning": {
    "technicalReasons": ["RESISTANCE_BREAK", "TREND_FOLLOWING"],
    "fundamentalReasons": [],
    "exitPrimaryReason": "Target achieved at 125 resistance level",
    "exitReasoningSummary": "Reached planned profit target of 30%. Price showed signs of rejection at 125 level, which was predetermined resistance.",
    "exitConfidenceLevel": 9,
    "exitSupportingIndicators": [
      "Target reached (30% gain)",
      "Risk-reward ratio of 3:1 satisfied",
      "Resistance at 125 confirmed"
    ],
    "exitConflictingIndicators": [],
    "exitQualityScore": 9,
    "streategy": "Scalping"
  }
}
```

### Example 4: Loss Trade with Analysis

```json
{
  "tradeId": "TRD-2025-004",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "BANKNIFTY",
    "rawSymbol": "BANKNIFTY25JAN44000PE",
    "exchange": "NSE",
    "segment": "INDEX_OPTIONS",
    "indexType": "BANKNIFTY",
    "derivativeInfo": {
      "derivativeType": "OPTIONS",
      "strikePrice": 44000.00,
      "expiryDate": "2025-01-30",
      "optionType": "PUT",
      "underlyingSymbol": "BANKNIFTY"
    },
    "lotSize": "15"
  },
  "symbol": "BANKNIFTY",
  "strategy": "Swing Trading",
  "status": "LOSS",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-14T10:00:00Z",
    "price": 180.00,
    "quantity": 15,
    "totalValue": 2700.00,
    "fees": 25.00,
    "reason": "Expected reversal from resistance"
  },
  "exitInfo": {
    "timestamp": "2025-01-14T14:30:00Z",
    "price": 150.00,
    "quantity": 15,
    "totalValue": 2250.00,
    "fees": 25.00,
    "reason": "Stop loss triggered"
  },
  "metrics": {
    "profitLoss": -500.00,
    "profitLossPercentage": -18.52,
    "returnOnEquity": -17.5,
    "riskAmount": 450.00,
    "rewardAmount": 900.00,
    "riskRewardRatio": 2.0,
    "holdingTimeHours": 4,
    "holdingTimeMinutes": 270,
    "maxAdverseExcursion": -550.00,
    "maxFavorableExcursion": 50.00
  },
  "notes": "Stop loss properly executed. Market didn't respect resistance as expected.",
  "tags": ["loss", "stop-loss-hit", "swing-trade", "learning-opportunity"],
  "userId": "ssd2658",
  "psychologyData": {
    "entryPsychologyFactors": ["CALM_ANALYSIS", "PATIENCE"],
    "exitPsychologyFactors": ["STOP_LOSS_HIT", "PLAN_FOLLOWED", "RATIONAL_EXIT"],
    "behaviorPatterns": ["DISCIPLINED_TRADING", "RULE_FOLLOWING"],
    "categorizedTags": {
      "STRATEGY": ["swing-trading", "reversal"],
      "RISK": ["managed", "stop-loss-used"],
      "PERFORMANCE": ["loss", "controlled-loss"],
      "LESSONS": ["respect-market", "plan-execution"]
    },
    "psychologyNotes": "Good discipline in following stop loss. No emotional revenge trading."
  },
  "entryReasoning": {
    "technicalReasons": ["RESISTANCE_BREAK", "PATTERN_RECOGNITION"],
    "fundamentalReasons": [],
    "primaryReason": "Expected reversal from 44,100 resistance",
    "reasoningSummary": "Setup looked promising with potential reversal pattern, but market showed stronger bullish momentum than anticipated.",
    "confidenceLevel": 6,
    "supportingIndicators": [
      "Double top pattern forming",
      "RSI overbought"
    ],
    "conflictingIndicators": [
      "Strong bullish trend",
      "Volume not supporting reversal"
    ],
    "streategy": "Swing Trading"
  },
  "exitReasoning": {
    "exitPrimaryReason": "Stop loss triggered at predetermined level",
    "exitReasoningSummary": "Market didn't reverse as expected. Stop loss executed as planned to limit losses.",
    "exitConfidenceLevel": 10,
    "exitSupportingIndicators": [
      "Stop loss at -20% hit",
      "No reversal signs",
      "Bullish momentum continuing"
    ],
    "exitConflictingIndicators": [],
    "exitQualityScore": 9,
    "streategy": "Swing Trading"
  }
}
```

---

## Update Trade Request Examples

### Example 1: Update Trade Status to Closed

```json
{
  "tradeId": "TRD-2025-001",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "NIFTY",
    "rawSymbol": "NIFTY"
  },
  "status": "CLOSED",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-15T10:30:00Z",
    "price": 22500.00,
    "quantity": 25
  },
  "exitInfo": {
    "timestamp": "2025-01-15T15:30:00Z",
    "price": 22800.00,
    "quantity": 25,
    "totalValue": 570000.00,
    "fees": 150.00,
    "reason": "Target reached"
  },
  "userId": "ssd2658"
}
```

---

## Filter Trade Request Examples

### Example 1: Filter by Portfolio and Status

**GET Request:**
```
/api/v1/trades/filter?portfolioIds=163d0143-4fcb-480c-ac20-622f14e0e293&statuses=WIN,LOSS&page=0&size=20&sort=profitLoss,desc
```

### Example 2: Filter by Date Range and Strategy

**GET Request:**
```
/api/v1/trades/filter?startDate=2025-01-01&endDate=2025-01-31&strategies=Momentum Trading,Scalping&page=0&size=50
```

### Example 3: Filter Using Favorite Filter Configuration

**POST Request to `/api/v1/trades/details/filter`:**

```json
{
  "userId": "ssd2658",
  "favoriteFilterId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
  "metricsConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "dateRange": {
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    },
    "tradeCharacteristics": {
      "statuses": ["WIN", "LOSS"],
      "strategies": ["Momentum Trading"],
      "tags": ["intraday"],
      "minHoldingTimeHours": 1,
      "maxHoldingTimeHours": 24
    },
    "profitLossFilters": {
      "minProfitLoss": -10000.00,
      "maxProfitLoss": 50000.00
    }
  }
}
```

---

## Response Examples

### Example 1: Single Trade Response (201 Created)

```json
{
  "tradeId": "TRD-2025-002",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "BANKNIFTY",
    "rawSymbol": "BANKNIFTY2091722500CE",
    "exchange": "NSE",
    "segment": "INDEX_OPTIONS",
    "indexType": "BANKNIFTY",
    "derivativeInfo": {
      "derivativeType": "OPTIONS",
      "strikePrice": 22500.00,
      "expiryDate": "2025-09-17",
      "optionType": "CALL",
      "underlyingSymbol": "BANKNIFTY"
    },
    "lotSize": "15"
  },
  "symbol": "BANKNIFTY",
  "strategy": "Momentum Trading",
  "status": "CLOSED",
  "tradePositionType": "LONG",
  "entryInfo": {
    "timestamp": "2025-01-15T10:30:00Z",
    "price": 125.50,
    "quantity": 15,
    "totalValue": 1882.50,
    "fees": 25.00
  },
  "exitInfo": {
    "timestamp": "2025-01-15T14:45:00Z",
    "price": 185.75,
    "quantity": 15,
    "totalValue": 2786.25,
    "fees": 25.00
  },
  "metrics": {
    "profitLoss": 853.75,
    "profitLossPercentage": 45.32,
    "holdingTimeHours": 4
  },
  "userId": "ssd2658"
}
```

### Example 2: Paginated Trade List Response (200 OK)

```json
{
  "content": [
    {
      "tradeId": "TRD-2025-003",
      "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
      "symbol": "NIFTY",
      "status": "WIN",
      "metrics": {
        "profitLoss": 1430.00,
        "profitLossPercentage": 30.11
      }
    },
    {
      "tradeId": "TRD-2025-002",
      "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
      "symbol": "BANKNIFTY",
      "status": "WIN",
      "metrics": {
        "profitLoss": 853.75,
        "profitLossPercentage": 45.32
      }
    }
  ],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

### Example 3: Filtered Trade Details Response (200 OK)

```json
{
  "trades": [
    {
      "tradeId": "TRD-2025-003",
      "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
      "symbol": "NIFTY",
      "status": "WIN",
      "strategy": "Scalping",
      "metrics": {
        "profitLoss": 1430.00,
        "profitLossPercentage": 30.11
      }
    }
  ],
  "totalCount": 45,
  "appliedFilterName": "High Probability Intraday Trades",
  "filterSummary": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "symbols": ["NIFTY", "BANKNIFTY"],
    "statuses": ["WIN", "LOSS"],
    "dateRange": "2025-01-01 to 2025-12-31",
    "strategies": ["Momentum Trading"],
    "profitLossRange": "-10000.00 to 50000.00",
    "holdingTimeRange": "1 to 24 hours"
  },
  "page": 0,
  "size": 20,
  "totalPages": 3,
  "isFirst": true,
  "isLast": false
}
```

### Example 4: Error Response (400 Bad Request)

```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid trade data",
  "path": "/api/v1/trades/details",
  "details": [
    "Field 'portfolioId' is required",
    "Field 'tradeId' must not be empty"
  ]
}
```

---

## cURL Command Examples

### Example 1: Create a New Trade

```bash
curl -X POST http://localhost:8073/api/v1/trades/details \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "TRD-2025-001",
    "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
    "instrumentInfo": {
      "symbol": "NIFTY",
      "rawSymbol": "NIFTY",
      "exchange": "NSE",
      "segment": "INDEX_OPTIONS"
    },
    "status": "OPEN",
    "tradePositionType": "LONG",
    "entryInfo": {
      "timestamp": "2025-01-15T10:30:00Z",
      "price": 22500.00,
      "quantity": 25
    },
    "userId": "ssd2658"
  }'
```

### Example 2: Get Trades by Portfolio

```bash
curl -X GET "http://localhost:8073/api/v1/trades/details/portfolio/163d0143-4fcb-480c-ac20-622f14e0e293?symbols=NIFTY,BANKNIFTY"
```

### Example 3: Update a Trade

```bash
curl -X PUT http://localhost:8073/api/v1/trades/details/TRD-2025-001 \
  -H "Content-Type: application/json" \
  -d '{
    "tradeId": "TRD-2025-001",
    "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
    "instrumentInfo": {
      "symbol": "NIFTY"
    },
    "status": "CLOSED",
    "tradePositionType": "LONG",
    "entryInfo": {
      "timestamp": "2025-01-15T10:30:00Z",
      "price": 22500.00,
      "quantity": 25
    },
    "exitInfo": {
      "timestamp": "2025-01-15T15:30:00Z",
      "price": 22800.00,
      "quantity": 25
    },
    "userId": "ssd2658"
  }'
```

### Example 4: Filter Trades

```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?portfolioIds=163d0143-4fcb-480c-ac20-622f14e0e293&statuses=WIN,LOSS&startDate=2025-01-01&endDate=2025-01-31&page=0&size=20&sort=profitLoss,desc"
```

### Example 5: Batch Create/Update Trades

```bash
curl -X POST http://localhost:8073/api/v1/trades/details/batch \
  -H "Content-Type: application/json" \
  -d '[
    {
      "tradeId": "TRD-2025-001",
      "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
      "status": "OPEN",
      "userId": "ssd2658"
    },
    {
      "tradeId": "TRD-2025-002",
      "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
      "status": "CLOSED",
      "userId": "ssd2658"
    }
  ]'
```

### Example 6: Get Trades by IDs

```bash
curl -X POST http://localhost:8073/api/v1/trades/details/by-ids \
  -H "Content-Type: application/json" \
  -d '["TRD-2025-001", "TRD-2025-002", "TRD-2025-003"]'
```

### Example 7: Filter Trades Using Favorite Filter

```bash
curl -X POST "http://localhost:8073/api/v1/trades/details/filter?page=0&size=20&sort=profitLoss,desc" \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "ssd2658",
    "favoriteFilterId": "f47ac10b-58cc-4372-a567-0e02b2c3d479",
    "metricsConfig": {
      "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
      "tradeCharacteristics": {
        "statuses": ["WIN", "LOSS"],
        "strategies": ["Momentum Trading"]
      }
    }
  }'
```

---

## Integration Testing Examples

### JavaScript/TypeScript (using fetch)

```typescript
// Create a new trade
async function createTrade() {
  const trade = {
    tradeId: "TRD-2025-001",
    portfolioId: "163d0143-4fcb-480c-ac20-622f14e0e293",
    instrumentInfo: {
      symbol: "NIFTY",
      rawSymbol: "NIFTY",
      exchange: "NSE",
      segment: "INDEX_OPTIONS"
    },
    status: "OPEN",
    tradePositionType: "LONG",
    entryInfo: {
      timestamp: "2025-01-15T10:30:00Z",
      price: 22500.00,
      quantity: 25
    },
    userId: "ssd2658"
  };

  const response = await fetch('http://localhost:8073/api/v1/trades/details', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(trade)
  });

  return await response.json();
}
```

### Python (using requests)

```python
import requests

def create_trade():
    trade = {
        "tradeId": "TRD-2025-001",
        "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
        "instrumentInfo": {
            "symbol": "NIFTY",
            "rawSymbol": "NIFTY",
            "exchange": "NSE",
            "segment": "INDEX_OPTIONS"
        },
        "status": "OPEN",
        "tradePositionType": "LONG",
        "entryInfo": {
            "timestamp": "2025-01-15T10:30:00Z",
            "price": 22500.00,
            "quantity": 25
        },
        "userId": "ssd2658"
    }
    
    response = requests.post(
        'http://localhost:8073/api/v1/trades/details',
        json=trade
    )
    
    return response.json()
```

---

## Notes

- All timestamps are in ISO 8601 format (UTC)
- Prices are in decimal format
- Portfolio IDs are UUIDs
- Trade IDs follow the pattern: TRD-YYYY-NNN
- Base URL: `http://localhost:8073/api/v1/trades`
- All endpoints return JSON responses
- Error responses follow standard format with status, message, and details

For complete API documentation, refer to the OpenAPI specification in `trade-controller-api-schema.json`.
