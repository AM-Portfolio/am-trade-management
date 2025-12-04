# Trade Controller API Schema - Summary

## Overview

This document provides a comprehensive summary of the **Trade Controller API JSON Schema**, which has been created for multi-language code generation (Java, Python, Flutter/Dart, TypeScript/JavaScript).

**Schema File:** `trade-controller-api-schema.json`  
**Schema Version:** 1.0.0  
**Date Created:** January 2025  
**Purpose:** Enable automatic code generation from TradeController API specification

---

## 🎯 Key Features

### 1. Complete API Coverage
The schema covers all **7 endpoints** from the TradeController:
- ✅ GET `/details/portfolio/{portfolioId}` - Get trades by portfolio
- ✅ POST `/details` - Create new trade
- ✅ PUT `/details/{tradeId}` - Update trade
- ✅ GET `/filter` - Filter trades by criteria
- ✅ POST `/details/batch` - Bulk operations
- ✅ POST `/details/by-ids` - Get by trade IDs
- ✅ POST `/details/filter` - Filter with favorite filter config

### 2. Comprehensive Data Models
The schema includes **19 detailed model definitions**:

#### Core Trade Models
1. **TradeDetails** - Complete trade information (main model)
2. **InstrumentInfo** - Instrument/security details
3. **DerivativeInfo** - Futures & options specific data
4. **EntryExitInfo** - Entry and exit transaction details
5. **TradeMetrics** - Performance and risk metrics

#### Trade Analysis Models
6. **TradePsychologyData** - Behavioral and psychology factors
7. **TradeEntryExistReasoning** - Entry/exit reasoning with technical/fundamental analysis
8. **Attachment** - File attachments for charts and analysis

#### Trade Execution Models
9. **TradeModel** - Individual trade execution from broker
10. **BasicInfo** - Basic execution information
11. **ExecutionInfo** - Order execution details
12. **FnOInfo** - F&O specific information
13. **Charges** - Brokerage and tax breakdown
14. **Financials** - Financial summary

#### Filter & Request Models
15. **FilterTradeDetailsRequest** - Filter request with favorite filter
16. **FilterTradeDetailsResponse** - Filtered results with pagination
17. **FilterSummary** - Applied filter criteria summary
18. **MetricsFilterConfig** - Reusable filter configuration

#### Error Handling
19. **ErrorResponse** - Standard error response structure

### 3. Rich Enum Definitions

#### Trade Status (6 values)
```
OPEN, CLOSED, WIN, LOSS, BREAKEVEN, CANCELLED
```

#### Position Types (2 values)
```
LONG, SHORT
```

#### Exchange (4 values)
```
NSE, BSE, MCX, NCDEX
```

#### Market Segments (7 values)
```
EQUITY, INDEX_OPTIONS, EQUITY_OPTIONS, EQUITY_FUTURES,
INDEX_FUTURES, CURRENCY_FUTURES, COMMODITY_FUTURES
```

#### Index Types (6 values)
```
NIFTY, BANKNIFTY, FINNIFTY, MIDCPNIFTY, SENSEX, OTHER
```

#### Derivative Types (4 values)
```
FUTURES, OPTIONS, CALL, PUT
```

#### Broker Types (6 values)
```
ZERODHA, UPSTOX, ANGEL, ICICI, HDFC, OTHER
```

#### Psychology Factors
- **Entry Psychology** (8 factors): FEAR_OF_MISSING_OUT, OVERCONFIDENCE, REVENGE_TRADING, GREED, PATIENCE, DISCIPLINE, CALM_ANALYSIS, EMOTIONAL_CONTROL
- **Exit Psychology** (8 factors): FEAR, GREED, PANIC, TARGET_ACHIEVED, STOP_LOSS_HIT, PLAN_FOLLOWED, EMOTIONAL_EXIT, RATIONAL_EXIT
- **Behavior Patterns** (6 patterns): OVERTRADING, LOSS_AVERSION, CONFIRMATION_BIAS, DISCIPLINED_TRADING, PLAN_ADHERENCE, RULE_FOLLOWING

