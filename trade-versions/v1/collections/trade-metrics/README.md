# Trade Metrics Collection

This directory contains the Postman collection and documentation for the **Trade Metrics API** (also known as the Matrix Controller).

## Files
- **`trade-metrics-collection.json`**: The Postman collection file. It includes a fully embedded JSON Schema for result validation.

## API Overview (`TradeMetricsController`)

The Trade Metrics API allows for flexible, in-depth analysis of trading performance. It supports:

### 1. Get Trade Metrics (`POST /api/v1/metrics`)
Retrieve detailed metrics based on complex filters.
- **Filters**: Portfolio IDs, Date Range, Instruments, Strategies, Trade Status, etc.
- **Metrics**: Performance (Win rate, P&L), Risk (Drawdown, Sharpe), Distribution, Timing, Patterns.

### 2. Available Metric Types (`GET /api/v1/metrics/types`)
Returns a list of all available metric types that can be requested (e.g., `PERFORMANCE`, `RISK`, `DISTRIBUTION`, `TIMING`, `PATTERN`).

### 3. Compare Metrics (`GET /api/v1/metrics/compare`)
Compare performance side-by-side.
- **Compare by**: Portfolios, Time Periods, or Strategies.
- **Parameters**: `portfolioIds`, `metricTypes`, `firstPeriodStart/End`, `secondPeriodStart/End`.

### 4. Metrics Trends (`GET /api/v1/metrics/trends`)
Analyze how metrics change over time.
- **Parameters**: `portfolioIds`, `metricTypes`, `interval` (DAILY, WEEKLY, MONTHLY).

## Usage
1.  Import `trade-metrics-collection.json` into Postman.
2.  Set your environment variables:
    -   `base_url`: API base URL (e.g., `http://localhost:8080`)
    -   `jwt_token`: Valid JWT for authentication.
3.  The collection uses an embedded schema (`tradeMetricsSchema` variable) to automatically validate responses in the "Tests" script of the requests.
