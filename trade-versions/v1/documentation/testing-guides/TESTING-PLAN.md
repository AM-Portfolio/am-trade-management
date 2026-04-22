# AM Trade Management API - Testing Plan Document

**Portfolio ID**: `8a57024c-05c2-475b-a2c4-0545865efa4a`  
**Date**: October 11, 2025  
**Environment**: Local Docker Development  
**Base URL**: `http://localhost:8073`

## 🎯 Testing Objectives

### Primary Goals
1. **Functional Validation** - Verify all API endpoints work correctly
2. **Data Integrity** - Ensure data operations maintain consistency
3. **Performance Assessment** - Measure response times and system behavior
4. **Error Handling** - Test edge cases and error scenarios
5. **Integration Testing** - Validate cross-service functionality

### Success Criteria
- All critical endpoints return expected responses
- Data persistence works correctly across operations
- Error handling provides meaningful feedback
- Response times are within acceptable limits
- No data corruption or loss occurs

## 🧪 Test Scenarios Overview

### Phase 1: Infrastructure & Health
- ✅ Container availability and health
- ✅ Database connectivity
- ✅ Actuator endpoints functionality

### Phase 2: Core Trade Operations
- 📊 Create trades for the specific portfolio
- 📊 Update and modify trade data
- 📊 Filter and search operations
- 📊 Batch operations testing

### Phase 3: Analytics & Reporting
- 📈 Portfolio summary generation
- 📈 Trade metrics calculation
- 📈 Performance analytics
- 📈 Calendar-based reporting

### Phase 4: Advanced Features
- 🔬 Trade comparison functionality
- 🔬 P&L heatmap generation
- 🔬 Trade replay analytics
- 🔬 Journal entry management

### Phase 5: User Management
- 👤 User preferences management
- 👤 Favorite filters creation
- 👤 User-specific data retrieval

## 📋 Detailed Test Plan

### Phase 1: Infrastructure Testing

#### 1.1 Health Check Validation
**Endpoint**: `GET /actuator/health`
**Purpose**: Verify application is running and healthy
**Expected**: 200 OK with status "UP"

**Test Cases**:
- Basic health check
- Service dependency status
- Resource availability

#### 1.2 Application Info Retrieval
**Endpoint**: `GET /actuator/info`
**Purpose**: Verify application metadata
**Expected**: Application version, build info

#### 1.3 Metrics Availability
**Endpoint**: `GET /actuator/metrics`
**Purpose**: Ensure monitoring capabilities
**Expected**: Available metrics list

### Phase 2: Core Trade Operations

#### 2.1 Portfolio Trade Retrieval
**Endpoint**: `GET /api/v1/trades/details/portfolio/{portfolioId}`
**Test Data**: Portfolio ID `8a57024c-05c2-475b-a2c4-0545865efa4a`

**Test Scenarios**:
```json
Scenario A: Empty Portfolio Check
- Request: GET with our portfolio ID
- Expected: Empty array or 404 if no trades exist
- Validation: Response structure is correct

Scenario B: With Symbol Filter
- Request: GET with symbols=AAPL,GOOGL,MSFT
- Expected: Filtered results or empty array
- Validation: Only requested symbols returned
```

#### 2.2 Trade Creation
**Endpoint**: `POST /api/v1/trades/details`

**Test Data Set**:
```json
Trade 1 - Apple Stock Purchase:
{
  "portfolioId": "8a57024c-05c2-475b-a2c4-0545865efa4a",
  "symbol": "AAPL",
  "userId": "test-user-2025",
  "quantity": 100,
  "price": 175.50,
  "tradeType": "BUY",
  "status": "EXECUTED",
  "strategy": "MOMENTUM",
  "entryDate": "2025-10-10",
  "notes": "Initial AAPL position - strong earnings expected"
}

Trade 2 - Google Stock Purchase:
{
  "portfolioId": "8a57024c-05c2-475b-a2c4-0545865efa4a",
  "symbol": "GOOGL",
  "userId": "test-user-2025",
  "quantity": 50,
  "price": 2850.00,
  "tradeType": "BUY",
  "status": "EXECUTED",
  "strategy": "VALUE",
  "entryDate": "2025-10-11",
  "notes": "GOOGL value play - undervalued vs competitors"
}

Trade 3 - Microsoft Stock Purchase:
{
  "portfolioId": "8a57024c-05c2-475b-a2c4-0545865efa4a",
  "symbol": "MSFT",
  "userId": "test-user-2025",
  "quantity": 75,
  "price": 420.25,
  "tradeType": "BUY",
  "status": "PENDING",
  "strategy": "GROWTH",
  "entryDate": "2025-10-11",
  "notes": "MSFT growth position - AI and cloud expansion"
}

Trade 4 - Tesla Stock (Sell Order):
{
  "portfolioId": "8a57024c-05c2-475b-a2c4-0545865efa4a",
  "symbol": "TSLA",
  "userId": "test-user-2025",
  "quantity": 25,
  "price": 240.75,
  "tradeType": "SELL",
  "status": "EXECUTED",
  "strategy": "PROFIT_TAKING",
  "entryDate": "2025-10-11",
  "exitDate": "2025-10-11",
  "notes": "TSLA profit taking - reached target price"
}
```

