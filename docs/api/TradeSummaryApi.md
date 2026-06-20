# Trade Summary API Documentation

**Base URL**: `/api/v1/trade-summary`
**Controller File**: `am-trade-api/src/main/java/am/trade/api/controller/TradeSummaryController.java`

This module generates persistent or dynamically generated snapshots of trading performance. Unlike the raw calculations running in `TradeMetrics`, these Summaries are formalized documents that encapsulate time-boxed statistics.

---

### 1. Composite (Real-time vs Cached) Summary Generation
*   **Method Name**: `getCompositeTradeSummary` [GET `/composite/{id}`]
*   **Lifecycle & Orchestration**: 
    1. Intricately orchestrated endpoint relying on multiple backend services.
    2. Evaluates the `forceRecalculate` query parameter.
    3. If `true`: Pulls the base summary via `tradeSummaryService` and forces immediate synchronous processing through `metricsCalculationService.calculateDetailedMetrics()`.
    4. If `false`: Serves a rapid response using pre-cached metrics off the basic ID.
*   **Database Resources**: Read sweeps on cached `TradeSummary` models in **MongoDB**, with potential to trigger high-CPU fallback calculations if the cache is stale or forced.

---

### 2. Tiered Summary Retrieval
*   **Method Names**: `getBasicTradeSummary` [GET `/basic`], `getDetailedTradeSummary` [GET `/detailed`]
*   **Lifecycle & Orchestration**: 
    1. Provides structured tiers of detail (Basic vs Detailed) preventing network bloat.
    2. Defers logic resolution immediately to `tradeSummaryService`.
*   **Database Resources**: Fast, indexed ID lookups inside **MongoDB**.

---

### 3. Dynamic Timeline Retrievals
*   **Method Name**: `getTradeDetailsByTimePeriod` [GET `/trades`]
*   **Lifecycle & Orchestration**: 
    1. Tracks requests using a locally generated `UUID processId` mapping directly onto SLF4J logs representing transaction-tracing mechanisms.
    2. Determines `periodType` dynamically allocating queries across DAY, MONTH, QUARTER, or CUSTOM logic.
    3. Delegates inner logic to `tradeSummaryService.getTradeDetailsByTimePeriod()`.
*   **Database Resources**: High volatility aggregate searches across **MongoDB**.

---

### 4. CRUD Operations on Summaries
*   **Method Names**: `createTradeSummary` [POST `/`], `deleteTradeSummary` [DELETE `/{id}`]
*   **Lifecycle & Orchestration**: 
    1. Standard insertion/deletion endpoints deferring identically mapped shapes strictly to `tradeSummaryService`.
*   **Database Resources**: Directly persists or strips `TradeSummary` entries from **MongoDB**.