#### Analysis Reasons
- **Technical Reasons** (8 types): BREAKOUT, SUPPORT_BOUNCE, RESISTANCE_BREAK, TREND_FOLLOWING, PATTERN_RECOGNITION, INDICATOR_SIGNAL, MOVING_AVERAGE_CROSS, RSI_DIVERGENCE
- **Fundamental Reasons** (6 types): EARNINGS_BEAT, SECTOR_STRENGTH, MARKET_SENTIMENT, NEWS_CATALYST, VALUATION, GROWTH_PROSPECTS

---

## 📊 Schema Statistics

| Metric | Count |
|--------|-------|
| **Total Definitions** | 19 |
| **API Endpoints** | 7 |
| **Enum Types** | 15+ |
| **Total Enum Values** | 80+ |
| **Required Fields (TradeDetails)** | 6 |
| **Optional Fields (TradeDetails)** | 12 |
| **Nested Object Levels** | Up to 4 levels deep |

---

## 🔍 Model Structure Breakdown

### TradeDetails (Main Model)

**Required Fields:**
```json
{
  "tradeId": "string (required)",
  "portfolioId": "UUID (required)",
  "instrumentInfo": "object (required)",
  "status": "enum (required)",
  "tradePositionType": "enum (required)",
  "entryInfo": "object (required)"
}
```

**Optional Fields:**
```json
{
  "symbol": "string",
  "strategy": "string",
  "exitInfo": "object",
  "metrics": "object",
  "tradeExecutions": "array",
  "notes": "string",
  "tags": "array",
  "userId": "string",
  "attachments": "array",
  "psychologyData": "object",
  "entryReasoning": "object",
  "exitReasoning": "object"
}
```

### InstrumentInfo

Supports multiple instrument types:
- ✅ Equities (stocks)
- ✅ Index Options (NIFTY, BANKNIFTY, etc.)
- ✅ Stock Options
- ✅ Index Futures
- ✅ Stock Futures
- ✅ Currency Futures
- ✅ Commodity Futures

**Key Fields:**
- `symbol` - Trading symbol
- `rawSymbol` - Original unparsed symbol
- `exchange` - NSE, BSE, MCX, NCDEX
- `segment` - Market segment
- `derivativeInfo` - F&O specific details
- `indexType` - For index-based instruments

### TradeMetrics

**Performance Metrics:**
- `profitLoss` - Absolute P&L
- `profitLossPercentage` - % return
- `returnOnEquity` - ROE

**Risk Metrics:**
- `riskAmount` - Risk amount
- `rewardAmount` - Reward amount
- `riskRewardRatio` - Risk-reward ratio
- `maxAdverseExcursion` - Max loss during trade
- `maxFavorableExcursion` - Max profit during trade

**Time Metrics:**
- `holdingTimeDays` - Days held
- `holdingTimeHours` - Hours held
- `holdingTimeMinutes` - Minutes held

---

## 🚀 Code Generation Support

### Supported Languages

| Language | Tool | Output Format |
|----------|------|---------------|
| **Java** | jsonschema2pojo-maven-plugin | POJOs with Jackson annotations |
| **Python** | datamodel-code-generator | Pydantic v2 models |
| **Flutter (Dart)** | quicktype | json_serializable classes |
| **TypeScript** | quicktype | Interfaces with type safety |

### Generated File Examples

**Java:**
```
target/generated-sources/json-schema/
├── TradeDetails.java
├── InstrumentInfo.java
├── DerivativeInfo.java
├── EntryExitInfo.java
├── TradeMetrics.java
├── TradePsychologyData.java
├── TradeEntryExistReasoning.java
├── FilterTradeDetailsRequest.java
├── FilterTradeDetailsResponse.java
└── ... (10 more files)
```

**Python:**
```python
# models/trade_models.py
class TradeDetails(BaseModel):
    trade_id: str
    portfolio_id: str
    instrument_info: InstrumentInfo
    status: TradeStatus
    # ... more fields
```