**Validation Points**:
- Trade ID generation
- Data persistence
- Timestamp accuracy
- Field validation

#### 2.3 Batch Trade Operations
**Endpoint**: `POST /api/v1/trades/details/batch`

**Test Scenario**: Create multiple trades in single request
- Submit 3-5 trades simultaneously
- Verify all trades are created correctly
- Test transaction integrity

#### 2.4 Trade Updates
**Endpoint**: `PUT /api/v1/trades/details/{tradeId}`

**Test Cases**:
- Update trade quantity
- Change trade status (PENDING → EXECUTED)
- Add exit date and profit/loss
- Update notes and strategy

#### 2.5 Trade Filtering
**Endpoint**: `GET /api/v1/trades/filter`

**Filter Test Scenarios**:
```
Scenario A: Symbol Filter
- symbols=AAPL,GOOGL
- Expected: Only AAPL and GOOGL trades

Scenario B: Status Filter  
- statuses=EXECUTED
- Expected: Only executed trades

Scenario C: Date Range Filter
- startDate=2025-10-01&endDate=2025-10-31
- Expected: Trades within October 2025

Scenario D: Strategy Filter
- strategies=MOMENTUM,VALUE
- Expected: Only momentum and value trades

Scenario E: Combined Filters
- portfolioIds=8a57024c-05c2-475b-a2c4-0545865efa4a&symbols=AAPL&statuses=EXECUTED
- Expected: Executed AAPL trades for our portfolio
```

### Phase 3: Analytics & Reporting

#### 3.1 Portfolio Summary
**Endpoint**: `GET /api/v1/portfolio-summary/{portfolioId}`

**Test Scenarios**:
- Get complete portfolio summary
- Verify calculated metrics (total value, P&L, etc.)
- Check position aggregations

#### 3.2 Trade Metrics
**Endpoint**: `GET /api/v1/metrics/portfolio/{portfolioId}`

**Validation**:
- Win/loss ratios
- Average trade size
- Total portfolio value
- Risk metrics

#### 3.3 Calendar-Based Queries
**Endpoints**: 
- `GET /api/v1/trades/calendar/day`
- `GET /api/v1/trades/calendar/month`

**Test Data**:
```
Day Query: date=2025-10-11
Month Query: year=2025&month=10
```

#### 3.4 Trade Summary by Time Period
**Endpoint**: `GET /api/v1/trade-summary/trades`

**Period Tests**:
- DAY: periodType=DAY&startDate=2025-10-11
- MONTH: periodType=MONTH&year=2025&month=10
- CUSTOM: periodType=CUSTOM&startDate=2025-10-01&endDate=2025-10-31

### Phase 4: Advanced Features

#### 4.1 Trade Analytics & Replay
**Endpoint**: `POST /api/v1/analytics/trade-replays`

**Test Scenario**:
```json
{
  "symbol": "AAPL",
  "startDate": "2025-10-01T00:00:00",
  "endDate": "2025-10-11T23:59:59",
  "strategy": "MOMENTUM",
  "parameters": {
    "riskLevel": "MEDIUM",
    "maxPositionSize": 1000
  }
}
```

#### 4.2 Trade Comparison
**Endpoint**: `POST /api/v1/comparison/trades`

**Test**: Compare AAPL vs GOOGL trades performance

