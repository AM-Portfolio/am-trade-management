# Core Trade API Collections

## Overview
This folder contains the fundamental trade management API collections. These are the core building blocks for trade operations.

## 📦 Collections

### 1. TradeController.postman_collection.json
**Purpose**: Core trade CRUD operations and management

**Main Endpoints**:
- `GET /api/v1/trades` - Retrieve all trades
- `GET /api/v1/trades/{id}` - Get trade by ID
- `POST /api/v1/trades` - Create new trade
- `PUT /api/v1/trades/{id}` - Update trade
- `DELETE /api/v1/trades/{id}` - Delete trade
- `POST /api/v1/trades/batch` - Batch operations
- `GET /api/v1/trades/filter` - Filter trades

**Use Case**: Daily trade management, CRUD operations

**Environment Variables**:
- `tradeId`: Trade identifier
- `portfolioId`: Portfolio identifier
- `symbol`: Stock symbol (e.g., AAPL, GOOGL)

---

### 2. TradeManagementController.postman_collection.json
**Purpose**: Advanced trade queries and calendar-based operations

**Main Endpoints**:
- `GET /api/v1/trades/calendar/*` - Calendar-based queries
- `GET /api/v1/trades/date-range` - Date range queries
- `GET /api/v1/trades/by-date` - Trades by specific date
- `GET /api/v1/trades/by-month` - Monthly trades
- `GET /api/v1/trades/by-strategy` - Filter by strategy

**Use Case**: Historical analysis, date-based reporting

**Environment Variables**:
- `startDate`: Start date (e.g., 2024-01-01)
- `endDate`: End date (e.g., 2024-12-31)
- `strategy`: Trading strategy name

---

### 3. TradeJournalController.postman_collection.json
**Purpose**: Trading journal and notes management

**Main Endpoints**:
- `GET /api/v1/journal` - Get all journal entries
- `POST /api/v1/journal` - Create new entry
- `PUT /api/v1/journal/{id}` - Update entry
- `DELETE /api/v1/journal/{id}` - Delete entry
- `GET /api/v1/journal/by-trade/{tradeId}` - Get entries for trade
- `POST /api/v1/journal/with-attachments` - Entry with files

**Use Case**: Trade analysis, decision documentation, behavior tracking

**Key Features**:
- Journal entries with rich text
- Image/video attachments
- Behavior pattern summaries
- Mood and market sentiment tracking
- Related trades linking

---

## 🚀 Quick Start

### Step 1: Import Collections
1. Open Postman
2. Click **Import**
3. Select files from this folder
4. Collections will be added to Postman

### Step 2: Set Environment
1. Select environment from dropdown
2. Verify base URL (usually `http://localhost:8073`)
3. Update test data IDs if needed

### Step 3: Make Your First Request
1. Open **TradeController** → **Get All Trades**
2. Click **Send**
3. View response in the panel below

---

## 📊 Example Workflows

### Create and Update Trade
```
1. POST /api/v1/trades          (Create)
2. GET /api/v1/trades/{id}      (Verify)
3. PUT /api/v1/trades/{id}      (Update)
4. GET /api/v1/trades/{id}      (Confirm)
```

### Add Journal Entry for Trade
```
1. GET /api/v1/trades/{id}      (Get trade details)
2. POST /api/v1/journal         (Create entry)
3. GET /api/v1/journal/by-trade/{tradeId}  (View entries)
```

### Analyze Historical Trades
```
1. GET /api/v1/trades/by-month?month=12&year=2024  (Get monthly)
2. POST /api/v1/trades/filter?strategy=MOMENTUM     (By strategy)
3. Review trade details and metrics
```

---

## 🔗 Related Resources

**Sample Data**:
- `/v1/sample-data/trade-payloads/` - Example request payloads
- `/v1/sample-data/journal-entries/` - Example journal entries

**Schemas**:
- `/v1/schemas/trade-schemas/` - Trade API contract definitions
- `/v1/schemas/journal/` - Journal API definitions

**Documentation**:
- `/v1/documentation/api-guides/TRADE-CONTROLLER-GUIDE.md` - Detailed guide
- `/v1/documentation/quick-reference/` - Quick lookup tables

---

## 🎯 API Response Format

### Success Response
```json
{
  "id": "trade-123",
  "portfolioId": "portfolio-456",
  "symbol": "AAPL",
  "quantity": 100,
  "price": 150.25,
  "status": "EXECUTED",
  "entryDate": "2024-01-15T10:30:00Z",
  "exitDate": null,
  "profitLoss": 50.00,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "error": "Trade not found",
  "code": "TRADE_NOT_FOUND",
  "status": 404,
  "timestamp": "2024-01-15T10:30:00Z",
  "traceId": "trace-789"
}
```

---

## 🧪 Testing Tips

### Tip 1: Use Pre-request Scripts
- Auto-generate timestamps
- Create unique IDs
- Set computed values

### Tip 2: Use Tests Tab
- Validate response codes
- Check data types
- Verify required fields

### Tip 3: Use Collections Runner
- Run multiple requests in sequence
- Test workflows end-to-end
- Generate test reports

---

## ⚙️ Configuration

### Base URL
Default: `http://localhost:8073`

Update in:
1. Environment variables
2. Collection settings
3. Individual requests

### Authentication
If required, configure:
1. Pre-request Script for token generation
2. Authorization tab with Bearer token
3. API key in headers

---

## 📝 Common Issues

### Issue: 404 Not Found
- Verify API is running
- Check base URL
- Confirm endpoint path

### Issue: Invalid Trade ID
- Use valid ID from GET request
- Check UUID format
- Verify trade exists

### Issue: Validation Error
- Check payload structure
- Verify required fields
- Review schema definition

---

## 📚 Next Steps

1. **Read**: Check `/v1/documentation/api-guides/` for detailed endpoint documentation
2. **Learn**: Review `/v1/sample-data/trade-payloads/` for examples
3. **Implement**: Use schema `/v1/schemas/trade-schemas/` for code generation
4. **Test**: Run collections against live API

---

## 🔗 Quick Links

- [Trade API Schema](../schemas/trade-schemas/README.md)
- [Trade Implementation Guide](../documentation/api-guides/TRADE-CONTROLLER-GUIDE.md)
- [Quick Reference](../documentation/quick-reference/QUICK-REFERENCE.md)
- [Sample Payloads](../sample-data/trade-payloads/README.md)

---

**Category**: Core Trade API  
**Version**: v1.0  
**Last Updated**: December 2025  
**Status**: ✅ Active