**Dart:**
```dart
// lib/models/trade_models.dart
@JsonSerializable()
class TradeDetails {
  final String tradeId;
  final String portfolioId;
  final InstrumentInfo instrumentInfo;
  // ... more fields
}
```

**TypeScript:**
```typescript
// src/models/TradeModels.ts
export interface TradeDetails {
    tradeId: string;
    portfolioId: string;
    instrumentInfo: InstrumentInfo;
    status: TradeStatus;
    // ... more fields
}
```

---

## 📝 API Endpoint Details

### 1. Get Trades by Portfolio
**GET** `/details/portfolio/{portfolioId}`

**Parameters:**
- `portfolioId` (path, required) - Portfolio UUID
- `symbols` (query, optional) - Array of symbols to filter

**Response:** Array of TradeDetails

---

### 2. Create Trade
**POST** `/details`

**Request Body:** TradeDetails (JSON)

**Response:** TradeDetails (201 Created)

**Required Fields:**
- tradeId
- portfolioId
- instrumentInfo
- status
- tradePositionType
- entryInfo
- userId

---

### 3. Update Trade
**PUT** `/details/{tradeId}`

**Parameters:**
- `tradeId` (path, required) - Trade ID

**Request Body:** TradeDetails (JSON)

**Response:** TradeDetails (200 OK)

---

### 4. Filter Trades
**GET** `/filter`

**Query Parameters:**
- `portfolioIds` - Portfolio UUIDs (optional)
- `symbols` - Trading symbols (optional)
- `statuses` - Trade statuses (optional)
- `startDate` - Start date yyyy-MM-dd (optional)
- `endDate` - End date yyyy-MM-dd (optional)
- `strategies` - Trading strategies (optional)
- `page` - Page number, 0-based (default: 0)
- `size` - Page size (default: 20)
- `sort` - Sort criteria (e.g., profitLoss,desc)

**Response:** Paginated list of TradeDetails

---

### 5. Batch Operations
**POST** `/details/batch`

**Request Body:** Array of TradeDetails

**Response:** Array of TradeDetails (200 OK)

**Use Cases:**
- Bulk import from broker statements
- Batch updates to multiple trades
- Initial data migration

---

### 6. Get by Trade IDs
**POST** `/details/by-ids`

**Request Body:** Array of trade IDs (strings)

**Response:** Array of TradeDetails

**Use Cases:**
- Fetch specific trades for comparison
- Retrieve trades for reporting
- Cross-reference analysis

---

### 7. Filter with Favorite Filter
**POST** `/details/filter`

**Query Parameters:**
- `page` - Page number (optional)
- `size` - Page size (optional)
- `sort` - Sort criteria (optional)

**Request Body:** FilterTradeDetailsRequest
```json
{
  "userId": "ssd2658",
  "favoriteFilterId": "uuid (optional)",
  "metricsConfig": {
    "portfolioIds": ["uuid"],
    "dateRange": {...},
    "tradeCharacteristics": {...},
    "profitLossFilters": {...}
  }
}
```

**Response:** FilterTradeDetailsResponse
```json
{
  "trades": [...],
  "totalCount": 150,
  "appliedFilterName": "My Filter",
  "filterSummary": {...},
  "page": 0,
  "size": 20,
  "totalPages": 8,
  "isFirst": true,
  "isLast": false
}
```

---

## 🛠️ Integration Features

### 1. Pagination Support
All list endpoints support Spring Data pagination:
- Page number (0-based)
- Page size
- Sorting (multiple fields, ASC/DESC)

### 2. Filtering Capabilities
Multi-dimensional filtering:
- By portfolio(s)
- By symbol(s)
- By status(es)
- By date range
- By strategy/strategies
- By profit/loss range
- By holding time

### 3. Error Handling
Consistent error responses across all endpoints:
```json
{
  "timestamp": "2025-01-15T10:30:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid trade data",
  "path": "/api/v1/trades/details",
  "details": ["Field 'portfolioId' is required"]
}
```

