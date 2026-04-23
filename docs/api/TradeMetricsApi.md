# Trade Metrics API Documentation

**Base URL**: `/api/v1/metrics`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/TradeMetricsController.java`

This module acts as the advanced analytics engine endpoint layout. Built for extremely flexible and dynamic data requests (like building custom charts / pivot tables on the UI).

---

### 1. Flexible Metric Extraction
*   **Method Name**: `getTradeMetrics` [POST `/`]
*   **Lifecycle & Orchestration**: 
    1. Binds dynamic front-end dashboards heavily relying on a custom `MetricsFilterRequest`. 
    2. Delegates calculation and retrieval to `tradeMetricsService.getMetrics()`.
*   **Database Resources**: Initiates highly volatile aggregation pipelines iterating over **MongoDB** trades based strictly on filtering criteria.

---

### 2. Timeline Comparison & Trending
*   **Method Names**: `compareMetrics` [GET `/compare`], `getMetricsTrends` [GET `/trends`]
*   **Lifecycle & Orchestration**: 
    1. Takes explicitly requested `Set<String> metricTypes` to only calculate exactly what the UI needs, saving huge computational loads.
    2. Iterates over `portfolioIds` comparing them between two absolute timestamps or slicing them across moving windows defined by `interval` (DAY, WEEK, MONTH).
    3. Invokes the `tradeMetricsService`.
*   **Database Resources**: Reads raw trade models in massive grouped structures from **MongoDB** to execute cross-period metric equations in-memory (e.g. Return on Equity shifts over 30 days).

---

### 3. Fetch System Metric Types
*   **Method Name**: `getAvailableMetricTypes` [GET `/types`]
*   **Lifecycle & Orchestration**: 
    1. Returns fundamental definitions powering the UI's dropdowns.
    2. Defers to `tradeMetricsService.getAvailableMetricTypes()`.
*   **Database Resources**: Usually a static system dictionary or enumerated pull. Does not typically hit core databases.
