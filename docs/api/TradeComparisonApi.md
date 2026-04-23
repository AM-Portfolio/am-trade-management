# Trade Comparison API Documentation

**Base URL**: `/api/v1/comparison`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/TradeComparisonController.java`

This module handles dimensional comparative analysis. It powers features allowing the trader to compare Portfolio A against Portfolio B, or Strategy A against Strategy B, rendering performance delta metrics.

---

### 1. General Trade Comparison
*   **Method Name**: `compareTradePerformance` [POST `/`]
*   **Lifecycle & Orchestration**: 
    1. Reads a generic `@Valid ComparisonRequest` JSON capable of defining entirely different A/B metrics targets.
    2. Defers calculation completely to `tradeComparisonService.compareTradePerformance()`.
*   **Database Resources**: Runs dual-tracked aggregation queries in **MongoDB** parsing heavy subsets of trades.

---

### 2. Compare Specific Portfolios
*   **Method Name**: `comparePortfolios` [GET `/portfolios`]
*   **Lifecycle & Orchestration**: 
    1. Explicit GET route requiring `portfolioId1` and `portfolioId2`.
    2. Automatically processes optional `startDate` and `endDate` boundaries to limit the comparison scope.
    3. Hands off to `TradeComparisonService`.
    4. Automatically maps illegal argument exceptions into structured `ErrorResponse` objects with a 400 status code.
*   **Database Resources**: Executes two distinct lookup queries fetching nested portfolio documents from **MongoDB** to compute metric differentials.

---

### 3. Compare Time Periods
*   **Method Name**: `compareTimePeriods` [GET `/time-periods`]
*   **Lifecycle & Orchestration**: 
    1. Explicit A/B split testing logic across timelines utilizing `period1Start/End` vs `period2Start/End`.
    2. Takes an optional `portfolioId` to restrict time tests to a specific container, otherwise assumes global user context.
    3. Handled by `TradeComparisonService`.
*   **Database Resources**: Heavy time-range scanning queries mapping timestamps on `Trade` fields against dual chronological ranges in **MongoDB**.

---

### 4. Compare Strategies
*   **Method Name**: `compareStrategies` [GET `/strategies`]
*   **Lifecycle & Orchestration**: 
    1. Allows traders to pit analytical approaches against each other (e.g. `strategy1=Breakout` vs `strategy2=MeanReversion`).
    2. Optional time restrictions map identically to Portfolio comparisons.
    3. Offloaded directly to `tradeComparisonService.compareStrategies()`.
*   **Database Resources**: Scans datasets targeting customized string boundaries nested deep inside `TradingNote` components natively inside the **MongoDB** persistence layer.