---

## 📚 Documentation Files

| File | Purpose |
|------|---------|
| `trade-controller-api-schema.json` | **JSON Schema** - Source of truth for API structure |
| `TRADE-SCHEMA-CODE-GENERATION-GUIDE.md` | **How-to guide** - Generate code in 4 languages |
| `TRADE-CONTROLLER-EXAMPLES.md` | **Examples library** - Request/response samples |
| `TRADE-CONTROLLER-QUICK-REF.md` | **Quick reference** - Endpoints, filters, models |
| `README.md` (this file) | **Summary** - Overview and statistics |

---

## 🎓 Best Practices

### 1. Code Generation
- **Automate**: Integrate schema generation into CI/CD pipeline
- **Validate**: Always validate against schema before deployment
- **Version**: Keep schema versioned with API changes
- **Document**: Update examples when schema changes

### 2. API Usage
- **Pagination**: Always use pagination for large datasets
- **Filtering**: Use specific filters to reduce response size
- **Sorting**: Apply sorting for predictable results
- **Error Handling**: Handle all HTTP status codes appropriately

### 3. Data Modeling
- **Required Fields**: Always provide all required fields
- **Validation**: Use generated models for automatic validation
- **Type Safety**: Leverage generated types for compile-time checks
- **Enums**: Use enum values for consistency

---

## 🔄 Updates & Maintenance

### Version History

**v1.0.0 (January 2025)**
- Initial schema creation
- 19 model definitions
- 7 API endpoints
- Complete enum coverage
- Multi-language support

### Planned Enhancements
- [ ] WebSocket support for real-time trade updates
- [ ] GraphQL schema generation
- [ ] Additional broker types
- [ ] Extended metrics calculations
- [ ] Performance optimization filters

---

## 🧪 Testing

### Test Scripts Available
- `test-comprehensive-filter.sh` - Bash script (Linux/Mac)
- `test-comprehensive-filter.ps1` - PowerShell script (Windows)

### Test Coverage
- ✅ Create trade operations
- ✅ Read/retrieve operations
- ✅ Update operations
- ✅ Filter operations
- ✅ Batch operations
- ✅ Error scenarios

---

## 📞 Support & Resources

### Getting Started
1. Review the schema: `trade-controller-api-schema.json`
2. Check examples: `TRADE-CONTROLLER-EXAMPLES.md`
3. Follow code generation guide: `TRADE-SCHEMA-CODE-GENERATION-GUIDE.md`
4. Use quick reference: `TRADE-CONTROLLER-QUICK-REF.md`

### Code Generation Commands

**Java:**
```bash
mvn jsonschema2pojo:generate
```

**Python:**
```bash
datamodel-codegen --input trade-controller-api-schema.json --output models.py
```

**Flutter:**
```bash
quicktype --src trade-controller-api-schema.json --lang dart --out models.dart
```

**TypeScript:**
```bash
quicktype --src trade-controller-api-schema.json --lang typescript --out models.ts
```

---

## 🎯 Summary

The **Trade Controller API Schema** provides:
- ✅ **Complete API Coverage** - All 7 endpoints documented
- ✅ **19 Data Models** - Comprehensive type definitions
- ✅ **80+ Enum Values** - Rich domain vocabulary
- ✅ **4 Languages** - Java, Python, Flutter, TypeScript
- ✅ **Full Examples** - Request/response samples
- ✅ **Quick Reference** - Fast lookup guide
- ✅ **Type Safety** - Compile-time validation
- ✅ **Auto-generation** - Reduce manual coding

**Ready to use for:**
- Backend API development (Java/Spring Boot)
- Frontend development (TypeScript/React)
- Mobile development (Flutter/Dart)
- Data analysis (Python/Pandas)
- API testing (Postman/REST clients)
- Documentation generation
- Contract testing

---

**Last Updated:** January 2025  
**Schema Version:** 1.0.0  
**Maintained by:** AM Trade Management Team
