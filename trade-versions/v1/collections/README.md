# Postman Collections - v1

## Overview

This folder contains Postman collection exports for all API controllers in the AM Trade Management system.

## Collections by API Controller

### Core Trade APIs

#### TradeController.postman_collection.json
- **Purpose**: Core trade CRUD operations and filtering
- **Key Endpoints**:
  - `POST /api/v1/trades/details` - Create new trade
  - `PUT /api/v1/trades/details/{tradeId}` - Update trade
  - `GET /api/v1/trades/details/portfolio/{portfolioId}` - Get trades by portfolio
  - `POST /api/v1/trades/filter` - Filter trades with multiple criteria
  - `POST /api/v1/trades/details/batch` - Batch operations
- **Use Case**: Primary collection for managing individual trades

#### TradeManagementController.postman_collection.json
- **Purpose**: Advanced trade management and querying
- **Key Features**: Calendar-based queries, advanced filtering
- **Use Case**: Complex trade analysis and retrieval

### Trade Analysis & Metrics

#### TradeMetricsController.postman_collection.json
- **Purpose**: Performance metrics and KPIs
- **Metrics Included**: Profit/loss, win rate, risk/reward, drawdown, Sharpe ratio
- **Use Case**: Portfolio performance analysis

#### TradeSummaryController.postman_collection.json
- **Purpose**: Time-period based trade summaries
- **Features**: Daily, weekly, monthly summaries
- **Use Case**: Period-based performance review

#### TradeAnalyticsController.postman_collection.json / TradeReplayController.postman_collection.json
- **Purpose**: Historical trade analysis and replay scenarios
- **Use Case**: Backtesting and scenario analysis

#### TradeComparisonController.postman_collection.json
- **Purpose**: Side-by-side trade comparison
- **Features**: Comparative metrics and analysis
- **Use Case**: Trade performance comparison

### Portfolio & Aggregation

#### PortfolioSummaryController.postman_collection.json
- **Purpose**: Portfolio-level analytics and summaries
- **Features**: Multi-portfolio comparisons, aggregated metrics
- **Use Case**: Overall portfolio health check

#### ProfitLossHeatmapController.postman_collection.json
- **Purpose**: P&L visualization and heatmap data
- **Use Case**: Visual performance analysis

### Trade Journal & Notes

#### TradeJournalController.postman_collection.json
- **Purpose**: Trading journal entry management
- **Features**: Create/read/update journal entries, attachments
- **Use Case**: Trading journal and documentation

### Saved Filters & Preferences

#### FavoriteFilterController.postman_collection.json
- **Purpose**: Manage saved filter configurations
- **Features**: Filter CRUD operations, apply saved filters
- **Use Case**: Quick access to frequently used filters

#### UserPreferencesController.postman_collection.json
- **Purpose**: User settings and preferences
- **Use Case**: User-specific configuration

## How to Import Collections

### In Postman Desktop
1. Click "Import" button (top-left)
2. Select "Upload Files" tab
3. Choose `.postman_collection.json` file
4. Click "Import"

### Via Postman Web
1. Sign in to Postman
2. Click "Import" in workspace
3. Upload JSON file
4. Confirm import

## Using Collections

### 1. Environment Setup
```json
{
  "base_url": "http://localhost:8080",
  "api_version": "v1",
  "user_id": "user_12345",
  "portfolio_id": "portfolio_uuid"
}
```

### 2. Running Requests
- Collections include pre-configured requests
- Replace variables in `{{variable_name}}` format
- Use sample data from `../sample-data/` folder

### 3. Testing
- Collections include tests for response validation
- Check "Tests" tab in each request
- Review "Response" after execution

## Organization by Feature

### Trade Management
1. TradeController - CRUD & filtering
2. TradeManagementController - Advanced queries
3. TradeJournalController - Trade notes

### Analysis & Reporting
1. TradeMetricsController - Performance KPIs
2. TradeSummaryController - Period summaries
3. TradeComparisonController - Trade comparison
4. TradeAnalyticsController - Historical analysis

### Portfolio Management
1. PortfolioSummaryController - Portfolio metrics
2. ProfitLossHeatmapController - Visualization

### User & Configuration
1. FavoriteFilterController - Saved filters
2. UserPreferencesController - User settings

## Workflow Examples

### Workflow 1: Create & Analyze Trade
1. Use TradeController → Create Trade
2. Use TradeMetricsController → Get Trade Metrics
3. Use TradeJournalController → Add Journal Entry

### Workflow 2: Filter & Compare
1. Use FavoriteFilterController → Apply Saved Filter
2. Use TradeComparisonController → Compare Trades
3. Use ProfitLossHeatmapController → View Heatmap

### Workflow 3: Portfolio Analysis
1. Use PortfolioSummaryController → Get Portfolio Summary
2. Use TradeSummaryController → Get Period Summary
3. Use TradeMetricsController → Get Portfolio Metrics

## Tips & Best Practices

### Collection Management
- **Folder Organization**: Collections are pre-organized by controller
- **Naming**: Clear, descriptive names for easy identification
- **Documentation**: Check "Description" tab for request details

### Variable Usage
- Define variables in Postman environment
- Use `{{variable_name}}` in requests
- Override values in collection/folder/request level

### Testing
- Verify responses match schema definitions
- Check status codes (200, 201, 400, 404, 500)
- Validate JSON structure using collection tests

### Debugging
- Use "Network" tab to see raw HTTP traffic
- Check request/response headers
- Verify authentication tokens
- Review server logs for errors

## Common Issues

### Variables Not Resolving
- **Issue**: `{{variable_name}}` appearing in URLs
- **Solution**: Set variable in environment or collection

### 401/403 Errors
- **Issue**: Authentication failures
- **Solution**: Check token/API key in Headers

### Request Timeout
- **Issue**: Slow responses
- **Solution**: Check server status, increase timeout in Postman

### Schema Mismatch
- **Issue**: Validation failures
- **Solution**: Compare request/response with schema in `../schemas/`

## Related Resources

- **Schemas**: See `../schemas/` for detailed API contracts
- **Sample Data**: See `../sample-data/` for request examples
- **Documentation**: See `../documentation/` for guides

## Version Information

- **Collections Format**: Postman v2.1
- **API Version**: v1.0.0
- **Last Updated**: December 2025
- **Compatibility**: Postman 10.0+

---

**Tips for New Users**:
1. Start with TradeController collection
2. Import TradeJournalController for detailed examples
3. Use FavoriteFilterController for complex filtering
4. Check sample-data for realistic payloads
