# Profit/Loss Heatmap API Documentation

**Base URL**: `/api/v1/heatmap`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/ProfitLossHeatmapController.java`

This module provides time-series aggregations suitable for generating financial heatmap visualizations (e.g., GitHub-style contribution squares colored by trading profit or loss).

---

### 1. View Yearly Heatmap
*   **Method Name**: `getYearlyHeatmap` [GET `/yearly`]
*   **Lifecycle & Orchestration**: 
    1. Aggregates data for a full 12-month calendar span for a specified `{portfolioId}`.
    2. Includes an optional query param `includeTradeDetails` (defaults to false) to allow the frontend to drill down on click.
    3. Pipes to `profitLossHeatmapService.getYearlyHeatmap()`.
*   **Database Resources**: Aggregates massive buckets of data stored inside **MongoDB** organized by year.

---

### 2. View Monthly & Daily Heatmaps
*   **Method Names**: `getMonthlyHeatmap` [GET `/monthly`], `getDailyHeatmap` [GET `/daily`]
*   **Lifecycle & Orchestration**: 
    1. Focuses resolutions down to strict constraints: financial year brackets (`financialYear` param) and specific month bracket constraints.
    2. Piped directly to `profitLossHeatmapService`.
*   **Database Resources**: Generates time-bounded aggregation pipelines querying `Trade` entities in **MongoDB** mapped to calendar matrices.

---

### 3. Fetch Unified Custom Granularity
*   **Method Name**: `getHeatmap` [POST `/`]
*   **Lifecycle & Orchestration**: 
    1. Extensible endpoint mapping complex HTTP POST JSON logic into a generic `HeatmapRequest`.
    2. Allows the UI to dictate the `granularity` instead of relying on rigid URL paths.
    3. Handles multi-portfolio queries (`request.getPortfolioIds()`).
*   **Database Resources**: Highly dynamic search relying heavily on grouped projections against multiple portfolios in **MongoDB** resulting in serialized `ProfitLossHeatmapData`.