#### 4.3 P&L Heatmap
**Endpoint**: `GET /api/v1/heatmap`

**Parameters**:
- portfolioId: 8a57024c-05c2-475b-a2c4-0545865efa4a
- granularity: DAILY
- Date range: Last 30 days

### Phase 5: User Management

#### 5.1 User Preferences
**Test User**: `test-user-2025`

**Create Preferences**:
```json
{
  "userId": "test-user-2025",
  "theme": "DARK",
  "language": "EN",
  "timezone": "America/New_York",
  "currency": "USD",
  "notifications": {
    "email": true,
    "push": false,
    "sms": false
  },
  "dashboardLayout": {
    "widgets": ["portfolio", "trades", "metrics"],
    "refreshInterval": 30
  }
}
```

#### 5.2 Trade Journal
**Create Journal Entries** for each trade:
```json
{
  "userId": "test-user-2025",
  "portfolioId": "8a57024c-05c2-475b-a2c4-0545865efa4a",
  "entryDate": "2025-10-11",
  "title": "AAPL Position Analysis",
  "content": "Strong technical setup with earnings catalyst approaching. Entry at $175.50 provides good risk/reward ratio.",
  "sentiment": "POSITIVE",
  "tags": ["earnings", "technical-analysis", "momentum"]
}
```

#### 5.3 Favorite Filters
**Create Filter** for our portfolio:
```json
{
  "userId": "test-user-2025",
  "filterName": "My Tech Portfolio Trades",
  "description": "Filter for technology stocks in my main portfolio",
  "criteria": {
    "portfolioIds": ["8a57024c-05c2-475b-a2c4-0545865efa4a"],
    "symbols": ["AAPL", "GOOGL", "MSFT", "TSLA"],
    "strategies": ["MOMENTUM", "VALUE", "GROWTH"],
    "statuses": ["EXECUTED", "PENDING"]
  },
  "isPublic": false
}
```

## 📊 Expected Results & Validation

### Data Validation Checklist
- [ ] Trade IDs are properly generated (UUID format)
- [ ] Timestamps are accurate and consistent
- [ ] Calculated fields (P&L, percentages) are correct
- [ ] Data relationships are maintained (trade → portfolio → user)
- [ ] Pagination works correctly for large result sets

### Performance Benchmarks
- [ ] Health check: < 100ms
- [ ] Simple GET requests: < 500ms
- [ ] Complex filtering: < 2 seconds
- [ ] Batch operations: < 5 seconds
- [ ] Analytics calculations: < 10 seconds

### Error Handling Validation
- [ ] 400 Bad Request for invalid data
- [ ] 404 Not Found for missing resources
- [ ] 422 Unprocessable Entity for business rule violations
- [ ] 500 Internal Server Error handling
- [ ] Proper error message formatting

## 🔍 Test Data Cleanup

### After Testing
1. **Document all created trade IDs** for potential cleanup
2. **Save successful response samples** for documentation
3. **Note any data inconsistencies** discovered
4. **Record performance metrics** for baseline

### Cleanup Commands
```bash
# If cleanup endpoints exist
DELETE /api/v1/trades/{tradeId}
DELETE /api/v1/preferences/{userId}
DELETE /api/v1/journal/{journalId}
```

## 📋 Test Execution Checklist

### Pre-Test Setup
- [ ] Docker container is running
- [ ] Health check passes
- [ ] Postman collections imported
- [ ] Environment variables configured

### During Testing
- [ ] Record all request/response pairs
- [ ] Note response times
- [ ] Validate data accuracy
- [ ] Test error scenarios
- [ ] Document any issues

### Post-Test Analysis
- [ ] Summarize findings
- [ ] Identify performance bottlenecks  
- [ ] Document API behavior patterns
- [ ] Create recommendations for improvements

## 🎯 Success Metrics

### Functional Success
- ✅ All CRUD operations work correctly
- ✅ Data filtering and search functions properly
- ✅ Analytics calculations are accurate
- ✅ User management features function

### Non-Functional Success
- ✅ Response times meet benchmarks
- ✅ Error handling is comprehensive
- ✅ Data integrity is maintained
- ✅ System remains stable under load

This comprehensive testing plan ensures thorough validation of the AM Trade Management API with real portfolio data while maintaining data quality and system integrity.