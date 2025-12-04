#!/bin/bash
# Favorite Filter API - Comprehensive Filter Example
# Copy and paste these curl commands for quick testing

BASE_URL="http://localhost:8073/api/v1/filters"
USER_ID="ssd2658"

echo "==================================="
echo "Favorite Filter API - Curl Examples"
echo "==================================="
echo ""

# 1. Create Comprehensive Filter
echo "1. Creating comprehensive filter with all options..."
curl -X POST "${BASE_URL}?userId=${USER_ID}" \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Comprehensive Filter Example",
  "description": "Filter with all possible configuration options",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": [
      "163d0143-4fcb-480c-ac20-622f14e0e293"
    ],
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
      "baseSymbols": [
        "NIFTY",
        "BANKNIFTY",
        "RELIANCE",
        "TCS"
      ],
      "indexTypes": [
        "NIFTY",
        "BANKNIFTY",
        "FINNIFTY"
      ],
      "derivativeTypes": [
        "FUTURES",
        "OPTIONS"
      ]
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
      "directions": [
        "LONG",
        "SHORT"
      ],
      "statuses": [
        "OPEN",
        "CLOSED",
        "CANCELLED"
      ],
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
}'

echo ""
echo ""

# 2. Create High Risk Intraday Filter
echo "2. Creating high-risk intraday filter..."
curl -X POST "${BASE_URL}?userId=${USER_ID}" \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "High Risk Intraday Trades",
  "description": "Filter for high-risk intraday trades with momentum strategy",
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
      "minProfitLoss": -10000.0
    },
    "dateRange": {
      "startDate": "2025-01-01",
      "endDate": "2025-12-31"
    }
  }
}'

echo ""
echo ""

# 3. Create Winning Trades Filter
echo "3. Creating winning trades filter..."
curl -X POST "${BASE_URL}?userId=${USER_ID}" \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Winning Trades",
  "description": "All winning trades with profit above 5000",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "tradeCharacteristics": {
      "statuses": ["WIN", "CLOSED"]
    },
    "profitLossFilters": {
      "minProfitLoss": 5000.0
    },
    "timePeriod": {
      "period": "LAST_30_DAYS"
    }
  }
}'

echo ""
echo ""

# 4. Create Options Strategy Filter
echo "4. Creating options strategy filter..."
curl -X POST "${BASE_URL}?userId=${USER_ID}" \
  -H 'Content-Type: application/json' \
  -d '{
  "name": "Options Trading Analysis",
  "description": "Filter for analyzing options trading performance",
  "isDefault": false,
  "filterConfig": {
    "portfolioIds": ["163d0143-4fcb-480c-ac20-622f14e0e293"],
    "instruments": ["NIFTY", "BANKNIFTY", "FINNIFTY"],
    "instrumentFilters": {
      "marketSegments": ["INDEX_OPTIONS"],
      "indexTypes": ["NIFTY", "BANKNIFTY"],
      "derivativeTypes": ["OPTIONS"]
    },
    "tradeCharacteristics": {
      "strategies": ["Options Strategy"],
      "tags": ["options", "premium-selling"]
    },
    "metricTypes": ["PERFORMANCE", "RISK", "WIN_RATE", "RISK_REWARD"],
    "groupBy": ["STRATEGY", "SYMBOL"]
  }
}'

echo ""
echo ""

# 5. Get All Filters
echo "5. Getting all filters..."
curl -X GET "${BASE_URL}?userId=${USER_ID}"

echo ""
echo ""

# 6. Get Default Filter
echo "6. Getting default filter..."
curl -X GET "${BASE_URL}/default?userId=${USER_ID}"

echo ""
echo ""

echo "==================================="
echo "All curl commands executed!"
echo "==================================="
