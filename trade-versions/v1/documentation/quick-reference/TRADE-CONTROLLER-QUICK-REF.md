# Trade Controller API - Quick Reference

## Base URL
```
http://localhost:8073/api/v1/trades
```

## Quick Navigation
- [Endpoints](#endpoints)
- [Common Request Examples](#common-request-examples)
- [Response Codes](#response-codes)
- [Data Models](#data-models)

---

## Endpoints

| Method | Endpoint | Description | Auth Required |
|--------|----------|-------------|---------------|
| GET | `/details/portfolio/{portfolioId}` | Get trades by portfolio | Yes |
| POST | `/details` | Create new trade | Yes |
| PUT | `/details/{tradeId}` | Update existing trade | Yes |
| GET | `/filter` | Filter trades by criteria | Yes |
| POST | `/details/batch` | Bulk create/update trades | Yes |
| POST | `/details/by-ids` | Get trades by IDs | Yes |
| POST | `/details/filter` | Filter with favorite filter | Yes |

---

## Common Request Examples

### 1. Create a Simple Trade

**Endpoint:** `POST /details`

```json
{
  "tradeId": "TRD-2025-001",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "instrumentInfo": {
    "symbol": "NIFTY",
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

### 2. Get Portfolio Trades

**Endpoint:** `GET /details/portfolio/{portfolioId}`

**cURL:**
```bash
curl -X GET "http://localhost:8073/api/v1/trades/details/portfolio/163d0143-4fcb-480c-ac20-622f14e0e293?symbols=NIFTY,BANKNIFTY"
```

### 3. Filter Trades

**Endpoint:** `GET /filter`

**Query Parameters:**
- `portfolioIds` - Portfolio IDs (comma-separated)
- `symbols` - Symbols (comma-separated)
- `statuses` - Statuses: OPEN, CLOSED, WIN, LOSS, BREAKEVEN, CANCELLED
- `startDate` - Start date (yyyy-MM-dd)
- `endDate` - End date (yyyy-MM-dd)
- `strategies` - Strategies (comma-separated)
- `page` - Page number (0-based)
- `size` - Page size
- `sort` - Sort criteria (e.g., profitLoss,desc)

**cURL:**
```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?portfolioIds=163d0143-4fcb-480c-ac20-622f14e0e293&statuses=WIN,LOSS&page=0&size=20&sort=profitLoss,desc"
```

### 4. Update Trade Status

**Endpoint:** `PUT /details/{tradeId}`

```json
{
  "tradeId": "TRD-2025-001",
  "portfolioId": "163d0143-4fcb-480c-ac20-622f14e0e293",
  "status": "CLOSED",
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

### 5. Filter with Favorite Filter Configuration

**Endpoint:** `POST /details/filter?page=0&size=20`

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
      "tags": ["intraday"]
    },
    "profitLossFilters": {
      "minProfitLoss": -10000.00,
      "maxProfitLoss": 50000.00
    }
  }
}
```

---

## Response Codes

| Code | Description | Example Use Case |
|------|-------------|------------------|
| 200 | OK | Successful GET/PUT request |
| 201 | Created | Successful POST request |
| 400 | Bad Request | Invalid parameters or data |
| 404 | Not Found | Trade or portfolio not found |
| 500 | Internal Server Error | Server-side error |

---

## Data Models

### TradeDetails (Core Model)

**Required Fields:**
- `tradeId` (string) - Unique trade identifier
- `portfolioId` (UUID) - Portfolio ID
- `instrumentInfo` (object) - Instrument information
- `status` (enum) - Trade status
- `tradePositionType` (enum) - LONG or SHORT
- `entryInfo` (object) - Entry information
- `userId` (string) - User ID

**Optional Fields:**
- `symbol` (string) - Trading symbol
- `strategy` (string) - Trading strategy
- `exitInfo` (object) - Exit information
- `metrics` (object) - Trade metrics
- `notes` (string) - Trade notes
- `tags` (array) - Trade tags
- `attachments` (array) - File attachments
- `psychologyData` (object) - Psychology data
- `entryReasoning` (object) - Entry reasoning
- `exitReasoning` (object) - Exit reasoning

### Trade Status Values

```
OPEN       - Trade is currently active
CLOSED     - Trade is closed
WIN        - Profitable trade
LOSS       - Losing trade
BREAKEVEN  - Break-even trade
CANCELLED  - Trade cancelled
```

### Position Types

```
LONG  - Long position (buy first, sell later)
SHORT - Short position (sell first, buy later)
```

### Exchange Values

```
NSE    - National Stock Exchange
BSE    - Bombay Stock Exchange
MCX    - Multi Commodity Exchange
NCDEX  - National Commodity & Derivatives Exchange
```

### Market Segments

```
EQUITY              - Equity segment
INDEX_OPTIONS       - Index options
EQUITY_OPTIONS      - Stock options
EQUITY_FUTURES      - Stock futures
INDEX_FUTURES       - Index futures
CURRENCY_FUTURES    - Currency futures
COMMODITY_FUTURES   - Commodity futures
```

---

## Common Filters

### Filter by Winning Trades

```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?statuses=WIN&page=0&size=20&sort=profitLoss,desc"
```

### Filter by Date Range

```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?startDate=2025-01-01&endDate=2025-01-31&page=0&size=50"
```

### Filter by Strategy

```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?strategies=Momentum Trading,Scalping&page=0&size=20"
```

### Filter by Symbol

```bash
curl -X GET "http://localhost:8073/api/v1/trades/filter?symbols=NIFTY,BANKNIFTY&statuses=WIN,LOSS&page=0&size=20"
```

---

## Pagination

All list endpoints support pagination:

**Query Parameters:**
- `page` - Page number (0-based, default: 0)
- `size` - Page size (default: 20)
- `sort` - Sort field and direction (e.g., `profitLoss,desc`)

**Response Structure:**
```json
{
  "content": [...],
  "totalElements": 150,
  "totalPages": 8,
  "size": 20,
  "number": 0
}
```

---

## Error Handling

**Standard Error Response:**
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

## Code Generation

Generate models for your preferred language:

### Java
```bash
mvn jsonschema2pojo:generate
```

### Python
```bash
datamodel-codegen --input postman/trade-controller-api-schema.json --output models.py
```

### Flutter (Dart)
```bash
quicktype --src postman/trade-controller-api-schema.json --lang dart --out models.dart
```

### TypeScript
```bash
quicktype --src postman/trade-controller-api-schema.json --lang typescript --out models.ts
```

---

## Additional Resources

- **Schema File:** `postman/trade-controller-api-schema.json`
- **Examples:** `postman/TRADE-CONTROLLER-EXAMPLES.md`
- **Code Generation Guide:** `postman/TRADE-SCHEMA-CODE-GENERATION-GUIDE.md`
- **OpenAPI Documentation:** Available at runtime via Swagger UI

---

## Support

For issues or questions:
1. Check the schema file for complete API specifications
2. Review examples in `TRADE-CONTROLLER-EXAMPLES.md`
3. Consult the code generation guide for language-specific implementations
4. Contact the development team

---

**Last Updated:** January 2025  
**API Version:** 1.0.0  
**Base URL:** http://localhost:8073/api/v1/trades
