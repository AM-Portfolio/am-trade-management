#!/bin/bash
# Favorite Filter API - Comprehensive Filter Example
# User identity comes from Authorization Bearer JWT (not userId query params).

BASE_URL="http://localhost:8073/api/v1/filters"
JWT_TOKEN="${JWT_TOKEN:-replace-with-your-jwt}"
AUTH_HEADER="Authorization: Bearer ${JWT_TOKEN}"

echo "==================================="
echo "Favorite Filter API - Curl Examples"
echo "==================================="
echo ""

# 1. Create Comprehensive Filter
echo "1. Creating comprehensive filter with all options..."
curl -X POST "${BASE_URL}" \
  -H "Content-Type: application/json" \
  -H "${AUTH_HEADER}" \
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
      "RISK"
    ],
    "instruments": [
      "NIFTY",
      "BANKNIFTY"
    ],
    "tradeCharacteristics": {
      "statuses": ["WIN", "LOSS"],
      "strategies": ["Momentum Trading"],
      "tags": ["intraday"]
    }
  }
}'

echo ""
echo "2. Listing all filters..."
curl -X GET "${BASE_URL}" -H "${AUTH_HEADER}"

echo ""
echo "3. Getting default filter..."
curl -X GET "${BASE_URL}/default" -H "${AUTH_HEADER}"
